package com.samjhadoo.service.ai;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;

import java.util.List;
import java.util.Map;

public interface AIMatchingService {
    /**
     * Find matching mentors based on user preferences and requirements
     * @param user The user looking for a mentor
     * @param preferences Map of preferences (e.g., skills, experience level, etc.)
     * @param sessionType Type of session (MENTORSHIP, FRIENDLY_TALK, etc.)
     * @param limit Maximum number of mentors to return
     * @return List of matching mentor profiles with matching scores
     */
    List<MentorMatchResult> findMatchingMentors(User user, Map<String, Object> preferences, 
                                              SessionType sessionType, int limit);

    /**
     * Calculate compatibility score between a user and mentor
     * @param user The user (mentee)
     * @param mentor The mentor
     * @param preferences User's preferences
     * @param sessionType Type of session
     * @return Matching score between 0 and 1 (1 being perfect match)
     */
    double calculateMatchScore(User user, MentorProfile mentor, 
                             Map<String, Object> preferences, SessionType sessionType);

    /**
     * Get match explanation for why a mentor was recommended
     * @param user The user (mentee)
     * @param mentor The mentor
     * @param sessionType Type of session
     * @return Human-readable explanation of the match
     */
    String getMatchExplanation(User user, MentorProfile mentor, SessionType sessionType);

    /**
     * Result class for mentor matching
     */
    class MentorMatchResult {
        private final MentorProfile mentorProfile;
        private final double matchScore;
        private final String explanation;

        public MentorMatchResult(MentorProfile mentorProfile, double matchScore, String explanation) {
            this.mentorProfile = mentorProfile;
            this.matchScore = matchScore;
            this.explanation = explanation;
        }

        public MentorProfile getMentorProfile() {
            return mentorProfile;
        }

        public double getMatchScore() {
            return matchScore;
        }

        public String getExplanation() {
            return explanation;
        }
    }
}
