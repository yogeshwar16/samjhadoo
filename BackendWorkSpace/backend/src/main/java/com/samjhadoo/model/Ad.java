package com.samjhadoo.model.ads;

import com.samjhadoo.model.enums.ads.AdType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an individual advertisement within a campaign.
 */
@Entity
@Table(name = "ads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String adId; // UUID for external reference

    @Column(nullable = false)
    private String title;

    @Lob
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdType adType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AdCampaign campaign;

    @Column(name = "creative_url", nullable = false)
    private String creativeUrl; // URL to ad creative (image, video, etc.)

    @Column(name = "creative_type")
    private String creativeType; // MIME type or format

    @Column(name = "click_url")
    private String clickUrl; // Destination URL

    @Column(name = "impression_url")
    private String impressionUrl; // Tracking URL for impressions

    @Column(name = "width")
    private Integer width; // For banner/video ads

    @Column(name = "height")
    private Integer height; // For banner/video ads

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // For video ads

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "priority", nullable = false)
    private int priority; // Higher number = higher priority within campaign

    @Column(name = "weight", nullable = false)
    private double weight; // Weight for rotation algorithm (0-1)

    @Column(name = "max_impressions")
    private Long maxImpressions; // -1 for unlimited

    @Column(name = "delivered_impressions", nullable = false)
    private long deliveredImpressions;

    @Column(name = "max_clicks")
    private Long maxClicks; // -1 for unlimited

    @Column(name = "delivered_clicks", nullable = false)
    private long deliveredClicks;

    @Column(name = "max_actions")
    private Long maxActions; // -1 for unlimited

    @Column(name = "delivered_actions", nullable = false)
    private long deliveredActions;

    @Column(name = "ctr_target", precision = 5, scale = 2)
    private BigDecimal ctrTarget; // Target click-through rate

    @Column(name = "cpm_target", precision = 10, scale = 2)
    private BigDecimal cpmTarget; // Target cost per mille

    @Column(name = "cpc_target", precision = 10, scale = 2)
    private BigDecimal cpcTarget; // Target cost per click

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

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
    private boolean excludePremium;

    @Column(name = "require_consent", nullable = false)
    private boolean requireConsent;

    @Column(name = "is_rewarded_ad", nullable = false)
    private boolean isRewardedAd;

    @Column(name = "reward_amount", precision = 10, scale = 2)
    private BigDecimal rewardAmount;

    @Column(name = "reward_currency")
    private String rewardCurrency;

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
        if (maxImpressions == null) {
            maxImpressions = -1L; // Default to unlimited
        }
        if (maxClicks == null) {
            maxClicks = -1L; // Default to unlimited
        }
        if (maxActions == null) {
            maxActions = -1L; // Default to unlimited
        }
        if (deliveredImpressions == 0) {
            deliveredImpressions = 0;
        }
        if (deliveredClicks == 0) {
            deliveredClicks = 0;
        }
        if (deliveredActions == 0) {
            deliveredActions = 0;
        }
        if (weight == 0) {
            weight = 1.0; // Default weight
        }
        if (priority == 0) {
            priority = 1; // Default priority
        }
        if (requireConsent) {
            requireConsent = false; // Default to not requiring consent
        }
        if (excludePremium) {
            excludePremium = false; // Default to show to all users
        }
        if (isRewardedAd) {
            isRewardedAd = false; // Default to not rewarded
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the ad is currently active.
     * @return true if active and within date range
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return active &&
               (startDate == null || !startDate.isAfter(now)) &&
               (endDate == null || !endDate.isBefore(now));
    }

    /**
     * Checks if the ad has reached its impression limit.
     * @return true if max impressions reached
     */
    public boolean hasReachedImpressionLimit() {
        return maxImpressions > 0 && deliveredImpressions >= maxImpressions;
    }

    /**
     * Checks if the ad has reached its click limit.
     * @return true if max clicks reached
     */
    public boolean hasReachedClickLimit() {
        return maxClicks > 0 && deliveredClicks >= maxClicks;
    }

    /**
     * Records an impression delivery.
     * @return true if impression was recorded
     */
    public boolean recordImpression() {
        if (hasReachedImpressionLimit()) {
            return false;
        }

        deliveredImpressions++;
        return true;
    }

    /**
     * Records a click delivery.
     * @return true if click was recorded
     */
    public boolean recordClick() {
        if (hasReachedClickLimit()) {
            return false;
        }

        deliveredClicks++;
        return true;
    }

    /**
     * Records an action delivery (conversion).
     * @return true if action was recorded
     */
    public boolean recordAction() {
        deliveredActions++;
        return true;
    }

    /**
     * Gets the click-through rate (CTR) as a percentage.
     * @return CTR percentage (0-100)
     */
    public double getClickThroughRate() {
        if (deliveredImpressions == 0) {
            return 0;
        }
        return ((double) deliveredClicks / deliveredImpressions) * 100;
    }

    /**
     * Gets the conversion rate as a percentage.
     * @return Conversion rate percentage (0-100)
     */
    public double getConversionRate() {
        if (deliveredClicks == 0) {
            return 0;
        }
        return ((double) deliveredActions / deliveredClicks) * 100;
    }

    /**
     * Checks if the ad is performing well against targets.
     * @return true if meeting performance targets
     */
    public boolean isPerformingWell() {
        double ctr = getClickThroughRate();

        // Check CTR target
        if (ctrTarget != null && ctr < ctrTarget.doubleValue()) {
            return false;
        }

        // Check CPM target
        if (cpmTarget != null && getCostPerMille().compareTo(cpmTarget) > 0) {
            return false;
        }

        // Check CPC target
        if (cpcTarget != null && getCostPerClick().compareTo(cpcTarget) > 0) {
            return false;
        }

        return true;
    }

    /**
     * Gets the cost per click (CPC).
     * @return CPC amount
     */
    public BigDecimal getCostPerClick() {
        if (deliveredClicks == 0) {
            return BigDecimal.ZERO;
        }
        return campaign.getSpentBudget().divide(BigDecimal.valueOf(deliveredClicks), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gets the cost per mille (CPM).
     * @return CPM amount
     */
    public BigDecimal getCostPerMille() {
        if (deliveredImpressions == 0) {
            return BigDecimal.ZERO;
        }
        return campaign.getSpentBudget().divide(BigDecimal.valueOf(deliveredImpressions / 1000.0), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gets the ad quality score based on performance metrics.
     * @return Quality score (0-100)
     */
    public int getQualityScore() {
        int score = 50; // Base score

        // CTR performance
        double ctr = getClickThroughRate();
        if (ctr >= 3.0) score += 20;
        else if (ctr >= 2.0) score += 10;
        else if (ctr >= 1.0) score += 5;

        // Conversion performance
        double conversion = getConversionRate();
        if (conversion >= 10.0) score += 15;
        else if (conversion >= 5.0) score += 10;
        else if (conversion >= 2.0) score += 5;

        // Target achievement
        if (isPerformingWell()) score += 15;

        return Math.min(100, score);
    }

    /**
     * Gets the file extension from the creative URL.
     * @return File extension or empty string
     */
    public String getFileExtension() {
        if (creativeUrl != null && creativeUrl.contains(".")) {
            return creativeUrl.substring(creativeUrl.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Checks if this is a rewarded ad.
     * @return true if rewarded ad
     */
    public boolean offersReward() {
        return isRewardedAd && rewardAmount != null && rewardAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets the aspect ratio of the ad.
     * @return Aspect ratio (width/height), or 0 if dimensions not available
     */
    public double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return 0;
    }

    /**
     * Checks if the ad is optimized for mobile devices.
     * @return true if mobile-optimized
     */
    public boolean isMobileOptimized() {
        return width != null && height != null && width <= 640 && height <= 480;
    }

    /**
     * Gets the estimated file load time based on size and type.
     * @return Estimated load time in seconds
     */
    public double getEstimatedLoadTime() {
        if (fileSizeBytes == null) {
            return 0;
        }

        // Rough estimation based on file size and type
        if (adType == AdType.VIDEO) {
            return fileSizeBytes / (1024.0 * 1024.0 * 2.0); // Assume 2MB/s for video
        } else if (adType == AdType.IMAGE) {
            return fileSizeBytes / (1024.0 * 1024.0 * 5.0); // Assume 5MB/s for images
        } else {
            return fileSizeBytes / (1024.0 * 1024.0 * 10.0); // Assume 10MB/s for other types
        }
    }
}
