package com.samjhadoo.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Long id;
    private String userName;
    private String status;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private BigDecimal frozenBalance;
    private BigDecimal totalEarned;
    private BigDecimal totalSpent;
    private BigDecimal monthlyLimit;
    private BigDecimal monthlySpent;
    private String currency;
    private boolean verified;
    private int verificationLevel;
    private boolean kycCompleted;
    private LocalDateTime lastTransactionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal lockedBalance;
    private BigDecimal effectiveBalance;
    private boolean isOperational;
    private double utilizationPercentage;
    private boolean requiresKYCForTransaction;
    private int healthScore;
    private boolean isEligibleForAutoPayout;
}
