package com.urlshortener.controller;

import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for URL management and analytics endpoints.
 *
 * BASE PATH: /api/urls
 *
 * ENDPOINTS:
 *   POST   /api/urls                       → create a short URL
 *   GET    /api/urls/{shortCode}           → get URL details
 *   GET    /api/urls                       → list all URLs
 *   DELETE /api/urls/{shortCode}           → delete a URL
 *   GET    /api/urls/{shortCode}/analytics → get click analytics
 *
 * WHY SEPARATE FROM THE REDIRECT CONTROLLER?
 *   The redirect endpoint (GET /{shortCode}) belongs to the root path and
 *   returns an HTTP redirect (302), NOT JSON.  Mixing that behaviour inside
 *   the JSON API controller would be confusing.  Keeping them separate follows
 *   the Single Responsibility Principle.
 */
@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    // ── POST /api/urls ────────────────────────────────────────────────────────

    /**
     * Create a short URL.
     *
     * @Valid triggers Bean Validation on the request body.
     * Returns 201 Created with the new (or existing) UrlResponse.
     *
     * REQUEST:  { "originalUrl": "https://example.com/long/path" }
     * RESPONSE: { "id": 1, "shortCode": "aB3xK9p", "shortUrl": "http://localhost:8080/aB3xK9p", ... }
     */
    @PostMapping
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        UrlResponse response = service.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/urls/{shortCode} ─────────────────────────────────────────────

    /**
     * Retrieve details for a single short URL (no click is recorded).
     *
     * Returns 200 OK or 404 Not Found via GlobalExceptionHandler.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> getShortUrl(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getByShortCode(shortCode));
    }

    // ── GET /api/urls ─────────────────────────────────────────────────────────

    /**
     * List all stored short URLs.
     *
     * Returns 200 OK with a JSON array (empty array if none exist).
     */
    @GetMapping
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(service.getAllUrls());
    }

    // ── DELETE /api/urls/{shortCode} ──────────────────────────────────────────

    /**
     * Delete a short URL and its associated click history.
     *
     * Returns 204 No Content on success, 404 if code not found.
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode) {
        service.deleteByShortCode(shortCode);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/urls/{shortCode}/analytics ──────────────────────────────────

    /**
     * Retrieve click analytics for a short URL.
     *
     * Returns 200 OK with click count and full timestamp history,
     * or 404 if the short code does not exist.
     */
    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getAnalytics(shortCode));
    }
}
