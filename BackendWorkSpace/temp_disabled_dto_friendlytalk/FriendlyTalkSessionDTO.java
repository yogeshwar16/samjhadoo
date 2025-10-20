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
public class FriendlyTalkSessionDTO {
    private Long id;
    private String initiatorName;
    private String receiverName;
    private String status;
    private MoodType initiatorMood;
    private MoodType receiverMood;
    private boolean anonymous;
    private String topic;
    private String notes;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int durationMinutes;
    private int satisfactionRating;
    private String feedback;
    private LocalDateTime reportedAt;
    private String reportReason;
    private String moderatorName;
    private String moderationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
