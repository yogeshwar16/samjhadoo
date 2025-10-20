package com.samjhadoo.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI-generated post-session insights
 */
@Data
@Builder
public class PostSessionInsightsResponse {

    private String summary;
    private List<String> keyTakeaways;
    private List<String> actionItems;
    private List<String> nextSteps;
    private List<String> recommendedResources;
    private String followUpRecommendation;
}
