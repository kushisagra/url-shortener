package com.kushagra.urlshortner.Repository;

import com.kushagra.urlshortner.Entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    // Spring auto-implements this by reading the method name!
    // "find by short code" → SELECT * FROM urls WHERE short_code = ?
    Optional<Url> findByShortCode(String shortCode);

    // Check if a code already exists (for uniqueness validation)
    boolean existsByShortCode(String shortCode);
}