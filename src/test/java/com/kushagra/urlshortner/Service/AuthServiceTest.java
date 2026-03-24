package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.DTO.AuthResponse;
import com.kushagra.urlshortner.DTO.LoginRequest;
import com.kushagra.urlshortner.DTO.RegisterRequest;
import com.kushagra.urlshortner.Entity.User;
import com.kushagra.urlshortner.Repository.UserRepository;
import com.kushagra.urlshortner.Security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService
 *
 * Tests registration and login logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtils, authenticationManager);
    }

    // ==================== NESTED CLASS: register Tests ====================
    @Nested
    @DisplayName("register() method")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_NewUser_ShouldSucceed() {
            // Arrange
            RegisterRequest request = new RegisterRequest("test@example.com", "password123");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtUtils.generateToken("test@example.com")).thenReturn("jwt.token.here");

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getMessage()).contains("successful");

            // Verify password was hashed
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
            assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_ExistingEmail_ShouldThrowException() {
            // Arrange
            RegisterRequest request = new RegisterRequest("existing@example.com", "password123");
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already registered");

            // Verify no user was saved
            verify(userRepository, never()).save(any());
            verify(jwtUtils, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Should hash password before saving")
        void register_ShouldHashPassword() {
            // Arrange
            RegisterRequest request = new RegisterRequest("test@example.com", "plainPassword");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedHash");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtUtils.generateToken(anyString())).thenReturn("token");

            // Act
            authService.register(request);

            // Assert
            verify(passwordEncoder).encode("plainPassword");
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("plainPassword");
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$encodedHash");
        }
    }

    // ==================== NESTED CLASS: login Tests ====================
    @Nested
    @DisplayName("login() method")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully with valid credentials")
        void login_ValidCredentials_ShouldSucceed() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", null));
            when(jwtUtils.generateToken("test@example.com")).thenReturn("jwt.token.here");

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception with invalid credentials")
        void login_InvalidCredentials_ShouldThrowException() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            // Verify no token was generated
            verify(jwtUtils, never()).generateToken(anyString());
        }
    }
}

