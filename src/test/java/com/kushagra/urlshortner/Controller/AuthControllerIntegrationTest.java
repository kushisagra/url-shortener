package com.kushagra.urlshortner.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kushagra.urlshortner.DTO.LoginRequest;
import com.kushagra.urlshortner.DTO.RegisterRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for AuthController
 *
 * @SpringBootTest — loads full application context
 * @AutoConfigureMockMvc — provides MockMvc for HTTP testing
 *
 * Tests the FULL request lifecycle:
 * HTTP Request → Security Filter → Controller → Service → Repository → Response
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
    }

    // ==================== Registration Tests ====================
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_ValidRequest_ShouldReturn201() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("newuser@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.message").value("Registration successful"));
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void register_DuplicateEmail_ShouldReturn400() throws Exception {
            // Arrange: Register first user
            RegisterRequest request = new RegisterRequest("duplicate@example.com", "password123");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Act & Assert: Try to register again with same email
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(containsString("already registered")));
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void register_InvalidEmail_ShouldReturn400() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("not-an-email", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.email").exists());
        }

        @Test
        @DisplayName("Should reject short password")
        void register_ShortPassword_ShouldReturn400() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("test@example.com", "12345"); // 5 chars

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.password").exists());
        }
    }

    // ==================== Login Tests ====================
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        void registerTestUser() throws Exception {
            // Register a test user before login tests
            RegisterRequest request = new RegisterRequest("login@example.com", "password123");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Should login with valid credentials")
        void login_ValidCredentials_ShouldReturn200() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("login@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.email").value("login@example.com"));
        }

        @Test
        @DisplayName("Should reject invalid password")
        void login_InvalidPassword_ShouldReturn401() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("login@example.com", "wrongpassword");

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value(containsString("Invalid")));
        }

        @Test
        @DisplayName("Should reject non-existent user")
        void login_NonExistentUser_ShouldReturn401() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("notexist@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Token Validation Helper ====================
    /**
     * Helper method to get a valid JWT token for testing protected endpoints
     */
    protected String getValidToken() throws Exception {
        RegisterRequest request = new RegisterRequest("tokenuser@example.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}

