package com.urlshortener.exception;

/**
 * Thrown when a submitted URL fails format or scheme validation.
 */
public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
