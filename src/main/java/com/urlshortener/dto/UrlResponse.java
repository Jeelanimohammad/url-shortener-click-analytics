package com.urlshortener.dto;

import java.time.LocalDateTime;

/**
 * DTO returned by POST /api/urls and GET /api/urls/{shortCode}.
 *
 * RESPONSE body example:
 * {
 *   "id": 1,
 *   "originalUrl": "https://www.example.com/long/path",
 *   "shortCode": "aB3xK9p",
 *   "shortUrl": "http://localhost:8080/aB3xK9p",
 *   "createdAt": "2024-05-01T10:30:00",
 *   "clickCount": 5
 * }
 */
public class UrlResponse {

    private Long          id;
    private String        originalUrl;
    private String        shortCode;
    private String        shortUrl;       // full clickable short link
    private LocalDateTime createdAt;
    private Long          clickCount;

    // ── Constructors ──────────────────────────────────────────────────────────

    public UrlResponse() {}

    public UrlResponse(Long id, String originalUrl, String shortCode,
                       String shortUrl, LocalDateTime createdAt, Long clickCount) {
        this.id          = id;
        this.originalUrl = originalUrl;
        this.shortCode   = shortCode;
        this.shortUrl    = shortUrl;
        this.createdAt   = createdAt;
        this.clickCount  = clickCount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getOriginalUrl()               { return originalUrl; }
    public void setOriginalUrl(String url)       { this.originalUrl = url; }

    public String getShortCode()                 { return shortCode; }
    public void setShortCode(String code)        { this.shortCode = code; }

    public String getShortUrl()                  { return shortUrl; }
    public void setShortUrl(String url)          { this.shortUrl = url; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public Long getClickCount()                  { return clickCount; }
    public void setClickCount(Long count)        { this.clickCount = count; }
}
