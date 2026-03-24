package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.Repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ShortCodeGenerator
 *
 * These tests run in ISOLATION — we mock the UrlRepository
 * so no real database is involved.
 *
 * @ExtendWith(MockitoExtension.class) — enables Mockito annotations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShortCodeGenerator Unit Tests")
class ShortCodeGeneratorTest {

    @Mock
    private UrlRepository urlRepository;  // Mocked — no real DB calls!

    private ShortCodeGenerator shortCodeGenerator;

    @BeforeEach
    void setUp() {
        // Create the generator with mocked repository
        shortCodeGenerator = new ShortCodeGenerator(urlRepository);
    }

    @Test
    @DisplayName("Should generate a short code of length 8")
    void generate_ShouldReturnCodeOfLength8() {
        // Arrange: Mock repository to say code doesn't exist
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act
        String code = shortCodeGenerator.generate();

        // Assert
        assertThat(code).hasSize(8);
    }

    @Test
    @DisplayName("Should generate code with only alphanumeric characters")
    void generate_ShouldContainOnlyAlphanumericCharacters() {
        // Arrange
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act
        String code = shortCodeGenerator.generate();

        // Assert: Only contains a-z, A-Z, 0-9
        assertThat(code).matches("^[a-zA-Z0-9]+$");
    }

    @Test
    @DisplayName("Should retry on collision and return unique code")
    void generate_ShouldRetryOnCollision() {
        // Arrange: First call returns true (collision), second returns false
        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(true)   // 1st attempt: collision!
                .thenReturn(false); // 2nd attempt: success

        // Act
        String code = shortCodeGenerator.generate();

        // Assert: Should have tried twice
        verify(urlRepository, times(2)).existsByShortCode(anyString());
        assertThat(code).isNotNull();
    }

    @Test
    @DisplayName("Should use fallback after max retries")
    void generate_ShouldUseFallbackAfterMaxRetries() {
        // Arrange: Always return true (simulate constant collisions)
        when(urlRepository.existsByShortCode(anyString())).thenReturn(true);

        // Act
        String code = shortCodeGenerator.generate();

        // Assert: Should have tried MAX_RETRIES (5) times, then fallback
        verify(urlRepository, times(5)).existsByShortCode(anyString());
        assertThat(code).isNotNull();
        // Fallback code might be longer due to timestamp suffix
    }

    @Test
    @DisplayName("Should generate different codes on multiple calls")
    void generate_ShouldGenerateDifferentCodes() {
        // Arrange
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act: Generate multiple codes
        String code1 = shortCodeGenerator.generate();
        String code2 = shortCodeGenerator.generate();
        String code3 = shortCodeGenerator.generate();

        // Assert: All codes should be different (statistically)
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code2).isNotEqualTo(code3);
        assertThat(code1).isNotEqualTo(code3);
    }
}

