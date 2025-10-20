package com.samjhadoo.dto.gamification;

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
public class PointsAccountDTO {
    private Long id;
    private BigDecimal balance;
    private BigDecimal lifetimeEarned;
    private BigDecimal lifetimeSpent;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
