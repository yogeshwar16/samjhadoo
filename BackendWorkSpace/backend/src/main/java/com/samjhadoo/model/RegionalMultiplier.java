package com.samjhadoo.model.pricing;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Regional pricing multipliers for city-tier pricing
 */
@Data
@Entity
@Table(name = "regional_multipliers")
public class RegionalMultiplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String regionCode; // TIER_1, TIER_2, TIER_3, or country code

    @Column(nullable = false, length = 100)
    private String regionName; // Bengaluru, Mumbai, Gurgaon

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier; // 1.2 for Tier 1, 1.0 for Tier 2, 0.8 for Tier 3

    @Column(nullable = false)
    private LocalDateTime effectiveFrom;

    @Column
    private LocalDateTime effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
