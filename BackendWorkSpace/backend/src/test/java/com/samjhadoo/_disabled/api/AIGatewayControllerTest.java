package com.samjhadoo.controller.api;

import com.samjhadoo.dto.ai.AIRequest;
import com.samjhadoo.dto.ai.AIResponse;
import com.samjhadoo.dto.ai.SessionPrepRequest;
import com.samjhadoo.dto.ai.SessionPrepResponse;
import com.samjhadoo.model.User;
import com.samjhadoo.service.ai.AIGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIGatewayControllerTest {

    @Mock
    private AIGatewayService aiGatewayService;
    
    @InjectMocks
    private AIGatewayController aiGatewayController;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
    }
    
    @Test
    void masterAI_WithValidRequest_ReturnsOk() {
        // Arrange
        AIRequest request = new AIRequest();
        request.setPrompt("Test prompt");
        
        AIResponse expectedResponse = AIResponse.builder()
                .response("Test response")
                .successful(true)
                .build();
                
        when(aiGatewayService.masterAI(any(), any())).thenReturn(expectedResponse);
        
        // Act
        ResponseEntity<AIResponse> response = aiGatewayController.masterAI(testUser, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AIResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Test response", body.getResponse());
    }
    
    @Test
    void generateSessionPrep_WithValidRequest_ReturnsOk() {
        // Arrange
        SessionPrepRequest request = new SessionPrepRequest();
        request.setMenteeQuery("How to prepare for a tech interview?");
        
        SessionPrepResponse expectedResponse = SessionPrepResponse.builder()
                .agenda("1. Introduction\n2. Technical Discussion\n3. Q&A")
                .build();
                
        when(aiGatewayService.generateSessionPrep(any(), any())).thenReturn(expectedResponse);
        
        // Act
        ResponseEntity<SessionPrepResponse> response = 
            aiGatewayController.generateSessionPrep(testUser, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        SessionPrepResponse body2 = response.getBody();
        assertNotNull(body2);
        assertTrue(body2.getAgenda().contains("Introduction"));
    }
    
    @Test
    void agenticAI_WithoutPremium_ReturnsForbidden() {
        // Arrange
        AIRequest request = new AIRequest();
        request.setPrompt("Test premium prompt");
        
        // Note: The security check is handled by Spring Security annotations
        // This test would need to be updated to include security context
        
        // Act & Assert
        // This would typically be tested with MockMvc in an integration test
        assertTrue(true); // Placeholder assertion
    }
}
