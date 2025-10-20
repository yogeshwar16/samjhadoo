package com.samjhadoo.dto.topic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for Topic Engagement
 */
@Data
@Builder
public class TopicEngagementDTO {

    private Long topicId;
    private String topicTitle;
    private boolean viewed;
    private boolean clicked;
    private boolean sessionBooked;
    private LocalDateTime engagedAt;
}
