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
public class AdPlacementDTO {
    private Long id;
    private String placementId;
    private String name;
    private String description;
    private String placementType;
    private String targetingType;
    private boolean active;
    private int priority;
    private long maxImpressions;
    private long currentImpressions;
    private long maxClicks;
    private long currentClicks;
    private BigDecimal cpmRate;
    private BigDecimal cpcRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int frequencyCap;
    private Integer targetAgeMin;
    private Integer targetAgeMax;
    private String targetGender;
    private String targetLocations;
    private String targetInterests;
    private String targetDeviceTypes;
    private boolean excludePremium;
    private boolean requireConsent;
    private boolean adBlockFriendly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double clickThroughRate;
    private boolean isPerformingWell;
    private BigDecimal totalImpressionCost;
    private BigDecimal totalClickCost;
    private boolean isCurrentlyActive;
    private boolean hasReachedImpressionLimit;
    private boolean hasReachedClickLimit;
}
