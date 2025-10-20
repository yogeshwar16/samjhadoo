package com.samjhadoo.dto.pricing;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response after price confirmation
 */
@Data
@Builder
public class PriceConfirmationResponse {

    private Long breakdownId;
    private String sessionId;
    private BigDecimal finalPrice;
    private BigDecimal mentorPayout;
    private boolean locked;
    private String message;
}
