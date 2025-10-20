package com.samjhadoo.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI usage analytics
 */
@Data
@Builder
public class AIAnalyticsDTO {

    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private long totalTokens;
    private BigDecimal totalCost;
    private double averageResponseTime;
    private double successRate;
}
