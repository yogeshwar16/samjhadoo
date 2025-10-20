package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.UserStreakDTO;
import com.samjhadoo.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing user streaks and login tracking.
 */
public interface StreakService {

    /**
     * Updates the streak for a user based on login activity.
     * @param user The user who logged in
     * @return true if streak was updated, false if already updated today
     */
    boolean updateStreak(User user);

    /**
     * Gets the current streak information for a user.
     * @param user The user
     * @return The user's streak information
     */
    UserStreakDTO getUserStreak(User user);

    /**
     * Gets users with active streaks.
     * @param limit Maximum number of users to return
     * @return List of users with active streaks ordered by streak length
     */
    List<UserStreakDTO> getUsersWithActiveStreaks(int limit);

    /**
     * Gets users with the longest streaks.
     * @param limit Maximum number of users to return
     * @return List of users ordered by maximum streak days
     */
    List<UserStreakDTO> getUsersWithLongestStreaks(int limit);

    /**
     * Gets users with the most total logins.
     * @param limit Maximum number of users to return
     * @return List of users ordered by total logins
     */
    List<UserStreakDTO> getUsersWithMostLogins(int limit);

    /**
     * Checks if a user has an active streak today.
     * @param user The user
     * @return true if user logged in today and streak is active
     */
    boolean hasActiveStreakToday(User user);

    /**
     * Gets the date of the last login for a user.
     * @param user The user
     * @return The last login date, or null if no login history
     */
    LocalDate getLastLoginDate(User user);

    /**
     * Manually resets a user's streak (admin function).
     * @param user The user whose streak to reset
     * @param reason Reason for the reset
     * @return true if reset successfully
     */
    boolean resetUserStreak(User user, String reason);

    /**
     * Gets streak statistics for all users.
     * @return Map of streak statistics
     */
    java.util.Map<String, Object> getStreakStatistics();

    /**
     * Awards points for streak milestones.
     * @param user The user
     * @param streakDays The current streak days
     * @return Points awarded for the streak milestone, or 0 if no milestone reached
     */
    int awardStreakMilestonePoints(User user, int streakDays);

    /**
     * Gets users who need streak milestone rewards.
     * @return List of users eligible for streak milestone rewards
     */
    List<UserStreakDTO> getUsersForStreakMilestoneRewards();
}
