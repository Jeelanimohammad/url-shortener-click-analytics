package com.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity that maps to the "short_urls" table in PostgreSQL.
 *
 * Think of an entity as a Java class that directly mirrors a database table:
 *   - each field  → a column
 *   - each instance → a row
 */
@Entity
@Table(
    name = "short_urls",
    uniqueConstraints = {
        // Database-level guarantee: no two rows can share the same short code.
        @UniqueConstraint(name = "uq_short_code", columnNames = "short_code")
    }
)
public class ShortUrl {

    /** Primary key — auto-incremented by the database (SERIAL / BIGSERIAL). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original long URL submitted by the user.
     * nullable = false → database enforces NOT NULL.
     * length = 2048 → supports very long URLs.
     */
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    /**
     * The unique 7-character alphanumeric code (e.g., "aB3xK9p").
     * Stored separately from the full short link so the base URL can be changed
     * without touching the database.
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    /** Automatically recorded when this entity is first persisted. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Incremented each time the short link is used for redirection. */
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    /**
     * One-to-Many relationship: one ShortUrl can have many ClickEvent records.
     *
     * mappedBy = "shortUrl" → ClickEvent owns the foreign-key column.
     * cascade = ALL         → saving / deleting a ShortUrl does the same to its clicks.
     * orphanRemoval         → removes ClickEvent rows when removed from the list.
     */
    @OneToMany(mappedBy = "shortUrl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClickEvent> clickEvents = new ArrayList<>();

    /** Runs automatically before the entity is first saved. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public ShortUrl() {}

    public ShortUrl(String originalUrl, String shortCode) {
        this.originalUrl = originalUrl;
        this.shortCode   = shortCode;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getOriginalUrl()               { return originalUrl; }
    public void setOriginalUrl(String url)       { this.originalUrl = url; }

    public String getShortCode()                 { return shortCode; }
    public void setShortCode(String shortCode)   { this.shortCode = shortCode; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public Long getClickCount()                  { return clickCount; }
    public void setClickCount(Long count)        { this.clickCount = count; }

    public List<ClickEvent> getClickEvents()     { return clickEvents; }
    public void setClickEvents(List<ClickEvent> e) { this.clickEvents = e; }
}
