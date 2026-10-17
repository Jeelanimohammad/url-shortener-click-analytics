package com.urlshortener.service;

import com.urlshortener.exception.InvalidUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for UrlValidationService.
 *
 * No mocks needed — this service has no dependencies.
 * We test a range of valid and invalid inputs to confirm every rule.
 */
class UrlValidationServiceTest {

    private final UrlValidationService validator = new UrlValidationService();

    // ── Valid URLs ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid https URL passes validation")
    void validate_validHttpsUrl() {
        assertThatNoException().isThrownBy(
                () -> validator.validate("https://www.google.com"));
    }

    @Test
    @DisplayName("Valid http URL passes validation")
    void validate_validHttpUrl() {
        assertThatNoException().isThrownBy(
                () -> validator.validate("http://example.com/path?q=1"));
    }

    @Test
    @DisplayName("URL with port number passes validation")
    void validate_urlWithPort() {
        assertThatNoException().isThrownBy(
                () -> validator.validate("https://api.example.com:8443/v1"));
    }

    // ── Invalid URLs ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Completely malformed string fails validation")
    void validate_malformedString() {
        assertThatThrownBy(() -> validator.validate("not a url at all :::"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    @DisplayName("ftp:// scheme is rejected")
    void validate_ftpSchemeRejected() {
        assertThatThrownBy(() -> validator.validate("ftp://files.example.com"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("ftp");
    }

    @Test
    @DisplayName("javascript: scheme is rejected (XSS risk)")
    void validate_javascriptSchemeRejected() {
        assertThatThrownBy(() -> validator.validate("javascript:alert('xss')"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    @DisplayName("URL with no host is rejected")
    void validate_noHost() {
        assertThatThrownBy(() -> validator.validate("https:///path/only"))
                .isInstanceOf(InvalidUrlException.class);
    }
}
