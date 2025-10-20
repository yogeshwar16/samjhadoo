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
 * Represents a payout request from a mentor to withdraw earnings.
 */
@Entity
@Table(name = "payout_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest {

    public enum PayoutStatus {
        PENDING,        // Request submitted
        UNDER_REVIEW,   // Being reviewed by admin
        APPROVED,       // Approved for processing
        PROCESSING,     // Being processed by payment gateway
        COMPLETED,      // Successfully paid out
        FAILED,         // Payout failed
        CANCELLED,      // Cancelled by user/admin
        ON_HOLD         // On hold for verification
    }

    public enum PayoutMethod {
        BANK_TRANSFER,  // Direct bank transfer
        UPI,            // UPI payment
        PAYPAL,         // PayPal withdrawal
        CRYPTO,         // Cryptocurrency
        CHECK,          // Physical check
        GIFT_CARD       // Gift card/credits
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String payoutRequestId; // UUID for external reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutMethod method;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee;

    @Column(name = "net_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Lob
    @Column(name = "payment_details")
    private String paymentDetails; // Encrypted payment information

    @Column(name = "bank_account_number")
    private String bankAccountNumber; // Encrypted

    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "paypal_email")
    private String paypalEmail;

    @Column(name = "crypto_address")
    private String cryptoAddress;

    @Column(name = "crypto_currency")
    private String cryptoCurrency;

    @Lob
    @Column
    private String notes;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "external_payout_id")
    private String externalPayoutId; // ID from payment processor

    @Column(name = "estimated_processing_days", nullable = false)
    private int estimatedProcessingDays;

    @Column(name = "is_auto_payout", nullable = false)
    private boolean autoPayout;

    @Column(name = "auto_payout_threshold", precision = 12, scale = 2)
    private BigDecimal autoPayoutThreshold;

    @Column(name = "next_auto_payout_date")
    private LocalDateTime nextAutoPayoutDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Lob
    @Column(name = "review_notes")
    private String reviewNotes;

    @Column(name = "priority", nullable = false)
    private int priority; // Higher number = higher priority

    @Column(name = "is_urgent", nullable = false)
    private boolean urgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
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
            status = PayoutStatus.PENDING;
        }
        if (estimatedProcessingDays == 0) {
            estimatedProcessingDays = 3; // Default 3 days
        }
        if (priority == 0) {
            priority = 1; // Default priority
        }
        if (autoPayout) {
            autoPayout = false; // Default to manual
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (status == PayoutStatus.APPROVED && approvedAt == null) {
            approvedAt = LocalDateTime.now();
        }

        if (status == PayoutStatus.PROCESSING && processedAt == null) {
            processedAt = LocalDateTime.now();
        }

        if (status == PayoutStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }

        if (status == PayoutStatus.FAILED && failedAt == null) {
            failedAt = LocalDateTime.now();
        }
    }

    /**
     * Approves the payout request.
     * @param reviewer The admin reviewing
     * @param notes Review notes
     * @return true if approved successfully
     */
    public boolean approve(User reviewer, String notes) {
        if (status != PayoutStatus.PENDING && status != PayoutStatus.UNDER_REVIEW) {
            return false;
        }

        this.status = PayoutStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewNotes = notes;
        return true;
    }

    /**
     * Marks the payout as processing.
     * @return true if marked as processing
     */
    public boolean markProcessing() {
        if (status != PayoutStatus.APPROVED) {
            return false;
        }

        this.status = PayoutStatus.PROCESSING;
        return true;
    }

    /**
     * Marks the payout as completed.
     * @param externalId External payout ID from payment processor
     * @return true if completed successfully
     */
    public boolean markCompleted(String externalId) {
        if (status != PayoutStatus.PROCESSING) {
            return false;
        }

        this.status = PayoutStatus.COMPLETED;
        this.externalPayoutId = externalId;
        return true;
    }

    /**
     * Marks the payout as failed.
     * @param reason Reason for failure
     * @return true if marked as failed
     */
    public boolean markFailed(String reason) {
        if (status == PayoutStatus.COMPLETED) {
            return false; // Can't fail completed payouts
        }

        this.status = PayoutStatus.FAILED;
        this.failureReason = reason;
        return true;
    }

    /**
     * Cancels the payout request.
     * @return true if cancelled successfully
     */
    public boolean cancel() {
        if (status == PayoutStatus.COMPLETED || status == PayoutStatus.PROCESSING) {
            return false; // Can't cancel completed or processing payouts
        }

        this.status = PayoutStatus.CANCELLED;
        return true;
    }

    /**
     * Checks if the payout is eligible for auto-processing.
     * @return true if eligible for auto-payout
     */
    public boolean isEligibleForAutoPayout() {
        return autoPayout &&
               amount.compareTo(autoPayoutThreshold) >= 0 &&
               wallet.isEligibleForAutoPayout() &&
               status == PayoutStatus.PENDING;
    }

    /**
     * Checks if the payout is high-priority.
     * @return true if urgent or high amount
     */
    public boolean isHighPriority() {
        return urgent ||
               amount.compareTo(new BigDecimal("10000")) >= 0 || // ₹10,000+
               priority >= 5;
    }

    /**
     * Gets the payout age in hours.
     * @return Hours since payout was requested
     */
    public long getAgeInHours() {
        if (requestedAt != null) {
            return java.time.Duration.between(requestedAt, LocalDateTime.now()).toHours();
        }
        return 0;
    }

    /**
     * Checks if the payout is overdue for processing.
     * @return true if past estimated processing time
     */
    public boolean isOverdue() {
        if (status == PayoutStatus.PENDING || status == PayoutStatus.APPROVED) {
            LocalDateTime expectedCompletion = requestedAt.plusDays(estimatedProcessingDays);
            return expectedCompletion.isBefore(LocalDateTime.now());
        }
        return false;
    }

    /**
     * Gets the payout processing time in hours.
     * @return Processing time, or 0 if not completed
     */
    public long getProcessingTimeHours() {
        if (requestedAt != null && completedAt != null) {
            return java.time.Duration.between(requestedAt, completedAt).toHours();
        }
        return 0;
    }

    /**
     * Checks if the payout amount is above the suspicious threshold.
     * @return true if amount is suspiciously high
     */
    public boolean isAmountSuspicious() {
        // Flag payouts above ₹50,000 as potentially suspicious
        BigDecimal suspiciousThreshold = new BigDecimal("50000");
        return amount.compareTo(suspiciousThreshold) > 0;
    }

    /**
     * Gets the payout priority score for queue ordering.
     * @return Priority score (higher = higher priority)
     */
    public int getPriorityScore() {
        int score = priority;

        // High amount payouts get priority
        if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            score += 20;
        }

        // Urgent payouts get priority
        if (urgent) {
            score += 30;
        }

        // Older payouts get priority
        score += (int) getAgeInHours() / 24; // +1 per day old

        // High-value mentors get priority
        if (wallet.getTotalEarned().compareTo(new BigDecimal("100000")) >= 0) {
            score += 10;
        }

        return score;
    }

    /**
     * Checks if the payout requires additional verification.
     * @return true if needs extra verification
     */
    public boolean requiresAdditionalVerification() {
        return isAmountSuspicious() ||
               !wallet.isKycCompleted() ||
               wallet.getVerificationLevel() < 2 ||
               isHighPriority();
    }

    /**
     * Gets the payout method display name.
     * @return Human-readable payout method
     */
    public String getPayoutMethodDisplayName() {
        return switch (method) {
            case BANK_TRANSFER -> "Bank Transfer";
            case UPI -> "UPI";
            case PAYPAL -> "PayPal";
            case CRYPTO -> "Cryptocurrency";
            case CHECK -> "Check";
            case GIFT_CARD -> "Gift Card";
        };
    }

    /**
     * Checks if this is a cross-border payout.
     * @return true if international transfer
     */
    public boolean isCrossBorder() {
        // In a real implementation, this would check if bank details are international
        return "PAYPAL".equals(method.name()) ||
               "CRYPTO".equals(method.name());
    }

    /**
     * Gets the estimated payout completion date.
     * @return Estimated completion date
     */
    public LocalDateTime getEstimatedCompletionDate() {
        if (status == PayoutStatus.COMPLETED) {
            return completedAt;
        }

        LocalDateTime baseDate = switch (status) {
            case PENDING -> requestedAt;
            case UNDER_REVIEW -> requestedAt;
            case APPROVED -> approvedAt != null ? approvedAt : requestedAt;
            case PROCESSING -> processedAt != null ? processedAt : requestedAt;
            default -> requestedAt;
        };

        return baseDate.plusDays(estimatedProcessingDays);
    }

    /**
     * Checks if the payout is delayed beyond normal processing time.
     * @return true if delayed
     */
    public boolean isDelayed() {
        LocalDateTime expectedCompletion = getEstimatedCompletionDate();
        return expectedCompletion.isBefore(LocalDateTime.now()) &&
               status != PayoutStatus.COMPLETED &&
               status != PayoutStatus.FAILED;
    }
}
