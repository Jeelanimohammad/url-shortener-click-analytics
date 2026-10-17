package com.urlshortener.service;

import com.urlshortener.config.AppConfig;
import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.ClickEvent;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UrlShortenerService.
 *
 * UNIT vs INTEGRATION TESTS:
 *   - Unit tests isolate ONE class and mock all its dependencies.
 *     They run instantly (no database, no Spring context).
 *   - Integration tests spin up the full Spring context + real database and
 *     test that all layers work together.  Slower but more realistic.
 *
 * WHY MOCKITO?
 *   We use @Mock to create fake implementations of repositories and services.
 *   Mockito lets us "program" how mocks behave using when(...).thenReturn(...)
 *   and verify that expected calls were made using verify(...).
 *
 * @ExtendWith(MockitoExtension.class) activates Mockito without Spring,
 * making tests very fast.
 */
@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    // ── Mocks (fake implementations injected into the service) ────────────────

    @Mock private ShortUrlRepository       shortUrlRepository;
    @Mock private ClickEventRepository     clickEventRepository;
    @Mock private ShortCodeGeneratorService codeGenerator;
    @Mock private UrlValidationService     validator;
    @Mock private AppConfig                appConfig;

    /** The real class under test — Mockito injects the mocks above into it. */
    @InjectMocks
    private UrlShortenerService service;

    // ── Test data ─────────────────────────────────────────────────────────────

    private static final String ORIGINAL_URL = "https://www.example.com/long/path";
    private static final String SHORT_CODE   = "aB3xK9p";
    private static final String BASE_URL     = "http://localhost:8080";
    private static final String FULL_SHORT   = BASE_URL + "/" + SHORT_CODE;

    /** Builds a sample entity as it would come back from the DB. */
    private ShortUrl sampleEntity() {
        ShortUrl s = new ShortUrl(ORIGINAL_URL, SHORT_CODE);
        s.setId(1L);
        s.setCreatedAt(LocalDateTime.of(2024, 5, 1, 10, 30));
        s.setClickCount(0L);
        return s;
    }

    @BeforeEach
    void setUpBaseUrl() {
        lenient().when(appConfig.getBaseUrl()).thenReturn(BASE_URL);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createShortUrl
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Creating a short URL succeeds and returns correct response")
    void createShortUrl_success() {
        // ARRANGE
        when(shortUrlRepository.findByOriginalUrl(ORIGINAL_URL)).thenReturn(Optional.empty());
        when(codeGenerator.generateUniqueCode()).thenReturn(SHORT_CODE);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(sampleEntity());

        // ACT
        UrlResponse response = service.createShortUrl(new CreateUrlRequest(ORIGINAL_URL));

        // ASSERT
        assertThat(response.getShortCode()).isEqualTo(SHORT_CODE);
        assertThat(response.getShortUrl()).isEqualTo(FULL_SHORT);
        assertThat(response.getOriginalUrl()).isEqualTo(ORIGINAL_URL);

        // Verify the validator was called exactly once
        verify(validator, times(1)).validate(ORIGINAL_URL);
        // Verify we actually saved to the database
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("Creating a short URL for an existing long URL returns the existing record")
    void createShortUrl_deduplication() {
        // ARRANGE — pretend the URL already exists
        when(shortUrlRepository.findByOriginalUrl(ORIGINAL_URL))
                .thenReturn(Optional.of(sampleEntity()));

        // ACT
        UrlResponse response = service.createShortUrl(new CreateUrlRequest(ORIGINAL_URL));

        // ASSERT — same short code returned
        assertThat(response.getShortCode()).isEqualTo(SHORT_CODE);

        // The code generator should NOT be called for duplicates
        verify(codeGenerator, never()).generateUniqueCode();
        // No new row should be inserted
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    @DisplayName("Creating a short URL with an invalid URL throws InvalidUrlException")
    void createShortUrl_invalidUrl() {
        // ARRANGE — make the validator throw
        doThrow(new InvalidUrlException("Invalid URL format: 'not-a-url'"))
                .when(validator).validate(anyString());

        // ACT & ASSERT
        assertThatThrownBy(() -> service.createShortUrl(new CreateUrlRequest("not-a-url")))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("Invalid URL");

        // No database interaction after a validation failure
        verify(shortUrlRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getByShortCode
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Retrieving an existing short URL returns the correct UrlResponse")
    void getByShortCode_found() {
        when(shortUrlRepository.findByShortCode(SHORT_CODE))
                .thenReturn(Optional.of(sampleEntity()));

        UrlResponse response = service.getByShortCode(SHORT_CODE);

        assertThat(response.getShortCode()).isEqualTo(SHORT_CODE);
        assertThat(response.getOriginalUrl()).isEqualTo(ORIGINAL_URL);
        assertThat(response.getClickCount()).isZero();
    }

    @Test
    @DisplayName("Retrieving a non-existent short code throws ShortCodeNotFoundException")
    void getByShortCode_notFound() {
        when(shortUrlRepository.findByShortCode("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByShortCode("missing"))
                .isInstanceOf(ShortCodeNotFoundException.class)
                .hasMessageContaining("missing");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resolveAndTrack
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resolving a valid short code returns the original URL and increments click count")
    void resolveAndTrack_incrementsClickCount() {
        ShortUrl entity = sampleEntity(); // clickCount = 0
        when(shortUrlRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
        when(shortUrlRepository.save(entity)).thenReturn(entity);
        when(clickEventRepository.save(any(ClickEvent.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = service.resolveAndTrack(SHORT_CODE);

        // Original URL must be returned
        assertThat(result).isEqualTo(ORIGINAL_URL);

        // Click count must have been incremented to 1
        assertThat(entity.getClickCount()).isEqualTo(1L);

        // A ClickEvent row must have been persisted
        verify(clickEventRepository, times(1)).save(any(ClickEvent.class));
    }

    @Test
    @DisplayName("Resolving a non-existent short code throws ShortCodeNotFoundException and does NOT track a click")
    void resolveAndTrack_notFound() {
        when(shortUrlRepository.findByShortCode("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveAndTrack("bad"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        // NO click event must be recorded for an invalid code
        verify(clickEventRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAnalytics
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Analytics returns correct click count and history")
    void getAnalytics_returnsCorrectData() {
        ShortUrl entity = sampleEntity();
        entity.setClickCount(2L);

        ClickEvent click1 = new ClickEvent(entity);
        click1.setClickedAt(LocalDateTime.of(2024, 5, 1, 11, 0));
        ClickEvent click2 = new ClickEvent(entity);
        click2.setClickedAt(LocalDateTime.of(2024, 5, 1, 12, 0));

        when(shortUrlRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));
        when(clickEventRepository.findByShortUrl(entity)).thenReturn(List.of(click1, click2));

        AnalyticsResponse analytics = service.getAnalytics(SHORT_CODE);

        assertThat(analytics.getTotalClicks()).isEqualTo(2L);
        assertThat(analytics.getClickHistory()).hasSize(2);
        assertThat(analytics.getShortCode()).isEqualTo(SHORT_CODE);
        assertThat(analytics.getOriginalUrl()).isEqualTo(ORIGINAL_URL);
    }

    @Test
    @DisplayName("Analytics for a non-existent short code throws ShortCodeNotFoundException")
    void getAnalytics_notFound() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAnalytics("missing"))
                .isInstanceOf(ShortCodeNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteByShortCode
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deleting an existing short URL calls repository.delete")
    void delete_existingCode() {
        ShortUrl entity = sampleEntity();
        when(shortUrlRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(entity));

        service.deleteByShortCode(SHORT_CODE);

        verify(shortUrlRepository, times(1)).delete(entity);
    }

    @Test
    @DisplayName("Deleting a non-existent short code throws ShortCodeNotFoundException")
    void delete_notFound() {
        when(shortUrlRepository.findByShortCode("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteByShortCode("ghost"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        verify(shortUrlRepository, never()).delete(any());
    }
}
