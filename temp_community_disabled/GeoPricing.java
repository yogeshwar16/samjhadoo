package com.samjhadoo.model.community;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Country and city-tier based pricing
 */
@Data
@Entity
@Table(name = "geo_pricing")
public class GeoPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String countryCode; // IN, US, UK

    @Column(nullable = false, length = 100)
    private String countryName;

    @Column(length = 20)
    private String cityTier; // TIER_1, TIER_2, TIER_3 (India-specific)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePer10Min; // Base price for 10 minutes

    @Column(nullable = false, length = 3)
    private String currency; // INR, USD, GBP

    @Column(length = 10)
    private String currencySymbol; // ₹, $, £

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
