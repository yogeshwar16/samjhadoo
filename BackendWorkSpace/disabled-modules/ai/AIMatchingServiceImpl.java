package com.samjhadoo.service.ai;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIMatchingServiceImpl implements AIMatchingService {

    private final MentorProfileRepository mentorProfileRepository;
    
    // Weights for different matching criteria (sum should be 1.0)
    private static final double SKILL_WEIGHT = 0.4;
    private static final double EXPERIENCE_WEIGHT = 0.3;
    private static final double AVAILABILITY_WEIGHT = 0.2;
    private static final double RATING_WEIGHT = 0.1;

    @Override
    public List<MentorMatchResult> findMatchingMentors(User user, Map<String, Object> preferences, 
                                                     SessionType sessionType, int limit) {
        // Get all active mentors
        List<MentorProfile> allMentors = mentorProfileRepository.findByIsActiveTrue();
        
        // Calculate match scores for each mentor
        return allMentors.stream()
                .map(mentor -> {
                    double score = calculateMatchScore(user, mentor, preferences, sessionType);
                    String explanation = getMatchExplanation(user, mentor, sessionType);
                    return new MentorMatchResult(mentor, score, explanation);
                })
                .filter(result -> result.getMatchScore() > 0) // Filter out zero-score matches
                .sorted(Comparator.comparingDouble(MentorMatchResult::getMatchScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateMatchScore(User user, MentorProfile mentor, 
                                    Map<String, Object> preferences, SessionType sessionType) {
        double score = 0.0;
        
        // 1. Skill matching (40% weight)
        Set<String> requiredSkills = (Set<String>) preferences.getOrDefault("skills", new HashSet<String>());
        if (!requiredSkills.isEmpty()) {
            Set<String> mentorSkills = mentor.getSkills();
            long matchingSkills = requiredSkills.stream()
                    .filter(mentorSkills::contains)
                    .count();
            score += SKILL_WEIGHT * (double) matchingSkills / requiredSkills.size();
        }
        
        // 2. Experience level (30% weight)
        int minExperience = (int) preferences.getOrDefault("minExperience", 0);
        if (mentor.getExperience() != null && mentor.getExperience().length() > 0) {
            try {
                int mentorExp = Integer.parseInt(mentor.getExperience().split("\s+")[0]);
                score += EXPERIENCE_WEIGHT * Math.min(1.0, (double) mentorExp / Math.max(1, minExperience));
            } catch (NumberFormatException e) {
                log.warn("Invalid experience format for mentor: {}", mentor.getId());
            }
        }
        
        // 3. Availability (20% weight)
        // This is a simplified version - in real implementation, check actual availability slots
        score += AVAILABILITY_WEIGHT * 0.8; // Assuming 80% availability by default
        
        // 4. Rating (10% weight)
        // In a real implementation, fetch the mentor's average rating
        score += RATING_WEIGHT * 4.5 / 5.0; // Assuming 4.5/5 rating by default
        
        return Math.min(1.0, score); // Cap at 1.0
    }

    @Override
    public String getMatchExplanation(User user, MentorProfile mentor, SessionType sessionType) {
        List<String> reasons = new ArrayList<>();
        
        // Add skill-based explanation
        if (mentor.getSkills() != null && !mentor.getSkills().isEmpty()) {
            String skills = String.join(", ", mentor.getSkills());
            reasons.add("Skills: " + skills);
        }
        
        // Add experience-based explanation
        if (mentor.getExperience() != null && !mentor.getExperience().isEmpty()) {
            reasons.add("Experience: " + mentor.getExperience());
        }
        
        // Add session type specific explanation
        if (sessionType == SessionType.FRIENDLY_TALK) {
            reasons.add("Specializes in friendly conversations and support");
        } else {
            reasons.add("Professional mentor with expertise in their field");
        }
        
        return String.join(" | ", reasons);
    }
}
