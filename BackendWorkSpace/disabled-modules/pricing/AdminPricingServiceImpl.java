package com.samjhadoo.service.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjhadoo.dto.pricing.*;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.pricing.*;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.pricing.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPricingServiceImpl implements AdminPricingService {

    private final PricingTierRepository tierRepository;
    private final RegionalMultiplierRepository multiplierRepository;
    private final PromoRuleRepository promoRepository;
    private final MentorPricingRepository mentorPricingRepository;
    private final PricingAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final PricingEngineService pricingEngineService;
    private final ObjectMapper objectMapper;

    // ============= Pricing Tiers =============

    @Override
    @Transactional(readOnly = true)
    public List<PricingTierDTO> getAllTiers() {
        return tierRepository.findAll().stream()
                .map(this::toTierDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PricingTierDTO createTier(PricingTierDTO dto) {
        PricingTier tier = new PricingTier();
        tier.setName(dto.getName());
        tier.setHourlyRate(dto.getHourlyRate());
        tier.setMinRate(dto.getMinRate());
        tier.setMaxRate(dto.getMaxRate());
        tier.setActive(dto.isActive());
        tier.setDescription(dto.getDescription());

        tier = tierRepository.save(tier);
        logAudit("SYSTEM", "CREATE", "PricingTier", tier.getId(), null, tierToJson(tier), "New tier created");
        
        return toTierDTO(tier);
    }

    @Override
    @Transactional
    public PricingTierDTO updateTier(Long id, PricingTierDTO dto) {
        PricingTier tier = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing tier not found"));

        String before = tierToJson(tier);

        tier.setName(dto.getName());
        tier.setHourlyRate(dto.getHourlyRate());
        tier.setMinRate(dto.getMinRate());
        tier.setMaxRate(dto.getMaxRate());
        tier.setActive(dto.isActive());
        tier.setDescription(dto.getDescription());

        tier = tierRepository.save(tier);
        logAudit("SYSTEM", "UPDATE", "PricingTier", tier.getId(), before, tierToJson(tier), "Tier updated");

        return toTierDTO(tier);
    }

    @Override
    @Transactional
    public void deleteTier(Long id) {
        PricingTier tier = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing tier not found"));

        logAudit("SYSTEM", "DELETE", "PricingTier", tier.getId(), tierToJson(tier), null, "Tier deleted");
        tierRepository.delete(tier);
    }

    // ============= Regional Multipliers =============

    @Override
    @Transactional(readOnly = true)
    public List<RegionalMultiplierDTO> getAllMultipliers() {
        return multiplierRepository.findAll().stream()
                .map(this::toMultiplierDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegionalMultiplierDTO createMultiplier(RegionalMultiplierDTO dto) {
        RegionalMultiplier multiplier = new RegionalMultiplier();
        multiplier.setRegionCode(dto.getRegionCode());
        multiplier.setRegionName(dto.getRegionName());
        multiplier.setMultiplier(dto.getMultiplier());
        multiplier.setEffectiveFrom(dto.getEffectiveFrom());
        multiplier.setEffectiveTo(dto.getEffectiveTo());
        multiplier.setActive(dto.isActive());

        multiplier = multiplierRepository.save(multiplier);
        logAudit("SYSTEM", "CREATE", "RegionalMultiplier", multiplier.getId(), null, multiplierToJson(multiplier), "Multiplier created");

        return toMultiplierDTO(multiplier);
    }

    @Override
    @Transactional
    public RegionalMultiplierDTO updateMultiplier(Long id, RegionalMultiplierDTO dto) {
        RegionalMultiplier multiplier = multiplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional multiplier not found"));

        String before = multiplierToJson(multiplier);

        multiplier.setRegionCode(dto.getRegionCode());
        multiplier.setRegionName(dto.getRegionName());
        multiplier.setMultiplier(dto.getMultiplier());
        multiplier.setEffectiveFrom(dto.getEffectiveFrom());
        multiplier.setEffectiveTo(dto.getEffectiveTo());
        multiplier.setActive(dto.isActive());

        multiplier = multiplierRepository.save(multiplier);
        logAudit("SYSTEM", "UPDATE", "RegionalMultiplier", multiplier.getId(), before, multiplierToJson(multiplier), "Multiplier updated");

        return toMultiplierDTO(multiplier);
    }

    @Override
    @Transactional
    public void deleteMultiplier(Long id) {
        RegionalMultiplier multiplier = multiplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regional multiplier not found"));

        logAudit("SYSTEM", "DELETE", "RegionalMultiplier", multiplier.getId(), multiplierToJson(multiplier), null, "Multiplier deleted");
        multiplierRepository.delete(multiplier);
    }

    // ============= Promo Rules =============

    @Override
    @Transactional(readOnly = true)
    public List<PromoRuleDTO> getAllPromos() {
        return promoRepository.findAll().stream()
                .map(this::toPromoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromoRuleDTO createPromo(PromoRuleDTO dto) {
        PromoRule promo = new PromoRule();
        promo.setCode(dto.getCode());
        promo.setName(dto.getName());
        promo.setType(dto.getType());
        promo.setValue(dto.getValue());
        promo.setApplicableRegions(dto.getApplicableRegions());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());
        promo.setActive(dto.isActive());
        promo.setStackable(dto.isStackable());
        promo.setUsageLimit(dto.getUsageLimit());
        promo.setDescription(dto.getDescription());

        promo = promoRepository.save(promo);
        logAudit("SYSTEM", "CREATE", "PromoRule", promo.getId(), null, promoToJson(promo), "Promo created");

        return toPromoDTO(promo);
    }

    @Override
    @Transactional
    public PromoRuleDTO updatePromo(Long id, PromoRuleDTO dto) {
        PromoRule promo = promoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo rule not found"));

        String before = promoToJson(promo);

        promo.setCode(dto.getCode());
        promo.setName(dto.getName());
        promo.setType(dto.getType());
        promo.setValue(dto.getValue());
        promo.setApplicableRegions(dto.getApplicableRegions());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());
        promo.setActive(dto.isActive());
        promo.setStackable(dto.isStackable());
        promo.setUsageLimit(dto.getUsageLimit());
        promo.setDescription(dto.getDescription());

        promo = promoRepository.save(promo);
        logAudit("SYSTEM", "UPDATE", "PromoRule", promo.getId(), before, promoToJson(promo), "Promo updated");

        return toPromoDTO(promo);
    }

    @Override
    @Transactional
    public void deletePromo(Long id) {
        PromoRule promo = promoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo rule not found"));

        logAudit("SYSTEM", "DELETE", "PromoRule", promo.getId(), promoToJson(promo), null, "Promo deleted");
        promoRepository.delete(promo);
    }

    // ============= Mentor Override =============

    @Override
    @Transactional
    public void overrideMentorRate(Long mentorId, BigDecimal rate, String reason) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        MentorPricing mentorPricing = new MentorPricing();
        mentorPricing.setMentor(mentor);
        mentorPricing.setBaseRate(rate);
        mentorPricing.setEnforcedByAdmin(true);
        mentorPricing.setAdminReason(reason);
        mentorPricing.setEffectiveFrom(LocalDateTime.now());

        mentorPricing = mentorPricingRepository.save(mentorPricing);
        logAudit("SYSTEM", "OVERRIDE", "MentorPricing", mentorPricing.getId(), null, 
                String.format("Rate: %.2f, Reason: %s", rate, reason), reason);
    }

    // ============= Audit Logs =============

    @Override
    @Transactional(readOnly = true)
    public Page<String> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::formatAuditLog);
    }

    // ============= Simulation =============

    @Override
    public PriceCalculationResponse simulatePrice(PriceCalculationRequest request) {
        return pricingEngineService.calculatePrice(request);
    }

    // ============= Helper Methods =============

    private void logAudit(String actor, String action, String entityType, Long entityId, 
                         String before, String after, String reason) {
        PricingAuditLog log = new PricingAuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeState(before);
        log.setAfterState(after);
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    private String formatAuditLog(PricingAuditLog log) {
        return String.format("[%s] %s %s %s (ID: %d) - %s",
                log.getTimestamp(), log.getActor(), log.getAction(), log.getEntityType(), 
                log.getEntityId(), log.getReason());
    }

    // DTO Converters

    private PricingTierDTO toTierDTO(PricingTier tier) {
        return PricingTierDTO.builder()
                .id(tier.getId())
                .name(tier.getName())
                .hourlyRate(tier.getHourlyRate())
                .minRate(tier.getMinRate())
                .maxRate(tier.getMaxRate())
                .active(tier.isActive())
                .description(tier.getDescription())
                .build();
    }

    private RegionalMultiplierDTO toMultiplierDTO(RegionalMultiplier multiplier) {
        return RegionalMultiplierDTO.builder()
                .id(multiplier.getId())
                .regionCode(multiplier.getRegionCode())
                .regionName(multiplier.getRegionName())
                .multiplier(multiplier.getMultiplier())
                .effectiveFrom(multiplier.getEffectiveFrom())
                .effectiveTo(multiplier.getEffectiveTo())
                .active(multiplier.isActive())
                .build();
    }

    private PromoRuleDTO toPromoDTO(PromoRule promo) {
        return PromoRuleDTO.builder()
                .id(promo.getId())
                .code(promo.getCode())
                .name(promo.getName())
                .type(promo.getType())
                .value(promo.getValue())
                .applicableRegions(promo.getApplicableRegions())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .active(promo.isActive())
                .stackable(promo.isStackable())
                .usageLimit(promo.getUsageLimit())
                .description(promo.getDescription())
                .build();
    }

    // JSON Converters for Audit
    private String tierToJson(PricingTier tier) {
        try {
            return objectMapper.writeValueAsString(toTierDTO(tier));
        } catch (Exception e) {
            return "{}";
        }
    }

    private String multiplierToJson(RegionalMultiplier multiplier) {
        try {
            return objectMapper.writeValueAsString(toMultiplierDTO(multiplier));
        } catch (Exception e) {
            return "{}";
        }
    }

    private String promoToJson(PromoRule promo) {
        try {
            return objectMapper.writeValueAsString(toPromoDTO(promo));
        } catch (Exception e) {
            return "{}";
        }
    }
}
