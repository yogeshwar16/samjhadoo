package com.samjhadoo.controller.api;

import com.samjhadoo.dto.pricing.*;
import com.samjhadoo.service.pricing.PricingEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Public pricing API for price calculation and confirmation
 */
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingEngineService pricingEngineService;

    /**
     * Calculate session price with full breakdown
     */
    @PostMapping("/calc")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(@Valid @RequestBody PriceCalculationRequest request) {
        PriceCalculationResponse response = pricingEngineService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm and lock price for booking
     */
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriceConfirmationResponse> confirmPrice(@Valid @RequestBody PriceConfirmationRequest request) {
        PriceConfirmationResponse response = pricingEngineService.confirmPrice(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get price breakdown by token
     */
    @GetMapping("/breakdown/{token}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriceCalculationResponse> getBreakdown(@PathVariable String token) {
        PriceCalculationResponse response = pricingEngineService.getBreakdownByToken(token);
        return ResponseEntity.ok(response);
    }
}
