package com.urlshortener.service;

import com.urlshortener.config.AppConfig;
import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.ClickEvent;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core business logic for URL shortening, redirection, and analytics.
 *
 * @Transactional ensures that each public method runs inside a database
 * transaction.  If any step throws an exception, the whole transaction is
 * rolled back — keeping the database consistent.
 *
 * WHY KEEP LOGIC HERE (NOT IN CONTROLLER)?
 *   Controllers should only parse HTTP requests and format HTTP responses.
 *   Putting business rules in the service layer makes them reusable and
 *   independently testable.
 */
@Service
public class UrlShortenerService {

    private final ShortUrlRepository       shortUrlRepository;
    private final ClickEventRepository     clickEventRepository;
    private final ShortCodeGeneratorService codeGenerator;
    private final UrlValidationService     validator;
    private final AppConfig                appConfig;

    // Constructor injection — preferred over @Autowired on fields
    public UrlShortenerService(ShortUrlRepository shortUrlRepository,
                               ClickEventRepository clickEventRepository,
                               ShortCodeGeneratorService codeGenerator,
                               UrlValidationService validator,
                               AppConfig appConfig) {
        this.shortUrlRepository   = shortUrlRepository;
        this.clickEventRepository = clickEventRepository;
        this.codeGenerator        = codeGenerator;
        this.validator            = validator;
        this.appConfig            = appConfig;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE — POST /api/urls
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shortens a URL.  If the same original URL was already shortened, the
     * existing record is returned (idempotent behaviour).
     *
     * Steps:
     *   1. Validate the URL format and scheme.
     *   2. Check if the URL already has a short code — return that if so.
     *   3. Generate a unique short code.
     *   4. Persist the new ShortUrl entity.
     *   5. Return a UrlResponse DTO.
     */
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        String originalUrl = request.getOriginalUrl().trim();

        // Step 1: Validate
        validator.validate(originalUrl);

        // Step 2: Deduplication — return existing short URL for same long URL
        Optional<ShortUrl> existing = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return toUrlResponse(existing.get());
        }

        // Step 3: Generate unique code
        String shortCode = codeGenerator.generateUniqueCode();

        // Step 4: Persist
        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);
        ShortUrl saved    = shortUrlRepository.save(shortUrl);

        // Step 5: Return DTO
        return toUrlResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — GET /api/urls/{shortCode}
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UrlResponse getByShortCode(String shortCode) {
        ShortUrl shortUrl = findOrThrow(shortCode);
        return toUrlResponse(shortUrl);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIST — GET /api/urls
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls() {
        return shortUrlRepository.findAll()
                .stream()
                .map(this::toUrlResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — DELETE /api/urls/{shortCode}
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteByShortCode(String shortCode) {
        ShortUrl shortUrl = findOrThrow(shortCode);
        shortUrlRepository.delete(shortUrl);
        // Cascade = ALL on ShortUrl.clickEvents means click_events rows are
        // also deleted automatically.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REDIRECT — GET /{shortCode}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a short code to its original URL and records the visit.
     *
     * WHY @Transactional here?
     *   We do two writes in sequence (increment click count + insert ClickEvent).
     *   If the second write fails, we want the first to be rolled back too.
     *
     * @return the original long URL to redirect to
     */
    @Transactional
    public String resolveAndTrack(String shortCode) {
        ShortUrl shortUrl = findOrThrow(shortCode);

        // Increment counter (stored on the ShortUrl row for fast retrieval)
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        // Insert a timestamped row into click_events for history
        ClickEvent event = new ClickEvent(shortUrl);
        clickEventRepository.save(event);

        return shortUrl.getOriginalUrl();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANALYTICS — GET /api/urls/{shortCode}/analytics
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String shortCode) {
        ShortUrl shortUrl = findOrThrow(shortCode);

        // Fetch all click timestamps from the click_events table
        List<LocalDateTime> history = clickEventRepository
                .findByShortUrl(shortUrl)
                .stream()
                .map(ClickEvent::getClickedAt)
                .collect(Collectors.toList());

        return new AnalyticsResponse(
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                buildShortUrl(shortUrl.getShortCode()),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount(),
                history
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch a ShortUrl by code, or throw a 404-mapped exception. */
    private ShortUrl findOrThrow(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
    }

    /** Map a ShortUrl entity to the UrlResponse DTO. */
    private UrlResponse toUrlResponse(ShortUrl shortUrl) {
        return new UrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                buildShortUrl(shortUrl.getShortCode()),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount()
        );
    }

    /** Build the full short link: base URL + "/" + code. */
    private String buildShortUrl(String shortCode) {
        return appConfig.getBaseUrl() + "/" + shortCode;
    }
}
