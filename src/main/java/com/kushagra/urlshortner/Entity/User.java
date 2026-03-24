package com.kushagra.urlshortner.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User entity for authentication.
 *
 * Why we need this:
 * - Stores registered users who can create/delete URLs
 * - Password is stored as a BCrypt hash (never plain text!)
 * - Role determines what actions the user can perform
 */
@Entity
@Table(name = "users")  // "user" is a reserved keyword in PostgreSQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt hashed — NEVER store plain text!

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Role enum — keeps authorization simple.
     * USER = can create URLs
     * ADMIN = can manage all URLs (future feature)
     */
    public enum Role {
        USER,
        ADMIN
    }
}

