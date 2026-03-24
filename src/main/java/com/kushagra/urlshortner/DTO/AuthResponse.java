package com.kushagra.urlshortner.DTO;

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
public class AuthResponse {

    private String token;      // JWT token
    private String tokenType;  // Always "Bearer"
    private String email;      // User's email
    private String message;    // Success message
}

