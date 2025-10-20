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
public class AdLogDTO {
    private Long id;
    private String adTitle;
    private String campaignName;
    private String placementName;
    private String userName;
    private String logType;
    private String sessionId;
    private String ipAddress;
    private String deviceType;
    private String userAgent;
    private String referrerUrl;
    private String destinationUrl;
    private Integer viewDurationSeconds;
    private Double completionPercentage;
    private BigDecimal rewardEarned;
    private BigDecimal costIncurred;
    private boolean isFraudulent;
    private String fraudReason;
    private String geolocation;
    private boolean userConsentGiven;
    private boolean adBlockerDetected;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private boolean isHighValueInteraction;
    private int interactionValueScore;
    private boolean indicatesEngagement;
    private boolean indicatesDissatisfaction;
    private int timeSpentSeconds;
    private boolean isLikelyBotTraffic;
    private String geographicRegion;
    private boolean hasUserConsent;
    private double qualityScore;
}
