package com.samjhadoo.repository.ads;

import com.samjhadoo.model.ads.AdPlacement;
import com.samjhadoo.model.ads.AdPlacement.PlacementType;
import com.samjhadoo.model.ads.AdPlacement.TargetingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdPlacementRepository extends JpaRepository<AdPlacement, Long> {

    List<AdPlacement> findByActiveTrue();

    List<AdPlacement> findByPlacementType(PlacementType placementType);

    List<AdPlacement> findByTargetingType(TargetingType targetingType);

    @Query("SELECT p FROM AdPlacement p WHERE p.active = true ORDER BY p.priority DESC")
    List<AdPlacement> findActiveOrderByPriority();

    @Query("SELECT p FROM AdPlacement p WHERE p.active = true AND p.placementType = :type ORDER BY p.priority DESC")
    List<AdPlacement> findActiveByPlacementType(@Param("type") PlacementType type);

    @Query("SELECT p FROM AdPlacement p WHERE p.active = true AND p.startDate <= :now AND (p.endDate IS NULL OR p.endDate >= :now) ORDER BY p.priority DESC")
    List<AdPlacement> findCurrentlyActive(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM AdPlacement p WHERE p.excludePremium = false ORDER BY p.priority DESC")
    List<AdPlacement> findNonPremiumExcluding();

    @Query("SELECT p FROM AdPlacement p WHERE p.requireConsent = false ORDER BY p.priority DESC")
    List<AdPlacement> findConsentFree();

    @Query("SELECT p FROM AdPlacement p WHERE p.targetAgeMin <= :age AND (p.targetAgeMax IS NULL OR p.targetAgeMax >= :age) ORDER BY p.priority DESC")
    List<AdPlacement> findByAgeRange(@Param("age") int age);

    @Query("SELECT p FROM AdPlacement p WHERE p.targetGender = :gender OR p.targetGender = 'ALL' ORDER BY p.priority DESC")
    List<AdPlacement> findByGender(@Param("gender") String gender);

    @Query("SELECT p FROM AdPlacement p WHERE p.targetLocations LIKE %:location% ORDER BY p.priority DESC")
    List<AdPlacement> findByLocation(@Param("location") String location);

    @Query("SELECT p FROM AdPlacement p WHERE p.targetInterests LIKE %:interest% ORDER BY p.priority DESC")
    List<AdPlacement> findByInterest(@Param("interest") String interest);

    @Query("SELECT p FROM AdPlacement p WHERE p.targetDeviceTypes LIKE %:device% ORDER BY p.priority DESC")
    List<AdPlacement> findByDeviceType(@Param("device") String device);

    @Query("SELECT p FROM AdPlacement p WHERE p.currentImpressions >= :minImpressions ORDER BY p.currentImpressions DESC")
    List<AdPlacement> findByMinImpressions(@Param("minImpressions") long minImpressions);

    @Query("SELECT p FROM AdPlacement p WHERE p.currentClicks >= :minClicks ORDER BY p.currentClicks DESC")
    List<AdPlacement> findByMinClicks(@Param("minClicks") long minClicks);

    // @Query("SELECT p FROM AdPlacement p WHERE p.clickThroughRate >= :minCtr ORDER BY p.clickThroughRate DESC")
    // List<AdPlacement> findHighPerforming(@Param("minCtr") double minCtr);

    @Query("SELECT COUNT(p) FROM AdPlacement p WHERE p.active = true")
    long countActivePlacements();

    // @Query("SELECT AVG(p.clickThroughRate) FROM AdPlacement p WHERE p.currentImpressions > 0")
    // Double getAverageClickThroughRate();

    @Query("SELECT SUM(p.currentImpressions) FROM AdPlacement p WHERE p.active = true")
    Long getTotalActiveImpressions();

    @Query("SELECT SUM(p.currentClicks) FROM AdPlacement p WHERE p.active = true")
    Long getTotalActiveClicks();

    @Query("SELECT p FROM AdPlacement p WHERE p.endDate < :now AND p.active = true ORDER BY p.endDate DESC")
    List<AdPlacement> findExpiringSoon(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM AdPlacement p WHERE p.currentImpressions >= p.maxImpressions AND p.maxImpressions > 0 ORDER BY p.currentImpressions DESC")
    List<AdPlacement> findReachedImpressionLimit();
}
