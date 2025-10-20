package com.samjhadoo.dto.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request for post-session insights
 */
@Data
public class PostSessionInsightsRequest {

    @NotNull
    private Long sessionId;

    private String sessionNotes;
    
    private String menteeGoals;
    
    private String discussionTopics;
}
