package com.samjhadoo.dto.community;

import com.samjhadoo.model.enums.CommunityTag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Community Discount
 */
@Data
@Builder
public class CommunityDiscountDTO {

    private Long id;

    @NotNull
    private CommunityTag communityTag;

    @NotNull
    @Positive
    private BigDecimal discountPercent;

    private boolean requiresVerification;
    private boolean active;
    private String description;

    @NotNull
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
