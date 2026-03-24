package com.kushagra.urlshortner.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "click_events", indexes = {
        // Index on shortCode — analytics queries always filter by this
        @Index(name = "idx_click_short_code", columnList = "shortCode"),
        // Index on clickedAt — for time-range queries (last 7 days etc.)
        @Index(name = "idx_click_time", columnList = "clickedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column
    private String ipAddress;

    @Column
    private String country;

    @Column
    private String device;    // MOBILE, DESKTOP, TABLET

    @Column
    private String browser;   // Chrome, Firefox, Safari etc.

    @Column
    private String os;        // Windows, macOS, Android, iOS

    @Column
    private String referer;   // where the user came from (optional header)

    @PrePersist
    public void prePersist() {
        this.clickedAt = LocalDateTime.now();
    }
}