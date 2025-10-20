package com.samjhadoo.service.ai;

import com.samjhadoo.config.AIConfiguration;
import com.samjhadoo.dto.ai.AIResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final AIConfiguration aiConfig;
    private OpenAiService openAiService;
    
    @PostConstruct
    public void init() {
        this.openAiService = new OpenAiService(
            aiConfig.getOpenai().getApiKey(),
            Duration.ofSeconds(aiConfig.getOpenai().getTimeoutSeconds())
        );
    }

    @Retryable(
        value = { Exception.class },
        maxAttemptsExpression = "${ai.retry.max-attempts}",
        backoff = @Backoff(
            delayExpression = "${ai.retry.backoff-ms}",
            multiplierExpression = "${ai.retry.multiplier}"
        )
    )
    public AIResponse generateResponse(String model, String systemPrompt, String userPrompt) {
        try {
            List<ChatMessage> messages = List.of(
                new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userPrompt)
            );

            var completionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(aiConfig.getOpenai().getTemperature())
                .maxTokens(aiConfig.getOpenai().getMaxTokens())
                .build();

            var completion = openAiService.createChatCompletion(completionRequest);
            var message = completion.getChoices().get(0).getMessage();
            var usage = completion.getUsage();

            return AIResponse.builder()
                .response(message.getContent())
                .successful(true)
                .model(model)
                .promptTokens(usage.getPromptTokens())
                .completionTokens(usage.getCompletionTokens())
                .totalTokens(usage.getTotalTokens())
                .build();

        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
            return AIResponse.builder()
                .successful(false)
                .errorMessage("Failed to generate response: " + e.getMessage())
                .build();
        }
    }
}
