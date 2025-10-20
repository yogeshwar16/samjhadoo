package com.samjhadoo.service.ai;

import com.samjhadoo.dto.ai.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.AITier;

import java.time.LocalDateTime;

/**
 * AI Gateway Service for routing requests to appropriate AI tiers
 */
public interface AIGatewayService {

    // Generic AI requests
    AIResponse sendRequest(User user, AITier tier, AIRequest request);
    
    // Master AI (Freemium)
    AIResponse masterAI(User user, AIRequest request);
    
    // Agentic AI (Premium)
    AIResponse agenticAI(User user, AIRequest request);
    
    // Session Preparation
    SessionPrepResponse generateSessionPrep(User user, SessionPrepRequest request);
    
    // Post-Session Insights
    PostSessionInsightsResponse generatePostSessionInsights(User user, PostSessionInsightsRequest request);
    
    // Rate Limiting
    boolean checkRateLimit(User user, AITier tier);
    void incrementRateLimit(User user, AITier tier);
    
    // Analytics
    AIAnalyticsDTO getAnalytics(LocalDateTime start, LocalDateTime end);
}
