package com.samjhadoo.dto.visualquery;

import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryDTO {
    private Long id;
    private String queryId;
    private String userName;
    private String title;
    private String description;
    private QueryStatus status;
    private QueryCategory category;
    private String aiSuggestedCategory;
    private int urgencyLevel;
    private boolean anonymous;
    private boolean isPublic;
    private boolean allowMentorBidding;
    private BigDecimal maxBudget;
    private Long preferredMentorId;
    private String preferredMentorName;
    private String locationContext;
    private String tags;
    private String assignedMentorName;
    private LocalDateTime submittedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime responseDeadline;
    private int resolutionRating;
    private String resolutionFeedback;
    private int viewCount;
    private int helpfulVotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VisualQueryMediaDTO> mediaAttachments;
    private List<VisualQueryResponseDTO> responses;
    private boolean isOverdue;
    private long responseTimeHours;
    private boolean isActive;
}
