package com.samjhadoo.service.pricing;

import com.samjhadoo.dto.pricing.*;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.pricing.*;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.pricing.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingEngineServiceImpl implements PricingEngineService {

    private final UserRepository userRepository;
    private final MentorPricingRepository mentorPricingRepository;
    private final RegionalMultiplierRepository regionalMultiplierRepository;
    private final SurgeRuleRepository surgeRuleRepository;
    private final PromoRuleRepository promoRuleRepository;
    private final CommissionPolicyRepository commissionPolicyRepository;
    private final SessionPriceBreakdownRepository breakdownRepository;
    private final com.samjhadoo.repository.community.CommunityDiscountRepository communityDiscountRepository;

    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
    private static final BigDecimal AGENTIC_AI_FEE = new BigDecimal("50.00"); // ₹50 for AI
    private static final String DEFAULT_CURRENCY = "INR";

    @Override
    @Transactional
    public PriceCalculationResponse calculatePrice(PriceCalculationRequest request) {
        log.info("Calculating price for mentor: {}, minutes: {}", request.getMentorId(), request.getSlotMinutes());

        List<String> explanations = new ArrayList<>();

        // 1. Get mentor's base rate
        BigDecimal mentorBaseRate = getMentorBaseRate(request.getMentorId());
        explanations.add(String.format("Mentor base rate: ₹%.2f/hour", mentorBaseRate));

        // 2. Calculate base price (rate * minutes / 60)
        BigDecimal basePrice = mentorBaseRate
                .multiply(BigDecimal.valueOf(request.getSlotMinutes()))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        explanations.add(String.format("Base price for %d minutes: ₹%.2f", request.getSlotMinutes(), basePrice));

        // 3. Apply regional multiplier
        BigDecimal regionalMultiplier = BigDecimal.ONE;
        if (request.getRegionCode() != null) {
            regionalMultiplier = getRegionalMultiplier(request.getRegionCode());
            basePrice = basePrice.multiply(regionalMultiplier);
            explanations.add(String.format("Regional multiplier (%s): %.2fx = ₹%.2f", 
                    request.getRegionCode(), regionalMultiplier, basePrice));
        }

        // 4. Apply surge pricing if applicable
        BigDecimal surgeMultiplier = BigDecimal.ONE;
        if (request.getSkillId() != null) {
            surgeMultiplier = getSurgeMultiplier(request.getSkillId(), request.getRegionCode());
            if (surgeMultiplier.compareTo(BigDecimal.ONE) > 0) {
                basePrice = basePrice.multiply(surgeMultiplier);
                explanations.add(String.format("Surge pricing: %.2fx = ₹%.2f", surgeMultiplier, basePrice));
            }
        }

        BigDecimal subtotal = basePrice;

        // 5. Apply promo discount
        BigDecimal promoDiscount = BigDecimal.ZERO;
        String appliedPromoCode = null;
        if (request.getPromoCode() != null) {
            PromoRule promo = promoRuleRepository.findValidPromoByCode(request.getPromoCode(), request.getSessionDate())
                    .orElse(null);
            if (promo != null && isPromoApplicable(promo, request.getRegionCode())) {
                promoDiscount = calculatePromoDiscount(promo, subtotal);
                appliedPromoCode = promo.getCode();
                subtotal = subtotal.subtract(promoDiscount);
                explanations.add(String.format("Promo discount (%s): -₹%.2f = ₹%.2f", 
                        promo.getCode(), promoDiscount, subtotal));
            }
        }

        // 6. Apply community discount
        BigDecimal communityDiscount = BigDecimal.ZERO;
        if (request.getUserId() != null) {
            communityDiscount = applyCommunityDiscount(request.getUserId(), subtotal);
            if (communityDiscount.compareTo(BigDecimal.ZERO) > 0) {
                subtotal = subtotal.subtract(communityDiscount);
                explanations.add(String.format("Community discount: -₹%.2f = ₹%.2f", communityDiscount, subtotal));
            }
        }

        // 7. Apply charm pricing (round to end in 9)
        subtotal = applyCharmPricing(subtotal);
        explanations.add(String.format("Charm pricing applied: ₹%.2f", subtotal));

        // 8. Calculate platform commission
        BigDecimal commission = calculateCommission(subtotal);
        explanations.add(String.format("Platform commission: ₹%.2f", commission));

        // 9. Calculate tax (GST)
        BigDecimal tax = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        explanations.add(String.format("GST (18%%): ₹%.2f", tax));

        // 10. Add Agentic AI fee if requested
        BigDecimal aiFee = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getUseAgenticAI())) {
            aiFee = AGENTIC_AI_FEE;
            explanations.add(String.format("Agentic AI fee: ₹%.2f", aiFee));
        }

        // 11. Apply credits
        BigDecimal creditsApplied = BigDecimal.ZERO;
        if (request.getCreditsToApply() != null && request.getCreditsToApply().compareTo(BigDecimal.ZERO) > 0) {
            creditsApplied = request.getCreditsToApply();
            explanations.add(String.format("Credits applied: -₹%.2f", creditsApplied));
        }

        // 12. Calculate final price
        BigDecimal finalPrice = subtotal.add(tax).add(aiFee).subtract(creditsApplied);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        explanations.add(String.format("Final price: ₹%.2f", finalPrice));

        // 13. Calculate mentor payout (subtotal - commission)
        BigDecimal mentorPayout = subtotal.subtract(commission);
        explanations.add(String.format("Mentor payout: ₹%.2f", mentorPayout));

        // 14. Save breakdown and generate token
        String token = UUID.randomUUID().toString();
        SessionPriceBreakdown breakdown = saveBreakdown(
                token, request, mentorBaseRate, basePrice, regionalMultiplier, surgeMultiplier,
                promoDiscount, appliedPromoCode, communityDiscount, subtotal, commission,
                tax, aiFee, creditsApplied, finalPrice, mentorPayout, explanations
        );

        return buildResponse(breakdown, explanations);
    }

    @Override
    @Transactional
    public PriceConfirmationResponse confirmPrice(PriceConfirmationRequest request) {
        log.info("Confirming price with token: {}", request.getBreakdownToken());

        SessionPriceBreakdown breakdown = breakdownRepository.findByBreakdownToken(request.getBreakdownToken())
                .orElseThrow(() -> new ResourceNotFoundException("Price breakdown not found"));

        if (breakdown.isLocked()) {
            throw new IllegalStateException("Price already confirmed and locked");
        }

        // Lock the breakdown
        breakdown.setLocked(true);
        breakdown = breakdownRepository.save(breakdown);

        return PriceConfirmationResponse.builder()
                .breakdownId(breakdown.getId())
                .sessionId(breakdown.getSessionId())
                .finalPrice(breakdown.getFinalPrice())
                .mentorPayout(breakdown.getMentorPayout())
                .locked(true)
                .message("Price confirmed and locked successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceCalculationResponse getBreakdownByToken(String token) {
        SessionPriceBreakdown breakdown = breakdownRepository.findByBreakdownToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Price breakdown not found"));

        return buildResponse(breakdown, breakdown.getExplanations());
    }

    // Helper methods

    private BigDecimal getMentorBaseRate(Long mentorId) {
        return mentorPricingRepository.findCurrentPricingForMentor(mentorId, LocalDateTime.now())
                .map(MentorPricing::getBaseRate)
                .orElse(new BigDecimal("300.00")); // Default rate if not set
    }

    private BigDecimal getRegionalMultiplier(String regionCode) {
        return regionalMultiplierRepository.findCurrentMultiplierForRegion(regionCode, LocalDateTime.now())
                .map(RegionalMultiplier::getMultiplier)
                .orElse(BigDecimal.ONE);
    }

    private BigDecimal getSurgeMultiplier(Long skillId, String regionCode) {
        // Try region-specific surge first
        if (regionCode != null) {
            return surgeRuleRepository.findBySkillIdAndRegionCodeAndActiveTrue(skillId, regionCode)
                    .map(SurgeRule::getMultiplier)
                    .orElseGet(() -> getSurgeMultiplierGlobal(skillId));
        }
        return getSurgeMultiplierGlobal(skillId);
    }

    private BigDecimal getSurgeMultiplierGlobal(Long skillId) {
        return surgeRuleRepository.findBySkillIdAndRegionCodeIsNullAndActiveTrue(skillId)
                .map(SurgeRule::getMultiplier)
                .orElse(BigDecimal.ONE);
    }

    private boolean isPromoApplicable(PromoRule promo, String regionCode) {
        if (promo.getApplicableRegions() == null || promo.getApplicableRegions().isEmpty()) {
            return true; // Global promo
        }
        return regionCode != null && promo.getApplicableRegions().contains(regionCode);
    }

    private BigDecimal calculatePromoDiscount(PromoRule promo, BigDecimal amount) {
        if (promo.getType() == PromoRule.PromoType.PERCENTAGE) {
            return amount.multiply(promo.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return promo.getValue(); // Fixed discount
        }
    }

    private BigDecimal applyCharmPricing(BigDecimal amount) {
        // Round to nearest 10 and subtract 1 (e.g., 523 -> 520 -> 519, or 527 -> 530 -> 529)
        long rounded = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        long nearestTen = ((rounded + 5) / 10) * 10;
        return BigDecimal.valueOf(nearestTen - 1);
    }

    private BigDecimal calculateCommission(BigDecimal subtotal) {
        return commissionPolicyRepository.findCurrentPolicy(LocalDateTime.now())
                .map(policy -> subtotal.multiply(policy.getPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .orElse(subtotal.multiply(new BigDecimal("0.15"))) // Default 15%
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyCommunityDiscount(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCommunityTag() == null) {
            return BigDecimal.ZERO;
        }

        return communityDiscountRepository.findActiveDiscountForCommunity(user.getCommunityTag(), LocalDateTime.now())
                .map(discount -> amount.multiply(discount.getDiscountPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private SessionPriceBreakdown saveBreakdown(
            String token, PriceCalculationRequest request, BigDecimal mentorBaseRate,
            BigDecimal basePrice, BigDecimal regionalMultiplier, BigDecimal surgeMultiplier,
            BigDecimal promoDiscount, String promoCode, BigDecimal communityDiscount,
            BigDecimal subtotal, BigDecimal commission, BigDecimal tax, BigDecimal aiFee,
            BigDecimal creditsApplied, BigDecimal finalPrice, BigDecimal mentorPayout,
            List<String> explanations) {

        SessionPriceBreakdown breakdown = new SessionPriceBreakdown();
        breakdown.setBreakdownToken(token);
        breakdown.setSessionId(null); // Will be set during booking
        breakdown.setMentorBaseRate(mentorBaseRate);
        breakdown.setSlotMinutes(request.getSlotMinutes());
        breakdown.setBasePrice(basePrice);
        breakdown.setRegionalMultiplier(regionalMultiplier);
        breakdown.setSurgeMultiplier(surgeMultiplier);
        breakdown.setPromoDiscount(promoDiscount);
        breakdown.setPromoCode(promoCode);
        breakdown.setCommunityDiscount(communityDiscount);
        breakdown.setSubtotal(subtotal);
        breakdown.setPlatformCommission(commission);
        breakdown.setTax(tax);
        breakdown.setAgenticAiFee(aiFee);
        breakdown.setCreditsApplied(creditsApplied);
        breakdown.setFinalPrice(finalPrice);
        breakdown.setMentorPayout(mentorPayout);
        breakdown.setExplanations(explanations);
        breakdown.setLocked(false);

        return breakdownRepository.save(breakdown);
    }

    private PriceCalculationResponse buildResponse(SessionPriceBreakdown breakdown, List<String> explanations) {
        return PriceCalculationResponse.builder()
                .breakdownToken(breakdown.getBreakdownToken())
                .mentorBaseRate(breakdown.getMentorBaseRate())
                .slotMinutes(breakdown.getSlotMinutes())
                .basePrice(breakdown.getBasePrice())
                .regionalMultiplier(breakdown.getRegionalMultiplier())
                .surgeMultiplier(breakdown.getSurgeMultiplier())
                .promoDiscount(breakdown.getPromoDiscount())
                .promoCode(breakdown.getPromoCode())
                .communityDiscount(breakdown.getCommunityDiscount())
                .subtotal(breakdown.getSubtotal())
                .platformCommission(breakdown.getPlatformCommission())
                .tax(breakdown.getTax())
                .agenticAiFee(breakdown.getAgenticAiFee())
                .creditsApplied(breakdown.getCreditsApplied())
                .finalPrice(breakdown.getFinalPrice())
                .mentorPayout(breakdown.getMentorPayout())
                .explanations(explanations)
                .currency(DEFAULT_CURRENCY)
                .build();
    }
}
