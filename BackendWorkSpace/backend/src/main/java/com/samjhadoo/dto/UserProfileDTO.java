package com.samjhadoo.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String userId;
    private String userName;
    private String userEmail;

    // Points
    private BigDecimal currentPoints;
    private BigDecimal lifetimeEarned;
    private BigDecimal lifetimeSpent;

    // Streaks
    private int currentStreakDays;
    private int maxStreakDays;
    private int totalLogins;
    private boolean streakActiveToday;

    // Badges & Achievements
    private List<UserBadgeDTO> recentBadges;
    private List<UserAchievementDTO> inProgressAchievements;
    private List<UserAchievementDTO> completedAchievements;
    private int totalBadgesEarned;
    private int totalAchievementsCompleted;

    // Referrals
    private int totalReferrals;
    private int completedReferrals;
    private String referralCode;
}
