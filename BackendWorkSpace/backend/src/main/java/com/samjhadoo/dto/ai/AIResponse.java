package com.samjhadoo.dto.ai;

import lombok.Builder;
import lombok.Data;

/**
 * Generic AI response
 */
@Data
@Builder
public class AIResponse {

    private String response;
    private boolean successful;
    private String errorMessage;
    
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    
    private String model;
    private Integer responseTimeMs;
}
