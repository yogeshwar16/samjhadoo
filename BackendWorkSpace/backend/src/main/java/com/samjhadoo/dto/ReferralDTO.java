package com.samjhadoo.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDTO {
    private Long id;
    private String referrerName;
    private String refereeName;
    private String refereeEmail;
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private boolean rewardAwarded;
    private String rewardDescription;
    private boolean active;
}
