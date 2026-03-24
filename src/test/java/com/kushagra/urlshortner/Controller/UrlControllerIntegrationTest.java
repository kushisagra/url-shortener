package com.kushagra.urlshortner.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kushagra.urlshortner.DTO.RegisterRequest;
import com.kushagra.urlshortner.DTO.ShortenRequest;
import com.kushagra.urlshortner.Entity.Url;
import com.kushagra.urlshortner.Repository.UrlRepository;
import com.kushagra.urlshortner.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for UrlController
 *
 * Tests the full URL shortening flow including:
 * - Authentication (protected routes)
 * - URL creation
 * - URL redirection
 * - Rate limiting
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UrlController Integration Tests")
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        urlRepository.deleteAll();
        userRepository.deleteAll();

        // Register a user and get token
        validToken = registerAndGetToken("testuser@example.com", "password123");
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    // ==================== Shorten URL Tests ====================
    @Nested
    @DisplayName("POST /api/shorten")
    class ShortenTests {

        @Test
        @DisplayName("Should shorten URL with valid token")
        void shorten_WithValidToken_ShouldReturn201() throws Exception {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");

            // Act & Assert
            mockMvc.perform(post("/api/shorten")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.shortCode").isNotEmpty())
                    .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                    .andExpect(jsonPath("$.originalUrl").value("https://google.com"));
        }

        @Test
        @DisplayName("Should reject request without token")
        void shorten_WithoutToken_ShouldReturn401Or403() throws Exception {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");

            // Act & Assert: Should be forbidden/unauthorized
            mockMvc.perform(post("/api/shorten")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Should use custom alias when provided")
        void shorten_WithCustomAlias_ShouldUseIt() throws Exception {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setCustomAlias("myalias");

            // Act & Assert
            mockMvc.perform(post("/api/shorten")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.shortCode").value("myalias"));
        }

        @Test
        @DisplayName("Should reject duplicate custom alias")
        void shorten_DuplicateAlias_ShouldReturn409() throws Exception {
            // Arrange: Create first URL
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");
            request.setCustomAlias("taken");

            mockMvc.perform(post("/api/shorten")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Act & Assert: Try to use same alias
            mockMvc.perform(post("/api/shorten")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value(containsString("taken")));
        }

        @Test
        @DisplayName("Should include rate limit header")
        void shorten_ShouldIncludeRateLimitHeader() throws Exception {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");

            // Act & Assert
            mockMvc.perform(post("/api/shorten")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("X-RateLimit-Remaining"));
        }
    }

    // ==================== Redirect Tests ====================
    @Nested
    @DisplayName("GET /{shortCode}")
    class RedirectTests {

        @Test
        @DisplayName("Should redirect to original URL")
        void redirect_ValidShortCode_ShouldRedirect() throws Exception {
            // Arrange: Create a URL directly in DB
            Url url = Url.builder()
                    .shortCode("testcode")
                    .originalUrl("https://example.com")
                    .build();
            urlRepository.save(url);

            // Act & Assert
            mockMvc.perform(get("/testcode"))
                    .andExpect(status().isFound()) // 302
                    .andExpect(header().string("Location", "https://example.com"));
        }

        @Test
        @DisplayName("Should work without authentication (public route)")
        void redirect_WithoutToken_ShouldWork() throws Exception {
            // Arrange
            Url url = Url.builder()
                    .shortCode("public")
                    .originalUrl("https://example.com")
                    .build();
            urlRepository.save(url);

            // Act & Assert: No token needed!
            mockMvc.perform(get("/public"))
                    .andExpect(status().isFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent short code")
        void redirect_NonExistentCode_ShouldReturn404() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/notexist"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(containsString("not found")));
        }

        @Test
        @DisplayName("Should return 410 for expired URL")
        void redirect_ExpiredUrl_ShouldReturn410() throws Exception {
            // Arrange
            Url url = Url.builder()
                    .shortCode("expired")
                    .originalUrl("https://example.com")
                    .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                    .build();
            urlRepository.save(url);

            // Act & Assert
            mockMvc.perform(get("/expired"))
                    .andExpect(status().isGone()) // 410
                    .andExpect(jsonPath("$.error").value(containsString("expired")));
        }
    }

    // ==================== Rate Limiting Tests ====================
    @Nested
    @DisplayName("Rate Limiting")
    class RateLimitingTests {

        @Test
        @DisplayName("Should block after exceeding rate limit")
        void shorten_ExceedRateLimit_ShouldReturn429() throws Exception {
            // Arrange
            ShortenRequest request = new ShortenRequest();
            request.setOriginalUrl("https://google.com");

            // Make 10 requests (the limit)
            for (int i = 0; i < 10; i++) {
                request.setCustomAlias("url" + i); // Different alias each time
                mockMvc.perform(post("/api/shorten")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // Act & Assert: 11th request should be rate limited
            request.setCustomAlias("url11");
            mockMvc.perform(post("/api/shorten")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isTooManyRequests()) // 429
                    .andExpect(jsonPath("$.error").value(containsString("Rate limit")));
        }
    }
}

