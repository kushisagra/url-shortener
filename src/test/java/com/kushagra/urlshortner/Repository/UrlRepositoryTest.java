package com.kushagra.urlshortner.Repository;

import com.kushagra.urlshortner.Entity.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository Tests for UrlRepository
 *
 * @SpringBootTest — loads full application context
 * @Transactional — each test runs in a transaction that's rolled back
 * @ActiveProfiles("test") — uses application-test.properties with H2 database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UrlRepository Tests")
class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    private Url testUrl;

    @BeforeEach
    void setUp() {
        // Clean slate for each test
        urlRepository.deleteAll();

        // Create a test URL
        testUrl = Url.builder()
                .shortCode("abc12345")
                .originalUrl("https://example.com")
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve URL by short code")
    void findByShortCode_ExistingCode_ShouldReturnUrl() {
        // Arrange
        urlRepository.save(testUrl);

        // Act
        Optional<Url> found = urlRepository.findByShortCode("abc12345");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(found.get().getShortCode()).isEqualTo("abc12345");
    }

    @Test
    @DisplayName("Should return empty when short code not found")
    void findByShortCode_NonExistingCode_ShouldReturnEmpty() {
        // Act
        Optional<Url> found = urlRepository.findByShortCode("notexist");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if short code exists")
    void existsByShortCode_ExistingCode_ShouldReturnTrue() {
        // Arrange
        urlRepository.save(testUrl);

        // Act & Assert
        assertThat(urlRepository.existsByShortCode("abc12345")).isTrue();
        assertThat(urlRepository.existsByShortCode("notexist")).isFalse();
    }

    @Test
    @DisplayName("Should save URL with expiration date")
    void save_WithExpiration_ShouldPersistExpirationDate() {
        // Arrange
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        testUrl.setExpiresAt(expiresAt);

        // Act
        Url saved = urlRepository.save(testUrl);
        Optional<Url> found = urlRepository.findByShortCode("abc12345");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getExpiresAt()).isNotNull();
        assertThat(found.get().getExpiresAt().toLocalDate()).isEqualTo(expiresAt.toLocalDate());
    }

    @Test
    @DisplayName("Should update click count")
    void save_UpdateClickCount_ShouldPersist() {
        // Arrange
        urlRepository.save(testUrl);

        // Act
        Url url = urlRepository.findByShortCode("abc12345").get();
        url.setClickCount(100L);
        urlRepository.save(url);

        // Assert
        Url updated = urlRepository.findByShortCode("abc12345").get();
        assertThat(updated.getClickCount()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should auto-generate createdAt timestamp")
    void save_ShouldAutoGenerateCreatedAt() {
        // Act
        Url saved = urlRepository.save(testUrl);

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Should handle multiple URLs")
    void findAll_MultipleUrls_ShouldReturnAll() {
        // Arrange
        urlRepository.save(testUrl);
        urlRepository.save(Url.builder()
                .shortCode("xyz98765")
                .originalUrl("https://google.com")
                .build());
        urlRepository.save(Url.builder()
                .shortCode("def45678")
                .originalUrl("https://github.com")
                .build());

        // Act
        long count = urlRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }
}

