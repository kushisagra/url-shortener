package com.kushagra.urlshortner.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility — handles token creation and validation.
 *
 * JWT Structure:
 * - Header: algorithm + token type (automatically added)
 * - Payload: claims (subject=email, issued-at, expiration)
 * - Signature: HMAC-SHA256 hash of header+payload using our secret key
 *
 * The signature ensures tokens can't be tampered with.
 * If someone modifies the payload, the signature won't match.
 */
@Component
@Slf4j
public class JwtUtils {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        // Create a secure key from our secret string
        // The key must be at least 256 bits for HS256 algorithm
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = expirationMs;
    }

    /**
     * Generate a JWT token for a user.
     *
     * @param email The user's email (becomes the "subject" claim)
     * @return Signed JWT token string
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)                    // Who this token is for
                .issuedAt(now)                     // When token was created
                .expiration(expiryDate)            // When token expires
                .signWith(secretKey)               // Sign with our secret key
                .compact();                        // Build the token string
    }

    /**
     * Extract the email (subject) from a JWT token.
     *
     * @param token The JWT token string
     * @return The email stored in the token
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)             // Use our key to verify signature
                .build()
                .parseSignedClaims(token)          // Parse and validate
                .getPayload()
                .getSubject();                     // Get the "sub" claim
    }

    /**
     * Validate a JWT token.
     *
     * Checks:
     * 1. Signature is valid (not tampered)
     * 2. Token is not expired
     * 3. Token is properly formatted
     *
     * @param token The JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT signature does not match: {}", e.getMessage());
        }
        return false;
    }
}

