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
public class BadgeDTO {
    private Long id;
    private String name;
    private BadgeType type;
    private String description;
    private String iconUrl;
    private Integer pointsValue;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
