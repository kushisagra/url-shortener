package com.kushagra.urlshortner.Config;

import com.kushagra.urlshortner.Security.CustomUserDetailsService;
import com.kushagra.urlshortner.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration — the heart of Spring Security setup.
 *
 * Key concepts:
 *
 * 1. STATELESS sessions — we don't use server-side sessions.
 *    Each request must include a JWT token for authentication.
 *    This is standard for REST APIs.
 *
 * 2. Filter chain — requests pass through a series of filters.
 *    Our JwtAuthenticationFilter runs BEFORE Spring's default filter.
 *
 * 3. Route protection:
 *    - PUBLIC: /api/auth/**, GET /{shortCode}
 *    - PROTECTED: Everything else (requires JWT)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Main security filter chain configuration.
     *
     * This defines WHAT is protected and HOW.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            // CSRF protection is for session-based auth with cookies
            .csrf(csrf -> csrf.disable())

            // Configure route permissions
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication required
                .requestMatchers("/api/auth/**").permitAll()           // Login/Register
                .requestMatchers(HttpMethod.GET, "/{shortCode}").permitAll()  // Redirect (anyone can use short links)
                .requestMatchers("/api/analytics/**").permitAll()      // Analytics (optional: make protected later)

                // Swagger UI endpoints — public for API documentation
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Stateless session — no server-side session storage
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Use our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter BEFORE Spring's default authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Provider — connects UserDetailsService with PasswordEncoder.
     *
     * When a user logs in:
     * 1. DaoAuthenticationProvider loads user via userDetailsService
     * 2. Compares submitted password with stored hash using passwordEncoder
     * 3. If match, authentication succeeds
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Password Encoder — BCrypt is the industry standard.
     *
     * BCrypt automatically:
     * - Generates a random salt for each password
     * - Uses adaptive hashing (can increase work factor over time)
     * - Protects against rainbow table attacks
     *
     * NEVER store plain text passwords!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager — used by AuthService to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


