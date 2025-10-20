package com.samjhadoo.model.pricing;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable price breakdown snapshot for a session
 */
@Data
@Entity
@Table(name = "session_price_breakdowns")
public class SessionPriceBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String breakdownToken; // Unique token for price lock

    @Column(nullable = false)
    private String sessionId; // Link to session

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mentorBaseRate;

    @Column(nullable = false)
    private Integer slotMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice; // rate * minutes / 60

    @Column(precision = 10, scale = 2)
    private BigDecimal regionalMultiplier;

    @Column(precision = 10, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(precision = 10, scale = 2)
    private BigDecimal promoDiscount;

    @Column(length = 50)
    private String promoCode;

    @Column(precision = 10, scale = 2)
    private BigDecimal communityDiscount;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal platformCommission;

    @Column(precision = 10, scale = 2)
    private BigDecimal tax; // GST

    @Column(precision = 10, scale = 2)
    private BigDecimal agenticAiFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal creditsApplied;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mentorPayout; // finalPrice - commission

    @ElementCollection
    @CollectionTable(name = "price_breakdown_explanations", joinColumns = @JoinColumn(name = "breakdown_id"))
    @Column(name = "explanation")
    private List<String> explanations = new ArrayList<>();

    @Column(nullable = false)
    private boolean locked = false; // Once confirmed, cannot change

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
