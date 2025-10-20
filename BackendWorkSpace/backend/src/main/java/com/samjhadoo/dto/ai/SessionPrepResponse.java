package com.samjhadoo.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI-generated session preparation
 */
@Data
@Builder
public class SessionPrepResponse {

    private String agenda;
    private List<String> talkingPoints;
    private List<String> resources;
    private List<String> preparationTips;
    private String estimatedDuration;
}
