package com.samjhadoo.service.ai;

import com.samjhadoo.config.AIConfiguration;
import com.samjhadoo.dto.ai.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.AIInteraction;
import com.samjhadoo.model.ai.AIRateLimit;
import com.samjhadoo.model.enums.AIRequestType;
import com.samjhadoo.model.enums.AITier;
import com.samjhadoo.repository.ai.AIInteractionRepository;
import com.samjhadoo.repository.ai.AIRateLimitRepository;
import com.samjhadoo.repository.ai.AIConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIGatewayServiceTest {

    @Mock
    private AIInteractionRepository interactionRepository;
    
    @Mock
    private AIRateLimitRepository rateLimitRepository;
    
    @Mock
    private AIConfigRepository configRepository;
    
    @Mock
    private OpenAIService openAIService;
    
    @Mock
    private AIConfiguration aiConfig;
    
    @Mock
    private RetryTemplate retryTemplate;
    
    @InjectMocks
    private AIGatewayServiceImpl aiGatewayService;
    
    private User testUser;
    private AIRequest testRequest;
    private AIResponse successResponse;
    private AIConfiguration.OpenAIConfig openAIConfig;
    private AIConfiguration.RateLimit rateLimitConfig;
    private AIConfiguration.Cost costConfig;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        
        testRequest = new AIRequest();
        testRequest.setRequestType(AIRequestType.CHAT);
        testRequest.setPrompt("Test prompt");
        
        successResponse = AIResponse.builder()
                .response("Test response")
                .successful(true)
                .promptTokens(10)
                .completionTokens(20)
                .totalTokens(30)
                .build();
                
        // Setup config mocks
        openAIConfig = mock(AIConfiguration.OpenAIConfig.class);
        when(openAIConfig.getModel(any())).thenReturn("gpt-4");
        when(openAIConfig.getTemperature()).thenReturn(0.7);
        when(openAIConfig.getMaxTokens()).thenReturn(1000);
        
        rateLimitConfig = mock(AIConfiguration.RateLimit.class);
        when(rateLimitConfig.getMaster()).thenReturn(10);
        when(rateLimitConfig.getAgentic()).thenReturn(50);
        
        costConfig = mock(AIConfiguration.Cost.class);
        when(costConfig.getGpt35()).thenReturn(0.002);
        when(costConfig.getGpt4()).thenReturn(0.03);
        
        when(aiConfig.getOpenai()).thenReturn(openAIConfig);
        when(aiConfig.getRateLimit()).thenReturn(rateLimitConfig);
        when(aiConfig.getCost()).thenReturn(costConfig);
    }
    
    @Test
    void sendRequest_WithValidRequest_ReturnsResponse() {
        // Arrange
        when(rateLimitRepository.findByUserIdAndTier(anyLong(), any()))
            .thenReturn(Optional.of(createRateLimit(0)));
            
        when(openAIService.generateResponse(anyString(), anyString(), anyString()))
            .thenReturn(successResponse);
            
        // Act
        AIResponse response = aiGatewayService.sendRequest(testUser, AITier.AGENTIC, testRequest);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals("Test response", response.getResponse());
        
        verify(interactionRepository, times(1)).save(any(AIInteraction.class));
        verify(rateLimitRepository, times(1)).save(any(AIRateLimit.class));
    }
    
    @Test
    void checkRateLimit_WhenBelowLimit_ReturnsTrue() {
        // Arrange
        when(rateLimitRepository.findByUserIdAndTier(anyLong(), any()))
            .thenReturn(Optional.of(createRateLimit(4))); // 4/5 requests used
            
        // Act & Assert
        assertTrue(aiGatewayService.checkRateLimit(testUser, AITier.MASTER));
    }
    
    @Test
    void checkRateLimit_WhenAtLimit_ReturnsFalse() {
        // Arrange
        when(rateLimitRepository.findByUserIdAndTier(anyLong(), any()))
            .thenReturn(Optional.of(createRateLimit(5))); // 5/5 requests used
            
        // Act & Assert
        assertFalse(aiGatewayService.checkRateLimit(testUser, AITier.MASTER));
    }
    
    @Test
    void generateSessionPrep_WithValidRequest_ReturnsSessionPrep() {
        // Arrange
        SessionPrepRequest request = new SessionPrepRequest();
        request.setMenteeQuery("How to prepare for a tech interview?");
        request.setMentorExpertise("Software Engineering");
        
        AIResponse aiResponse = AIResponse.builder()
                .response("Session prep content")
                .successful(true)
                .build();
                
        when(rateLimitRepository.findByUserIdAndTier(anyLong(), any()))
            .thenReturn(Optional.of(createRateLimit(0)));
            
        when(openAIService.generateResponse(anyString(), anyString(), anyString()))
            .thenReturn(aiResponse);
        
        // Act
        SessionPrepResponse response = aiGatewayService.generateSessionPrep(testUser, request);
        
        // Assert
        assertNotNull(response);
        assertFalse(response.getTalkingPoints().isEmpty());
    }
    
    private AIRateLimit createRateLimit(int requestCount) {
        AIRateLimit rateLimit = new AIRateLimit();
        rateLimit.setRequestCount(requestCount);
        rateLimit.setWindowStart(LocalDateTime.now());
        rateLimit.setWindowEnd(LocalDateTime.now().plusHours(1));
        return rateLimit;
    }
}
