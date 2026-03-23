package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.DTO.ShortenRequest;
import com.kushagra.urlshortner.DTO.ShortenResponse;
import com.kushagra.urlshortner.Entity.Url;
import com.kushagra.urlshortner.Exception.DuplicateAliasException;
import com.kushagra.urlshortner.Exception.UrlExpiredException;
import com.kushagra.urlshortner.Exception.UrlNotFoundException;
import com.kushagra.urlshortner.Repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    private final CacheService cacheService;

    @Value("${app.base-url}")
    private String baseUrl;

    public ShortenResponse shortenResponse(ShortenRequest request){

        String shortCode;

        if(request.getCustomAlias() != null && !request.getCustomAlias().isEmpty()){
            // User provided a custom alias
            shortCode = request.getCustomAlias();
            if(urlRepository.existsByShortCode(shortCode)){
                throw new DuplicateAliasException(request.getCustomAlias());
            }
        } else {
            // Generate a random short code
                shortCode = shortCodeGenerator.generate();
        }

        LocalDateTime expiresAt = null;
        if (request.getExpiryInDays() != null) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpiryInDays());
        }

        Url url= Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .expiresAt(expiresAt)
                .build();

        Url saved = urlRepository.save(url);

        return ShortenResponse.builder()
                .shortCode(saved.getShortCode())
                .shortUrl(baseUrl + "/" + saved.getShortCode())
                .originalUrl(saved.getOriginalUrl())
                .createdAt(saved.getCreatedAt())
                .expiresAt(saved.getExpiresAt())
                .build();



    }

    public String resolveUrl(String shortCode) {

        String cachedUrl = cacheService.getCachedUrl(shortCode);
        if (cachedUrl != null) {
            return cachedUrl;
        }
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        // Check if expired
        if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new UrlExpiredException(shortCode);
        }
        // 4. Store in Redis so next request is a cache hit
        // Use remaining TTL if link has expiry, otherwise use default
        if (url.getExpiresAt() != null) {
            Duration remainingTtl = Duration.between(LocalDateTime.now(), url.getExpiresAt());
            cacheService.cacheUrl(shortCode, url.getOriginalUrl(), remainingTtl);
        } else {
            cacheService.cacheUrl(shortCode, url.getOriginalUrl());
        }


        // Increment click count
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }
}


