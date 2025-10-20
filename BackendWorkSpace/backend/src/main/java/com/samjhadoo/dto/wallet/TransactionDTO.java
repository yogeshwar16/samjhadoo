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
public class TransactionDTO {
    private Long id;
    private String transactionId;
    private String walletId;
    private String userName;
    private String type;
    private String status;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;
    private String description;
    private String referenceId;
    private String externalTransactionId;
    private String paymentMethod;
    private String paymentGateway;
    private String recipientWalletId;
    private String escrowId;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private String geolocation;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private int retryCount;
    private boolean suspicious;
    private String suspicionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isCreditTransaction;
    private boolean isDebitTransaction;
    private boolean isCompleted;
    private boolean isPending;
    private boolean isFailed;
    private long ageInHours;
    private boolean isEligibleForRetry;
    private long processingTimeMinutes;
    private boolean isAmountSuspicious;
    private BigDecimal effectiveAmount;
    private boolean isHighValueTransaction;
    private int riskScore;
    private boolean shouldBeReviewed;
    private String category;
    private boolean isAdRelated;
    private boolean isSessionRelated;
}
