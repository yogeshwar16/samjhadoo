package com.samjhadoo.model.pricing;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit log for all pricing changes
 */
@Data
@Entity
@Table(name = "pricing_audit_logs")
public class PricingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String actor; // Admin username or system

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, OVERRIDE

    @Column(nullable = false, length = 100)
    private String entityType; // PricingTier, MentorPricing, PromoRule, etc.

    @Column(nullable = false)
    private Long entityId;

    @Lob
    @Column
    private String beforeState; // JSON snapshot

    @Lob
    @Column
    private String afterState; // JSON snapshot

    @Column(length = 500)
    private String reason; // Why this change was made

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
