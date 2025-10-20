package com.samjhadoo.dto.gamification;

import com.samjhadoo.model.enums.gamification.PointsReason;
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
public class PointsTransactionDTO {
    private Long id;
    private BigDecimal delta;
    private PointsReason reason;
    private String referenceId;
    private String description;
    private LocalDateTime transactionDate;
    private LocalDateTime expiresAt;
    private boolean reversed;
    private String reversalReason;
    private LocalDateTime reversedAt;
}
