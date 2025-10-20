package com.samjhadoo.service.ads;

import com.samjhadoo.dto.ads.AdDTO;
import com.samjhadoo.dto.ads.AdPlacementDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ads.AdPlacement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service for managing advertisements and placements.
 * Handles ad serving, tracking, and analytics.
 */
public interface AdService {
    
    /**
     * Gets an ad to display for a user at a specific placement
     * @param placementId The placement ID
     * @param user The user viewing the ad (can be null for anonymous)
     * @return Ad to display or null if no ad available
     */
    AdDTO getAdForPlacement(String placementId, User user);
    
    /**
     * Gets multiple ads for a placement (for rotation)
     * @param placementId The placement ID
     * @param count Number of ads to get
     * @param user The user
     * @return List of ads
     */
    List<AdDTO> getAdsForPlacement(String placementId, int count, User user);
    
    /**
     * Records an ad impression
     * @param adId The ad ID
     * @param user The user who saw the ad
     * @param placementId The placement where ad was shown
     * @param metadata Additional tracking metadata
     */
    void recordImpression(String adId, User user, String placementId, Map<String, Object> metadata);
    
    /**
     * Records an ad click
     * @param adId The ad ID
     * @param user The user who clicked
     * @param placementId The placement
     * @param metadata Additional tracking metadata
     */
    void recordClick(String adId, User user, String placementId, Map<String, Object> metadata);
    
    /**
     * Records an ad action (e.g., rewarded ad completion)
     * @param adId The ad ID
     * @param user The user who completed the action
     * @param actionType Type of action
     * @param rewardAmount Credits rewarded (if applicable)
     */
    void recordAction(String adId, User user, String actionType, Integer rewardAmount);
    
    /**
     * Checks if user should see ads (premium users might be ad-free)
     * @param user The user
     * @return true if ads should be shown
     */
    boolean shouldShowAds(User user);
    
    /**
     * Checks if user has reached frequency cap for a placement
     * @param user The user
     * @param placementId The placement
     * @return true if frequency cap reached
     */
    boolean hasReachedFrequencyCap(User user, String placementId);
    
    /**
     * Gets ad analytics for admin
     * @param adId The ad ID (null for all ads)
     * @param startDate Start date for analytics
     * @param endDate End date for analytics
     * @return Analytics data
     */
    Map<String, Object> getAdAnalytics(String adId, java.time.LocalDateTime startDate, 
                                       java.time.LocalDateTime endDate);
    
    /**
     * Gets placement performance metrics
     * @param placementId The placement ID
     * @return Performance metrics
     */
    Map<String, Object> getPlacementMetrics(String placementId);
    
    // Admin methods
    
    /**
     * Creates a new ad
     * @param adDTO Ad details
     * @return Created ad
     */
    AdDTO createAd(AdDTO adDTO);
    
    /**
     * Updates an existing ad
     * @param adId The ad ID
     * @param adDTO Updated ad details
     * @return Updated ad
     */
    AdDTO updateAd(String adId, AdDTO adDTO);
    
    /**
     * Deletes an ad
     * @param adId The ad ID
     */
    void deleteAd(String adId);
    
    /**
     * Gets all ads
     * @param pageable Pagination params
     * @return Page of ads
     */
    Page<AdDTO> getAllAds(Pageable pageable);
    
    /**
     * Creates a new placement
     * @param placementDTO Placement details
     * @return Created placement
     */
    AdPlacementDTO createPlacement(AdPlacementDTO placementDTO);
    
    /**
     * Updates a placement
     * @param placementId The placement ID
     * @param placementDTO Updated placement details
     * @return Updated placement
     */
    AdPlacementDTO updatePlacement(String placementId, AdPlacementDTO placementDTO);
    
    /**
     * Gets all placements
     * @param pageable Pagination params
     * @return Page of placements
     */
    Page<AdPlacementDTO> getAllPlacements(Pageable pageable);
}
