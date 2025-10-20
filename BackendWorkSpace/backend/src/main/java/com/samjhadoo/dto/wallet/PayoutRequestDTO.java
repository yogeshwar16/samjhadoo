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
public class PayoutRequestDTO {
    private Long id;
    private String payoutRequestId;
    private String walletId;
    private String userName;
    private String status;
    private String method;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;
    private String paymentDetails;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String upiId;
    private String paypalEmail;
    private String cryptoAddress;
    private String cryptoCurrency;
    private String notes;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private String externalPayoutId;
    private int estimatedProcessingDays;
    private boolean autoPayout;
    private BigDecimal autoPayoutThreshold;
    private LocalDateTime nextAutoPayoutDate;
    private String reviewedByName;
    private String reviewNotes;
    private int priority;
    private boolean urgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEligibleForAutoPayout;
    private boolean isHighPriority;
    private long ageInHours;
    private boolean isOverdue;
    private long processingTimeHours;
    private boolean isAmountSuspicious;
    private int priorityScore;
    private boolean requiresAdditionalVerification;
    private boolean isCrossBorder;
    private String payoutMethodDisplayName;
    private LocalDateTime estimatedCompletionDate;
    private boolean isDelayed;
}
