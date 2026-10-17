package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for the entire application.
 *
 * @RestControllerAdvice intercepts exceptions thrown from any @RestController.
 * Instead of returning Spring Boot's default noisy error page, we return clean
 * JSON using our ErrorResponse DTO.
 *
 * HOW IT WORKS:
 *   1. A controller method throws (or lets propagate) an exception.
 *   2. Spring finds the matching @ExceptionHandler method here.
 *   3. We build an ErrorResponse and wrap it in a ResponseEntity with the
 *      correct HTTP status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 — Short code not found ────────────────────────────────────────────

    @ExceptionHandler(ShortCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ShortCodeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    // ── 400 — Invalid URL format / scheme ─────────────────────────────────────

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    // ── 400 — Bean Validation failures (@NotBlank, @Size, etc.) ──────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Collect every field error into one readable string
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Validation Failed", details));
    }

    // ── 500 — Catch-all for unexpected errors ─────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Log the real error server-side; return a safe message to the caller
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred. Please try again later."));
    }
}
