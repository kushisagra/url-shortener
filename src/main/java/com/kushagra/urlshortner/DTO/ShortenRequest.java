package com.kushagra.urlshortner.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;

@Data
@Schema(description = "Request body for shortening a URL")
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(
            regexp = "^(https?://).*",
            message = "URL must start with http:// or https://"
    )
    @Schema(
            description = "The original URL to shorten",
            example = "https://www.example.com/very/long/path/to/resource",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String originalUrl;

    @Schema(
            description = "Optional custom alias for the short URL (leave empty for auto-generated)",
            example = "my-custom-link",
            nullable = true
    )
    private String customAlias;

    @Schema(
            description = "Optional expiration in days (null = never expires)",
            example = "30",
            nullable = true
    )
    private Integer expiryInDays;
}

