package com.samjhadoo.dto.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to confirm and lock the price
 */
@Data
public class PriceConfirmationRequest {

    @NotBlank
    private String breakdownToken;

    @NotNull
    private Long userId;

    @NotNull
    private Long mentorId;

    private String bookingDetails; // Additional booking info
}
