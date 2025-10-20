package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.MoodDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.Mood;
import com.samjhadoo.model.enums.friendlytalk.MoodType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user moods and mood-based matching.
 */
public interface MoodService {

    /**
     * Sets or updates a user's mood.
     * @param user The user
     * @param moodType The mood type
     * @param intensity Intensity level (1-10)
     * @param description Optional description
     * @param anonymous Whether the mood should be anonymous
     * @param lookingForTalk Whether the user is looking for a talk
     * @return The updated mood DTO
     */
    MoodDTO setUserMood(User user, MoodType moodType, int intensity, String description,
                       boolean anonymous, boolean lookingForTalk);

    /**
     * Gets a user's current mood.
     * @param user The user
     * @return The current mood DTO or null if no active mood
     */
    MoodDTO getUserMood(User user);

    /**
     * Clears a user's mood.
     * @param user The user
     * @return true if cleared successfully
     */
    boolean clearUserMood(User user);

    /**
     * Gets users looking for talk with similar moods.
     * @param user The user to match
     * @param limit Maximum number of matches to return
     * @return List of compatible mood DTOs
     */
    List<MoodDTO> findCompatibleMoods(User user, int limit);

    /**
     * Gets users with high intensity moods looking for talk.
     * @param minIntensity Minimum intensity threshold
     * @param limit Maximum number of results
     * @return List of high intensity mood DTOs
     */
    List<MoodDTO> findHighIntensityMoods(int minIntensity, int limit);

    /**
     * Gets all active moods looking for talk.
     * @param limit Maximum number of results
     * @return List of active mood DTOs
     */
    List<MoodDTO> getActiveMoodsLookingForTalk(int limit);

    /**
     * Gets moods by type for users looking for talk.
     * @param moodType The mood type
     * @param limit Maximum number of results
     * @return List of mood DTOs of the specified type
     */
    List<MoodDTO> getMoodsByType(MoodType moodType, int limit);

    /**
     * Expires old moods that are past their expiration time.
     * @return Number of moods expired
     */
    int expireOldMoods();

    /**
     * Gets mood statistics for analytics.
     * @return Map of mood statistics
     */
    java.util.Map<String, Object> getMoodStatistics();

    /**
     * Checks if a user's mood is compatible with another mood for matching.
     * @param mood1 First mood
     * @param mood2 Second mood
     * @return true if moods are compatible
     */
    boolean areMoodsCompatible(Mood mood1, Mood mood2);

    /**
     * Calculates a compatibility score between two moods.
     * @param mood1 First mood
     * @param mood2 Second mood
     * @return Compatibility score (0-100, higher is better)
     */
    int calculateMoodCompatibility(Mood mood1, Mood mood2);
}
