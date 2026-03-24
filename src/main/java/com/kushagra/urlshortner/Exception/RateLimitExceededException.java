package com.kushagra.urlshortner.Exception;

/**
 * Thrown when a client exceeds the rate limit.
 * Returns HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String ipAddress) {
        super("Rate limit exceeded for IP: " + ipAddress + ". Please try again later.");
    }
}

