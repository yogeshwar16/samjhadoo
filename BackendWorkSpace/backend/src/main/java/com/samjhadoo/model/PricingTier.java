package com.samjhadoo.model.pricing;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents pricing tiers (Bronze, Silver, Gold) with hourly rates
 */
@Data
@Entity
@Table(name = "pricing_tiers")
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // BRONZE, SILVER, GOLD

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxRate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking
}
