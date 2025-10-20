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
public class MoodDTO {
    private Long id;
    private String userId;
    private String userName;
    private MoodType moodType;
    private int intensity;
    private String description;
    private boolean anonymous;
    private boolean lookingForTalk;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUpdated;
    private boolean active;
}
