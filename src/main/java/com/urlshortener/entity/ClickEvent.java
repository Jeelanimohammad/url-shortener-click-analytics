package com.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity that maps to the "click_events" table.
 *
 * Every time a short link is used for redirection, one ClickEvent row is
 * inserted.  This gives us a full history of visits instead of just a counter.
 */
@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One: many ClickEvent rows belong to one ShortUrl.
     *
     * @JoinColumn → the "short_url_id" foreign-key column lives in THIS table.
     * nullable = false → every click must belong to a valid short URL.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    /** Timestamp of when this click occurred. */
    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    @PrePersist
    protected void onCreate() {
        this.clickedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public ClickEvent() {}

    public ClickEvent(ShortUrl shortUrl) {
        this.shortUrl = shortUrl;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public ShortUrl getShortUrl()               { return shortUrl; }
    public void setShortUrl(ShortUrl shortUrl)  { this.shortUrl = shortUrl; }

    public LocalDateTime getClickedAt()         { return clickedAt; }
    public void setClickedAt(LocalDateTime t)   { this.clickedAt = t; }
}
