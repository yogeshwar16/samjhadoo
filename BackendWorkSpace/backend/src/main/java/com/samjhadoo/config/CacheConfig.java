package com.samjhadoo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USER_PROFILES_CACHE = "userProfiles";
    public static final String MENTOR_PROFILES_CACHE = "mentorProfiles";
    public static final String SESSIONS_CACHE = "sessions";
    
    // AI Matching caches
    public static final String MENTOR_MATCHES_CACHE = "mentorMatches";
    public static final String MATCH_PREFERENCES_CACHE = "matchPreferences";
    public static final String RECOMMENDED_SKILLS_CACHE = "recommendedSkills";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(USER_PROFILES_CACHE, buildCache(15, TimeUnit.MINUTES, 1000));
        cacheManager.registerCustomCache(MENTOR_PROFILES_CACHE, buildCache(30, TimeUnit.MINUTES, 500));
        cacheManager.registerCustomCache(SESSIONS_CACHE, buildCache(5, TimeUnit.MINUTES, 2000));
        
        // AI Matching caches
        cacheManager.registerCustomCache(MENTOR_MATCHES_CACHE, buildCache(10, TimeUnit.MINUTES, 1000));
        cacheManager.registerCustomCache(MATCH_PREFERENCES_CACHE, buildCache(20, TimeUnit.MINUTES, 500));
        cacheManager.registerCustomCache(RECOMMENDED_SKILLS_CACHE, buildCache(60, TimeUnit.MINUTES, 200));
        
        return cacheManager;
    }

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildCache(
            long duration, TimeUnit timeUnit, long maxSize) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, timeUnit)
                .maximumSize(maxSize)
                .recordStats()
                .build();
    }
}
