package com.samjhadoo.service.ai;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.MatchPreference;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.repository.MatchPreferenceRepository;
import com.samjhadoo.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIMatchingServiceImplV2 implements AIMatchingServiceV2 {

    private final MentorProfileRepository mentorProfileRepository;
    private final MatchPreferenceRepository matchPreferenceRepository;
    
    // Default weights for matching algorithm
    private Map<String, Double> matchingWeights = Map.of(
        "skills", 0.4,
        "experience", 0.3,
        "availability", 0.15,
        "rating", 0.1,
        "responseTime", 0.05
    );

    @Override
    @Cacheable(value = "mentorMatches", key = "#user.id + '-' + #sessionType + '-' + #limit")
    public List<MentorMatchResult> findMatchingMentors(User user, Map<String, Object> preferences, 
                                                     SessionType sessionType, int limit) {
        List<MentorProfile> mentors = mentorProfileRepository.findByIsActiveTrue();
        
        return mentors.parallelStream()
            .map(mentor -> {
                double score = calculateMatchScore(user, mentor, preferences, sessionType);
                String explanation = generateMatchExplanation(user, mentor, sessionType);
                Map<String, Double> scoreBreakdown = calculateScoreBreakdown(user, mentor, preferences, sessionType);
                return new MentorMatchResult(mentor, score, explanation, scoreBreakdown);
            })
            .filter(result -> result.getMatchScore() > 0)
            .sorted(Comparator.comparingDouble(MentorMatchResult::getMatchScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public double calculateMatchScore(User user, MentorProfile mentor, 
                                    Map<String, Object> preferences, SessionType sessionType) {
        Map<String, Double> scoreBreakdown = calculateScoreBreakdown(user, mentor, preferences, sessionType);
        return scoreBreakdown.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    private Map<String, Double> calculateScoreBreakdown(User user, MentorProfile mentor, 
                                                      Map<String, Object> preferences, SessionType sessionType) {
        Map<String, Double> breakdown = new HashMap<>();
        
        // Skill matching
        Set<String> requiredSkills = (Set<String>) preferences.getOrDefault("skills", Set.of());
        if (!requiredSkills.isEmpty()) {
            double skillScore = calculateSkillMatchScore(mentor.getSkills(), requiredSkills);
            breakdown.put("skills", skillScore * matchingWeights.get("skills"));
        }
        
        // Experience matching
        int minExperience = (int) preferences.getOrDefault("minExperience", 0);
        double experienceScore = calculateExperienceScore(mentor.getExperience(), minExperience);
        breakdown.put("experience", experienceScore * matchingWeights.get("experience"));
        
        // Add other scoring factors...
        
        return breakdown;
    }
    
    private double calculateSkillMatchScore(Set<String> mentorSkills, Set<String> requiredSkills) {
        if (mentorSkills == null || mentorSkills.isEmpty()) return 0;
        
        long matchingSkills = requiredSkills.stream()
            .filter(mentorSkills::contains)
            .count();
            
        return (double) matchingSkills / requiredSkills.size();
    }
    
    private double calculateExperienceScore(String experience, int minRequired) {
        if (experience == null || experience.trim().isEmpty()) return 0;
        
        try {
            int years = Integer.parseInt(experience.split("\\s+")[0]);
            return Math.min(1.0, (double) years / Math.max(1, minRequired));
        } catch (NumberFormatException e) {
            log.warn("Invalid experience format: {}", experience);
            return 0;
        }
    }
    
    private String generateMatchExplanation(User user, MentorProfile mentor, SessionType sessionType) {
        // Generate a human-readable explanation of the match
        List<String> reasons = new ArrayList<>();
        
        if (mentor.getSkills() != null && !mentor.getSkills().isEmpty()) {
            String skills = String.join(", ", mentor.getSkills());
            reasons.add("Skills: " + skills);
        }
        
        if (mentor.getExperience() != null && !mentor.getExperience().isEmpty()) {
            reasons.add("Experience: " + mentor.getExperience());
        }
        
        return String.join(" | ", reasons);
    }
    
    // Implement other interface methods with appropriate implementations
    
    @Override
    public long countPotentialMatches(User user) {
        // Implementation for counting potential matches
        return mentorProfileRepository.countByIsActiveTrue();
    }
    
    @Override
    public Set<String> getRecommendedSkills(User user) {
        // Implementation for skill recommendations
        return Set.of("Java", "Spring Boot", "Microservices");
    }
    
    @Override
    public List<MentorMatchResult> getPopularMentors(User user, int limit) {
        // Implementation for popular mentors
        return Collections.emptyList();
    }
    
    @Override
    public String getMatchExplanation(User user, MentorProfile mentor, SessionType sessionType) {
        return generateMatchExplanation(user, mentor, sessionType);
    }
    
    @Override
    public MatchQualityScore getMatchQuality(User user, MentorProfile mentor, SessionType sessionType) {
        // Implementation for match quality score
        return new MatchQualityScore(
            0.8,
            Map.of("skills", 0.9, "experience", 0.7, "availability", 0.8),
            List.of("Strong skill match", "Good availability"),
            List.of("Could improve response time")
        );
    }
    
    @Override
    public Map<String, Double> batchCalculateMatchScores(User user, List<MentorProfile> mentors, 
                                                       SessionType sessionType) {
        return mentors.stream()
            .collect(Collectors.toMap(
                MentorProfile::getId,
                mentor -> calculateMatchScore(user, mentor, Collections.emptyMap(), sessionType)
            ));
    }
    
    @Override
    @Async
    public CompletableFuture<List<MentorMatchResult>> findMatchesUsingPreferences(String userId) {
        // Async implementation for preference-based matching
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
    
    @Override
    public void updateMatchingWeights(Map<String, Double> newWeights) {
        this.matchingWeights = new HashMap<>(newWeights);
    }
}
