package com.samjhadoo.dto.ai;

import com.samjhadoo.model.enums.AIRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * Generic AI request
 */
@Data
public class AIRequest {

    @NotNull
    private AIRequestType requestType;

    @NotBlank
    private String prompt;

    private Map<String, Object> context; // Additional context (session details, user info, etc.)
    
    private Integer maxTokens; // Override default
}
