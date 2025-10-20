package com.samjhadoo.service.community;

import com.samjhadoo.dto.community.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.CommunityTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing community tags, discounts, and verifications
 */
public interface CommunityManagementService {

    // User Community Tag Management
    void updateUserCommunityTag(User user, CommunityTag tag, String documentUrl);
    CommunityTag getUserCommunityTag(Long userId);

    // Verification Management
    void requestVerification(User user, CommunityTag tag, String documentUrl);
    Page<VerificationRequestDTO> getPendingVerifications(Pageable pageable);
    void approveVerification(Long verificationId, String adminUsername, String notes);
    void rejectVerification(Long verificationId, String adminUsername, String reason);

    // Community Discounts (Admin)
    List<CommunityDiscountDTO> getAllDiscounts();
    CommunityDiscountDTO createDiscount(CommunityDiscountDTO dto);
    CommunityDiscountDTO updateDiscount(Long id, CommunityDiscountDTO dto);
    void deleteDiscount(Long id);

    // Geo Pricing (Admin)
    List<GeoPricingDTO> getAllGeoPricing();
    GeoPricingDTO createGeoPricing(GeoPricingDTO dto);
    GeoPricingDTO updateGeoPricing(Long id, GeoPricingDTO dto);
    void deleteGeoPricing(Long id);
}
