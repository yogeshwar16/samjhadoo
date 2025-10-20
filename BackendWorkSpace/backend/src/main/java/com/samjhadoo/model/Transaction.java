package com.samjhadoo.model.wallet;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.wallet.TransactionStatus;
import com.samjhadoo.model.enums.wallet.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction in the wallet system.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId; // UUID for external reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal fee; // Transaction fee

    @Column(name = "net_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal netAmount; // Amount after fees

    @Column(name = "currency", nullable = false)
    private String currency;

    @Lob
    @Column
    private String description;

    @Column(name = "reference_id")
    private String referenceId; // Reference to external system (payment gateway, etc.)

    @Column(name = "external_transaction_id")
    private String externalTransactionId; // ID from payment processor

    @Column(name = "payment_method")
    private String paymentMethod; // UPI, CARD, NET_BANKING, etc.

    @Column(name = "payment_gateway")
    private String paymentGateway; // RAZORPAY, STRIPE, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transaction_id")
    private Transaction relatedTransaction; // For refunds, transfers, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_wallet_id")
    private Wallet recipientWallet; // For transfers

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_id")
    private Escrow escrow; // For escrow transactions

    @Lob
    @Column(name = "metadata")
    private String metadata; // JSON metadata for additional info

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "geolocation")
    private String geolocation;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "is_suspicious", nullable = false)
    private boolean suspicious;

    @Column(name = "suspicion_reason")
    private String suspicionReason;

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
        if (initiatedAt == null) {
            initiatedAt = LocalDateTime.now();
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
            status = TransactionStatus.PENDING;
        }
        if (retryCount == 0) {
            retryCount = 0;
        }
        if (suspicious) {
            suspicious = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (status == TransactionStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }

        if (status == TransactionStatus.FAILED && failedAt == null) {
            failedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the transaction as completed.
     */
    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marks the transaction as failed.
     * @param reason Reason for failure
     */
    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Marks the transaction as cancelled.
     */
    public void markCancelled() {
        this.status = TransactionStatus.CANCELLED;
    }

    /**
     * Marks the transaction as disputed.
     */
    public void markDisputed() {
        this.status = TransactionStatus.DISPUTED;
    }

    /**
     * Marks the transaction as on hold.
     */
    public void markOnHold() {
        this.status = TransactionStatus.ON_HOLD;
    }

    /**
     * Increments retry count for failed transactions.
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * Marks the transaction as suspicious.
     * @param reason Reason for suspicion
     */
    public void markSuspicious(String reason) {
        this.suspicious = true;
        this.suspicionReason = reason;
    }

    /**
     * Checks if the transaction is a credit transaction.
     * @return true if transaction adds money to wallet
     */
    public boolean isCreditTransaction() {
        return type == TransactionType.TOP_UP ||
               type == TransactionType.REFUND ||
               type == TransactionType.REWARD ||
               type == TransactionType.CASHBACK ||
               type == TransactionType.BONUS ||
               type == TransactionType.INTEREST ||
               type == TransactionType.TRANSFER_RECEIVED ||
               type == TransactionType.ESCROW_RELEASE;
    }

    /**
     * Checks if the transaction is a debit transaction.
     * @return true if transaction removes money from wallet
     */
    public boolean isDebitTransaction() {
        return type == TransactionType.PAYMENT ||
               type == TransactionType.WITHDRAWAL ||
               type == TransactionType.FEE ||
               type == TransactionType.CHARGEBACK ||
               type == TransactionType.ADJUSTMENT ||
               type == TransactionType.TRANSFER_SENT ||
               type == TransactionType.ESCROW_HOLD;
    }

    /**
     * Checks if the transaction is completed successfully.
     * @return true if completed
     */
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    /**
     * Checks if the transaction is pending.
     * @return true if pending
     */
    public boolean isPending() {
        return status == TransactionStatus.PENDING || status == TransactionStatus.PROCESSING;
    }

    /**
     * Checks if the transaction failed.
     * @return true if failed
     */
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    /**
     * Gets the transaction age in hours.
     * @return Hours since transaction was created
     */
    public long getAgeInHours() {
        if (createdAt != null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
        }
        return 0;
    }

    /**
     * Checks if the transaction is eligible for retry.
     * @return true if can be retried
     */
    public boolean isEligibleForRetry() {
        return status == TransactionStatus.FAILED && retryCount < 3;
    }

    /**
     * Gets the transaction processing time in minutes.
     * @return Processing time, or 0 if not completed
     */
    public long getProcessingTimeMinutes() {
        if (initiatedAt != null && completedAt != null) {
            return java.time.Duration.between(initiatedAt, completedAt).toMinutes();
        }
        return 0;
    }

    /**
     * Checks if the transaction amount is above the suspicious threshold.
     * @return true if amount is suspiciously high
     */
    public boolean isAmountSuspicious() {
        // Flag transactions above ₹50,000 as potentially suspicious
        BigDecimal suspiciousThreshold = new BigDecimal("50000");
        return amount.compareTo(suspiciousThreshold) > 0;
    }

    /**
     * Gets the effective amount (amount minus fees).
     * @return Net amount after fees
     */
    public BigDecimal getEffectiveAmount() {
        return netAmount != null ? netAmount : amount.subtract(fee);
    }

    /**
     * Checks if this is a high-value transaction.
     * @return true if amount >= ₹10,000
     */
    public boolean isHighValueTransaction() {
        BigDecimal highValueThreshold = new BigDecimal("10000");
        return amount.compareTo(highValueThreshold) >= 0;
    }

    /**
     * Gets the transaction risk score based on various factors.
     * @return Risk score (0-100, higher = riskier)
     */
    public int getRiskScore() {
        int score = 0;

        // High amount increases risk
        if (isHighValueTransaction()) {
            score += 30;
        }

        // Suspicious amount increases risk
        if (isAmountSuspicious()) {
            score += 25;
        }

        // Failed transactions increase risk
        if (isFailed()) {
            score += 20;
        }

        // Many retries increase risk
        score += Math.min(15, retryCount * 3);

        // Suspicious flag increases risk
        if (suspicious) {
            score += 40;
        }

        return Math.min(100, score);
    }

    /**
     * Checks if the transaction should be flagged for review.
     * @return true if should be reviewed
     */
    public boolean shouldBeReviewed() {
        return getRiskScore() >= 50 || isHighValueTransaction() || suspicious;
    }

    /**
     * Creates a refund transaction for this transaction.
     * @param reason Reason for refund
     * @return New refund transaction
     */
    public Transaction createRefund(String reason) {
        return Transaction.builder()
                .wallet(this.wallet)
                .user(this.user)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.PENDING)
                .amount(this.amount)
                .fee(BigDecimal.ZERO) // No fee for refunds
                .netAmount(this.amount)
                .currency(this.currency)
                .description("Refund: " + reason)
                .referenceId("REFUND_OF_" + this.transactionId)
                .relatedTransaction(this)
                .build();
    }

    /**
     * Checks if the transaction is related to ads (rewards, payments).
     * @return true if ad-related
     */
    public boolean isAdRelated() {
        return type == TransactionType.REWARD ||
               type == TransactionType.PAYMENT && "ADS".equals(metadata);
    }

    /**
     * Checks if the transaction is related to sessions (payments).
     * @return true if session-related
     */
    public boolean isSessionRelated() {
        return type == TransactionType.PAYMENT && metadata != null && metadata.contains("SESSION");
    }

    /**
     * Gets the transaction category for analytics.
     * @return Category name
     */
    public String getCategory() {
        if (isCreditTransaction()) {
            return "CREDIT";
        } else if (isDebitTransaction()) {
            return "DEBIT";
        } else {
            return "TRANSFER";
        }
    }
}
