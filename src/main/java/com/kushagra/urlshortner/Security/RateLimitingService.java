package com.kushagra.urlshortner.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Service — prevents API abuse.
 *
 * How it works:
 * 1. Each IP address gets a "bucket" with a request counter
 * 2. Counter increments on each request
 * 3. If counter exceeds limit, request is rejected
 * 4. Bucket resets after the time window expires
 *
 * This is a simple "fixed window" rate limiter.
 * For production, consider "sliding window" or "token bucket" algorithms.
 *
 * Note: This is in-memory, so it resets on server restart.
 * For distributed systems, use Redis-based rate limiting.
 */
@Service
@Slf4j
public class RateLimitingService {

    private final int maxRequests;
    private final long windowSizeMs;

    // ConcurrentHashMap for thread-safe access across multiple requests
    private final ConcurrentHashMap<String, RequestBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingService(
            @Value("${app.rate-limit.max-requests}") int maxRequests,
            @Value("${app.rate-limit.window-seconds}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSeconds * 1000L;
        log.info("Rate limiting configured: {} requests per {} seconds", maxRequests, windowSeconds);
    }

    /**
     * Check if a request from this IP is allowed.
     *
     * @param ipAddress The client's IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String ipAddress) {
        long currentTime = System.currentTimeMillis();

        // Get or create bucket for this IP
        RequestBucket bucket = buckets.compute(ipAddress, (ip, existing) -> {
            if (existing == null || existing.isExpired(currentTime, windowSizeMs)) {
                // No bucket or expired — create new one
                return new RequestBucket(currentTime);
            }
            return existing;
        });

        // Try to increment counter
        int currentCount = bucket.incrementAndGet();

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP: {} ({} requests)", ipAddress, currentCount);
            return false;
        }

        log.debug("Request allowed for IP: {} ({}/{} requests)", ipAddress, currentCount, maxRequests);
        return true;
    }

    /**
     * Get remaining requests for an IP.
     * Useful for including in response headers.
     */
    public int getRemainingRequests(String ipAddress) {
        RequestBucket bucket = buckets.get(ipAddress);
        if (bucket == null) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - bucket.getCount());
    }

    /**
     * Request bucket — tracks requests within a time window.
     */
    private static class RequestBucket {
        private final long windowStart;
        private final AtomicInteger count;

        RequestBucket(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }

        boolean isExpired(long currentTime, long windowSizeMs) {
            return currentTime - windowStart >= windowSizeMs;
        }

        int incrementAndGet() {
            return count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }
    }

    /**
     * Cleanup old buckets periodically.
     * Call this from a scheduled task to prevent memory leaks.
     */
    public void cleanupExpiredBuckets() {
        long currentTime = System.currentTimeMillis();
        int removed = 0;

        var iterator = buckets.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired(currentTime, windowSizeMs)) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Cleaned up {} expired rate limit buckets", removed);
        }
    }
}

