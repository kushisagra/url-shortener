package com.kushagra.urlshortner.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response returned after successfully shortening a URL")
public class ShortenResponse {

    @Schema(description = "Full shortened URL", example = "https://url-shortener-zcrg.onrender.com/aB3xKz")
    private String shortUrl;

    @Schema(description = "Just the short code", example = "aB3xKz")
    private String shortCode;

    @Schema(description = "The original URL that was shortened", example = "https://www.example.com/very/long/path")
    private String originalUrl;

    @Schema(description = "When the short URL was created", example = "2026-03-26T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the short URL expires (null = never)", example = "2026-04-25T10:30:00", nullable = true)
    private LocalDateTime expiresAt;
}
