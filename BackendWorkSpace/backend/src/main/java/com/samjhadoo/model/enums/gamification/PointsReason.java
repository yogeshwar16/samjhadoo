package com.samjhadoo.model.enums.gamification;

/**
 * Reasons for awarding or deducting points in the gamification system.
 * Each value maps to a specific user action or achievement.
 */
public enum PointsReason {
    // Session-related points
    SESSION_COMPLETED_AS_MENTEE(50),
    SESSION_COMPLETED_AS_MENTOR(50),
    SESSION_CANCELLED(-20),
    
    // Rating-related points
    GAVE_FEEDBACK(10),
    RECEIVED_5_STAR(20),
    
    // Community engagement
    POSTED_QUESTION(5),
    POSTED_ANSWER(10),
    ANSWER_ACCEPTED(15),
    
    // Profile completion
    PROFILE_PICTURE_ADDED(10),
    BIO_COMPLETED(10),
    SKILLS_ADDED(5),
    
    // Referral program
    REFERRAL_SIGNUP(100),
    REFERRAL_COMPLETED_SESSION(200),
    
    // Streaks
    DAILY_LOGIN(5),
    WEEKLY_STREAK(25),
    MONTHLY_STREAK(100),
    
    // Achievements
    BADGE_EARNED(50),
    ACHIEVEMENT_UNLOCKED(100),
    
    // Manual adjustments
    ADMIN_GRANT(0),  // Value set by admin
    SYSTEM_ADJUSTMENT(0);  // For system corrections

    private final int defaultPoints;

    PointsReason(int defaultPoints) {
        this.defaultPoints = defaultPoints;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }
}
