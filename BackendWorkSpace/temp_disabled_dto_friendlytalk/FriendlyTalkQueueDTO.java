package com.samjhadoo.dto.friendlytalk;

import com.samjhadoo.model.enums.friendlytalk.MoodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendlyTalkQueueDTO {
    private Long id;
    private String userName;
    private String status;
    private MoodType moodType;
    private int intensity;
    private boolean anonymous;
    private String preferredTopics;
    private String avoidTopics;
    private int maxWaitMinutes;
    private LocalDateTime joinedAt;
    private LocalDateTime matchedAt;
    private LocalDateTime expiresAt;
    private int estimatedWaitMinutes;
    private String matchedWithName;
    private String matchingCriteria;
    private int retryCount;
    private LocalDateTime lastRetryAt;
    private int waitingTimeMinutes;
    private boolean active;
}
