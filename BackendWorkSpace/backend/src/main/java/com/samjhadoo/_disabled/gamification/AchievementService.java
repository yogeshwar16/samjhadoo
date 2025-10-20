package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.AchievementDTO;
import com.samjhadoo.dto.gamification.UserAchievementDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Achievement;
import com.samjhadoo.model.enums.gamification.AchievementType;

import java.util.List;

/**
 * Service for managing achievements and user progress.
 */
public interface AchievementService {

    /**
     * Gets all active achievements.
     * @return List of active achievements
     */
    List<AchievementDTO> getAllActiveAchievements();

    /**
     * Gets achievements by type.
     * @param type The achievement type
     * @return List of achievements of the specified type
     */
    List<AchievementDTO> getAchievementsByType(AchievementType type);

    /**
     * Gets an achievement by ID.
     * @param achievementId The achievement ID
     * @return The achievement DTO or null if not found
     */
    AchievementDTO getAchievementById(Long achievementId);

    /**
     * Gets all achievements for a user (both completed and in progress).
     * @param user The user
     * @return List of user achievements
     */
    List<UserAchievementDTO> getUserAchievements(User user);

    /**
     * Gets achievements in progress for a user.
     * @param user The user
     * @return List of in-progress achievements
     */
    List<UserAchievementDTO> getUserAchievementsInProgress(User user);

    /**
     * Gets completed achievements for a user.
     * @param user The user
     * @return List of completed achievements
     */
    List<UserAchievementDTO> getUserAchievementsCompleted(User user);

    /**
     * Processes progress for an achievement type.
     * @param user The user
     * @param achievementType The achievement type
     * @param increment The amount to increment progress by
     * @return true if achievement was completed, false otherwise
     */
    boolean processAchievementProgress(User user, AchievementType achievementType, int increment);

    /**
     * Manually awards an achievement to a user (admin function).
     * @param user The user
     * @param achievementId The achievement ID
     * @return true if awarded, false if already completed
     */
    boolean awardAchievementToUser(User user, Long achievementId);

    /**
     * Gets achievements ready for completion check.
     * @return List of user achievements that may have reached their threshold
     */
    List<UserAchievementDTO> getAchievementsReadyForCompletion();

    /**
     * Creates a new achievement (admin function).
     * @param achievementDTO The achievement data
     * @return The created achievement
     */
    Achievement createAchievement(AchievementDTO achievementDTO);

    /**
     * Updates an existing achievement (admin function).
     * @param achievementId The achievement ID
     * @param achievementDTO The updated achievement data
     * @return The updated achievement
     */
    Achievement updateAchievement(Long achievementId, AchievementDTO achievementDTO);

    /**
     * Deactivates an achievement (admin function).
     * @param achievementId The achievement ID
     * @return true if deactivated successfully
     */
    boolean deactivateAchievement(Long achievementId);

    /**
     * Gets the count of users who have completed a specific achievement.
     * @param achievementId The achievement ID
     * @return Count of users who completed the achievement
     */
    long getAchievementUserCount(Long achievementId);

    /**
     * Resets progress for a repeatable achievement (admin function).
     * @param user The user
     * @param achievementId The achievement ID
     * @return true if reset successfully
     */
    boolean resetAchievementProgress(User user, Long achievementId);
}
