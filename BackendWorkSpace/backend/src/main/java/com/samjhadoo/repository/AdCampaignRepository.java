package com.samjhadoo.repository.ads;

import com.samjhadoo.model.ads.AdCampaign;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ads.AdCampaign.CampaignStatus;
import com.samjhadoo.model.ads.AdCampaign.CampaignType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdCampaignRepository extends JpaRepository<AdCampaign, Long> {

    List<AdCampaign> findByAdvertiser(User advertiser);

    List<AdCampaign> findByStatus(CampaignStatus status);

    List<AdCampaign> findByCampaignType(CampaignType campaignType);

    @Query("SELECT c FROM AdCampaign c WHERE c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<AdCampaign> findActiveCampaigns();

    @Query("SELECT c FROM AdCampaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND (c.endDate IS NULL OR c.endDate >= :now) ORDER BY c.priority DESC")
    List<AdCampaign> findCurrentlyActive(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM AdCampaign c WHERE c.advertiser = :advertiser ORDER BY c.createdAt DESC")
    List<AdCampaign> findByAdvertiserOrderByCreated(@Param("advertiser") User advertiser);

    @Query("SELECT c FROM AdCampaign c WHERE c.dailyBudget >= :minBudget ORDER BY c.dailyBudget DESC")
    List<AdCampaign> findHighBudgetCampaigns(@Param("minBudget") BigDecimal minBudget);

    @Query("SELECT c FROM AdCampaign c WHERE c.spentBudget >= :minSpent ORDER BY c.spentBudget DESC")
    List<AdCampaign> findByMinSpent(@Param("minSpent") BigDecimal minSpent);

    @Query("SELECT c FROM AdCampaign c WHERE c.deliveredImpressions >= :minImpressions ORDER BY c.deliveredImpressions DESC")
    List<AdCampaign> findByMinImpressions(@Param("minImpressions") long minImpressions);

    @Query("SELECT c FROM AdCampaign c WHERE c.clickThroughRate >= :minCtr ORDER BY c.clickThroughRate DESC")
    List<AdCampaign> findHighPerformingCampaigns(@Param("minCtr") double minCtr);

    @Query("SELECT c FROM AdCampaign c WHERE c.endDate < :now AND c.status = 'ACTIVE' ORDER BY c.endDate DESC")
    List<AdCampaign> findExpiringSoon(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM AdCampaign c WHERE c.spentBudget >= c.totalBudget * 0.9 AND c.totalBudget > 0 ORDER BY c.spentBudget DESC")
    List<AdCampaign> findNearlyExhaustedBudget();

    @Query("SELECT c FROM AdCampaign c WHERE c.isRewardedAd = true ORDER BY c.createdAt DESC")
    List<AdCampaign> findRewardedAdCampaigns();

    @Query("SELECT c FROM AdCampaign c WHERE c.excludePremium = false ORDER BY c.priority DESC")
    List<AdCampaign> findNonPremiumExcluding();

    @Query("SELECT COUNT(c) FROM AdCampaign c WHERE c.status = 'ACTIVE'")
    long countActiveCampaigns();

    @Query("SELECT SUM(c.deliveredImpressions) FROM AdCampaign c WHERE c.status = 'ACTIVE'")
    Long getTotalActiveImpressions();

    @Query("SELECT SUM(c.deliveredClicks) FROM AdCampaign c WHERE c.status = 'ACTIVE'")
    Long getTotalActiveClicks();

    @Query("SELECT AVG(c.clickThroughRate) FROM AdCampaign c WHERE c.deliveredImpressions > 0")
    Double getAverageClickThroughRate();

    @Query("SELECT SUM(c.spentBudget) FROM AdCampaign c WHERE c.status = 'ACTIVE'")
    BigDecimal getTotalActiveSpent();

    @Query("SELECT c FROM AdCampaign c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<AdCampaign> findRecentCampaigns(@Param("since") LocalDateTime since);

    @Query("SELECT c FROM AdCampaign c WHERE c.targetInterests LIKE %:interest% ORDER BY c.createdAt DESC")
    List<AdCampaign> findByTargetInterest(@Param("interest") String interest);

    @Query("SELECT c FROM AdCampaign c WHERE c.targetLocations LIKE %:location% ORDER BY c.createdAt DESC")
    List<AdCampaign> findByTargetLocation(@Param("location") String location);

    @Query("SELECT c FROM AdCampaign c WHERE c.advertiser = :advertiser AND c.status IN ('ACTIVE', 'PAUSED') ORDER BY c.createdAt DESC")
    List<AdCampaign> findActiveByAdvertiser(@Param("advertiser") User advertiser);
}
