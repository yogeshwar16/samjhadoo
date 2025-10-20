package com.samjhadoo.repository.ads;

import com.samjhadoo.model.ads.AdLog;
import com.samjhadoo.model.ads.Ad;
import com.samjhadoo.model.ads.AdCampaign;
import com.samjhadoo.model.ads.AdPlacement;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ads.AdLog.LogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdLogRepository extends JpaRepository<AdLog, Long> {

    List<AdLog> findByAd(Ad ad);

    List<AdLog> findByCampaign(AdCampaign campaign);

    List<AdLog> findByPlacement(AdPlacement placement);

    List<AdLog> findByUser(User user);

    List<AdLog> findByLogType(LogType logType);

    @Query("SELECT al FROM AdLog al WHERE al.ad = :ad ORDER BY al.createdAt DESC")
    List<AdLog> findByAdOrderByCreated(@Param("ad") Ad ad);

    @Query("SELECT al FROM AdLog al WHERE al.user = :user ORDER BY al.createdAt DESC")
    List<AdLog> findByUserOrderByCreated(@Param("user") User user);

    @Query("SELECT al FROM AdLog al WHERE al.logType = :type AND al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AdLog> findByTypeSince(@Param("type") LogType type, @Param("since") LocalDateTime since);

    @Query("SELECT al FROM AdLog al WHERE al.deviceType = :device ORDER BY al.createdAt DESC")
    List<AdLog> findByDeviceType(@Param("device") com.samjhadoo.model.ads.AdLog.DeviceType device);

    @Query("SELECT al FROM AdLog al WHERE al.isFraudulent = true ORDER BY al.createdAt DESC")
    List<AdLog> findFraudulentLogs();

    @Query("SELECT al FROM AdLog al WHERE al.userConsentGiven = false AND al.requireConsent = true ORDER BY al.createdAt DESC")
    List<AdLog> findWithoutConsent();

    @Query("SELECT al FROM AdLog al WHERE al.adBlockerDetected = true ORDER BY al.createdAt DESC")
    List<AdLog> findWithAdBlocker();

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.ad = :ad")
    long countByAd(@Param("ad") Ad ad);

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.campaign = :campaign")
    long countByCampaign(@Param("campaign") AdCampaign campaign);

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT SUM(al.costIncurred) FROM AdLog al WHERE al.campaign = :campaign")
    BigDecimal getTotalCostByCampaign(@Param("campaign") AdCampaign campaign);

    @Query("SELECT SUM(al.rewardEarned) FROM AdLog al WHERE al.user = :user")
    BigDecimal getTotalRewardsByUser(@Param("user") User user);

    @Query("SELECT AVG(al.viewDurationSeconds) FROM AdLog al WHERE al.logType = 'VIEW' AND al.viewDurationSeconds IS NOT NULL")
    Double getAverageViewDuration();

    @Query("SELECT al FROM AdLog al WHERE al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AdLog> findRecentLogs(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AdLog al WHERE al.geolocation = :location ORDER BY al.createdAt DESC")
    List<AdLog> findByGeolocation(@Param("location") String location);

    @Query("SELECT al FROM AdLog al WHERE al.sessionId = :sessionId ORDER BY al.createdAt ASC")
    List<AdLog> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.logType = 'IMPRESSION' AND al.createdAt >= :since")
    long countImpressionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.logType = 'CLICK' AND al.createdAt >= :since")
    long countClicksSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(al) FROM AdLog al WHERE al.logType = 'REWARD' AND al.createdAt >= :since")
    long countRewardsSince(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AdLog al WHERE al.logType = 'REPORT' ORDER BY al.createdAt DESC")
    List<AdLog> findReportedAds();

    @Query("SELECT al FROM AdLog al WHERE al.logType = 'BLOCK' ORDER BY al.createdAt DESC")
    List<AdLog> findBlockedAds();

    @Query("SELECT al FROM AdLog al WHERE al.completionPercentage >= :minPercentage ORDER BY al.createdAt DESC")
    List<AdLog> findHighCompletionLogs(@Param("minPercentage") double minPercentage);

    @Query("SELECT al FROM AdLog al WHERE al.rewardEarned > 0 ORDER BY al.rewardEarned DESC")
    List<AdLog> findRewardedInteractions();

    @Query("SELECT al FROM AdLog al WHERE al.costIncurred > 0 ORDER BY al.costIncurred DESC")
    List<AdLog> findCostlyInteractions();

    // TODO: qualityScore is a computed method, not a field - needs custom implementation
    // @Query("SELECT AVG(al.qualityScore) FROM AdLog al WHERE al.qualityScore > 0")
    // Double getAverageQualityScore();

    @Query("SELECT al FROM AdLog al WHERE al.isFraudulent = false AND al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AdLog> findValidLogsSince(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AdLog al WHERE al.user = :user AND al.logType = 'IMPRESSION' AND al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AdLog> findUserImpressionsSince(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT al.user) FROM AdLog al WHERE al.logType = 'IMPRESSION' AND al.createdAt >= :since")
    long countUniqueUsersSince(@Param("since") LocalDateTime since);
}
