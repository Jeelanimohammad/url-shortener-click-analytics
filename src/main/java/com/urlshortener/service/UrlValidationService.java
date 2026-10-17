package com.urlshortener.service;

import com.urlshortener.exception.InvalidUrlException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Validates URLs submitted by clients before creating short links.
 *
 * VALIDATION RULES (in order):
 *   1. Not null / not blank  → enforced by @NotBlank in the DTO (before this runs)
 *   2. Parseable as a URI    → rejects completely malformed strings
 *   3. Has a valid scheme    → only http / https are allowed (blocks javascript:,
 *                              file:, ftp: etc. which would be unsafe)
 *   4. Has a non-empty host  → rejects relative URLs like "/foo/bar"
 *
 * Keeping this in its own Service makes it easy to unit-test and to add new
 * rules (e.g. block specific domains) without touching other classes.
 */
@Service
public class UrlValidationService {

    /** Only these URL schemes are accepted. */
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    /**
     * Validates the given URL string.
     *
     * @param url the raw URL submitted by the client
     * @throws InvalidUrlException if the URL is malformed or uses an unsafe scheme
     */
    public void validate(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format: '" + url + "'");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException(
                    "URL scheme '" + scheme + "' is not allowed. " +
                    "Only http and https URLs are accepted.");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidUrlException(
                    "URL must contain a valid host: '" + url + "'");
        }
    }
}
