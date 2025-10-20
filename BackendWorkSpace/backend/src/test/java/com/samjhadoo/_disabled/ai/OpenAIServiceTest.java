package com.samjhadoo.service.ai;

import com.samjhadoo.config.AIConfiguration;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {

    @Mock
    private AIConfiguration aiConfig;
    
    @Mock
    private AIConfiguration.OpenAIConfig openAIConfig;
    
    @Mock
    private OpenAiService openAiService;
    
    @InjectMocks
    private OpenAIService openAIService;
    
    @BeforeEach
    void setUp() {
        when(aiConfig.getOpenai()).thenReturn(openAIConfig);
        when(openAIConfig.getApiKey()).thenReturn("test-api-key");
        when(openAIConfig.getTimeoutSeconds()).thenReturn(30);
        when(openAIConfig.getTemperature()).thenReturn(0.7);
        when(openAIConfig.getMaxTokens()).thenReturn(1000);
    }
    
    @Test
    void generateResponse_WithValidInput_ReturnsResponse() {
        // Arrange
        String model = "gpt-4";
        String systemPrompt = "You are a helpful assistant.";
        String userPrompt = "Hello, how are you?";
        
        // Mock the OpenAiService response
        com.theokanning.openai.completion.chat.ChatCompletionResult mockResult = 
            new com.theokanning.openai.completion.chat.ChatCompletionResult();
        
        com.theokanning.openai.completion.chat.ChatCompletionChoice choice = 
            new com.theokanning.openai.completion.chat.ChatCompletionChoice();
        
        com.theokanning.openai.completion.chat.ChatMessage message = 
            new com.theokanning.openai.completion.chat.ChatMessage("assistant", "I'm doing well, thank you!");
        
        choice.setMessage(message);
        mockResult.setChoices(List.of(choice));
        
        com.theokanning.openai.Usage usage = new com.theokanning.openai.Usage();
        usage.setPromptTokens(10);
        usage.setCompletionTokens(20);
        usage.setTotalTokens(30);
        mockResult.setUsage(usage);
        
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
            .thenReturn(mockResult);
        
        // Act
        var response = openAIService.generateResponse(model, systemPrompt, userPrompt);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals("I'm doing well, thank you!", response.getResponse());
        assertEquals(10, response.getPromptTokens());
        assertEquals(20, response.getCompletionTokens());
        assertEquals(30, response.getTotalTokens());
        
        // Verify the request was built correctly
        ArgumentCaptor<ChatCompletionRequest> requestCaptor = 
            ArgumentCaptor.forClass(ChatCompletionRequest.class);
        
        verify(openAiService).createChatCompletion(requestCaptor.capture());
        
        ChatCompletionRequest request = requestCaptor.getValue();
        assertEquals(model, request.getModel());
        assertEquals(2, request.getMessages().size());
        assertEquals("system", request.getMessages().get(0).getRole());
        assertEquals(systemPrompt, request.getMessages().get(0).getContent());
        assertEquals("user", request.getMessages().get(1).getRole());
        assertEquals(userPrompt, request.getMessages().get(1).getContent());
    }
    
    @Test
    void generateResponse_WhenOpenAIError_ReturnsErrorResponse() {
        // Arrange
        String model = "gpt-4";
        String systemPrompt = "You are a helpful assistant.";
        String userPrompt = "Hello, how are you?";
        
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
            .thenThrow(new RuntimeException("OpenAI API error"));
        
        // Act
        var response = openAIService.generateResponse(model, systemPrompt, userPrompt);
        
        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertTrue(response.getErrorMessage().contains("Failed to generate response"));
    }
}
