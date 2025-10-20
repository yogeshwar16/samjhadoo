package com.samjhadoo.config;

import com.samjhadoo.service.ai.OpenAIService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public OpenAIService testOpenAIService() {
        return Mockito.mock(OpenAIService.class);
    }
    
    @Bean
    @Primary
    public RetryTemplate testRetryTemplate() {
        return new RetryTemplate();
    }
}
