package com.kushagra.urlshortner.Controller;

import com.kushagra.urlshortner.DTO.AuthResponse;
import com.kushagra.urlshortner.DTO.LoginRequest;
import com.kushagra.urlshortner.DTO.RegisterRequest;
import com.kushagra.urlshortner.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Register and login to get JWT tokens. No authentication required for these endpoints.")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     */
    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account and returns a JWT token.
                    
                    **Use this token in the Authorization header for protected endpoints:**
                    ```
                    Authorization: Bearer <your_token>
                    ```
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "tokenType": "Bearer",
                                      "email": "user@example.com",
                                      "message": "Registration successful"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (email format, password too short)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirements // Empty = No security required (public endpoint)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login an existing user.
     */
    @Operation(
            summary = "Login with existing credentials",
            description = """
                    Authenticates user and returns a JWT token.
                    
                    **Steps:**
                    1. Send email and password
                    2. Receive JWT token in response
                    3. Use token in Authorization header: `Bearer <token>`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "tokenType": "Bearer",
                                      "email": "user@example.com",
                                      "message": "Login successful"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(mediaType = "application/json")
            )
    })
    @SecurityRequirements // Empty = No security required (public endpoint)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

