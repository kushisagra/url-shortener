package com.kushagra.urlshortner.Repository;

import com.kushagra.urlshortner.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 *
 * findByEmail — used during login to fetch user by their email address.
 * existsByEmail — used during registration to check if email is already taken.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

