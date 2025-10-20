package com.samjhadoo.model.pricing;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Promotional discounts and festival offers
 */
@Data
@Entity
@Table(name = "promo_rules")
public class PromoRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // DIWALI2025, STUDENT20

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromoType type; // PERCENTAGE, FIXED

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value; // 20 for 20%, or 100 for ₹100 off

    @ElementCollection
    @CollectionTable(name = "promo_regions", joinColumns = @JoinColumn(name = "promo_id"))
    @Column(name = "region_code")
    private Set<String> applicableRegions = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean stackable = false; // Can combine with other promos

    @Column
    private Integer usageLimit; // Max uses per user

    @Column(length = 1000)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum PromoType {
        PERCENTAGE,
        FIXED
    }
}
