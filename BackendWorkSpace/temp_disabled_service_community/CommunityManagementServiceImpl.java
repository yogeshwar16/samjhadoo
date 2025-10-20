package com.samjhadoo.service.community;

import com.samjhadoo.dto.community.*;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.community.*;
import com.samjhadoo.model.enums.CommunityTag;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.community.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityManagementServiceImpl implements CommunityManagementService {

    private final UserRepository userRepository;
    private final CommunityDiscountRepository discountRepository;
    private final CommunityVerificationRepository verificationRepository;
    private final GeoPricingRepository geoPricingRepository;

    // ============= User Community Tag Management =============

    @Override
    @Transactional
    public void updateUserCommunityTag(User user, CommunityTag tag, String documentUrl) {
        user.setCommunityTag(tag);
        userRepository.save(user);
        log.info("Updated community tag for user {} to {}", user.getId(), tag);

        // If document provided and verification required, create verification request
        if (documentUrl != null && requiresVerification(tag)) {
            requestVerification(user, tag, documentUrl);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityTag getUserCommunityTag(Long userId) {
        return userRepository.findById(userId)
                .map(User::getCommunityTag)
                .orElse(null);
    }

    // ============= Verification Management =============

    @Override
    @Transactional
    public void requestVerification(User user, CommunityTag tag, String documentUrl) {
        CommunityVerification verification = new CommunityVerification();
        verification.setUser(user);
        verification.setCommunityTag(tag);
        verification.setStatus(CommunityVerification.VerificationStatus.PENDING);
        verification.setDocumentUrl(documentUrl);

        verificationRepository.save(verification);
        log.info("Verification requested for user {} with tag {}", user.getId(), tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VerificationRequestDTO> getPendingVerifications(Pageable pageable) {
        return verificationRepository.findByStatus(CommunityVerification.VerificationStatus.PENDING, pageable)
                .map(this::toVerificationDTO);
    }

    @Override
    @Transactional
    public void approveVerification(Long verificationId, String adminUsername, String notes) {
        CommunityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        verification.setStatus(CommunityVerification.VerificationStatus.APPROVED);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedBy(adminUsername);
        verification.setNotes(notes);

        verificationRepository.save(verification);
        log.info("Verification {} approved by {}", verificationId, adminUsername);
    }

    @Override
    @Transactional
    public void rejectVerification(Long verificationId, String adminUsername, String reason) {
        CommunityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found"));

        verification.setStatus(CommunityVerification.VerificationStatus.REJECTED);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedBy(adminUsername);
        verification.setNotes(reason);

        verificationRepository.save(verification);
        log.info("Verification {} rejected by {}", verificationId, adminUsername);
    }

    // ============= Community Discounts (Admin) =============

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::toDiscountDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommunityDiscountDTO createDiscount(CommunityDiscountDTO dto) {
        CommunityDiscount discount = new CommunityDiscount();
        discount.setCommunityTag(dto.getCommunityTag());
        discount.setDiscountPercent(dto.getDiscountPercent());
        discount.setRequiresVerification(dto.isRequiresVerification());
        discount.setActive(dto.isActive());
        discount.setDescription(dto.getDescription());
        discount.setEffectiveFrom(dto.getEffectiveFrom());
        discount.setEffectiveTo(dto.getEffectiveTo());

        discount = discountRepository.save(discount);
        return toDiscountDTO(discount);
    }

    @Override
    @Transactional
    public CommunityDiscountDTO updateDiscount(Long id, CommunityDiscountDTO dto) {
        CommunityDiscount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community discount not found"));

        discount.setCommunityTag(dto.getCommunityTag());
        discount.setDiscountPercent(dto.getDiscountPercent());
        discount.setRequiresVerification(dto.isRequiresVerification());
        discount.setActive(dto.isActive());
        discount.setDescription(dto.getDescription());
        discount.setEffectiveFrom(dto.getEffectiveFrom());
        discount.setEffectiveTo(dto.getEffectiveTo());

        discount = discountRepository.save(discount);
        return toDiscountDTO(discount);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    // ============= Geo Pricing (Admin) =============

    @Override
    @Transactional(readOnly = true)
    public List<GeoPricingDTO> getAllGeoPricing() {
        return geoPricingRepository.findAll().stream()
                .map(this::toGeoPricingDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GeoPricingDTO createGeoPricing(GeoPricingDTO dto) {
        GeoPricing geoPricing = new GeoPricing();
        geoPricing.setCountryCode(dto.getCountryCode());
        geoPricing.setCountryName(dto.getCountryName());
        geoPricing.setCityTier(dto.getCityTier());
        geoPricing.setBasePricePer10Min(dto.getBasePricePer10Min());
        geoPricing.setCurrency(dto.getCurrency());
        geoPricing.setCurrencySymbol(dto.getCurrencySymbol());
        geoPricing.setActive(dto.isActive());

        geoPricing = geoPricingRepository.save(geoPricing);
        return toGeoPricingDTO(geoPricing);
    }

    @Override
    @Transactional
    public GeoPricingDTO updateGeoPricing(Long id, GeoPricingDTO dto) {
        GeoPricing geoPricing = geoPricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geo pricing not found"));

        geoPricing.setCountryCode(dto.getCountryCode());
        geoPricing.setCountryName(dto.getCountryName());
        geoPricing.setCityTier(dto.getCityTier());
        geoPricing.setBasePricePer10Min(dto.getBasePricePer10Min());
        geoPricing.setCurrency(dto.getCurrency());
        geoPricing.setCurrencySymbol(dto.getCurrencySymbol());
        geoPricing.setActive(dto.isActive());

        geoPricing = geoPricingRepository.save(geoPricing);
        return toGeoPricingDTO(geoPricing);
    }

    @Override
    @Transactional
    public void deleteGeoPricing(Long id) {
        geoPricingRepository.deleteById(id);
    }

    // ============= Helper Methods =============

    private boolean requiresVerification(CommunityTag tag) {
        return discountRepository.findByCommunityTag(tag)
                .map(CommunityDiscount::isRequiresVerification)
                .orElse(false);
    }

    private VerificationRequestDTO toVerificationDTO(CommunityVerification verification) {
        return VerificationRequestDTO.builder()
                .id(verification.getId())
                .userId(verification.getUser().getId())
                .userName(verification.getUser().getFullName())
                .communityTag(verification.getCommunityTag())
                .status(verification.getStatus())
                .documentUrl(verification.getDocumentUrl())
                .notes(verification.getNotes())
                .createdAt(verification.getCreatedAt())
                .verifiedAt(verification.getVerifiedAt())
                .verifiedBy(verification.getVerifiedBy())
                .build();
    }

    private CommunityDiscountDTO toDiscountDTO(CommunityDiscount discount) {
        return CommunityDiscountDTO.builder()
                .id(discount.getId())
                .communityTag(discount.getCommunityTag())
                .discountPercent(discount.getDiscountPercent())
                .requiresVerification(discount.isRequiresVerification())
                .active(discount.isActive())
                .description(discount.getDescription())
                .effectiveFrom(discount.getEffectiveFrom())
                .effectiveTo(discount.getEffectiveTo())
                .build();
    }

    private GeoPricingDTO toGeoPricingDTO(GeoPricing geoPricing) {
        return GeoPricingDTO.builder()
                .id(geoPricing.getId())
                .countryCode(geoPricing.getCountryCode())
                .countryName(geoPricing.getCountryName())
                .cityTier(geoPricing.getCityTier())
                .basePricePer10Min(geoPricing.getBasePricePer10Min())
                .currency(geoPricing.getCurrency())
                .currencySymbol(geoPricing.getCurrencySymbol())
                .active(geoPricing.isActive())
                .build();
    }
}
