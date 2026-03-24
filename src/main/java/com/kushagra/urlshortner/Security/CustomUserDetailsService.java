package com.kushagra.urlshortner.Security;

import com.kushagra.urlshortner.Entity.User;
import com.kushagra.urlshortner.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService — tells Spring Security how to load users.
 *
 * Spring Security calls loadUserByUsername() during authentication.
 * We fetch the user from our database and convert to Spring's UserDetails.
 *
 * Why this is needed:
 * - Spring Security doesn't know about our User entity
 * - This bridges our database with Spring Security
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email (we use email as the username).
     *
     * Called by Spring Security during login.
     *
     * @param email The user's email address
     * @return UserDetails object that Spring Security can work with
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Convert our User entity to Spring Security's UserDetails
        // The authority is prefixed with "ROLE_" by convention
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}

