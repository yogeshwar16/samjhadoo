package com.samjhadoo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
public class AIMatchingConfig {

    // Cache Manager is provided by CacheConfig
    // AI matching specific caches: mentorMatches, mentorProfiles, matchPreferences, recommendedSkills
    
    // @Bean
    // public AIMatchingServiceV2 aiMatchingServiceV2() {
    //     return new AIMatchingServiceImplV2();
    // }
}
