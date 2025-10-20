package com.samjhadoo.model.ads;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an advertisement placement configuration and targeting rules.
 */
@Entity
@Table(name = "ad_placements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdPlacement {

    public enum PlacementType {
        HEADER,         // Top of page
        SIDEBAR,        // Side panel
        FOOTER,         // Bottom of page
        INTERSTITIAL,   // Full-screen overlay
        IN_FEED,        // Within content feed
        BANNER,         // Banner strip
        POPUP,          // Modal popup
        NATIVE,         // Native content integration
        VIDEO_OVERLAY   // Overlay on video content
    }

    public enum TargetingType {
        ALL_USERS,      // Show to all users
        PREMIUM_ONLY,   // Premium subscribers only
        FREE_ONLY,      // Free users only
        NEW_USERS,      // Users registered within X days
        ACTIVE_USERS,   // Users with recent activity
        INACTIVE_USERS, // Users with no recent activity
        LOCATION_BASED, // Geographic targeting
        INTEREST_BASED, // Interest-based targeting
        DEVICE_BASED    // Mobile/desktop targeting
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placementId; // UUID for external reference

    @Column(nullable = false)
    private String name;

    @Lob
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlacementType placementType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetingType targetingType;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "priority", nullable = false)
    private int priority; // Higher number = higher priority

    @Column(name = "max_impressions", nullable = false)
    private long maxImpressions; // -1 for unlimited

    @Column(name = "current_impressions", nullable = false)
    private long currentImpressions;

    @Column(name = "max_clicks")
    private long maxClicks; // -1 for unlimited

    @Column(name = "current_clicks", nullable = false)
    private long currentClicks;

    @Column(name = "cpm_rate", precision = 10, scale = 2)
    private BigDecimal cpmRate; // Cost per mille (1000 impressions)

    @Column(name = "cpc_rate", precision = 10, scale = 2)
    private BigDecimal cpcRate; // Cost per click

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "frequency_cap", nullable = false)
    private int frequencyCap; // Max impressions per user per day

    @Column(name = "target_age_min")
    private Integer targetAgeMin;

    @Column(name = "target_age_max")
    private Integer targetAgeMax;

    @Column(name = "target_gender")
    private String targetGender; // MALE, FEMALE, ALL

    @Column(name = "target_locations")
    private String targetLocations; // Comma-separated location codes

    @Column(name = "target_interests")
    private String targetInterests; // Comma-separated interest keywords

    @Column(name = "target_device_types")
    private String targetDeviceTypes; // MOBILE, DESKTOP, TABLET

    @Column(name = "exclude_premium", nullable = false)
    private boolean excludePremium; // Don't show to premium users

    @Column(name = "require_consent", nullable = false)
    private boolean requireConsent; // Require explicit ad consent

    @Column(name = "ad_block_friendly", nullable = false)
    private boolean adBlockFriendly; // Designed to work with ad blockers

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (maxImpressions == 0) {
            maxImpressions = -1; // Default to unlimited
        }
        if (maxClicks == 0) {
            maxClicks = -1; // Default to unlimited
        }
        if (frequencyCap == 0) {
            frequencyCap = 5; // Default 5 impressions per day
        }
        if (priority == 0) {
            priority = 1; // Default priority
        }
        if (currentImpressions == 0) {
            currentImpressions = 0;
        }
        if (currentClicks == 0) {
            currentClicks = 0;
        }
        if (requireConsent) {
            requireConsent = false; // Default to not requiring consent
        }
        if (excludePremium) {
            excludePremium = false; // Default to show to all users
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the placement is currently active.
     * @return true if active and within date range
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return active &&
               (startDate == null || !startDate.isAfter(now)) &&
               (endDate == null || !endDate.isBefore(now));
    }

    /**
     * Checks if the placement has reached its impression limit.
     * @return true if max impressions reached
     */
    public boolean hasReachedImpressionLimit() {
        return maxImpressions > 0 && currentImpressions >= maxImpressions;
    }

    /**
     * Checks if the placement has reached its click limit.
     * @return true if max clicks reached
     */
    public boolean hasReachedClickLimit() {
        return maxClicks > 0 && currentClicks >= maxClicks;
    }

    /**
     * Records an impression for this placement.
     * @return true if impression was recorded
     */
    public boolean recordImpression() {
        if (hasReachedImpressionLimit()) {
            return false;
        }

        currentImpressions++;
        return true;
    }

    /**
     * Records a click for this placement.
     * @return true if click was recorded
     */
    public boolean recordClick() {
        if (hasReachedClickLimit()) {
            return false;
        }

        currentClicks++;
        return true;
    }

    /**
     * Gets the click-through rate (CTR) as a percentage.
     * @return CTR percentage (0-100)
     */
    public double getClickThroughRate() {
        if (currentImpressions == 0) {
            return 0;
        }
        return ((double) currentClicks / currentImpressions) * 100;
    }

    /**
     * Checks if the placement is performing well.
     * @return true if CTR is above 2%
     */
    public boolean isPerformingWell() {
        return getClickThroughRate() >= 2.0;
    }

    /**
     * Gets the total cost for impressions served.
     * @return Total cost based on CPM rate
     */
    public BigDecimal getTotalImpressionCost() {
        if (cpmRate == null || currentImpressions == 0) {
            return BigDecimal.ZERO;
        }
        return cpmRate.multiply(BigDecimal.valueOf(currentImpressions / 1000.0));
    }

    /**
     * Gets the total cost for clicks received.
     * @return Total cost based on CPC rate
     */
    public BigDecimal getTotalClickCost() {
        if (cpcRate == null || currentClicks == 0) {
            return BigDecimal.ZERO;
        }
        return cpcRate.multiply(BigDecimal.valueOf(currentClicks));
    }

    /**
     * Checks if a user matches the targeting criteria.
     * @param userAge User's age
     * @param userGender User's gender
     * @param userLocation User's location
     * @param userInterests User's interests
     * @param deviceType User's device type
     * @param isPremium Whether user is premium
     * @param daysSinceRegistration Days since user registration
     * @return true if user matches targeting
     */
    public boolean matchesTargeting(Integer userAge, String userGender, String userLocation,
                                   List<String> userInterests, String deviceType,
                                   boolean isPremium, int daysSinceRegistration) {
        // Check premium exclusion
        if (excludePremium && isPremium) {
            return false;
        }

        // Check age targeting
        if (targetAgeMin != null && (userAge == null || userAge < targetAgeMin)) {
            return false;
        }
        if (targetAgeMax != null && (userAge == null || userAge > targetAgeMax)) {
            return false;
        }

        // Check gender targeting
        if (targetGender != null && !"ALL".equals(targetGender) && !targetGender.equals(userGender)) {
            return false;
        }

        // Check location targeting
        if (targetLocations != null && userLocation != null) {
            String[] targetLocs = targetLocations.split(",");
            boolean locationMatch = false;
            for (String targetLoc : targetLocs) {
                if (userLocation.contains(targetLoc.trim())) {
                    locationMatch = true;
                    break;
                }
            }
            if (!locationMatch) {
                return false;
            }
        }

        // Check interest targeting
        if (targetInterests != null && userInterests != null) {
            String[] targetInts = targetInterests.split(",");
            boolean interestMatch = false;
            for (String targetInt : targetInts) {
                if (userInterests.contains(targetInt.trim())) {
                    interestMatch = true;
                    break;
                }
            }
            if (!interestMatch) {
                return false;
            }
        }

        // Check device type targeting
        if (targetDeviceTypes != null && deviceType != null) {
            if (!targetDeviceTypes.contains(deviceType)) {
                return false;
            }
        }

        return true;
    }
}
