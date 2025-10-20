package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.MentorPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MentorPricingRepository extends JpaRepository<MentorPricing, Long> {
    
    @Query("SELECT mp FROM MentorPricing mp WHERE mp.mentor.id = :mentorId " +
           "AND mp.effectiveFrom <= :now AND (mp.effectiveTo IS NULL OR mp.effectiveTo >= :now) " +
           "ORDER BY mp.createdAt DESC")
    Optional<MentorPricing> findCurrentPricingForMentor(@Param("mentorId") Long mentorId, 
                                                         @Param("now") LocalDateTime now);
}
