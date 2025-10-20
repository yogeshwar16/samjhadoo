package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {
    
    Optional<PricingTier> findByName(String name);
    
    List<PricingTier> findByActiveTrue();
}
