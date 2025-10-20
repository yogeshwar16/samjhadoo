package com.samjhadoo.model.pricing;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Custom pricing set by mentors or enforced by admin
 */
@Data
@Entity
@Table(name = "mentor_pricing")
public class MentorPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate; // Per hour

    @Column(nullable = false)
    private boolean enforcedByAdmin = false;

    @Column(length = 500)
    private String adminReason; // Why admin enforced this rate

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
