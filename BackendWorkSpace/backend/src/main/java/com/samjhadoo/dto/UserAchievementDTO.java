package com.samjhadoo.dto.gamification;

import com.samjhadoo.model.enums.gamification.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementDTO {
    private Long id;
    private String achievementName;
    private AchievementType achievementType;
    private String achievementDescription;
    private int threshold;
    private int pointsReward;
    private int progress;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastUpdated;
    private double progressPercentage;
}
