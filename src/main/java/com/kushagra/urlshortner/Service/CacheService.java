package com.kushagra.urlshortner.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.cache.ttl-hours}")
    private long ttlHours;

    // Cache key prefix — avoids collision if you add other cached data later
    private static final String URL_PREFIX = "url:";

    public void cacheUrl(String shortCode, String originalUrl, Duration ttl) {
        String key = URL_PREFIX + shortCode;
        redisTemplate.opsForValue().set(key, originalUrl, ttl);
        log.info("Cached URL for code '{}' with TTL {}", shortCode, ttl);
    }

    public void cacheUrl(String shortCode, String originalUrl) {
        // Overload — uses default TTL when no expiry specified
        cacheUrl(shortCode, originalUrl, Duration.ofHours(ttlHours));
    }

    public String getCachedUrl(String shortCode) {
        String key = URL_PREFIX + shortCode;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.info("Cache HIT for short code '{}'", shortCode);
        } else {
            log.info("Cache MISS for short code '{}'", shortCode);
        }
        return value; // returns null if not in cache
    }

    public void evictUrl(String shortCode) {
        // Called when a URL is deleted or deactivated
        String key = URL_PREFIX + shortCode;
        Boolean deleted = redisTemplate.delete(key);
        log.info("Evicted cache for '{}': {}", shortCode, deleted);
    }
}