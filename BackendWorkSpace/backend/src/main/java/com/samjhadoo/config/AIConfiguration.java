package com.samjhadoo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Getter
@Setter
public class AIConfiguration {
    private OpenAIConfig openai;
    private RateLimit rateLimit;
    private Cost cost;
    private Retry retry;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Getter
    @Setter
    public static class OpenAIConfig {
        private String apiKey;
        private Model model;
        private Double temperature;
        private Integer maxTokens;
        private Integer timeoutSeconds;
        
        @Getter
        @Setter
        public static class Model {
            private String master;
            private String agentic;
        }
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Integer master;
        private Integer agentic;
        private Duration windowDuration;
    }

    @Getter
    @Setter
    public static class Cost {
        private Double gpt4;
        private Double gpt35;
    }

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts;
        private long backoffMs;
        private double multiplier;
    }
}
