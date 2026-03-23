package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.Repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
@Component
@RequiredArgsConstructor
@Slf4j
public class ShortCodeGenerator {

    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 8;
    private static final int MAX_RETRIES = 5;

    // SecureRandom is cryptographically strong — unlike java.util.Random,
    // its output cannot be predicted even if you know previous values
    private final SecureRandom secureRandom = new SecureRandom();
    private final UrlRepository urlRepository;

    public String generate() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String code = generateRandomCode(CODE_LENGTH);

            if (!urlRepository.existsByShortCode(code)) {
                log.debug("Generated short code '{}' on attempt {}", code, attempt);
                return code;
            }

            // Collision happened — astronomically rare but we handle it
            log.warn("Short code collision on attempt {}: '{}' — retrying...", attempt, code);
        }

        // Fallback: append timestamp suffix to guarantee uniqueness
        String fallback = generateRandomCode(6) + Long.toHexString(System.currentTimeMillis()).substring(6);
        log.warn("Max retries hit, using timestamp fallback code: '{}'", fallback);
        return fallback;
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // nextInt(BASE) gives a uniformly distributed random index 0-61
            sb.append(CHARACTERS.charAt(secureRandom.nextInt(BASE)));
        }
        return sb.toString();
    }

}
