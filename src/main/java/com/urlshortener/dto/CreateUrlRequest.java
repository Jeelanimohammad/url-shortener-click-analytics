package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) for incoming URL creation requests.
 *
 * WHY DTOs?
 *   - We never expose entity classes directly to the API layer.
 *   - DTOs carry only the fields the caller needs to send / receive.
 *   - Validation annotations (@NotBlank etc.) live here, not on entities.
 *
 * REQUEST body example:
 *   { "originalUrl": "https://www.example.com/some/very/long/path" }
 */
public class CreateUrlRequest {

    /**
     * @NotBlank → rejects null AND blank strings (e.g. "  " or "").
     * @Size      → limits input length to prevent absurdly long URLs.
     * The actual URL-format validation is done in UrlValidationService.
     */
    @NotBlank(message = "originalUrl must not be blank")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String originalUrl;

    // ── Constructors ──────────────────────────────────────────────────────────

    public CreateUrlRequest() {}

    public CreateUrlRequest(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getOriginalUrl()             { return originalUrl; }
    public void setOriginalUrl(String url)     { this.originalUrl = url; }
}
