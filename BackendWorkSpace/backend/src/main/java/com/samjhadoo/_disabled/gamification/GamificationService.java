package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.UserProfileDTO;
import com.samjhadoo.dto.gamification.LeaderboardDTO;
import com.samjhadoo.dto.gamification.PointsTransactionDTO;
import com.samjhadoo.model.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main service for gamification operations.
 * Coordinates all gamification features including badges, achievements, points, streaks, and referrals.
 */
public interface GamificationService {

    /**
     * Gets the complete gamification profile for a user.
     * @param user The user to get profile for
     * @return Complete gamification profile
     */
    UserProfileDTO getUserProfile(User user);

    /**
     * Awards points to a user for a specific reason.
     * @param user The user to award points to
     * @param amount The amount of points to award
     * @param reason The reason for awarding points
     * @param referenceId Optional reference ID for the source (e.g., session ID)
     * @param description Optional description
     * @return The updated points balance
     */
    BigDecimal awardPoints(User user, BigDecimal amount, com.samjhadoo.model.enums.gamification.PointsReason reason,
                          String referenceId, String description);

    /**
     * Deducts points from a user.
     * @param user The user to deduct points from
     * @param amount The amount of points to deduct
     * @param reason The reason for deducting points
     * @param referenceId Optional reference ID
     * @param description Optional description
     * @return The updated points balance
     */
    BigDecimal deductPoints(User user, BigDecimal amount, com.samjhadoo.model.enums.gamification.PointsReason reason,
                           String referenceId, String description);

    /**
     * Updates user streak for a login activity.
     * @param user The user who logged in
     * @return true if streak was updated, false if already updated today
     */
    boolean updateStreak(User user);

    /**
     * Processes an achievement for a user (increments progress and checks for completion).
     * @param user The user
     * @param achievementType The type of achievement
     * @param increment The amount to increment progress by
     * @return true if achievement was completed, false otherwise
     */
    boolean processAchievement(User user, com.samjhadoo.model.enums.gamification.AchievementType achievementType, int increment);

    /**
     * Awards a badge to a user.
     * @param user The user to award badge to
     * @param badgeId The badge to award
     * @param reason Reason for awarding the badge
     * @return true if badge was awarded, false if already owned
     */
    boolean awardBadge(User user, Long badgeId, String reason);

    /**
     * Gets leaderboard for points.
     * @param limit Maximum number of entries
     * @return List of leaderboard entries ordered by points
     */
    List<LeaderboardDTO> getPointsLeaderboard(int limit);

    /**
     * Gets leaderboard for streaks.
     * @param limit Maximum number of entries
     * @return List of leaderboard entries ordered by current streak
     */
    List<LeaderboardDTO> getStreakLeaderboard(int limit);

    /**
     * Gets leaderboard for badges earned.
     * @param limit Maximum number of entries
     * @return List of leaderboard entries ordered by badge count
     */
    List<LeaderboardDTO> getBadgeLeaderboard(int limit);

    /**
     * Gets points transaction history for a user.
     * @param user The user
     * @param limit Maximum number of transactions to return
     * @return List of recent transactions
     */
    List<PointsTransactionDTO> getPointsHistory(User user, int limit);
}
