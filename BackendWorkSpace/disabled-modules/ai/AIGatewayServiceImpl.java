package com.samjhadoo.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjhadoo.dto.ai.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.AIConfig;
import com.samjhadoo.model.ai.AIInteraction;
import com.samjhadoo.model.ai.AIRateLimit;
import com.samjhadoo.model.enums.AIRequestType;
import com.samjhadoo.model.enums.AITier;
import com.samjhadoo.repository.ai.AIConfigRepository;
import com.samjhadoo.repository.ai.AIInteractionRepository;
import com.samjhadoo.repository.ai.AIRateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIGatewayServiceImpl implements AIGatewayService {

    private final AIInteractionRepository interactionRepository;
    private final AIRateLimitRepository rateLimitRepository;
    private final AIConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.cost.gpt4:0.03}")
    private BigDecimal gpt4CostPer1kTokens;

    @Value("${ai.cost.gpt35:0.002}")
    private BigDecimal gpt35CostPer1kTokens;

    // ============= Generic AI Requests =============

    @Override
    @Transactional
    public AIResponse sendRequest(User user, AITier tier, AIRequest request) {
        long startTime = System.currentTimeMillis();

        // Check rate limit
        if (!checkRateLimit(user, tier)) {
            return AIResponse.builder()
                    .successful(false)
                    .errorMessage("Rate limit exceeded. Please try again later.")
                    .build();
        }

        // Get AI config
        AIConfig config = configRepository.findByTierAndEnabledTrue(tier)
                .orElseThrow(() -> new IllegalStateException("AI service not configured for tier: " + tier));

        AIResponse response;
        try {
            // Call AI service (OpenAI or compatible API)
            response = callAIService(config, request);
            
            // Increment rate limit
            incrementRateLimit(user, tier);

        } catch (Exception e) {
            log.error("AI request failed for user {}, tier {}: {}", user.getId(), tier, e.getMessage());
            
            // Fallback to Master AI if Agentic fails
            if (tier == AITier.AGENTIC) {
                log.info("Falling back to Master AI for user {}", user.getId());
                return masterAI(user, request);
            }

            response = AIResponse.builder()
                    .successful(false)
                    .errorMessage("AI service temporarily unavailable. Please try again.")
                    .build();
        }

        // Log interaction
        logInteraction(user, tier, request, response, System.currentTimeMillis() - startTime);

        return response;
    }

    @Override
    public AIResponse masterAI(User user, AIRequest request) {
        return sendRequest(user, AITier.MASTER, request);
    }

    @Override
    public AIResponse agenticAI(User user, AIRequest request) {
        return sendRequest(user, AITier.AGENTIC, request);
    }

    // ============= Session Preparation =============

    @Override
    @Transactional
    public SessionPrepResponse generateSessionPrep(User user, SessionPrepRequest request) {
        String prompt = buildSessionPrepPrompt(request);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setRequestType(AIRequestType.SESSION_PREP);
        aiRequest.setPrompt(prompt);

        AIResponse aiResponse = agenticAI(user, aiRequest);

        if (!aiResponse.isSuccessful()) {
            return SessionPrepResponse.builder()
                    .agenda("Unable to generate session preparation at this time.")
                    .talkingPoints(Arrays.asList("Review mentee's query", "Prepare relevant examples"))
                    .resources(Arrays.asList())
                    .preparationTips(Arrays.asList("Take notes during the session"))
                    .build();
        }

        return parseSessionPrepResponse(aiResponse.getResponse());
    }

    // ============= Post-Session Insights =============

    @Override
    @Transactional
    public PostSessionInsightsResponse generatePostSessionInsights(User user, PostSessionInsightsRequest request) {
        String prompt = buildPostSessionInsightsPrompt(request);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setRequestType(AIRequestType.POST_SESSION_INSIGHTS);
        aiRequest.setPrompt(prompt);

        AIResponse aiResponse = agenticAI(user, aiRequest);

        if (!aiResponse.isSuccessful()) {
            return PostSessionInsightsResponse.builder()
                    .summary("Session completed successfully.")
                    .keyTakeaways(Arrays.asList("Review session notes for details"))
                    .actionItems(Arrays.asList("Practice discussed concepts"))
                    .nextSteps(Arrays.asList("Schedule follow-up if needed"))
                    .recommendedResources(Arrays.asList())
                    .build();
        }

        return parsePostSessionInsightsResponse(aiResponse.getResponse());
    }

    // ============= Rate Limiting =============

    @Override
    @Transactional(readOnly = true)
    public boolean checkRateLimit(User user, AITier tier) {
        AIConfig config = configRepository.findByTierAndEnabledTrue(tier).orElse(null);
        if (config == null || !config.isEnabled()) {
            return false;
        }

        AIRateLimit rateLimit = rateLimitRepository.findByUserIdAndTier(user.getId(), tier).orElse(null);

        if (rateLimit == null) {
            return true; // No rate limit record, allow request
        }

        // Check if window has expired
        if (LocalDateTime.now().isAfter(rateLimit.getWindowEnd())) {
            return true; // Window expired, allow request
        }

        // Check if under limit
        return rateLimit.getRequestCount() < config.getRequestLimitPerHour();
    }

    @Override
    @Transactional
    public void incrementRateLimit(User user, AITier tier) {
        AIConfig config = configRepository.findByTier(tier).orElseThrow();

        AIRateLimit rateLimit = rateLimitRepository.findByUserIdAndTier(user.getId(), tier)
                .orElseGet(() -> {
                    AIRateLimit newLimit = new AIRateLimit();
                    newLimit.setUser(user);
                    newLimit.setTier(tier);
                    newLimit.setRequestCount(0);
                    newLimit.setWindowStart(LocalDateTime.now());
                    newLimit.setWindowEnd(LocalDateTime.now().plusHours(1));
                    return newLimit;
                });

        // Reset if window expired
        if (LocalDateTime.now().isAfter(rateLimit.getWindowEnd())) {
            rateLimit.setRequestCount(0);
            rateLimit.setWindowStart(LocalDateTime.now());
            rateLimit.setWindowEnd(LocalDateTime.now().plusHours(1));
        }

        rateLimit.setRequestCount(rateLimit.getRequestCount() + 1);
        rateLimitRepository.save(rateLimit);
    }

    // ============= Analytics =============

    @Override
    @Transactional(readOnly = true)
    public AIAnalyticsDTO getAnalytics(LocalDateTime start, LocalDateTime end) {
        long totalRequests = interactionRepository.count();
        long successfulRequests = totalRequests - interactionRepository.countBySuccessfulFalseAndCreatedAtBetween(start, end);
        long failedRequests = interactionRepository.countBySuccessfulFalseAndCreatedAtBetween(start, end);
        long totalTokens = interactionRepository.getTotalTokensBetween(start, end);
        BigDecimal totalCost = interactionRepository.getTotalCostBetween(start, end);

        double successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;

        return AIAnalyticsDTO.builder()
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .successRate(successRate)
                .build();
    }

    // ============= Helper Methods =============

    private AIResponse callAIService(AIConfig config, AIRequest request) {
        // TODO: Implement actual OpenAI API call
        // This is a placeholder implementation
        // In production, use OpenAI Java SDK or HTTP client

        log.info("Simulating AI call to model: {} for request type: {}", 
                config.getModelName(), request.getRequestType());

        // Simulate response
        String mockResponse = generateMockResponse(request);
        
        return AIResponse.builder()
                .response(mockResponse)
                .successful(true)
                .model(config.getModelName())
                .promptTokens(100)
                .completionTokens(200)
                .totalTokens(300)
                .responseTimeMs(500)
                .build();
    }

    private String generateMockResponse(AIRequest request) {
        switch (request.getRequestType()) {
            case CHAT:
                return "I'm here to help! How can I assist you today?";
            case MENTOR_SUGGESTION:
                return "Based on your interests and goals, I recommend connecting with mentors in software development and career coaching.";
            case EMOTIONAL_CHECKIN:
                return "I'm glad you reached out. It's completely normal to feel this way. Would you like to talk about what's on your mind?";
            default:
                return "I understand your question. Let me help you with that.";
        }
    }

    private void logInteraction(User user, AITier tier, AIRequest request, AIResponse response, long responseTime) {
        AIInteraction interaction = new AIInteraction();
        interaction.setUser(user);
        interaction.setTier(tier);
        interaction.setRequestType(request.getRequestType());
        interaction.setPrompt(request.getPrompt());
        interaction.setResponse(response.getResponse());
        interaction.setPromptTokens(response.getPromptTokens());
        interaction.setCompletionTokens(response.getCompletionTokens());
        interaction.setTotalTokens(response.getTotalTokens());
        interaction.setSuccessful(response.isSuccessful());
        interaction.setErrorMessage(response.getErrorMessage());
        interaction.setResponseTimeMs((int) responseTime);

        // Calculate cost
        BigDecimal costPer1k = tier == AITier.AGENTIC ? gpt4CostPer1kTokens : gpt35CostPer1kTokens;
        BigDecimal cost = costPer1k.multiply(BigDecimal.valueOf(response.getTotalTokens()))
                .divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
        interaction.setCost(cost);

        interactionRepository.save(interaction);
    }

    private String buildSessionPrepPrompt(SessionPrepRequest request) {
        return String.format(
                "Generate a structured session preparation plan for a mentorship session.\n\n" +
                "Mentee Query: %s\n" +
                "Mentor Expertise: %s\n" +
                "Session Goals: %s\n\n" +
                "Provide:\n" +
                "1. A clear agenda\n" +
                "2. Key talking points\n" +
                "3. Relevant resources or references\n" +
                "4. Preparation tips for the mentor\n" +
                "5. Estimated time allocation",
                request.getMenteeQuery(),
                request.getMentorExpertise(),
                request.getSessionGoals()
        );
    }

    private SessionPrepResponse parseSessionPrepResponse(String response) {
        // TODO: Implement proper parsing of structured AI response
        // For now, return a structured response
        return SessionPrepResponse.builder()
                .agenda(response)
                .talkingPoints(Arrays.asList(
                        "Review mentee's specific challenges",
                        "Share relevant industry examples",
                        "Discuss practical solutions"
                ))
                .resources(Arrays.asList(
                        "Relevant documentation",
                        "Sample projects",
                        "Industry articles"
                ))
                .preparationTips(Arrays.asList(
                        "Review mentee's background",
                        "Prepare specific examples",
                        "Set clear session goals"
                ))
                .estimatedDuration("45-60 minutes")
                .build();
    }

    private String buildPostSessionInsightsPrompt(PostSessionInsightsRequest request) {
        return String.format(
                "Generate post-session insights and action items based on this mentorship session.\n\n" +
                "Session Notes: %s\n" +
                "Mentee Goals: %s\n" +
                "Discussion Topics: %s\n\n" +
                "Provide:\n" +
                "1. A concise summary\n" +
                "2. Key takeaways\n" +
                "3. Actionable next steps\n" +
                "4. Recommended resources\n" +
                "5. Follow-up recommendations",
                request.getSessionNotes(),
                request.getMenteeGoals(),
                request.getDiscussionTopics()
        );
    }

    private PostSessionInsightsResponse parsePostSessionInsightsResponse(String response) {
        // TODO: Implement proper parsing of structured AI response
        return PostSessionInsightsResponse.builder()
                .summary(response)
                .keyTakeaways(Arrays.asList(
                        "Focus on practical application",
                        "Build consistent practice habits",
                        "Seek feedback regularly"
                ))
                .actionItems(Arrays.asList(
                        "Complete assigned exercises",
                        "Research discussed topics",
                        "Prepare questions for next session"
                ))
                .nextSteps(Arrays.asList(
                        "Schedule follow-up session in 2 weeks",
                        "Join relevant online communities",
                        "Build a sample project"
                ))
                .recommendedResources(Arrays.asList(
                        "Online courses",
                        "Documentation",
                        "Community forums"
                ))
                .followUpRecommendation("Schedule next session after completing initial action items")
                .build();
    }
}
