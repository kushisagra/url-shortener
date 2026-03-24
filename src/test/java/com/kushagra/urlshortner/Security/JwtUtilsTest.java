package com.kushagra.urlshortner.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit Tests for JwtUtils
 *
 * Tests JWT token generation and validation.
 * No mocking needed — JwtUtils is self-contained.
 */
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Test secret must be at least 256 bits (32 characters)
    private static final String TEST_SECRET = "TestSecretKeyForJWTTokenMustBeAtLeast256BitsLong12345";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void generateToken_ShouldReturnValidToken() {
        // Act
        String token = jwtUtils.generateToken("test@example.com");

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should extract email from token")
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtils.generateToken(email);

        // Act
        String extractedEmail = jwtUtils.getEmailFromToken(token);

        // Assert
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should validate a valid token")
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateToken("test@example.com");

        // Act
        boolean isValid = jwtUtils.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateToken("invalid.token.here");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject tampered token")
    void validateToken_TamperedToken_ShouldReturnFalse() {
        // Arrange: Create a completely invalid token structure
        String tamperedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJoYWNrZXJAZXhhbXBsZS5jb20ifQ.invalidsignature";

        // Act
        boolean isValid = jwtUtils.validateToken(tamperedToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        // Arrange: Create JwtUtils with 1ms expiration
        JwtUtils shortLivedJwtUtils = new JwtUtils(TEST_SECRET, 1);
        String token = shortLivedJwtUtils.generateToken("test@example.com");

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = shortLivedJwtUtils.validateToken(token);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void generateToken_DifferentUsers_ShouldGenerateDifferentTokens() {
        // Act
        String token1 = jwtUtils.generateToken("user1@example.com");
        String token2 = jwtUtils.generateToken("user2@example.com");

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should reject null token")
    void validateToken_NullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateToken(null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void validateToken_EmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateToken("");

        // Assert
        assertThat(isValid).isFalse();
    }
}

