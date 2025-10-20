package com.samjhadoo.service.pricing;

import com.samjhadoo.dto.pricing.PriceCalculationRequest;
import com.samjhadoo.dto.pricing.PriceCalculationResponse;
import com.samjhadoo.dto.pricing.PriceConfirmationRequest;
import com.samjhadoo.dto.pricing.PriceConfirmationResponse;

public interface PricingEngineService {

    /**
     * Calculate session price with full breakdown
     */
    PriceCalculationResponse calculatePrice(PriceCalculationRequest request);

    /**
     * Confirm and lock the price for booking
     */
    PriceConfirmationResponse confirmPrice(PriceConfirmationRequest request);

    /**
     * Get breakdown by token
     */
    PriceCalculationResponse getBreakdownByToken(String token);
}
