package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for ShortUrl.
 *
 * By extending JpaRepository<ShortUrl, Long> we automatically get:
 *   - save(), findById(), findAll(), delete(), count(), and more.
 *
 * Spring generates the SQL implementations at runtime — we never write JDBC
 * manually.  The method names below are translated by Spring into SQL queries:
 *   findByShortCode → SELECT * FROM short_urls WHERE short_code = ?
 *   existsByShortCode → SELECT EXISTS (... WHERE short_code = ?)
 *   findByOriginalUrl → SELECT * FROM short_urls WHERE original_url = ?
 */
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /** Find the entity for a given short code (used during redirection). */
    Optional<ShortUrl> findByShortCode(String shortCode);

    /** Check if a short code already exists (collision detection). */
    boolean existsByShortCode(String shortCode);

    /** Find an existing record for the same long URL (avoids duplicate entries). */
    Optional<ShortUrl> findByOriginalUrl(String originalUrl);
}
