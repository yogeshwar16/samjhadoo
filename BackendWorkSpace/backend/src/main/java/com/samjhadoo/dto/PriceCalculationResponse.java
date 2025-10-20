package com.samjhadoo.dto.pricing;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response with detailed price breakdown
 */
@Data
@Builder
public class PriceCalculationResponse {

    private String breakdownToken; // Token to lock this price

    private BigDecimal mentorBaseRate;
    private Integer slotMinutes;
    private BigDecimal basePrice;

    private BigDecimal regionalMultiplier;
    private BigDecimal surgeMultiplier;
    private BigDecimal promoDiscount;
    private String promoCode;
    private BigDecimal communityDiscount;

    private BigDecimal subtotal;
    private BigDecimal platformCommission;
    private BigDecimal tax;
    private BigDecimal agenticAiFee;
    private BigDecimal creditsApplied;

    private BigDecimal finalPrice;
    private BigDecimal mentorPayout;

    private List<String> explanations; // Human-readable breakdown

    private String currency; // INR, USD, etc.
}
