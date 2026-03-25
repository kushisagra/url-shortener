package com.kushagra.urlshortner.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO.
 *
 * Returned after successful login/registration.
 * Contains the JWT token the client should use for authenticated requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response returned after successful authentication")
public class AuthResponse {

    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type (always Bearer)", example = "Bearer")
    private String tokenType;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "Success message", example = "Login successful")
    private String message;
}

