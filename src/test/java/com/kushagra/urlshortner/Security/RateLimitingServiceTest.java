package com.kushagra.urlshortner.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit Tests for RateLimitingService
 *
 * Tests rate limiting logic.
 */
@DisplayName("RateLimitingService Unit Tests")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    private static final int MAX_REQUESTS = 5;
    private static final int WINDOW_SECONDS = 60;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService(MAX_REQUESTS, WINDOW_SECONDS);
    }

    @Test
    @DisplayName("Should allow requests under the limit")
    void isAllowed_UnderLimit_ShouldReturnTrue() {
        // Act & Assert: First 5 requests should be allowed
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertThat(rateLimitingService.isAllowed("192.168.1.1")).isTrue();
        }
    }

    @Test
    @DisplayName("Should block requests over the limit")
    void isAllowed_OverLimit_ShouldReturnFalse() {
        String ip = "192.168.1.2";

        // Exhaust the limit
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimitingService.isAllowed(ip);
        }

        // Act: 6th request should be blocked
        boolean allowed = rateLimitingService.isAllowed(ip);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("Should track different IPs separately")
    void isAllowed_DifferentIPs_ShouldTrackSeparately() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        // Exhaust limit for IP1
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimitingService.isAllowed(ip1);
        }

        // Assert: IP1 blocked, IP2 still allowed
        assertThat(rateLimitingService.isAllowed(ip1)).isFalse();
        assertThat(rateLimitingService.isAllowed(ip2)).isTrue();
    }

    @Test
    @DisplayName("Should return correct remaining requests")
    void getRemainingRequests_ShouldReturnCorrectCount() {
        String ip = "192.168.1.3";

        // Initially should have max requests
        assertThat(rateLimitingService.getRemainingRequests(ip)).isEqualTo(MAX_REQUESTS);

        // Make 2 requests
        rateLimitingService.isAllowed(ip);
        rateLimitingService.isAllowed(ip);

        // Should have 3 remaining
        assertThat(rateLimitingService.getRemainingRequests(ip)).isEqualTo(MAX_REQUESTS - 2);
    }

    @Test
    @DisplayName("Should return 0 remaining when limit exceeded")
    void getRemainingRequests_OverLimit_ShouldReturnZero() {
        String ip = "192.168.1.4";

        // Exhaust limit + 1 extra attempt
        for (int i = 0; i <= MAX_REQUESTS; i++) {
            rateLimitingService.isAllowed(ip);
        }

        // Assert
        assertThat(rateLimitingService.getRemainingRequests(ip)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reset after window expires")
    void isAllowed_AfterWindowExpires_ShouldReset() throws InterruptedException {
        // Create service with 1 second window
        RateLimitingService shortWindowService = new RateLimitingService(2, 1);
        String ip = "192.168.1.5";

        // Exhaust limit
        shortWindowService.isAllowed(ip);
        shortWindowService.isAllowed(ip);
        assertThat(shortWindowService.isAllowed(ip)).isFalse();

        // Wait for window to expire
        Thread.sleep(1100);

        // Should be allowed again
        assertThat(shortWindowService.isAllowed(ip)).isTrue();
    }
}

