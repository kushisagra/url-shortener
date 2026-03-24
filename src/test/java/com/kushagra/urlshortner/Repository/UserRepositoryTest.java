package com.kushagra.urlshortner.Repository;

import com.kushagra.urlshortner.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository Tests for UserRepository
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should save and find user by email")
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("notexist@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail_ShouldWorkCorrectly() {
        // Arrange
        userRepository.save(testUser);

        // Act & Assert
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should auto-generate createdAt timestamp")
    void save_ShouldAutoGenerateCreatedAt() {
        // Act
        User saved = userRepository.save(testUser);

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should store password hash correctly")
    void save_ShouldStorePasswordHash() {
        // Arrange
        String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeK7TlV6q7yX6nSqHOPw5JJ6eZWvuSq9S";
        testUser.setPassword(hashedPassword);

        // Act
        userRepository.save(testUser);
        User found = userRepository.findByEmail("test@example.com").get();

        // Assert
        assertThat(found.getPassword()).isEqualTo(hashedPassword);
    }

    @Test
    @DisplayName("Should default role to USER")
    void save_DefaultRole_ShouldBeUser() {
        // Arrange
        User newUser = User.builder()
                .email("new@example.com")
                .password("password")
                .build();

        // Act
        User saved = userRepository.save(newUser);

        // Assert
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);
    }
}

