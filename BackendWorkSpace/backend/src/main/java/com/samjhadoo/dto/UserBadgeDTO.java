package com.samjhadoo.dto.gamification;

import com.samjhadoo.model.enums.gamification.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeDTO {
    private Long id;
    private String badgeName;
    private BadgeType badgeType;
    private String badgeDescription;
    private String badgeIconUrl;
    private Integer badgePointsValue;
    private LocalDateTime awardedAt;
    private String awardedFor;
    private boolean notified;
}
