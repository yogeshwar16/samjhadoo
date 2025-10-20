package com.samjhadoo.controller.api.ads;

import com.samjhadoo.dto.ads.AdDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.ads.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advertisements", description = "Ad serving and interaction endpoints")
public class AdController {

    private final AdService adService;

    @GetMapping("/placement/{placementId}")
    @Operation(summary = "Get ad for placement",
               description = "Retrieves an advertisement to display at the specified placement")
    public ResponseEntity<AdDTO> getAdForPlacement(
            @PathVariable String placementId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser != null ? currentUser.getUser() : null;
            AdDTO ad = adService.getAdForPlacement(placementId, user);
            
            if (ad == null) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(ad);
        } catch (Exception e) {
            log.error("Error getting ad for placement {}: {}", placementId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/placement/{placementId}/multiple")
    @Operation(summary = "Get multiple ads for placement",
               description = "Retrieves multiple advertisements for rotation")
    public ResponseEntity<List<AdDTO>> getMultipleAds(
            @PathVariable String placementId,
            @RequestParam(defaultValue = "3") int count,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser != null ? currentUser.getUser() : null;
            List<AdDTO> ads = adService.getAdsForPlacement(placementId, count, user);
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            log.error("Error getting multiple ads: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{adId}/impression")
    @Operation(summary = "Record ad impression",
               description = "Records that an ad was displayed to the user")
    public ResponseEntity<Void> recordImpression(
            @PathVariable String adId,
            @RequestParam String placementId,
            @RequestBody(required = false) Map<String, Object> metadata,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser != null ? currentUser.getUser() : null;
            adService.recordImpression(adId, user, placementId, metadata != null ? metadata : new HashMap<>());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error recording impression for ad {}: {}", adId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{adId}/click")
    @Operation(summary = "Record ad click",
               description = "Records that a user clicked on an ad")
    public ResponseEntity<Map<String, String>> recordClick(
            @PathVariable String adId,
            @RequestParam String placementId,
            @RequestBody(required = false) Map<String, Object> metadata,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser != null ? currentUser.getUser() : null;
            adService.recordClick(adId, user, placementId, metadata != null ? metadata : new HashMap<>());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Click recorded");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recording click for ad {}: {}", adId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{adId}/action")
    @Operation(summary = "Record ad action",
               description = "Records that a user completed an action (e.g., watched rewarded ad)")
    public ResponseEntity<Map<String, Object>> recordAction(
            @PathVariable String adId,
            @RequestParam String actionType,
            @RequestParam(required = false) Integer rewardAmount,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            User user = currentUser.getUser();
            adService.recordAction(adId, user, actionType, rewardAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Action recorded");
            if (rewardAmount != null && rewardAmount > 0) {
                response.put("creditsAwarded", rewardAmount);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recording action for ad {}: {}", adId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/should-show")
    @Operation(summary = "Check if ads should be shown",
               description = "Checks if the user should see advertisements")
    public ResponseEntity<Map<String, Boolean>> shouldShowAds(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser != null ? currentUser.getUser() : null;
            boolean shouldShow = adService.shouldShowAds(user);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("shouldShowAds", shouldShow);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking ad eligibility: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/frequency-cap/{placementId}")
    @Operation(summary = "Check frequency cap",
               description = "Checks if user has reached the frequency cap for a placement")
    public ResponseEntity<Map<String, Boolean>> checkFrequencyCap(
            @PathVariable String placementId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            if (currentUser == null) {
                Map<String, Boolean> response = new HashMap<>();
                response.put("reachedCap", false);
                return ResponseEntity.ok(response);
            }
            
            User user = currentUser.getUser();
            boolean reachedCap = adService.hasReachedFrequencyCap(user, placementId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("reachedCap", reachedCap);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking frequency cap: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
