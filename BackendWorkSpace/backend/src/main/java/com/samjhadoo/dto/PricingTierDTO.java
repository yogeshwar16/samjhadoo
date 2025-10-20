package com.samjhadoo.dto.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for Pricing Tier
 */
@Data
@Builder
public class PricingTierDTO {

    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal hourlyRate;

    @NotNull
    @Positive
    private BigDecimal minRate;

    @NotNull
    @Positive
    private BigDecimal maxRate;

    private boolean active;
    private String description;
}
