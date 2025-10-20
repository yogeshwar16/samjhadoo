package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.ads.AdDTO;
import com.samjhadoo.dto.ads.AdPlacementDTO;
import com.samjhadoo.service.ads.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Advertisements", description = "Admin endpoints for managing ads")
public class AdminAdController {

    private final AdService adService;

    // Ad Management

    @PostMapping
    @Operation(summary = "Create ad", description = "Creates a new advertisement")
    public ResponseEntity<AdDTO> createAd(@Valid @RequestBody AdDTO adDTO) {
        try {
            AdDTO created = adService.createAd(adDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating ad: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{adId}")
    @Operation(summary = "Update ad", description = "Updates an existing advertisement")
    public ResponseEntity<AdDTO> updateAd(
            @PathVariable String adId,
            @Valid @RequestBody AdDTO adDTO) {
        try {
            AdDTO updated = adService.updateAd(adId, adDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating ad {}: {}", adId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{adId}")
    @Operation(summary = "Delete ad", description = "Deletes (deactivates) an advertisement")
    public ResponseEntity<Void> deleteAd(@PathVariable String adId) {
        try {
            adService.deleteAd(adId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting ad {}: {}", adId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all ads", description = "Retrieves all advertisements with pagination")
    public ResponseEntity<Page<AdDTO>> getAllAds(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdDTO> ads = adService.getAllAds(pageable);
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            log.error("Error getting ads: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Placement Management

    @PostMapping("/placements")
    @Operation(summary = "Create placement", description = "Creates a new ad placement")
    public ResponseEntity<AdPlacementDTO> createPlacement(@Valid @RequestBody AdPlacementDTO placementDTO) {
        try {
            AdPlacementDTO created = adService.createPlacement(placementDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating placement: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/placements/{placementId}")
    @Operation(summary = "Update placement", description = "Updates an existing ad placement")
    public ResponseEntity<AdPlacementDTO> updatePlacement(
            @PathVariable String placementId,
            @Valid @RequestBody AdPlacementDTO placementDTO) {
        try {
            AdPlacementDTO updated = adService.updatePlacement(placementId, placementDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating placement {}: {}", placementId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/placements")
    @Operation(summary = "Get all placements", description = "Retrieves all ad placements with pagination")
    public ResponseEntity<Page<AdPlacementDTO>> getAllPlacements(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdPlacementDTO> placements = adService.getAllPlacements(pageable);
            return ResponseEntity.ok(placements);
        } catch (Exception e) {
            log.error("Error getting placements: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Analytics

    @GetMapping("/analytics")
    @Operation(summary = "Get ad analytics", description = "Retrieves analytics for ads")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(required = false) String adId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Map<String, Object> analytics = adService.getAdAnalytics(adId, startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/placements/{placementId}/metrics")
    @Operation(summary = "Get placement metrics", description = "Retrieves performance metrics for a placement")
    public ResponseEntity<Map<String, Object>> getPlacementMetrics(@PathVariable String placementId) {
        try {
            Map<String, Object> metrics = adService.getPlacementMetrics(placementId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting placement metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
