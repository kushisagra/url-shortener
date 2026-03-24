package com.kushagra.urlshortner.Controller;

import com.kushagra.urlshortner.DTO.AnalyticsSummary;
import com.kushagra.urlshortner.DTO.ShortenRequest;
import com.kushagra.urlshortner.DTO.ShortenResponse;
import com.kushagra.urlshortner.Exception.RateLimitExceededException;
import com.kushagra.urlshortner.Security.RateLimitingService;
import com.kushagra.urlshortner.Service.AnalyticsService;
import com.kushagra.urlshortner.Service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * URL Controller — handles URL shortening and redirection.
 *
 * Route protection:
 * - POST /api/shorten — PROTECTED (requires JWT) + rate limited
 * - GET /{shortCode} — PUBLIC (anyone can access short links)
 * - GET /api/analytics/{shortCode} — PUBLIC (can be made protected later)
 */
@RestController
@RequiredArgsConstructor
public class UrlController {


    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final RateLimitingService rateLimitingService;

    /**
     * Shorten a URL.
     *
     * PROTECTED: Requires valid JWT token in Authorization header.
     * RATE LIMITED: Max 10 requests per minute per IP.
     *
     * Headers required:
     *   Authorization: Bearer <jwt_token>
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {

        // Rate limiting check
        String ipAddress = getClientIp(httpRequest);
        if (!rateLimitingService.isAllowed(ipAddress)) {
            throw new RateLimitExceededException(ipAddress);
        }

        ShortenResponse response = urlService.shortenResponse(request);

        // Include rate limit info in response headers (helpful for clients)
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Remaining",
                String.valueOf(rateLimitingService.getRemainingRequests(ipAddress)));

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {  // ← inject the raw HTTP request

        // Extract metadata from HTTP headers
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        String originalUrl = urlService.resolveUrl(shortCode, ipAddress, userAgent, referer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", originalUrl);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    // X-Forwarded-For handles the case where your app sits behind a proxy/load balancer
// In that case the real client IP is in this header, not remoteAddr
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim(); // take first IP if chain exists
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<AnalyticsSummary> getAnalytics(
            @PathVariable String shortCode) {

        AnalyticsSummary summary = analyticsService.getSummary(shortCode);
        return ResponseEntity.ok(summary);
    }
}