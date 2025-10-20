package com.samjhadoo.dto.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Regional Multiplier
 */
@Data
@Builder
public class RegionalMultiplierDTO {

    private Long id;

    @NotBlank
    private String regionCode;

    @NotBlank
    private String regionName;

    @NotNull
    @Positive
    private BigDecimal multiplier;

    @NotNull
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
    private boolean active;
}
