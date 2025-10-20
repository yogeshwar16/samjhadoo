package com.samjhadoo.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueStats {
    private BigDecimal totalRevenue;
    private long transactionCount;
    private BigDecimal averageTransactionValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
