package com.samjhadoo.controller.api;

import com.samjhadoo.dto.community.*;
import com.samjhadoo.service.community.CommunityManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API for managing community discounts, geo pricing, and verifications
 */
@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommunityController {

    private final CommunityManagementService communityManagementService;

    // ============= Community Discounts =============

    @GetMapping("/discounts")
    public ResponseEntity<List<CommunityDiscountDTO>> getAllDiscounts() {
        return ResponseEntity.ok(communityManagementService.getAllDiscounts());
    }

    @PostMapping("/discounts")
    public ResponseEntity<CommunityDiscountDTO> createDiscount(@Valid @RequestBody CommunityDiscountDTO dto) {
        return ResponseEntity.ok(communityManagementService.createDiscount(dto));
    }

    @PutMapping("/discounts/{id}")
    public ResponseEntity<CommunityDiscountDTO> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody CommunityDiscountDTO dto) {
        return ResponseEntity.ok(communityManagementService.updateDiscount(id, dto));
    }

    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        communityManagementService.deleteDiscount(id);
        return ResponseEntity.ok().build();
    }

    // ============= Geo Pricing =============

    @GetMapping("/geo-pricing")
    public ResponseEntity<List<GeoPricingDTO>> getAllGeoPricing() {
        return ResponseEntity.ok(communityManagementService.getAllGeoPricing());
    }

    @PostMapping("/geo-pricing")
    public ResponseEntity<GeoPricingDTO> createGeoPricing(@Valid @RequestBody GeoPricingDTO dto) {
        return ResponseEntity.ok(communityManagementService.createGeoPricing(dto));
    }

    @PutMapping("/geo-pricing/{id}")
    public ResponseEntity<GeoPricingDTO> updateGeoPricing(
            @PathVariable Long id,
            @Valid @RequestBody GeoPricingDTO dto) {
        return ResponseEntity.ok(communityManagementService.updateGeoPricing(id, dto));
    }

    @DeleteMapping("/geo-pricing/{id}")
    public ResponseEntity<Void> deleteGeoPricing(@PathVariable Long id) {
        communityManagementService.deleteGeoPricing(id);
        return ResponseEntity.ok().build();
    }

    // ============= Verification Management =============

    @GetMapping("/verifications/pending")
    public ResponseEntity<Page<VerificationRequestDTO>> getPendingVerifications(Pageable pageable) {
        return ResponseEntity.ok(communityManagementService.getPendingVerifications(pageable));
    }

    @PostMapping("/verifications/{id}/approve")
    public ResponseEntity<Void> approveVerification(
            @PathVariable Long id,
            @RequestParam String adminUsername,
            @RequestParam(required = false) String notes) {
        communityManagementService.approveVerification(id, adminUsername, notes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verifications/{id}/reject")
    public ResponseEntity<Void> rejectVerification(
            @PathVariable Long id,
            @RequestParam String adminUsername,
            @RequestParam String reason) {
        communityManagementService.rejectVerification(id, adminUsername, reason);
        return ResponseEntity.ok().build();
    }
}
