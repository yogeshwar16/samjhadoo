package com.samjhadoo.dto.gamification;

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
public class LeaderboardDTO {
    private Long userId;
    private String userName;
    private String userEmail;
    private int rank;
    private BigDecimal points;
    private int currentStreakDays;
    private int totalBadges;
    private int totalAchievements;
    private LocalDateTime lastActivity;
}
