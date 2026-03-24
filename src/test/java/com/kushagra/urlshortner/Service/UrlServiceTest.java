package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.DTO.ShortenRequest;
import com.kushagra.urlshortner.DTO.ShortenResponse;
import com.kushagra.urlshortner.Entity.Url;
import com.kushagra.urlshortner.Exception.DuplicateAliasException;
import com.kushagra.urlshortner.Exception.UrlExpiredException;
import com.kushagra.urlshortner.Exception.UrlNotFoundException;
import com.kushagra.urlshortner.Repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for UrlService
 *
 * We mock ALL dependencies:
 * - UrlRepository
 * - ShortCodeGenerator
 * - CacheService
 * - AnalyticsService
 *
 * This tests the UrlService logic in complete isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UrlService Unit Tests")
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private CacheService cacheService;

    @Mock
    private AnalyticsService analyticsService;

    @Captor
    private ArgumentCaptor<Url> urlCaptor;

    private UrlService urlService;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        urlService = new UrlService(urlRepository, shortCodeGenerator, cacheService, analyticsService);
        // Inject the baseUrl value (normally done by Spring)
        ReflectionTestUtils.setField(urlService, "baseUrl", BASE_URL);
    }

    // ==================== NESTED CLASS: shortenResponse Tests ====================
    @Nested
    @DisplayName("shortenResponse() method")
    class ShortenResponseTests {

        @Test
        @DisplayName("Should generate short code when no custom alias provided")
        void shortenResponse_WithNoCustomAlias_ShouldGenerateCode() {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setCustomAlias(null);

            when(shortCodeGenerator.generate()).thenReturn("abc12345");
            when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> {
                Url url = invocation.getArgument(0);
                url.setCreatedAt(LocalDateTime.now());
                return url;
            });

            // Act
            ShortenResponse response = urlService.shortenResponse(request);

            // Assert
            assertThat(response.getShortCode()).isEqualTo("abc12345");
            assertThat(response.getShortUrl()).isEqualTo(BASE_URL + "/abc12345");
            assertThat(response.getOriginalUrl()).isEqualTo("https://google.com");

            verify(shortCodeGenerator).generate();
            verify(urlRepository).save(urlCaptor.capture());
            assertThat(urlCaptor.getValue().getShortCode()).isEqualTo("abc12345");
        }

        @Test
        @DisplayName("Should use custom alias when provided")
        void shortenResponse_WithCustomAlias_ShouldUseIt() {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setCustomAlias("mylink");

            when(urlRepository.existsByShortCode("mylink")).thenReturn(false);
            when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> {
                Url url = invocation.getArgument(0);
                url.setCreatedAt(LocalDateTime.now());
                return url;
            });

            // Act
            ShortenResponse response = urlService.shortenResponse(request);

            // Assert
            assertThat(response.getShortCode()).isEqualTo("mylink");
            verify(shortCodeGenerator, never()).generate(); // Should NOT generate
        }

        @Test
        @DisplayName("Should throw DuplicateAliasException when custom alias exists")
        void shortenResponse_WithDuplicateAlias_ShouldThrowException() {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setCustomAlias("existing");

            when(urlRepository.existsByShortCode("existing")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> urlService.shortenResponse(request))
                    .isInstanceOf(DuplicateAliasException.class)
                    .hasMessageContaining("existing");

            verify(urlRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set expiration date when expiryInDays provided")
        void shortenResponse_WithExpiry_ShouldSetExpirationDate() {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setExpiryInDays(7);

            when(shortCodeGenerator.generate()).thenReturn("abc12345");
            when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ShortenResponse response = urlService.shortenResponse(request);

            // Assert
            verify(urlRepository).save(urlCaptor.capture());
            Url savedUrl = urlCaptor.getValue();
            assertThat(savedUrl.getExpiresAt()).isNotNull();
            assertThat(savedUrl.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(savedUrl.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(8));
        }
    }

    // ==================== NESTED CLASS: resolveUrl Tests ====================
    @Nested
    @DisplayName("resolveUrl() method")
    class ResolveUrlTests {

        @Test
        @DisplayName("Should return cached URL on cache hit")
        void resolveUrl_CacheHit_ShouldReturnCachedUrl() {
            // Arrange
            when(cacheService.getCachedUrl("abc123")).thenReturn("https://cached.com");

            // Act
            String result = urlService.resolveUrl("abc123", "127.0.0.1", "Mozilla", null);

            // Assert
            assertThat(result).isEqualTo("https://cached.com");
            verify(urlRepository, never()).findByShortCode(anyString()); // DB not called
            verify(analyticsService).recordClick("abc123", "127.0.0.1", "Mozilla", null);
        }

        @Test
        @DisplayName("Should query database on cache miss")
        void resolveUrl_CacheMiss_ShouldQueryDatabase() {
            // Arrange
            when(cacheService.getCachedUrl("abc123")).thenReturn(null); // Cache miss

            Url url = Url.builder()
                    .shortCode("abc123")
                    .originalUrl("https://example.com")
                    .clickCount(5L)
                    .build();
            when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));
            when(urlRepository.save(any(Url.class))).thenReturn(url);

            // Act
            String result = urlService.resolveUrl("abc123", "127.0.0.1", "Mozilla", null);

            // Assert
            assertThat(result).isEqualTo("https://example.com");
            verify(urlRepository).findByShortCode("abc123");
            verify(cacheService).cacheUrl(eq("abc123"), eq("https://example.com"));
        }

        @Test
        @DisplayName("Should throw UrlNotFoundException when URL not found")
        void resolveUrl_NotFound_ShouldThrowException() {
            // Arrange
            when(cacheService.getCachedUrl("notexist")).thenReturn(null);
            when(urlRepository.findByShortCode("notexist")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> urlService.resolveUrl("notexist", "127.0.0.1", "Mozilla", null))
                    .isInstanceOf(UrlNotFoundException.class)
                    .hasMessageContaining("notexist");
        }

        @Test
        @DisplayName("Should throw UrlExpiredException when URL is expired")
        void resolveUrl_Expired_ShouldThrowException() {
            // Arrange
            when(cacheService.getCachedUrl("expired")).thenReturn(null);

            Url url = Url.builder()
                    .shortCode("expired")
                    .originalUrl("https://example.com")
                    .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                    .build();
            when(urlRepository.findByShortCode("expired")).thenReturn(Optional.of(url));

            // Act & Assert
            assertThatThrownBy(() -> urlService.resolveUrl("expired", "127.0.0.1", "Mozilla", null))
                    .isInstanceOf(UrlExpiredException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should increment click count on resolve")
        void resolveUrl_ShouldIncrementClickCount() {
            // Arrange
            when(cacheService.getCachedUrl("abc123")).thenReturn(null);

            Url url = Url.builder()
                    .shortCode("abc123")
                    .originalUrl("https://example.com")
                    .clickCount(10L)
                    .build();
            when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));
            when(urlRepository.save(any(Url.class))).thenReturn(url);

            // Act
            urlService.resolveUrl("abc123", "127.0.0.1", "Mozilla", null);

            // Assert
            verify(urlRepository).save(urlCaptor.capture());
            assertThat(urlCaptor.getValue().getClickCount()).isEqualTo(11L);
        }
    }
}

