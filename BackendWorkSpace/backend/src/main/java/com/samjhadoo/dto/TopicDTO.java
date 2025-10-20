package com.samjhadoo.dto.topic;

import com.samjhadoo.model.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for Topic
 */
@Data
@Builder
public class TopicDTO {

    private Long id;
    private String title;
    private String description;
    private TopicCategory category;
    private Set<String> tags;
    private TopicDifficulty difficulty;
    private Set<CommunityTag> targetCommunities;
    private Set<String> supportedLanguages;
    private TopicStatus status;
    private boolean aiGenerated;
    private boolean seasonal;
    private LocalDateTime campaignStartDate;
    private LocalDateTime campaignEndDate;
    private long viewCount;
    private long clickCount;
    private long sessionCount;
    private long mentorCount;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
}
