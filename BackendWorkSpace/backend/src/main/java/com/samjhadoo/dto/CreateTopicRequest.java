package com.samjhadoo.dto.topic;

import com.samjhadoo.model.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request to create a topic
 */
@Data
public class CreateTopicRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TopicCategory category;

    private Set<String> tags;
    private TopicDifficulty difficulty;
    private Set<CommunityTag> targetCommunities;
    private Set<String> supportedLanguages;
    
    private boolean seasonal;
    private LocalDateTime campaignStartDate;
    private LocalDateTime campaignEndDate;
}
