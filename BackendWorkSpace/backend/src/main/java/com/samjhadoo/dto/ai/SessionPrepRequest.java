package com.samjhadoo.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request for AI-powered session preparation
 */
@Data
public class SessionPrepRequest {

    @NotNull
    private Long sessionId;

    @NotBlank
    private String menteeQuery;

    private String mentorExpertise;
    
    private String sessionGoals;
}
