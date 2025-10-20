package com.samjhadoo.controller.api;

import com.samjhadoo.dto.pricing.*;
import com.samjhadoo.service.pricing.AdminPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API for pricing management
 */
@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingController {

    private final AdminPricingService adminPricingService;

    // ============= Pricing Tiers =============

    @GetMapping("/tiers")
    public ResponseEntity<List<PricingTierDTO>> getAllTiers() {
        return ResponseEntity.ok(adminPricingService.getAllTiers());
    }

    @PostMapping("/tiers")
    public ResponseEntity<PricingTierDTO> createTier(@Valid @RequestBody PricingTierDTO dto) {
        return ResponseEntity.ok(adminPricingService.createTier(dto));
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<PricingTierDTO> updateTier(@PathVariable Long id, @Valid @RequestBody PricingTierDTO dto) {
        return ResponseEntity.ok(adminPricingService.updateTier(id, dto));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<Void> deleteTier(@PathVariable Long id) {
        adminPricingService.deleteTier(id);
        return ResponseEntity.ok().build();
    }

    // ============= Regional Multipliers =============

    @GetMapping("/regional-multipliers")
    public ResponseEntity<List<RegionalMultiplierDTO>> getAllMultipliers() {
        return ResponseEntity.ok(adminPricingService.getAllMultipliers());
    }

    @PostMapping("/regional-multipliers")
    public ResponseEntity<RegionalMultiplierDTO> createMultiplier(@Valid @RequestBody RegionalMultiplierDTO dto) {
        return ResponseEntity.ok(adminPricingService.createMultiplier(dto));
    }

    @PutMapping("/regional-multipliers/{id}")
    public ResponseEntity<RegionalMultiplierDTO> updateMultiplier(@PathVariable Long id, @Valid @RequestBody RegionalMultiplierDTO dto) {
        return ResponseEntity.ok(adminPricingService.updateMultiplier(id, dto));
    }

    @DeleteMapping("/regional-multipliers/{id}")
    public ResponseEntity<Void> deleteMultiplier(@PathVariable Long id) {
        adminPricingService.deleteMultiplier(id);
        return ResponseEntity.ok().build();
    }

    // ============= Promo Rules =============

    @GetMapping("/promos")
    public ResponseEntity<List<PromoRuleDTO>> getAllPromos() {
        return ResponseEntity.ok(adminPricingService.getAllPromos());
    }

    @PostMapping("/promos")
    public ResponseEntity<PromoRuleDTO> createPromo(@Valid @RequestBody PromoRuleDTO dto) {
        return ResponseEntity.ok(adminPricingService.createPromo(dto));
    }

    @PutMapping("/promos/{id}")
    public ResponseEntity<PromoRuleDTO> updatePromo(@PathVariable Long id, @Valid @RequestBody PromoRuleDTO dto) {
        return ResponseEntity.ok(adminPricingService.updatePromo(id, dto));
    }

    @DeleteMapping("/promos/{id}")
    public ResponseEntity<Void> deletePromo(@PathVariable Long id) {
        adminPricingService.deletePromo(id);
        return ResponseEntity.ok().build();
    }

    // ============= Mentor Override =============

    @PostMapping("/mentor/{mentorId}/override")
    public ResponseEntity<Void> overrideMentorRate(
            @PathVariable Long mentorId,
            @RequestParam java.math.BigDecimal rate,
            @RequestParam String reason) {
        adminPricingService.overrideMentorRate(mentorId, rate, reason);
        return ResponseEntity.ok().build();
    }

    // ============= Audit Logs =============

    @GetMapping("/audit")
    public ResponseEntity<Page<String>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(adminPricingService.getAuditLogs(pageable));
    }

    // ============= Simulation =============

    @PostMapping("/simulate")
    public ResponseEntity<PriceCalculationResponse> simulatePrice(@Valid @RequestBody PriceCalculationRequest request) {
        return ResponseEntity.ok(adminPricingService.simulatePrice(request));
    }
}
