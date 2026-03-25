package com.kushagra.urlshortner.Controller;

import com.kushagra.urlshortner.DTO.AnalyticsSummary;
import com.kushagra.urlshortner.DTO.ShortenRequest;
import com.kushagra.urlshortner.DTO.ShortenResponse;
import com.kushagra.urlshortner.Exception.RateLimitExceededException;
import com.kushagra.urlshortner.Security.RateLimitingService;
import com.kushagra.urlshortner.Service.AnalyticsService;
import com.kushagra.urlshortner.Service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "URL Shortening", description = "Create short URLs, redirect, and view analytics")
public class UrlController {


    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private final RateLimitingService rateLimitingService;

    /**
     * Shorten a URL.
     */
    @Operation(
            summary = "Shorten a URL",
            description = """
                    Creates a shortened URL for the given original URL.
                    
                    **🔒 Authentication Required** — Include JWT token in header.
                    
                    **🚦 Rate Limited** — Max 10 requests per minute per IP.
                    
                    **Optional features:**
                    - `customAlias`: Choose your own short code (e.g., "my-link")
                    - `expiryInDays`: Set expiration (null = never expires)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "URL shortened successfully",
                    headers = @Header(
                            name = "X-RateLimit-Remaining",
                            description = "Number of requests remaining in current window",
                            schema = @Schema(type = "integer")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShortenResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "shortUrl": "https://url-shortener-zcrg.onrender.com/aB3xKz",
                                      "shortCode": "aB3xKz",
                                      "originalUrl": "https://www.example.com/very/long/url",
                                      "createdAt": "2026-03-26T10:30:00",
                                      "expiresAt": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid URL format",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized — Missing or invalid JWT token",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Custom alias already taken",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded — Try again later",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirement(name = "bearerAuth")
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

    @Operation(
            summary = "Redirect to original URL",
            description = """
                    Redirects to the original URL associated with the short code.
                    
                    **🌐 Public** — No authentication required.
                    
                    This endpoint tracks analytics (IP, device, browser, referrer).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to original URL",
                    headers = @Header(
                            name = "Location",
                            description = "The original URL to redirect to",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Short code not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "URL has expired",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirements // Empty = No security required (public endpoint)
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "The short code to redirect", example = "aB3xKz")
            @PathVariable String shortCode,
            HttpServletRequest request) {

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

    @Operation(
            summary = "Get URL analytics",
            description = """
                    Returns analytics data for a shortened URL including:
                    - Total clicks
                    - Clicks in last 7 days
                    - Breakdown by country, device, and browser
                    
                    **🌐 Public** — No authentication required.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Analytics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnalyticsSummary.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "shortCode": "aB3xKz",
                                      "totalClicks": 150,
                                      "clicksLast7Days": 42,
                                      "byCountry": [
                                        {"country": "United States", "count": 80},
                                        {"country": "India", "count": 45}
                                      ],
                                      "byDevice": [
                                        {"device": "Mobile", "count": 90},
                                        {"device": "Desktop", "count": 60}
                                      ],
                                      "byBrowser": [
                                        {"browser": "Chrome", "count": 100},
                                        {"browser": "Safari", "count": 50}
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Short code not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirements // Empty = No security required (public endpoint)
    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<AnalyticsSummary> getAnalytics(
            @Parameter(description = "The short code to get analytics for", example = "aB3xKz")
            @PathVariable String shortCode) {

        AnalyticsSummary summary = analyticsService.getSummary(shortCode);
        return ResponseEntity.ok(summary);
    }
}