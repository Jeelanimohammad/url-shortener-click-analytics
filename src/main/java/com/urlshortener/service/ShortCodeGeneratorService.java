package com.urlshortener.service;

import com.urlshortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Generates unique short codes.
 *
 * ALGORITHM:
 *   1. Pick 7 characters at random from an alphanumeric alphabet (62 chars).
 *   2. Check if the code already exists in the database (collision check).
 *   3. If a collision is found, generate a new code and repeat.
 *
 * WHY SecureRandom instead of Random?
 *   SecureRandom uses a cryptographically strong random number generator.
 *   With 62^7 ≈ 3.5 trillion possibilities, collisions are extremely rare,
 *   but we still handle them gracefully.
 *
 * WHY 7 characters?
 *   - Short enough to be user-friendly.
 *   - Provides ~3.5 trillion unique combinations — plenty for most use cases.
 *   - Common choice in production URL shorteners (bit.ly uses 7).
 */
@Service
public class ShortCodeGeneratorService {

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 7;

    /** Thread-safe secure random instance — reused across calls. */
    private final SecureRandom random = new SecureRandom();

    private final ShortUrlRepository repository;

    public ShortCodeGeneratorService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates a unique short code that does not already exist in the database.
     *
     * @return a 7-character alphanumeric string guaranteed to be unique
     */
    public String generateUniqueCode() {
        String code;
        int attempts = 0;

        do {
            code = generateCode();
            attempts++;
            // Safety valve: fail loudly if something is very wrong
            if (attempts > 10) {
                throw new IllegalStateException(
                        "Could not generate a unique short code after 10 attempts. " +
                        "This is extremely unlikely — please investigate.");
            }
        } while (repository.existsByShortCode(code));

        return code;
    }

    /**
     * Produces one random 7-character code.
     * This method is package-private so it can be tested in isolation.
     */
    String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
