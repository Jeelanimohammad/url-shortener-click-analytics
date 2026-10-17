package com.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO returned by GET /api/urls/{shortCode}/analytics.
 *
 * RESPONSE body example:
 * {
 *   "shortCode": "aB3xK9p",
 *   "originalUrl": "https://www.example.com/long/path",
 *   "shortUrl": "http://localhost:8080/aB3xK9p",
 *   "createdAt": "2024-05-01T10:30:00",
 *   "totalClicks": 5,
 *   "clickHistory": ["2024-05-01T11:00:00", "2024-05-01T12:30:00"]
 * }
 */
public class AnalyticsResponse {

    private String            shortCode;
    private String            originalUrl;
    private String            shortUrl;
    private LocalDateTime     createdAt;
    private Long              totalClicks;
    private List<LocalDateTime> clickHistory;  // individual visit timestamps

    // ── Constructors ──────────────────────────────────────────────────────────

    public AnalyticsResponse() {}

    public AnalyticsResponse(String shortCode, String originalUrl, String shortUrl,
                             LocalDateTime createdAt, Long totalClicks,
                             List<LocalDateTime> clickHistory) {
        this.shortCode    = shortCode;
        this.originalUrl  = originalUrl;
        this.shortUrl     = shortUrl;
        this.createdAt    = createdAt;
        this.totalClicks  = totalClicks;
        this.clickHistory = clickHistory;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getShortCode()                          { return shortCode; }
    public void setShortCode(String c)                    { this.shortCode = c; }

    public String getOriginalUrl()                        { return originalUrl; }
    public void setOriginalUrl(String u)                  { this.originalUrl = u; }

    public String getShortUrl()                           { return shortUrl; }
    public void setShortUrl(String u)                     { this.shortUrl = u; }

    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDateTime t)             { this.createdAt = t; }

    public Long getTotalClicks()                          { return totalClicks; }
    public void setTotalClicks(Long c)                    { this.totalClicks = c; }

    public List<LocalDateTime> getClickHistory()          { return clickHistory; }
    public void setClickHistory(List<LocalDateTime> h)    { this.clickHistory = h; }
}
