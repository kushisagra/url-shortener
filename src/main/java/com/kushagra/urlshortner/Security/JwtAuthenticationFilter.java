package com.kushagra.urlshortner.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs on every request.
 *
 * How it works:
 * 1. Client sends request with header: "Authorization: Bearer <jwt_token>"
 * 2. This filter extracts the token
 * 3. Validates the token using JwtUtils
 * 4. If valid, sets up SecurityContext so the rest of the app knows who the user is
 *
 * OncePerRequestFilter ensures this runs exactly once per request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from the Authorization header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Validate token and set up security context
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {

                // Get email from token
                String email = jwtUtils.getEmailFromToken(jwt);

                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal (the user)
                                null,                  // Credentials (not needed after authentication)
                                userDetails.getAuthorities()  // User's roles/permissions
                        );

                // Attach request details (IP address, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set authentication in SecurityContext
                // Now the rest of the application knows this user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {}", email);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     *
     * Expected format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6..."
     *
     * @param request HTTP request
     * @return JWT token string, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }
        return null;
    }
}

