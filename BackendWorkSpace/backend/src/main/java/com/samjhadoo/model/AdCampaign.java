package com.samjhadoo.model.ads;

import com.samjhadoo.model.User;
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
 * Represents an advertising campaign with budget and targeting.
 */
@Entity
@Table(name = "ad_campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdCampaign {

    public enum CampaignStatus {
        DRAFT,          // Campaign being created
        PENDING,        // Submitted for review
        APPROVED,       // Approved and ready to run
        ACTIVE,         // Currently running
        PAUSED,         // Temporarily paused
        COMPLETED,      // Successfully completed
        EXPIRED,        // Campaign period ended
        CANCELLED,      // Cancelled by advertiser
        SUSPENDED       // Suspended due to violations
    }

    public enum CampaignType {
        CPC,            // Cost per click
        CPM,            // Cost per mille (1000 impressions)
        CPA,            // Cost per action
        FIXED          // Fixed budget campaign
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String campaignId; // UUID for external reference

    @Column(nullable = false)
    private String name;

    @Lob
    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignType campaignType;

    @Column(name = "daily_budget", precision = 12, scale = 2)
    private BigDecimal dailyBudget;

    @Column(name = "total_budget", precision = 12, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "spent_budget", precision = 12, scale = 2, nullable = false)
    private BigDecimal spentBudget;

    @Column(name = "bid_amount", precision = 10, scale = 2)
    private BigDecimal bidAmount; // For auction-based campaigns

    @Column(name = "target_cpm", precision = 10, scale = 2)
    private BigDecimal targetCPM; // Target cost per mille

    @Column(name = "target_cpc", precision = 10, scale = 2)
    private BigDecimal targetCPC; // Target cost per click

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "target_impressions")
    private Long targetImpressions;

    @Column(name = "target_clicks")
    private Long targetClicks;

    @Column(name = "target_actions")
    private Long targetActions;

    @Column(name = "delivered_impressions", nullable = false)
    private long deliveredImpressions;

    @Column(name = "delivered_clicks", nullable = false)
    private long deliveredClicks;

    @Column(name = "delivered_actions", nullable = false)
    private long deliveredActions;

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
    private boolean isRewardedAd; // Whether this campaign offers rewards

    @Column(name = "reward_amount", precision = 10, scale = 2)
    private BigDecimal rewardAmount; // Credits earned for watching rewarded ads

    @Column(name = "reward_currency")
    private String rewardCurrency; // Type of reward (CREDITS, POINTS, etc.)

    @Column(name = "ad_creative_url")
    private String adCreativeUrl; // URL to ad creative assets

    @Column(name = "ad_creative_type")
    private String adCreativeType; // IMAGE, VIDEO, HTML, etc.

    @Column(name = "click_url")
    private String clickUrl; // Destination URL for clicks

    @Column(name = "impression_url")
    private String impressionUrl; // Tracking URL for impressions

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Ad> ads = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (spentBudget == null) {
            spentBudget = BigDecimal.ZERO;
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
     * Checks if the campaign is currently active.
     * @return true if active and within date range
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == CampaignStatus.ACTIVE &&
               (startDate == null || !startDate.isAfter(now)) &&
               (endDate == null || !endDate.isBefore(now));
    }

    /**
     * Checks if the campaign has reached its budget limit.
     * @return true if budget exhausted
     */
    public boolean hasReachedBudgetLimit() {
        return totalBudget != null && spentBudget.compareTo(totalBudget) >= 0;
    }

    /**
     * Checks if the campaign has reached its impression target.
     * @return true if target reached
     */
    public boolean hasReachedImpressionTarget() {
        return targetImpressions != null && deliveredImpressions >= targetImpressions;
    }

    /**
     * Checks if the campaign has reached its click target.
     * @return true if target reached
     */
    public boolean hasReachedClickTarget() {
        return targetClicks != null && deliveredClicks >= targetClicks;
    }

    /**
     * Records an impression delivery.
     * @return true if impression was recorded
     */
    public boolean recordImpression() {
        if (hasReachedBudgetLimit() || hasReachedImpressionTarget()) {
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
        if (hasReachedBudgetLimit() || hasReachedClickTarget()) {
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
     * Updates the spent budget.
     * @param amount Amount to add to spent budget
     */
    public void updateSpentBudget(BigDecimal amount) {
        this.spentBudget = this.spentBudget.add(amount);
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
     * Gets the cost per click (CPC).
     * @return CPC amount
     */
    public BigDecimal getCostPerClick() {
        if (deliveredClicks == 0) {
            return BigDecimal.ZERO;
        }
        return spentBudget.divide(BigDecimal.valueOf(deliveredClicks), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Gets the cost per mille (CPM).
     * @return CPM amount
     */
    public BigDecimal getCostPerMille() {
        if (deliveredImpressions == 0) {
            return BigDecimal.ZERO;
        }
        return spentBudget.divide(BigDecimal.valueOf(deliveredImpressions / 1000.0), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Checks if the campaign is performing well.
     * @return true if CTR is above 2% and conversion rate is above 5%
     */
    public boolean isPerformingWell() {
        return getClickThroughRate() >= 2.0 && getConversionRate() >= 5.0;
    }

    /**
     * Gets the campaign progress as a percentage.
     * @return Progress percentage (0-100)
     */
    public double getProgressPercentage() {
        if (totalBudget != null && totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            return spentBudget.divide(totalBudget, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0;
    }

    /**
     * Checks if the campaign should be paused due to poor performance.
     * @return true if campaign should be paused
     */
    public boolean shouldPauseDueToPerformance() {
        // Pause if CTR is below 0.5% and we've delivered at least 1000 impressions
        return deliveredImpressions >= 1000 && getClickThroughRate() < 0.5;
    }

    /**
     * Checks if the campaign should be stopped due to budget exhaustion.
     * @return true if campaign should be stopped
     */
    public boolean shouldStopDueToBudget() {
        return hasReachedBudgetLimit() || getProgressPercentage() >= 95;
    }
}
