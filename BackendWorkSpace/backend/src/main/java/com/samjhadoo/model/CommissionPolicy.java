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
 * Platform commission policy
 */
@Data
@Entity
@Table(name = "commission_policies")
public class CommissionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent; // Commission percentage (e.g., 15.00 for 15%)

    @ElementCollection
    @CollectionTable(name = "commission_tiers", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "tier_name")
    private Set<String> applicableTiers = new HashSet<>(); // BRONZE, SILVER, GOLD

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

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
}
