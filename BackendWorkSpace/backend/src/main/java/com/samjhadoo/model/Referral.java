package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks user referrals and their status in the referral program.
 */
@Entity
@Table(name = "referrals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Referral {
    
    public enum ReferralStatus {
        PENDING,    // Referral created but not yet completed
        COMPLETED,  // Referee has completed required actions
        EXPIRED,    // Referral link expired
        REVOKED     // Referral was revoked by admin
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id")
    private User referee;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(name = "referee_email")
    private String refereeEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "reward_awarded", nullable = false)
    private boolean rewardAwarded;
    
    @Column(name = "reward_description")
    private String rewardDescription;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReferralStatus.PENDING;
        }
        if (expiresAt == null) {
            // Default to 30 days from creation
            expiresAt = LocalDateTime.now().plusDays(30);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // If status changed to COMPLETED, set completedAt
        if (status == ReferralStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Checks if the referral is currently active.
     * @return true if status is PENDING and not expired
     */
    public boolean isActive() {
        return status == ReferralStatus.PENDING && 
               (expiresAt == null || !expiresAt.isBefore(LocalDateTime.now()));
    }
    
    /**
     * Marks the referral as completed with the given referee.
     * @param referee The user who completed the referral
     * @return true if status was updated, false if already completed
     */
    public boolean completeReferral(User referee) {
        if (this.status != ReferralStatus.PENDING) {
            return false;
        }
        
        this.referee = referee;
        this.status = ReferralStatus.COMPLETED;
        return true;
    }
}
