package com.samjhadoo.model.community;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.CommunityTag;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Verification records for community membership
 */
@Data
@Entity
@Table(name = "community_verifications")
public class CommunityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityTag communityTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(length = 500)
    private String documentUrl; // ID card, Kisan ID, .edu email proof, etc.

    @Column(length = 1000)
    private String notes; // Admin notes

    @Column
    private LocalDateTime verifiedAt;

    @Column(length = 100)
    private String verifiedBy; // Admin username

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        EXPIRED
    }
}
