package com.samjhadoo.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for Geo-based Pricing
 */
@Data
@Builder
public class GeoPricingDTO {

    private Long id;

    @NotBlank
    private String countryCode;

    @NotBlank
    private String countryName;

    private String cityTier; // TIER_1, TIER_2, TIER_3

    @NotNull
    @Positive
    private BigDecimal basePricePer10Min;

    @NotBlank
    private String currency;

    private String currencySymbol;
    private boolean active;
}
