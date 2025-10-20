package com.samjhadoo.model.wallet;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an escrow account for holding funds during transactions.
 */
@Entity
@Table(name = "escrows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {

    public enum EscrowStatus {
        ACTIVE,         // Funds held in escrow
        RELEASED,       // Funds released to recipient
        REFUNDED,       // Funds refunded to sender
        DISPUTED,       // Dispute raised
        EXPIRED         // Escrow period expired
    }

    public enum EscrowType {
        SESSION_PAYMENT,    // Payment for mentoring session
        SERVICE_PAYMENT,    // Payment for other services
        MARKETPLACE,        // Marketplace transaction
        FREELANCE           // Freelance work payment
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String escrowId; // UUID for external reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_wallet_id", nullable = false)
    private Wallet recipientWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowType type;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee;

    @Column(name = "net_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Lob
    @Column
    private String description;

    @Column(name = "reference_id")
    private String referenceId; // Session ID, order ID, etc.

    @Column(name = "release_conditions")
    private String releaseConditions; // JSON conditions for auto-release

    @Column(name = "auto_release_enabled", nullable = false)
    private boolean autoReleaseEnabled;

    @Column(name = "auto_release_date")
    private LocalDateTime autoReleaseDate;

    @Column(name = "dispute_deadline")
    private LocalDateTime disputeDeadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "dispute_reason")
    private String disputeReason;

    @Lob
    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }
        if (netAmount == null) {
            netAmount = amount.subtract(fee);
        }
        if (currency == null) {
            currency = "INR";
        }
        if (status == null) {
            status = EscrowStatus.ACTIVE;
        }
        if (autoReleaseEnabled) {
            autoReleaseEnabled = false; // Default to manual release
        }
        if (disputeDeadline == null) {
            disputeDeadline = LocalDateTime.now().plusDays(7); // Default 7 days
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == EscrowStatus.RELEASED && releasedAt == null) {
            releasedAt = LocalDateTime.now();
        }

        if (status == EscrowStatus.REFUNDED && refundedAt == null) {
            refundedAt = LocalDateTime.now();
        }

        if (status == EscrowStatus.EXPIRED && expiredAt == null) {
            expiredAt = LocalDateTime.now();
        }
    }

    /**
     * Releases funds to the recipient.
     * @param releasedBy The user/admin releasing the funds
     * @return true if released successfully
     */
    public boolean releaseFunds(User releasedBy) {
        if (status != EscrowStatus.ACTIVE) {
            return false;
        }

        this.status = EscrowStatus.RELEASED;
        this.resolvedBy = releasedBy;
        return true;
    }

    /**
     * Refunds funds to the sender.
     * @param reason Reason for refund
     * @param refundedBy The user/admin processing the refund
     * @return true if refunded successfully
     */
    public boolean refundFunds(String reason, User refundedBy) {
        if (status != EscrowStatus.ACTIVE && status != EscrowStatus.DISPUTED) {
            return false;
        }

        this.status = EscrowStatus.REFUNDED;
        this.resolutionNotes = "Refunded: " + reason;
        this.resolvedBy = refundedBy;
        return true;
    }

    /**
     * Marks the escrow as expired.
     * @return true if expired successfully
     */
    public boolean markExpired() {
        if (status == EscrowStatus.ACTIVE) {
            this.status = EscrowStatus.EXPIRED;
            return true;
        }
        return false;
    }

    /**
     * Raises a dispute on the escrow.
     * @param reason Reason for dispute
     * @return true if disputed successfully
     */
    public boolean raiseDispute(String reason) {
        if (status == EscrowStatus.ACTIVE) {
            this.status = EscrowStatus.DISPUTED;
            this.disputeReason = reason;
            return true;
        }
        return false;
    }

    /**
     * Checks if the escrow can be auto-released.
     * @return true if conditions met for auto-release
     */
    public boolean canAutoRelease() {
        if (!autoReleaseEnabled) {
            return false;
        }

        // Check if conditions are met
        // In a real implementation, this would evaluate the release conditions
        return autoReleaseDate != null && autoReleaseDate.isBefore(LocalDateTime.now());
    }

    /**
     * Gets the escrow duration in days.
     * @return Days since escrow was created
     */
    public long getDurationDays() {
        if (createdAt != null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        }
        return 0;
    }

    /**
     * Checks if the escrow is overdue for resolution.
     * @return true if past dispute deadline
     */
    public boolean isOverdue() {
        return disputeDeadline != null && disputeDeadline.isBefore(LocalDateTime.now());
    }

    /**
     * Gets the escrow value at risk.
     * @return Amount at risk in escrow
     */
    public BigDecimal getValueAtRisk() {
        return status == EscrowStatus.ACTIVE ? amount : BigDecimal.ZERO;
    }

    /**
     * Checks if this escrow is related to a session payment.
     * @return true if session-related
     */
    public boolean isSessionRelated() {
        return type == EscrowType.SESSION_PAYMENT;
    }

    /**
     * Checks if this escrow is high-value.
     * @return true if amount >= ₹5,000
     */
    public boolean isHighValue() {
        BigDecimal highValueThreshold = new BigDecimal("5000");
        return amount.compareTo(highValueThreshold) >= 0;
    }

    /**
     * Gets the escrow priority score for dispute resolution.
     * @return Priority score (higher = higher priority)
     */
    public int getPriorityScore() {
        int score = 0;

        // Higher amount = higher priority
        if (isHighValue()) {
            score += 30;
        }

        // Older escrows = higher priority
        score += (int) getDurationDays();

        // Disputed escrows = higher priority
        if (status == EscrowStatus.DISPUTED) {
            score += 50;
        }

        // Overdue escrows = highest priority
        if (isOverdue()) {
            score += 100;
        }

        return score;
    }
}
