package com.samjhadoo.service.pricing;

import com.samjhadoo.dto.pricing.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AdminPricingService {

    // Pricing Tiers
    List<PricingTierDTO> getAllTiers();
    PricingTierDTO createTier(PricingTierDTO dto);
    PricingTierDTO updateTier(Long id, PricingTierDTO dto);
    void deleteTier(Long id);

    // Regional Multipliers
    List<RegionalMultiplierDTO> getAllMultipliers();
    RegionalMultiplierDTO createMultiplier(RegionalMultiplierDTO dto);
    RegionalMultiplierDTO updateMultiplier(Long id, RegionalMultiplierDTO dto);
    void deleteMultiplier(Long id);

    // Promo Rules
    List<PromoRuleDTO> getAllPromos();
    PromoRuleDTO createPromo(PromoRuleDTO dto);
    PromoRuleDTO updatePromo(Long id, PromoRuleDTO dto);
    void deletePromo(Long id);

    // Mentor Override
    void overrideMentorRate(Long mentorId, BigDecimal rate, String reason);

    // Audit Logs
    Page<String> getAuditLogs(Pageable pageable);

    // Simulation
    PriceCalculationResponse simulatePrice(PriceCalculationRequest request);
}
