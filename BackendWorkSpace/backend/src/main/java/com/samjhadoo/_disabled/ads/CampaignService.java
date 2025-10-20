package com.samjhadoo.service.ads;

import com.samjhadoo.dto.ads.AdCampaignDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Service for managing ad campaigns.
 */
public interface CampaignService {
    
    /**
     * Creates a new campaign
     * @param campaignDTO Campaign details
     * @return Created campaign
     */
    AdCampaignDTO createCampaign(AdCampaignDTO campaignDTO);
    
    /**
     * Updates a campaign
     * @param campaignId Campaign ID
     * @param campaignDTO Updated details
     * @return Updated campaign
     */
    AdCampaignDTO updateCampaign(String campaignId, AdCampaignDTO campaignDTO);
    
    /**
     * Deletes a campaign
     * @param campaignId Campaign ID
     */
    void deleteCampaign(String campaignId);
    
    /**
     * Gets a campaign by ID
     * @param campaignId Campaign ID
     * @return Campaign details
     */
    AdCampaignDTO getCampaign(String campaignId);
    
    /**
     * Gets all campaigns
     * @param pageable Pagination params
     * @return Page of campaigns
     */
    Page<AdCampaignDTO> getAllCampaigns(Pageable pageable);
    
    /**
     * Gets active campaigns
     * @param pageable Pagination params
     * @return Page of active campaigns
     */
    Page<AdCampaignDTO> getActiveCampaigns(Pageable pageable);
    
    /**
     * Pauses a campaign
     * @param campaignId Campaign ID
     */
    void pauseCampaign(String campaignId);
    
    /**
     * Resumes a paused campaign
     * @param campaignId Campaign ID
     */
    void resumeCampaign(String campaignId);
    
    /**
     * Gets campaign performance metrics
     * @param campaignId Campaign ID
     * @return Performance metrics
     */
    Map<String, Object> getCampaignMetrics(String campaignId);
    
    /**
     * Gets campaign budget status
     * @param campaignId Campaign ID
     * @return Budget status
     */
    Map<String, Object> getBudgetStatus(String campaignId);
}
