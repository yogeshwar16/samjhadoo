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
public class EscrowDTO {
    private Long id;
    private String escrowId;
    private String senderName;
    private String recipientName;
    private String status;
    private String type;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;
    private String description;
    private String referenceId;
    private String releaseConditions;
    private boolean autoReleaseEnabled;
    private LocalDateTime autoReleaseDate;
    private LocalDateTime disputeDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime expiredAt;
    private String disputeReason;
    private String resolutionNotes;
    private String resolvedByName;
    private boolean canAutoRelease;
    private long durationDays;
    private boolean isOverdue;
    private BigDecimal valueAtRisk;
    private boolean isSessionRelated;
    private boolean isHighValue;
    private int priorityScore;
}
