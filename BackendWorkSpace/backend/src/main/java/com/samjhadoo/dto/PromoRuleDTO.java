package com.samjhadoo.dto.pricing;

import com.samjhadoo.model.pricing.PromoRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for Promo Rule
 */
@Data
@Builder
public class PromoRuleDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private PromoRule.PromoType type;

    @NotNull
    @Positive
    private BigDecimal value;

    private Set<String> applicableRegions;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    private boolean active;
    private boolean stackable;
    private Integer usageLimit;
    private String description;
}
