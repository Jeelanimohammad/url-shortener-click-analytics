package com.urlshortener.repository;

import com.urlshortener.entity.ClickEvent;
import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ClickEvent.
 *
 * findByShortUrl translates to: SELECT * FROM click_events WHERE short_url_id = ?
 */
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    /** Retrieve the full click history for a given ShortUrl. */
    List<ClickEvent> findByShortUrl(ShortUrl shortUrl);
}
