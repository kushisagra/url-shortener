package com.kushagra.urlshortner.Controller;

import com.kushagra.urlshortner.DTO.AuthResponse;
import com.kushagra.urlshortner.DTO.LoginRequest;
import com.kushagra.urlshortner.DTO.RegisterRequest;
import com.kushagra.urlshortner.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller — public endpoints for login/register.
 *
 * Endpoints:
 * - POST /api/auth/register — create new account
 * - POST /api/auth/login — authenticate and get JWT token
 *
 * These endpoints are PUBLIC (no authentication required).
 * Configured in SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "password": "securePassword123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "email": "user@example.com",
     *   "message": "Registration successful"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login an existing user.
     *
     * Request body:
     * {
     *   "email": "user@example.com",
     *   "password": "securePassword123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "email": "user@example.com",
     *   "message": "Login successful"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

