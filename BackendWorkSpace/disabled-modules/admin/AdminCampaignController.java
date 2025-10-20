package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.ads.AdCampaignDTO;
import com.samjhadoo.service.ads.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Campaigns", description = "Admin endpoints for managing ad campaigns")
public class AdminCampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @Operation(summary = "Create campaign", description = "Creates a new ad campaign")
    public ResponseEntity<AdCampaignDTO> createCampaign(@Valid @RequestBody AdCampaignDTO campaignDTO) {
        try {
            AdCampaignDTO created = campaignService.createCampaign(campaignDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating campaign: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{campaignId}")
    @Operation(summary = "Update campaign", description = "Updates an existing ad campaign")
    public ResponseEntity<AdCampaignDTO> updateCampaign(
            @PathVariable String campaignId,
            @Valid @RequestBody AdCampaignDTO campaignDTO) {
        try {
            AdCampaignDTO updated = campaignService.updateCampaign(campaignId, campaignDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating campaign {}: {}", campaignId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{campaignId}")
    @Operation(summary = "Delete campaign", description = "Deletes (deactivates) an ad campaign")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String campaignId) {
        try {
            campaignService.deleteCampaign(campaignId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting campaign {}: {}", campaignId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{campaignId}")
    @Operation(summary = "Get campaign", description = "Retrieves a campaign by ID")
    public ResponseEntity<AdCampaignDTO> getCampaign(@PathVariable String campaignId) {
        try {
            AdCampaignDTO campaign = campaignService.getCampaign(campaignId);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            log.error("Error getting campaign {}: {}", campaignId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Retrieves all ad campaigns with pagination")
    public ResponseEntity<Page<AdCampaignDTO>> getAllCampaigns(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdCampaignDTO> campaigns = campaignService.getAllCampaigns(pageable);
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            log.error("Error getting campaigns: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get active campaigns", description = "Retrieves only active campaigns")
    public ResponseEntity<Page<AdCampaignDTO>> getActiveCampaigns(@PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdCampaignDTO> campaigns = campaignService.getActiveCampaigns(pageable);
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            log.error("Error getting active campaigns: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{campaignId}/pause")
    @Operation(summary = "Pause campaign", description = "Pauses an active campaign")
    public ResponseEntity<Void> pauseCampaign(@PathVariable String campaignId) {
        try {
            campaignService.pauseCampaign(campaignId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error pausing campaign {}: {}", campaignId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{campaignId}/resume")
    @Operation(summary = "Resume campaign", description = "Resumes a paused campaign")
    public ResponseEntity<Void> resumeCampaign(@PathVariable String campaignId) {
        try {
            campaignService.resumeCampaign(campaignId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error resuming campaign {}: {}", campaignId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{campaignId}/metrics")
    @Operation(summary = "Get campaign metrics", description = "Retrieves performance metrics for a campaign")
    public ResponseEntity<Map<String, Object>> getCampaignMetrics(@PathVariable String campaignId) {
        try {
            Map<String, Object> metrics = campaignService.getCampaignMetrics(campaignId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting campaign metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{campaignId}/budget")
    @Operation(summary = "Get budget status", description = "Retrieves budget status for a campaign")
    public ResponseEntity<Map<String, Object>> getBudgetStatus(@PathVariable String campaignId) {
        try {
            Map<String, Object> budget = campaignService.getBudgetStatus(campaignId);
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("Error getting budget status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
