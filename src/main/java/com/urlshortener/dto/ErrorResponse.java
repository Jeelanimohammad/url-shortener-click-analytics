package com.urlshortener.dto;

import java.time.LocalDateTime;

/**
 * A consistent JSON error envelope returned for every failed request.
 *
 * Error RESPONSE body example:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Short code 'xyz123' not found",
 *   "timestamp": "2024-05-01T10:30:00"
 * }
 *
 * Why a custom error response?
 *   Spring Boot's default error body is verbose and inconsistent.
 *   A uniform structure is easier for API consumers to parse.
 */
public class ErrorResponse {

    private int           status;
    private String        error;
    private String        message;
    private LocalDateTime timestamp;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getStatus()                       { return status; }
    public void setStatus(int status)            { this.status = status; }

    public String getError()                     { return error; }
    public void setError(String error)           { this.error = error; }

    public String getMessage()                   { return message; }
    public void setMessage(String message)       { this.message = message; }

    public LocalDateTime getTimestamp()          { return timestamp; }
    public void setTimestamp(LocalDateTime t)    { this.timestamp = t; }
}
