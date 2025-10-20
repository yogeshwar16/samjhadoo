package com.samjhadoo.model.wallet;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.wallet.WalletStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's wallet for managing credits, payments, and transactions.
 */
@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    @Column(name = "balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "available_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal availableBalance; // Balance available for spending

    @Column(name = "pending_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal pendingBalance; // Balance in pending transactions

    @Column(name = "frozen_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal frozenBalance; // Balance frozen due to disputes

    @Column(name = "total_earned", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalEarned; // Lifetime earnings

    @Column(name = "total_spent", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalSpent; // Lifetime spending

    @Column(name = "monthly_limit", precision = 12, scale = 2)
    private BigDecimal monthlyLimit; // Monthly spending limit

    @Column(name = "monthly_spent", precision = 12, scale = 2, nullable = false)
    private BigDecimal monthlySpent; // Current month spending

    @Column(name = "currency", nullable = false)
    private String currency; // INR, USD, etc.

    @Column(name = "is_verified", nullable = false)
    private boolean verified; // Whether wallet is verified for payments

    @Column(name = "verification_level", nullable = false)
    private int verificationLevel; // 0-3 (0=unverified, 3=fully verified)

    @Column(name = "kyc_completed", nullable = false)
    private boolean kycCompleted; // Whether KYC is completed

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PayoutRequest> payoutRequests = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (availableBalance == null) {
            availableBalance = BigDecimal.ZERO;
        }
        if (pendingBalance == null) {
            pendingBalance = BigDecimal.ZERO;
        }
        if (frozenBalance == null) {
            frozenBalance = BigDecimal.ZERO;
        }
        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        if (monthlySpent == null) {
            monthlySpent = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "INR"; // Default currency
        }
        if (status == null) {
            status = WalletStatus.ACTIVE;
        }
        if (verificationLevel == 0) {
            verificationLevel = 0;
        }
        if (kycCompleted) {
            kycCompleted = false;
        }
        if (verified) {
            verified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastTransactionAt = LocalDateTime.now();
    }

    /**
     * Adds credits to the wallet.
     * @param amount Amount to add
     * @return New balance
     */
    public BigDecimal addCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.totalEarned = this.totalEarned.add(amount);
        this.lastTransactionAt = LocalDateTime.now();

        return this.balance;
    }

    /**
     * Deducts credits from the wallet.
     * @param amount Amount to deduct
     * @return New balance
     */
    public BigDecimal deductCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (this.availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.totalSpent = this.totalSpent.add(amount);

        // Check monthly limit
        if (monthlyLimit != null) {
            this.monthlySpent = this.monthlySpent.add(amount);
        }

        this.lastTransactionAt = LocalDateTime.now();

        return this.balance;
    }

    /**
     * Holds credits in pending state (for escrow).
     * @param amount Amount to hold
     * @return true if held successfully
     */
    public boolean holdCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (this.availableBalance.compareTo(amount) < 0) {
            return false;
        }

        this.availableBalance = this.availableBalance.subtract(amount);
        this.pendingBalance = this.pendingBalance.add(amount);

        return true;
    }

    /**
     * Releases held credits back to available balance.
     * @param amount Amount to release
     * @return true if released successfully
     */
    public boolean releaseCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (this.pendingBalance.compareTo(amount) < 0) {
            return false;
        }

        this.pendingBalance = this.pendingBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);

        return true;
    }

    /**
     * Freezes credits (for disputes or violations).
     * @param amount Amount to freeze
     * @return true if frozen successfully
     */
    public boolean freezeCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (this.availableBalance.compareTo(amount) < 0) {
            return false;
        }

        this.availableBalance = this.availableBalance.subtract(amount);
        this.frozenBalance = this.frozenBalance.add(amount);

        return true;
    }

    /**
     * Unfreezes credits back to available balance.
     * @param amount Amount to unfreeze
     * @return true if unfrozen successfully
     */
    public boolean unfreezeCredits(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (this.frozenBalance.compareTo(amount) < 0) {
            return false;
        }

        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);

        return true;
    }

    /**
     * Checks if wallet has sufficient balance for a transaction.
     * @param amount Required amount
     * @return true if sufficient balance available
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * Checks if wallet is within monthly spending limit.
     * @param additionalAmount Amount to be spent
     * @return true if within limit
     */
    public boolean isWithinMonthlyLimit(BigDecimal additionalAmount) {
        if (monthlyLimit == null) {
            return true; // No limit set
        }

        return monthlySpent.add(additionalAmount).compareTo(monthlyLimit) <= 0;
    }

    /**
     * Resets monthly spending (called at month start).
     */
    public void resetMonthlySpending() {
        this.monthlySpent = BigDecimal.ZERO;
    }

    /**
     * Gets the total locked balance (pending + frozen).
     * @return Total locked balance
     */
    public BigDecimal getLockedBalance() {
        return pendingBalance.add(frozenBalance);
    }

    /**
     * Gets the effective balance (available + pending).
     * @return Effective balance
     */
    public BigDecimal getEffectiveBalance() {
        return availableBalance.add(pendingBalance);
    }

    /**
     * Checks if wallet is active and operational.
     * @return true if wallet can be used for transactions
     */
    public boolean isOperational() {
        return status == WalletStatus.ACTIVE && verified;
    }

    /**
     * Gets wallet utilization percentage.
     * @return Utilization as percentage (0-100)
     */
    public double getUtilizationPercentage() {
        if (totalEarned.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return totalSpent.divide(totalEarned, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * Checks if wallet needs KYC verification for large transactions.
     * @param amount Transaction amount
     * @return true if KYC required
     */
    public boolean requiresKYCForTransaction(BigDecimal amount) {
        // Require KYC for transactions above ₹10,000
        BigDecimal kycThreshold = new BigDecimal("10000");
        return !kycCompleted && amount.compareTo(kycThreshold) > 0;
    }

    /**
     * Gets the wallet health score based on various metrics.
     * @return Health score (0-100)
     */
    public int getHealthScore() {
        int score = 100;

        // Reduce score for frozen funds
        if (frozenBalance.compareTo(BigDecimal.ZERO) > 0) {
            score -= 20;
        }

        // Reduce score for low verification level
        if (verificationLevel < 2) {
            score -= 15;
        }

        // Reduce score if not KYC completed
        if (!kycCompleted) {
            score -= 10;
        }

        // Reduce score for inactive wallet
        if (lastTransactionAt != null &&
            lastTransactionAt.isBefore(LocalDateTime.now().minusMonths(6))) {
            score -= 25;
        }

        return Math.max(0, score);
    }

    /**
     * Checks if wallet is eligible for auto-payout.
     * @return true if eligible
     */
    public boolean isEligibleForAutoPayout() {
        return status == WalletStatus.ACTIVE &&
               kycCompleted &&
               balance.compareTo(new BigDecimal("100")) >= 0 && // Minimum ₹100
               verificationLevel >= 2;
    }

    /**
     * Updates verification level.
     * @param newLevel New verification level (0-3)
     */
    public void updateVerificationLevel(int newLevel) {
        if (newLevel < 0 || newLevel > 3) {
            throw new IllegalArgumentException("Verification level must be between 0 and 3");
        }

        this.verificationLevel = newLevel;

        // Update verified status based on level
        this.verified = newLevel >= 2;
    }

    /**
     * Updates KYC completion status.
     * @param completed Whether KYC is completed
     */
    public void updateKYCStatus(boolean completed) {
        this.kycCompleted = completed;

        // Update verification level if KYC completed
        if (completed && verificationLevel < 3) {
            this.verificationLevel = 3;
            this.verified = true;
        }
    }

    /**
     * Suspends the wallet.
     * @param reason Reason for suspension
     */
    public void suspend(String reason) {
        this.status = WalletStatus.SUSPENDED;
        // In a real implementation, you'd log the suspension reason
    }

    /**
     * Freezes the wallet.
     * @param reason Reason for freezing
     */
    public void freeze(String reason) {
        this.status = WalletStatus.FROZEN;
        // In a real implementation, you'd log the freeze reason
    }

    /**
     * Reactivates a suspended wallet.
     */
    public void reactivate() {
        if (status == WalletStatus.SUSPENDED || status == WalletStatus.FROZEN) {
            this.status = WalletStatus.ACTIVE;
        }
    }

    /**
     * Closes the wallet permanently.
     */
    public void close() {
        this.status = WalletStatus.CLOSED;
    }
}
