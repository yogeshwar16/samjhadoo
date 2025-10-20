package com.samjhadoo.service.ai;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.MatchPreference;
import com.samjhadoo.model.enums.SessionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enhanced AI Matching Service with additional capabilities for mentor-mentee matching
 */
public interface AIMatchingServiceV2 {

    // Core Matching Methods
    
    /**
     * Find matching mentors based on user preferences and requirements
     */
    List<MentorMatchResult> findMatchingMentors(User user, Map<String, Object> preferences, 
                                              SessionType sessionType, int limit);
    
    /**
     * Calculate compatibility score between a user and mentor
     */
    double calculateMatchScore(User user, MentorProfile mentor, 
                             Map<String, Object> preferences, SessionType sessionType);
    
    // Enhanced Matching Capabilities
    
    /**
     * Count potential matches based on user preferences
     */
    long countPotentialMatches(User user);
    
    /**
     * Get recommended skills for a user based on their profile and goals
     */
    Set<String> getRecommendedSkills(User user);
    
    /**
     * Get popular mentors that might be a good match
     */
    List<MentorMatchResult> getPopularMentors(User user, int limit);
    
    /**
     * Get personalized match explanations
     */
    String getMatchExplanation(User user, MentorProfile mentor, SessionType sessionType);
    
    /**
     * Get match quality score for a mentor-mentee pair
     */
    MatchQualityScore getMatchQuality(User user, MentorProfile mentor, SessionType sessionType);
    
    // Batch Operations
    
    /**
     * Batch process match scores for multiple mentor-mentee pairs
     */
    Map<String, Double> batchCalculateMatchScores(User user, List<MentorProfile> mentors, 
                                                 SessionType sessionType);
    
    // Preference-based Matching
    
    /**
     * Find matches based on saved preferences
     */
    List<MentorMatchResult> findMatchesUsingPreferences(String userId);
    
    /**
     * Update matching algorithm weights
     */
    void updateMatchingWeights(Map<String, Double> newWeights);
    
    // Result Classes
    
    class MentorMatchResult {
        private final MentorProfile mentorProfile;
        private final double matchScore;
        private final String explanation;
        private final Map<String, Double> scoreBreakdown;

        public MentorMatchResult(MentorProfile mentorProfile, double matchScore, 
                               String explanation, Map<String, Double> scoreBreakdown) {
            this.mentorProfile = mentorProfile;
            this.matchScore = matchScore;
            this.explanation = explanation;
            this.scoreBreakdown = scoreBreakdown;
        }

        public MentorProfile getMentorProfile() { return mentorProfile; }
        public double getMatchScore() { return matchScore; }
        public String getExplanation() { return explanation; }
        public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    }
    
    class MatchQualityScore {
        private final double overallScore;
        private final Map<String, Double> dimensionScores; // e.g., "skills": 0.8, "experience": 0.6
        private final List<String> strengths;
        private final List<String> areasForImprovement;
        
        public MatchQualityScore(double overallScore, Map<String, Double> dimensionScores,
                               List<String> strengths, List<String> areasForImprovement) {
            this.overallScore = overallScore;
            this.dimensionScores = dimensionScores;
            this.strengths = strengths;
            this.areasForImprovement = areasForImprovement;
        }
        
        // Getters
        public double getOverallScore() { return overallScore; }
        public Map<String, Double> getDimensionScores() { return dimensionScores; }
        public List<String> getStrengths() { return strengths; }
        public List<String> getAreasForImprovement() { return areasForImprovement; }
    }
}
