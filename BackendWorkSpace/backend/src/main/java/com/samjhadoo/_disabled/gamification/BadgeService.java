package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.BadgeDTO;
import com.samjhadoo.dto.gamification.UserBadgeDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Badge;
import com.samjhadoo.model.enums.gamification.BadgeType;

import java.util.List;

/**
 * Service for managing badges and user badge awards.
 */
public interface BadgeService {

    /**
     * Gets all active badges.
     * @return List of active badges
     */
    List<BadgeDTO> getAllActiveBadges();

    /**
     * Gets badges by type.
     * @param type The badge type
     * @return List of badges of the specified type
     */
    List<BadgeDTO> getBadgesByType(BadgeType type);

    /**
     * Gets a badge by ID.
     * @param badgeId The badge ID
     * @return The badge DTO or null if not found
     */
    BadgeDTO getBadgeById(Long badgeId);

    /**
     * Gets all badges earned by a user.
     * @param user The user
     * @return List of user badges
     */
    List<UserBadgeDTO> getUserBadges(User user);

    /**
     * Gets recent badges earned by a user.
     * @param user The user
     * @param limit Maximum number of badges to return
     * @return List of recent user badges
     */
    List<UserBadgeDTO> getRecentUserBadges(User user, int limit);

    /**
     * Awards a badge to a user.
     * @param user The user to award badge to
     * @param badgeId The badge ID
     * @param reason Reason for awarding
     * @return true if awarded, false if already owned
     */
    boolean awardBadgeToUser(User user, Long badgeId, String reason);

    /**
     * Checks if a user has a specific badge.
     * @param user The user
     * @param badgeId The badge ID
     * @return true if user has the badge
     */
    boolean userHasBadge(User user, Long badgeId);

    /**
     * Gets the count of users who have earned a specific badge.
     * @param badgeId The badge ID
     * @return Count of users with the badge
     */
    long getBadgeUserCount(Long badgeId);

    /**
     * Creates a new badge (admin function).
     * @param badgeDTO The badge data
     * @return The created badge
     */
    Badge createBadge(BadgeDTO badgeDTO);

    /**
     * Updates an existing badge (admin function).
     * @param badgeId The badge ID
     * @param badgeDTO The updated badge data
     * @return The updated badge
     */
    Badge updateBadge(Long badgeId, BadgeDTO badgeDTO);

    /**
     * Deactivates a badge (admin function).
     * @param badgeId The badge ID
     * @return true if deactivated successfully
     */
    boolean deactivateBadge(Long badgeId);

    /**
     * Gets unnotified badges for a user (for notification system).
     * @param user The user
     * @return List of unnotified badges
     */
    List<UserBadgeDTO> getUnnotifiedUserBadges(User user);

    /**
     * Marks badges as notified for a user.
     * @param user The user
     * @param badgeIds List of badge IDs to mark as notified
     */
    void markBadgesAsNotified(User user, List<Long> badgeIds);
}
