package com.urlshortener.controller;

import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controller that handles browser redirects.
 *
 * ENDPOINT:
 *   GET /{shortCode} → Redirects to the original URL (HTTP 302)
 *
 * WHY HTTP 302 (FOUND) instead of 301 (MOVED PERMANENTLY)?
 *   - 301 is cached by browsers permanently.  If we ever need to update a
 *     short link, browsers that cached the 301 will never re-request it.
 *   - 302 tells the browser "found it temporarily here" — it checks the
 *     server every time, so analytics stay accurate.
 *
 * WHY NOT IN UrlController?
 *   This endpoint returns an HTTP redirect header (Location: ...), NOT a
 *   JSON body.  Mixing redirect and JSON endpoints in the same controller
 *   makes the code harder to understand and test.
 *
 * BROWSER-FRIENDLINESS:
 *   A user can paste "http://localhost:8080/aB3xK9p" directly into a browser,
 *   and this endpoint will redirect them transparently to the original page.
 */
@RestController
public class RedirectController {

    private final UrlShortenerService service;

    public RedirectController(UrlShortenerService service) {
        this.service = service;
    }

    /**
     * Resolves the short code, records the click, and issues a 302 redirect.
     *
     * If the short code does not exist:
     *   - In a browser: returns 404 JSON (handled by GlobalExceptionHandler).
     *   - The click is NOT recorded (no call to resolveAndTrack).
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        // resolveAndTrack throws ShortCodeNotFoundException if not found —
        // GlobalExceptionHandler converts that to a 404 JSON response.
        String originalUrl = service.resolveAndTrack(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)                // 302
                .location(URI.create(originalUrl))        // Location: <original URL>
                .build();
    }
}
