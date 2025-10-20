package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.RegionalMultiplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalMultiplierRepository extends JpaRepository<RegionalMultiplier, Long> {
    
    @Query("SELECT rm FROM RegionalMultiplier rm WHERE rm.regionCode = :regionCode " +
           "AND rm.effectiveFrom <= :now AND (rm.effectiveTo IS NULL OR rm.effectiveTo >= :now) " +
           "AND rm.active = true")
    Optional<RegionalMultiplier> findCurrentMultiplierForRegion(@Param("regionCode") String regionCode,
                                                                 @Param("now") LocalDateTime now);
    
    List<RegionalMultiplier> findByActiveTrue();
}
