package com.kushagra.urlshortner.Service;

import com.kushagra.urlshortner.DTO.AuthResponse;
import com.kushagra.urlshortner.DTO.LoginRequest;
import com.kushagra.urlshortner.DTO.RegisterRequest;
import com.kushagra.urlshortner.Entity.User;
import com.kushagra.urlshortner.Repository.UserRepository;
import com.kushagra.urlshortner.Security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service — handles registration and login.
 *
 * Registration flow:
 * 1. Check if email already exists
 * 2. Hash the password (never store plain text!)
 * 3. Save user to database
 * 4. Generate and return JWT token
 *
 * Login flow:
 * 1. AuthenticationManager validates email/password
 * 2. If valid, generate and return JWT token
 * 3. If invalid, throw exception (handled by GlobalExceptionHandler)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     *
     * @param request Registration details (email, password)
     * @return AuthResponse with JWT token
     * @throws IllegalArgumentException if email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Create new user with hashed password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash it!
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Generate token for immediate login
        String token = jwtUtils.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .message("Registration successful")
                .build();
    }

    /**
     * Authenticate an existing user.
     *
     * @param request Login details (email, password)
     * @return AuthResponse with JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager does the heavy lifting:
        // - Loads user from database (via UserDetailsService)
        // - Compares password hash
        // - Throws exception if invalid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        log.info("User logged in: {}", request.getEmail());

        // Generate token
        String token = jwtUtils.generateToken(request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(request.getEmail())
                .message("Login successful")
                .build();
    }
}

