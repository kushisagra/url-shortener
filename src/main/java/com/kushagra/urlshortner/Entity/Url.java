package com.kushagra.urlshortner.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode", unique = true)
})
@Data               // Lombok: generates getters, setters, toString, equals
@Builder            // Lombok: lets you use Url.builder().field(val).build()
@NoArgsConstructor  // Lombok: generates empty constructor (JPA needs this)
@AllArgsConstructor // Lombok: generates constructor with all fields
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment ID
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt; // null means never expires

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist // runs automatically before INSERT
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.clickCount == null) this.clickCount = 0L;
        if (this.active == null) this.active = true;
    }
}