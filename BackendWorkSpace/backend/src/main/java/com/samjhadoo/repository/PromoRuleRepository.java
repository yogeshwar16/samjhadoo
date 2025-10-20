package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.PromoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoRuleRepository extends JpaRepository<PromoRule, Long> {
    
    Optional<PromoRule> findByCodeAndActiveTrue(String code);
    
    @Query("SELECT pr FROM PromoRule pr WHERE pr.code = :code " +
           "AND pr.startDate <= :now AND pr.endDate >= :now " +
           "AND pr.active = true")
    Optional<PromoRule> findValidPromoByCode(@Param("code") String code, 
                                             @Param("now") LocalDateTime now);
    
    List<PromoRule> findByActiveTrue();
}
