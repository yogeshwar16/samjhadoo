package com.samjhadoo.dto.pricing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request to calculate session price
 */
@Data
public class PriceCalculationRequest {

    private Long userId; // For community discount

    @NotNull
    private Long mentorId;

    @NotNull
    @Min(10)
    private Integer slotMinutes; // 10, 15, 30, 60

    private String regionCode; // TIER_1, TIER_2, TIER_3, or country code

    private Long skillId; // For surge pricing

    private String userPlan; // FREE, PREMIUM

    @NotNull
    private LocalDateTime sessionDate;

    private Boolean useAgenticAI = false;

    private BigDecimal creditsToApply;

    private String promoCode;
}
