package com.kushagra.urlshortner.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ShortenResponse {
    private String shortUrl;       // full URL e.g. http://localhost:8080/aB3xKz
    private String shortCode;      // just the code e.g. aB3xKz
    private String originalUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
