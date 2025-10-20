package com.samjhadoo.service.ads;

import com.samjhadoo.dto.ads.AdCampaignDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.ads.Ad;
import com.samjhadoo.model.ads.AdCampaign;
import com.samjhadoo.model.ads.AdLog;
import com.samjhadoo.repository.ads.AdCampaignRepository;
import com.samjhadoo.repository.ads.AdLogRepository;
import com.samjhadoo.repository.ads.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final AdCampaignRepository campaignRepository;
    private final AdRepository adRepository;
    private final AdLogRepository adLogRepository;
    private final ModelMapper modelMapper;

    @Override
    public AdCampaignDTO createCampaign(AdCampaignDTO campaignDTO) {
        AdCampaign campaign = modelMapper.map(campaignDTO, AdCampaign.class);
        campaign.setCampaignId(UUID.randomUUID().toString());
        campaign.setActive(true);
        campaign.setSpentBudget(BigDecimal.ZERO);
        campaign.setDeliveredImpressions(0L);
        campaign.setDeliveredClicks(0L);
        
        campaign = campaignRepository.save(campaign);
        log.info("Created campaign: {}", campaign.getCampaignId());
        
        return modelMapper.map(campaign, AdCampaignDTO.class);
    }

    @Override
    public AdCampaignDTO updateCampaign(String campaignId, AdCampaignDTO campaignDTO) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        // Update fields
        modelMapper.map(campaignDTO, campaign);
        campaign = campaignRepository.save(campaign);
        
        log.info("Updated campaign: {}", campaignId);
        return modelMapper.map(campaign, AdCampaignDTO.class);
    }

    @Override
    public void deleteCampaign(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        campaign.setActive(false);
        campaignRepository.save(campaign);
        
        // Also deactivate all ads in this campaign
        List<Ad> ads = adRepository.findByCampaign(campaign);
        ads.forEach(ad -> ad.setActive(false));
        adRepository.saveAll(ads);
        
        log.info("Deleted campaign: {}", campaignId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdCampaignDTO getCampaign(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        return modelMapper.map(campaign, AdCampaignDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdCampaignDTO> getAllCampaigns(Pageable pageable) {
        return campaignRepository.findAll(pageable)
                .map(c -> modelMapper.map(c, AdCampaignDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdCampaignDTO> getActiveCampaigns(Pageable pageable) {
        return campaignRepository.findByActiveTrue(pageable)
                .map(c -> modelMapper.map(c, AdCampaignDTO.class));
    }

    @Override
    public void pauseCampaign(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        campaign.setActive(false);
        campaignRepository.save(campaign);
        
        log.info("Paused campaign: {}", campaignId);
    }

    @Override
    public void resumeCampaign(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        campaign.setActive(true);
        campaignRepository.save(campaign);
        
        log.info("Resumed campaign: {}", campaignId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCampaignMetrics(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("campaignId", campaignId);
        metrics.put("name", campaign.getName());
        metrics.put("status", campaign.isActive() ? "ACTIVE" : "PAUSED");
        metrics.put("totalImpressions", campaign.getDeliveredImpressions());
        metrics.put("totalClicks", campaign.getDeliveredClicks());
        
        // Calculate CTR
        double ctr = campaign.getDeliveredImpressions() > 0 ? 
                (double) campaign.getDeliveredClicks() / campaign.getDeliveredImpressions() * 100 : 0;
        metrics.put("ctr", ctr);
        
        // Get total actions
        List<Ad> ads = adRepository.findByCampaign(campaign);
        long totalActions = ads.stream().mapToLong(Ad::getDeliveredActions).sum();
        metrics.put("totalActions", totalActions);
        
        // Conversion rate
        double conversionRate = campaign.getDeliveredClicks() > 0 ?
                (double) totalActions / campaign.getDeliveredClicks() * 100 : 0;
        metrics.put("conversionRate", conversionRate);
        
        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBudgetStatus(String campaignId) {
        AdCampaign campaign = campaignRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        
        Map<String, Object> budget = new HashMap<>();
        budget.put("campaignId", campaignId);
        budget.put("totalBudget", campaign.getTotalBudget());
        budget.put("spentBudget", campaign.getSpentBudget());
        
        BigDecimal remaining = campaign.getTotalBudget().subtract(campaign.getSpentBudget());
        budget.put("remainingBudget", remaining);
        
        double percentageSpent = campaign.getTotalBudget().compareTo(BigDecimal.ZERO) > 0 ?
                campaign.getSpentBudget().divide(campaign.getTotalBudget(), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue() : 0;
        budget.put("percentageSpent", percentageSpent);
        
        budget.put("isOverBudget", campaign.getSpentBudget().compareTo(campaign.getTotalBudget()) > 0);
        
        return budget;
    }
}
