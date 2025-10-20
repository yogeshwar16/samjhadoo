package com.samjhadoo.service.ads;

import com.samjhadoo.dto.ads.AdDTO;
import com.samjhadoo.dto.ads.AdPlacementDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ads.Ad;
import com.samjhadoo.model.ads.AdCampaign;
import com.samjhadoo.model.ads.AdLog;
import com.samjhadoo.model.ads.AdPlacement;
import com.samjhadoo.repository.ads.AdCampaignRepository;
import com.samjhadoo.repository.ads.AdLogRepository;
import com.samjhadoo.repository.ads.AdPlacementRepository;
import com.samjhadoo.repository.ads.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final AdPlacementRepository placementRepository;
    private final AdCampaignRepository campaignRepository;
    private final AdLogRepository adLogRepository;
    private final ModelMapper modelMapper;
    
    // Cache for frequency tracking (in production, use Redis)
    private final Map<String, Integer> userFrequencyCache = new HashMap<>();

    @Override
    @Cacheable(value = "ads", key = "#placementId + '_' + (#user != null ? #user.id : 'anon')")
    public AdDTO getAdForPlacement(String placementId, User user) {
        // Check if user should see ads
        if (user != null && !shouldShowAds(user)) {
            return null;
        }
        
        // Check frequency cap
        if (user != null && hasReachedFrequencyCap(user, placementId)) {
            log.debug("User {} has reached frequency cap for placement {}", user.getId(), placementId);
            return null;
        }
        
        // Get placement
        AdPlacement placement = placementRepository.findByPlacementIdAndActiveTrue(placementId)
                .orElse(null);
        
        if (placement == null || !isPlacementActive(placement)) {
            log.debug("Placement {} not found or inactive", placementId);
            return null;
        }
        
        // Get eligible ads for this placement
        List<Ad> eligibleAds = getEligibleAds(placement, user);
        
        if (eligibleAds.isEmpty()) {
            log.debug("No eligible ads for placement {}", placementId);
            return null;
        }
        
        // Select ad using weighted random selection
        Ad selectedAd = selectAdByWeight(eligibleAds);
        
        return convertToDTO(selectedAd);
    }

    @Override
    public List<AdDTO> getAdsForPlacement(String placementId, int count, User user) {
        AdPlacement placement = placementRepository.findByPlacementIdAndActiveTrue(placementId)
                .orElse(null);
        
        if (placement == null) {
            return Collections.emptyList();
        }
        
        List<Ad> eligibleAds = getEligibleAds(placement, user);
        
        // Return up to 'count' ads
        return eligibleAds.stream()
                .limit(count)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void recordImpression(String adId, User user, String placementId, Map<String, Object> metadata) {
        Ad ad = adRepository.findByAdIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
        
        // Update impression count
        ad.setDeliveredImpressions(ad.getDeliveredImpressions() + 1);
        adRepository.save(ad);
        
        // Update placement impression count
        placementRepository.findByPlacementIdAndActiveTrue(placementId).ifPresent(placement -> {
            placement.setCurrentImpressions(placement.getCurrentImpressions() + 1);
            placementRepository.save(placement);
        });
        
        // Log the impression
        AdLog adLog = AdLog.builder()
                .ad(ad)
                .userId(user != null ? user.getId() : null)
                .placementId(placementId)
                .eventType(AdLog.EventType.IMPRESSION)
                .timestamp(LocalDateTime.now())
                .build();
        
        adLogRepository.save(adLog);
        
        log.debug("Recorded impression for ad {} at placement {}", adId, placementId);
    }

    @Override
    public void recordClick(String adId, User user, String placementId, Map<String, Object> metadata) {
        Ad ad = adRepository.findByAdIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
        
        // Update click count
        ad.setDeliveredClicks(ad.getDeliveredClicks() + 1);
        adRepository.save(ad);
        
        // Update placement click count
        placementRepository.findByPlacementIdAndActiveTrue(placementId).ifPresent(placement -> {
            placement.setCurrentClicks(placement.getCurrentClicks() + 1);
            placementRepository.save(placement);
        });
        
        // Log the click
        AdLog adLog = AdLog.builder()
                .ad(ad)
                .userId(user != null ? user.getId() : null)
                .placementId(placementId)
                .eventType(AdLog.EventType.CLICK)
                .timestamp(LocalDateTime.now())
                .build();
        
        adLogRepository.save(adLog);
        
        log.info("Recorded click for ad {} by user {}", adId, user != null ? user.getId() : "anonymous");
    }

    @Override
    public void recordAction(String adId, User user, String actionType, Integer rewardAmount) {
        Ad ad = adRepository.findByAdIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
        
        // Update action count
        ad.setDeliveredActions(ad.getDeliveredActions() + 1);
        adRepository.save(ad);
        
        // Log the action
        AdLog adLog = AdLog.builder()
                .ad(ad)
                .userId(user != null ? user.getId() : null)
                .eventType(AdLog.EventType.ACTION)
                .timestamp(LocalDateTime.now())
                .build();
        
        adLogRepository.save(adLog);
        
        // Award credits if applicable
        if (rewardAmount != null && rewardAmount > 0 && user != null) {
            // In production: walletService.addCredits(user, rewardAmount, "Ad reward");
            log.info("Awarded {} credits to user {} for ad action", rewardAmount, user.getId());
        }
        
        log.info("Recorded action {} for ad {} by user {}", actionType, adId, 
                user != null ? user.getId() : "anonymous");
    }

    @Override
    public boolean shouldShowAds(User user) {
        // Premium users might be ad-free
        // Check user's subscription status
        // For now, simple implementation
        if (user == null) {
            return true;
        }
        
        // In production: return !subscriptionService.isPremium(user);
        return true; // Show ads to all users for now
    }

    @Override
    public boolean hasReachedFrequencyCap(User user, String placementId) {
        if (user == null) {
            return false;
        }
        
        String cacheKey = user.getId() + ":" + placementId;
        Integer count = userFrequencyCache.getOrDefault(cacheKey, 0);
        
        // Get placement frequency cap
        AdPlacement placement = placementRepository.findByPlacementIdAndActiveTrue(placementId)
                .orElse(null);
        
        if (placement == null) {
            return false;
        }
        
        return count >= placement.getFrequencyCap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdAnalytics(String adId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (adId != null) {
            // Analytics for specific ad
            Ad ad = adRepository.findByAdId(adId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
            
            long impressions = adLogRepository.countByAdAndEventTypeAndTimestampBetween(
                    ad, AdLog.EventType.IMPRESSION, startDate, endDate);
            long clicks = adLogRepository.countByAdAndEventTypeAndTimestampBetween(
                    ad, AdLog.EventType.CLICK, startDate, endDate);
            long actions = adLogRepository.countByAdAndEventTypeAndTimestampBetween(
                    ad, AdLog.EventType.ACTION, startDate, endDate);
            
            analytics.put("adId", adId);
            analytics.put("impressions", impressions);
            analytics.put("clicks", clicks);
            analytics.put("actions", actions);
            analytics.put("ctr", impressions > 0 ? (double) clicks / impressions * 100 : 0);
            analytics.put("conversionRate", clicks > 0 ? (double) actions / clicks * 100 : 0);
        } else {
            // Overall analytics
            long totalImpressions = adLogRepository.countByEventTypeAndTimestampBetween(
                    AdLog.EventType.IMPRESSION, startDate, endDate);
            long totalClicks = adLogRepository.countByEventTypeAndTimestampBetween(
                    AdLog.EventType.CLICK, startDate, endDate);
            long totalActions = adLogRepository.countByEventTypeAndTimestampBetween(
                    AdLog.EventType.ACTION, startDate, endDate);
            
            analytics.put("totalImpressions", totalImpressions);
            analytics.put("totalClicks", totalClicks);
            analytics.put("totalActions", totalActions);
            analytics.put("overallCtr", totalImpressions > 0 ? (double) totalClicks / totalImpressions * 100 : 0);
        }
        
        analytics.put("startDate", startDate.toString());
        analytics.put("endDate", endDate.toString());
        
        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPlacementMetrics(String placementId) {
        AdPlacement placement = placementRepository.findByPlacementId(placementId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found"));
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("placementId", placementId);
        metrics.put("name", placement.getName());
        metrics.put("totalImpressions", placement.getCurrentImpressions());
        metrics.put("totalClicks", placement.getCurrentClicks());
        metrics.put("ctr", placement.getCurrentImpressions() > 0 ? 
                (double) placement.getCurrentClicks() / placement.getCurrentImpressions() * 100 : 0);
        metrics.put("fillRate", 100.0); // Simplified
        
        return metrics;
    }

    @Override
    public AdDTO createAd(AdDTO adDTO) {
        Ad ad = modelMapper.map(adDTO, Ad.class);
        ad.setAdId(UUID.randomUUID().toString());
        ad.setActive(true);
        ad.setDeliveredImpressions(0);
        ad.setDeliveredClicks(0);
        ad.setDeliveredActions(0);
        
        ad = adRepository.save(ad);
        log.info("Created ad: {}", ad.getAdId());
        
        return convertToDTO(ad);
    }

    @Override
    public AdDTO updateAd(String adId, AdDTO adDTO) {
        Ad ad = adRepository.findByAdId(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
        
        // Update fields
        modelMapper.map(adDTO, ad);
        ad = adRepository.save(ad);
        
        log.info("Updated ad: {}", adId);
        return convertToDTO(ad);
    }

    @Override
    public void deleteAd(String adId) {
        Ad ad = adRepository.findByAdId(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad not found"));
        
        ad.setActive(false);
        adRepository.save(ad);
        
        log.info("Deleted ad: {}", adId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAllAds(Pageable pageable) {
        return adRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public AdPlacementDTO createPlacement(AdPlacementDTO placementDTO) {
        AdPlacement placement = modelMapper.map(placementDTO, AdPlacement.class);
        placement.setPlacementId(UUID.randomUUID().toString());
        placement.setActive(true);
        placement.setCurrentImpressions(0);
        placement.setCurrentClicks(0);
        
        placement = placementRepository.save(placement);
        log.info("Created placement: {}", placement.getPlacementId());
        
        return modelMapper.map(placement, AdPlacementDTO.class);
    }

    @Override
    public AdPlacementDTO updatePlacement(String placementId, AdPlacementDTO placementDTO) {
        AdPlacement placement = placementRepository.findByPlacementId(placementId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found"));
        
        modelMapper.map(placementDTO, placement);
        placement = placementRepository.save(placement);
        
        log.info("Updated placement: {}", placementId);
        return modelMapper.map(placement, AdPlacementDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdPlacementDTO> getAllPlacements(Pageable pageable) {
        return placementRepository.findAll(pageable)
                .map(p -> modelMapper.map(p, AdPlacementDTO.class));
    }
    
    // Helper methods
    
    private boolean isPlacementActive(AdPlacement placement) {
        LocalDateTime now = LocalDateTime.now();
        return placement.isActive() && 
               now.isAfter(placement.getStartDate()) &&
               (placement.getEndDate() == null || now.isBefore(placement.getEndDate()));
    }
    
    private List<Ad> getEligibleAds(AdPlacement placement, User user) {
        // Get ads from active campaigns for this placement
        List<Ad> ads = adRepository.findByActiveTrue();
        
        // Filter based on targeting, budget, limits, etc.
        return ads.stream()
                .filter(ad -> ad.isActive())
                .filter(ad -> isAdEligible(ad, user))
                .filter(ad -> !hasReachedLimits(ad))
                .collect(Collectors.toList());
    }
    
    private boolean isAdEligible(Ad ad, User user) {
        // Check if ad is eligible for this user
        // Consider targeting criteria, user segments, etc.
        return true; // Simplified
    }
    
    private boolean hasReachedLimits(Ad ad) {
        if (ad.getMaxImpressions() != null && ad.getMaxImpressions() > 0) {
            if (ad.getDeliveredImpressions() >= ad.getMaxImpressions()) {
                return true;
            }
        }
        
        if (ad.getMaxClicks() != null && ad.getMaxClicks() > 0) {
            if (ad.getDeliveredClicks() >= ad.getMaxClicks()) {
                return true;
            }
        }
        
        return false;
    }
    
    private Ad selectAdByWeight(List<Ad> ads) {
        // Weighted random selection based on ad weight and priority
        double totalWeight = ads.stream().mapToDouble(Ad::getWeight).sum();
        double random = Math.random() * totalWeight;
        
        double cumulative = 0;
        for (Ad ad : ads) {
            cumulative += ad.getWeight();
            if (random <= cumulative) {
                return ad;
            }
        }
        
        // Fallback to first ad
        return ads.get(0);
    }
    
    private AdDTO convertToDTO(Ad ad) {
        return modelMapper.map(ad, AdDTO.class);
    }
}
