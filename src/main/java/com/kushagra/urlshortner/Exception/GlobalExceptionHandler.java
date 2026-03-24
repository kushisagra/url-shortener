package com.kushagra.urlshortner.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // intercepts exceptions from ALL controllers
public class GlobalExceptionHandler {

    // 401 Unauthorized — bad login credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 400 Bad Request — email already registered
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 404 — short code doesn't exist
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(UrlNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 410 Gone — URL existed but expired (semantically different from 404)
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleExpired(UrlExpiredException ex) {
        return buildError(HttpStatus.GONE, ex.getMessage());
    }

    // 409 Conflict — custom alias already taken
    @ExceptionHandler(DuplicateAliasException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateAliasException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 429 Too Many Requests — rate limit exceeded
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitExceededException ex) {
        return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // 400 Bad Request — @Valid annotation failed (e.g. blank URL)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        // Collect all validation error messages
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation failed");
        body.put("details", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // 500 — anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}