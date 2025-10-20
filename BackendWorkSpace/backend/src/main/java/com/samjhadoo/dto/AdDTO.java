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
public class AdDTO {
    private Long id;
    private String adId;
    private String title;
    private String description;
    private String adType;
    private String campaignName;
    private String advertiserName;
    private String creativeUrl;
    private String creativeType;
    private String clickUrl;
    private String impressionUrl;
    private Integer width;
    private Integer height;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private boolean active;
    private int priority;
    private double weight;
    private Long maxImpressions;
    private long deliveredImpressions;
    private Long maxClicks;
    private long deliveredClicks;
    private Long maxActions;
    private long deliveredActions;
    private BigDecimal ctrTarget;
    private BigDecimal cpmTarget;
    private BigDecimal cpcTarget;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double clickThroughRate;
    private double conversionRate;
    private boolean isPerformingWell;
    private BigDecimal costPerClick;
    private BigDecimal costPerMille;
    private int qualityScore;
    private String fileExtension;
    private boolean offersReward;
    private double aspectRatio;
    private boolean isMobileOptimized;
    private double estimatedLoadTime;
    private boolean isCurrentlyActive;
    private boolean hasReachedImpressionLimit;
    private boolean hasReachedClickLimit;
}
