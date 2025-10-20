package com.samjhadoo.model.ads;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a log entry for ad impressions, clicks, and interactions.
 */
@Entity
@Table(name = "ad_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdLog {

    public enum LogType {
        IMPRESSION,     // Ad was shown to user
        CLICK,          // User clicked on ad
        VIEW,           // User viewed ad content
        SKIP,           // User skipped ad
        COMPLETE,       // User completed watching ad (for video)
        ACTION,         // User performed desired action
        REWARD,         // User earned reward from ad
        REPORT,         // User reported ad as inappropriate
        BLOCK           // User blocked this advertiser
    }

    // Alias for LogType to support eventType field in builder
    public static final class EventType {
        public static final LogType IMPRESSION = LogType.IMPRESSION;
        public static final LogType CLICK = LogType.CLICK;
        public static final LogType VIEW = LogType.VIEW;
        public static final LogType SKIP = LogType.SKIP;
        public static final LogType COMPLETE = LogType.COMPLETE;
        public static final LogType ACTION = LogType.ACTION;
        public static final LogType REWARD = LogType.REWARD;
        public static final LogType REPORT = LogType.REPORT;
        public static final LogType BLOCK = LogType.BLOCK;
    }

    public enum DeviceType {
        MOBILE,
        DESKTOP,
        TABLET,
        SMART_TV,
        UNKNOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Ad ad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private AdCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_id")
    private AdPlacement placement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType logType;

    @Column(name = "session_id")
    private String sessionId; // User session identifier

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "referrer_url")
    private String referrerUrl; // Page where ad was shown

    @Column(name = "destination_url")
    private String destinationUrl; // Where user was taken after click

    @Column(name = "view_duration_seconds")
    private Integer viewDurationSeconds; // How long user viewed ad

    @Column(name = "completion_percentage")
    private Double completionPercentage; // For video ads

    @Column(name = "reward_earned", precision = 10, scale = 2)
    private BigDecimal rewardEarned; // Credits/points earned

    @Column(name = "cost_incurred", precision = 10, scale = 2)
    private BigDecimal costIncurred; // Cost to advertiser

    @Column(name = "is_fraudulent", nullable = false)
    private boolean isFraudulent;

    @Column(name = "fraud_reason")
    private String fraudReason;

    @Column(name = "geolocation")
    private String geolocation; // Country/city info

    @Column(name = "user_consent_given", nullable = false)
    private boolean userConsentGiven;

    @Builder.Default
    @Column(name = "require_consent", nullable = false)
    private boolean requireConsent = true; // Default to requiring consent

    @Column(name = "ad_blocker_detected", nullable = false)
    private boolean adBlockerDetected;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (deviceType == null) {
            deviceType = DeviceType.UNKNOWN;
        }
        if (userConsentGiven) {
            userConsentGiven = false; // Default to no consent recorded
        }
        if (adBlockerDetected) {
            adBlockerDetected = false; // Default to no ad blocker
        }
        if (isFraudulent) {
            isFraudulent = false; // Default to not fraudulent
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (logType == LogType.COMPLETE && processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the log entry as processed.
     */
    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marks the log entry as fraudulent.
     * @param reason Reason for fraud detection
     */
    public void markAsFraudulent(String reason) {
        this.isFraudulent = true;
        this.fraudReason = reason;
    }

    /**
     * Records a reward being earned.
     * @param amount Reward amount
     * @param currency Reward currency
     */
    public void recordReward(BigDecimal amount, String currency) {
        this.rewardEarned = amount;
        this.logType = LogType.REWARD;
    }

    /**
     * Records the cost incurred to advertiser.
     * @param cost Cost amount
     */
    public void recordCost(BigDecimal cost) {
        this.costIncurred = cost;
    }

    /**
     * Checks if this is a high-value interaction.
     * @return true if interaction is valuable (click, complete, action)
     */
    public boolean isHighValueInteraction() {
        return logType == LogType.CLICK ||
               logType == LogType.COMPLETE ||
               logType == LogType.ACTION ||
               logType == LogType.REWARD;
    }

    /**
     * Gets the interaction value score.
     * @return Score based on interaction type (0-10)
     */
    public int getInteractionValueScore() {
        return switch (logType) {
            case IMPRESSION -> 1;
            case VIEW -> 2;
            case SKIP -> 0;
            case CLICK -> 5;
            case COMPLETE -> 8;
            case ACTION -> 10;
            case REWARD -> 7;
            case REPORT -> -2;
            case BLOCK -> -5;
        };
    }

    /**
     * Checks if this interaction indicates user engagement.
     * @return true if user engaged with the ad
     */
    public boolean indicatesEngagement() {
        return logType == LogType.CLICK ||
               logType == LogType.VIEW ||
               logType == LogType.COMPLETE ||
               logType == LogType.ACTION;
    }

    /**
     * Checks if this interaction indicates user dissatisfaction.
     * @return true if user was dissatisfied (skip, report, block)
     */
    public boolean indicatesDissatisfaction() {
        return logType == LogType.SKIP ||
               logType == LogType.REPORT ||
               logType == LogType.BLOCK;
    }

    /**
     * Gets the time spent on this interaction in seconds.
     * @return Time spent, or 0 if not applicable
     */
    public int getTimeSpentSeconds() {
        if (viewDurationSeconds != null) {
            return viewDurationSeconds;
        }

        // Estimate based on interaction type
        return switch (logType) {
            case IMPRESSION -> 2;  // Brief impression
            case CLICK -> 5;       // Click and redirect time
            case VIEW -> 10;       // Average view time
            case COMPLETE -> 30;   // Full video completion
            case ACTION -> 15;     // Action completion time
            default -> 0;
        };
    }

    /**
     * Checks if this log entry is from a bot or automated traffic.
     * @return true if likely bot traffic
     */
    public boolean isLikelyBotTraffic() {
        // Check for signs of bot traffic
        if (userAgent != null) {
            String lowerUA = userAgent.toLowerCase();
            return lowerUA.contains("bot") ||
                   lowerUA.contains("crawler") ||
                   lowerUA.contains("spider") ||
                   lowerUA.contains("scraper");
        }
        return false;
    }

    /**
     * Gets the geographic region based on IP or user data.
     * @return Geographic region code
     */
    public String getGeographicRegion() {
        if (geolocation != null) {
            return geolocation;
        }
        if (ipAddress != null) {
            // Simple IP-based region detection (in real implementation, use GeoIP service)
            return "UNKNOWN";
        }
        return "UNKNOWN";
    }

    /**
     * Checks if the user gave consent for this ad interaction.
     * @return true if consent was given
     */
    public boolean hasUserConsent() {
        return userConsentGiven || !requireConsent;
    }

    /**
     * Gets the interaction quality score.
     * @return Quality score based on engagement and completion
     */
    public double getQualityScore() {
        double score = 0;

        // Base score from interaction type
        score += getInteractionValueScore();

        // Bonus for completion
        if (logType == LogType.COMPLETE && completionPercentage != null && completionPercentage >= 75) {
            score += 2;
        }

        // Penalty for dissatisfaction
        if (indicatesDissatisfaction()) {
            score -= 3;
        }

        // Bonus for long engagement
        if (getTimeSpentSeconds() > 30) {
            score += 1;
        }

        return Math.max(0, score);
    }

    // Custom builder methods to support eventType and userId aliases
    public static class AdLogBuilder {
        public AdLogBuilder eventType(LogType eventType) {
            this.logType = eventType;
            return this;
        }
        
        public AdLogBuilder userId(Long userId) {
            // Set the user object with just the ID
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                this.user = user;
            }
            return this;
        }
    }
}
