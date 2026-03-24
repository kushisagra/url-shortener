package com.kushagra.urlshortner.Repository;

import com.kushagra.urlshortner.Entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    // Total clicks for a short code


    long countByShortCode(String shortCode);

    // All clicks for a short code (for time-series analysis)
    List<ClickEvent> findByShortCodeOrderByClickedAtDesc(String shortCode);

    // Clicks within a time range — used for "last 7 days" analytics
    List<ClickEvent> findByShortCodeAndClickedAtBetween(
            String shortCode,
            LocalDateTime from,
            LocalDateTime to
    );

    // Group clicks by country — custom JPQL query
    @Query("SELECT c.country AS label, COUNT(c) AS count " +
            "FROM ClickEvent c " +
            "WHERE c.shortCode = :code AND c.country IS NOT NULL " +
            "GROUP BY c.country " +
            "ORDER BY COUNT(c) DESC")
    List<Map<String, Object>> countGroupedByCountry(@Param("code") String shortCode);

    // Group clicks by device type
    @Query("SELECT c.device AS label, COUNT(c) AS count " +
            "FROM ClickEvent c " +
            "WHERE c.shortCode = :code AND c.device IS NOT NULL " +
            "GROUP BY c.device " +
            "ORDER BY COUNT(c) DESC")
    List<Map<String, Object>> countGroupedByDevice(@Param("code") String shortCode);

    // Group clicks by browser
    @Query("SELECT c.browser AS label, COUNT(c) AS count " +
            "FROM ClickEvent c " +
            "WHERE c.shortCode = :code AND c.browser IS NOT NULL " +
            "GROUP BY c.browser " +
            "ORDER BY COUNT(c) DESC")
    List<Map<String, Object>> countGroupedByBrowser(@Param("code") String shortCode);
}