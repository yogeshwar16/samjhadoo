package com.samjhadoo.model.enums.gamification;

/**
 * Types of achievements users can earn in the platform.
 * Each type represents a category of achievements with specific progression rules.
 */
public enum AchievementType {
    SESSIONS_COMPLETED,     // Track total sessions completed (e.g., 10, 50, 100)
    MENTORING_HOURS,        // Track total hours spent mentoring
    FIVE_STAR_RATINGS,      // Number of 5-star ratings received
    COMMUNITY_POSTS,        // Number of forum posts/answers
    REFERRALS_SENT,         // Number of successful referrals
    REFERRALS_ACCEPTED,     // Number of referred users who signed up
    STREAK_DAYS,            // Consecutive days active
    PROFILE_COMPLETION,     // Completing profile sections
    EARLY_ADOPTER,          // For users who join before a certain date
    TOP_MENTOR,             // Top-rated mentor of the month
    TOP_LEARNER             // Most active learner of the month
}
