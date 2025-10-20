package com.samjhadoo.dto.ads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdCampaignDTO {
    private Long id;
    private String campaignId;
    private String name;
    private String description;
    private String advertiserName;
    private String status;
    private String campaignType;
    private BigDecimal dailyBudget;
    private BigDecimal totalBudget;
    private BigDecimal spentBudget;
    private BigDecimal bidAmount;
    private BigDecimal targetCPM;
    private BigDecimal targetCPC;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long targetImpressions;
    private Long targetClicks;
    private Long targetActions;
    private long deliveredImpressions;
    private long deliveredClicks;
    private long deliveredActions;
    private Integer targetAgeMin;
    private Integer targetAgeMax;
    private String targetGender;
    private String targetLocations;
    private String targetInterests;
    private String targetDeviceTypes;
    private boolean excludePremium;
    private boolean requireConsent;
    private boolean isRewardedAd;
    private BigDecimal rewardAmount;
    private String rewardCurrency;
    private String adCreativeUrl;
    private String adCreativeType;
    private String clickUrl;
    private String impressionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double clickThroughRate;
    private double conversionRate;
    private BigDecimal costPerClick;
    private BigDecimal costPerMille;
    private boolean isPerformingWell;
    private double progressPercentage;
    private boolean shouldPauseDueToPerformance;
    private boolean shouldStopDueToBudget;
    private boolean isCurrentlyActive;
    private boolean hasReachedBudgetLimit;
    private boolean hasReachedImpressionTarget;
    private boolean hasReachedClickTarget;
}
