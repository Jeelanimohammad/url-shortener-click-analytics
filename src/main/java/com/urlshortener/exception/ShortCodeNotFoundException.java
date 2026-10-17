package com.urlshortener.exception;

/**
 * Thrown when a requested short code does not exist in the database.
 *
 * Extending RuntimeException makes this an "unchecked" exception —
 * callers don't have to declare it in their method signatures.
 */
public class ShortCodeNotFoundException extends RuntimeException {

    private final String shortCode;

    public ShortCodeNotFoundException(String shortCode) {
        super("Short code '" + shortCode + "' not found");
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
