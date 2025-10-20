package com.samjhadoo.repository.ads;

import com.samjhadoo.model.ads.Ad;
import com.samjhadoo.model.ads.AdCampaign;
import com.samjhadoo.model.enums.ads.AdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    List<Ad> findByCampaign(AdCampaign campaign);

    List<Ad> findByAdType(AdType adType);

    List<Ad> findByActiveTrue();

    @Query("SELECT a FROM Ad a WHERE a.active = true ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findActiveOrderByPriority();

    @Query("SELECT a FROM Ad a WHERE a.active = true AND a.campaign.status = 'ACTIVE' ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findActiveInActiveCampaigns();

    @Query("SELECT a FROM Ad a WHERE a.active = true AND a.campaign = :campaign ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findActiveByCampaign(@Param("campaign") AdCampaign campaign);

    @Query("SELECT a FROM Ad a WHERE a.active = true AND a.adType = :type ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findActiveByType(@Param("type") AdType type);

    @Query("SELECT a FROM Ad a WHERE a.deliveredImpressions >= :minImpressions ORDER BY a.deliveredImpressions DESC")
    List<Ad> findByMinImpressions(@Param("minImpressions") long minImpressions);

    @Query("SELECT a FROM Ad a WHERE a.deliveredClicks >= :minClicks ORDER BY a.deliveredClicks DESC")
    List<Ad> findByMinClicks(@Param("minClicks") long minClicks);

    // TODO: clickThroughRate is a computed method, not a field - needs custom implementation
    // @Query("SELECT a FROM Ad a WHERE a.clickThroughRate >= :minCtr ORDER BY a.clickThroughRate DESC")
    // List<Ad> findHighPerformingAds(@Param("minCtr") double minCtr);

    @Query("SELECT a FROM Ad a WHERE a.isRewardedAd = true ORDER BY a.createdAt DESC")
    List<Ad> findRewardedAds();

    @Query("SELECT a FROM Ad a WHERE a.excludePremium = false ORDER BY a.priority DESC")
    List<Ad> findNonPremiumExcluding();

    @Query("SELECT a FROM Ad a WHERE a.campaign.advertiser = :advertiser ORDER BY a.createdAt DESC")
    List<Ad> findByAdvertiser(@Param("advertiser") com.samjhadoo.model.User advertiser);

    @Query("SELECT a FROM Ad a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Ad> findRecentAds(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM Ad a WHERE a.endDate < :now AND a.active = true ORDER BY a.endDate DESC")
    List<Ad> findExpiringSoon(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Ad a WHERE a.qualityScore >= :minScore ORDER BY a.qualityScore DESC")
    List<Ad> findHighQualityAds(@Param("minScore") int minScore);

    @Query("SELECT a FROM Ad a WHERE a.isMobileOptimized = true ORDER BY a.createdAt DESC")
    List<Ad> findMobileOptimizedAds();

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.active = true")
    long countActiveAds();

    @Query("SELECT SUM(a.deliveredImpressions) FROM Ad a WHERE a.active = true")
    Long getTotalActiveImpressions();

    @Query("SELECT SUM(a.deliveredClicks) FROM Ad a WHERE a.active = true")
    Long getTotalActiveClicks();

    // TODO: clickThroughRate is computed - should calculate as (SUM(clicks) / SUM(impressions)) * 100
    // @Query("SELECT AVG(a.clickThroughRate) FROM Ad a WHERE a.deliveredImpressions > 0")
    // Double getAverageClickThroughRate();

    @Query("SELECT a FROM Ad a WHERE a.campaign = :campaign AND a.active = true ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findActiveAdsInCampaign(@Param("campaign") AdCampaign campaign);

    @Query("SELECT a FROM Ad a WHERE a.targetInterests LIKE %:interest% ORDER BY a.createdAt DESC")
    List<Ad> findByTargetInterest(@Param("interest") String interest);

    @Query("SELECT a FROM Ad a WHERE a.targetLocations LIKE %:location% ORDER BY a.createdAt DESC")
    List<Ad> findByTargetLocation(@Param("location") String location);

    @Query("SELECT a FROM Ad a WHERE a.campaign.status = 'ACTIVE' AND a.active = true ORDER BY a.priority DESC, a.weight DESC")
    List<Ad> findAllActiveAds();
}
