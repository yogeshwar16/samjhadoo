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
public class AchievementDTO {
    private Long id;
    private AchievementType type;
    private String name;
    private String description;
    private int threshold;
    private int pointsReward;
    private boolean repeatable;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
