package com.samjhadoo.model.community;

import com.samjhadoo.model.enums.CommunityTag;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Discount rules for specific communities
 */
@Data
@Entity
@Table(name = "community_discounts")
public class CommunityDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CommunityTag communityTag;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent; // e.g., 30.00 for 30% off

    @Column(nullable = false)
    private boolean requiresVerification = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime effectiveFrom;

    @Column
    private LocalDateTime effectiveTo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
