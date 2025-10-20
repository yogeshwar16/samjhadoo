package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkQueueDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkSessionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkQueue;
import com.samjhadoo.model.enums.friendlytalk.MoodType;

import java.util.List;

/**
 * Service for managing the friendly talk queue and matching users.
 */
public interface QueueService {

    /**
     * Adds a user to the talk queue.
     * @param user The user to add
     * @param moodType The user's current mood
     * @param intensity Mood intensity
     * @param anonymous Whether the user wants anonymous matching
     * @param preferredTopics Preferred conversation topics
     * @param avoidTopics Topics to avoid
     * @param maxWaitMinutes Maximum wait time in minutes
     * @return The queue entry DTO
     */
    FriendlyTalkQueueDTO joinQueue(User user, MoodType moodType, int intensity,
                                  boolean anonymous, String preferredTopics,
                                  String avoidTopics, int maxWaitMinutes);

    /**
     * Removes a user from the queue.
     * @param user The user to remove
     * @return true if removed successfully
     */
    boolean leaveQueue(User user);

    /**
     * Gets a user's queue status.
     * @param user The user
     * @return The queue DTO or null if not in queue
     */
    FriendlyTalkQueueDTO getUserQueueStatus(User user);

    /**
     * Attempts to find a match for a user in the queue.
     * @param user The user to match
     * @return The matched session DTO or null if no match found
     */
    FriendlyTalkSessionDTO findMatch(User user);

    /**
     * Processes the matching queue and attempts to match waiting users.
     * @return Number of matches created
     */
    int processQueueMatching();

    /**
     * Gets all active queue entries.
     * @param limit Maximum number of entries to return
     * @return List of active queue DTOs
     */
    List<FriendlyTalkQueueDTO> getActiveQueue(int limit);

    /**
     * Gets queue entries by mood type.
     * @param moodType The mood type
     * @param limit Maximum number of entries to return
     * @return List of queue DTOs with the specified mood
     */
    List<FriendlyTalkQueueDTO> getQueueByMoodType(MoodType moodType, int limit);

    /**
     * Gets high intensity queue entries.
     * @param minIntensity Minimum intensity threshold
     * @param limit Maximum number of entries to return
     * @return List of high intensity queue DTOs
     */
    List<FriendlyTalkQueueDTO> getHighIntensityQueue(int minIntensity, int limit);

    /**
     * Expires old queue entries.
     * @return Number of entries expired
     */
    int expireOldQueueEntries();

    /**
     * Gets queue statistics.
     * @return Map of queue statistics
     */
    java.util.Map<String, Object> getQueueStatistics();

    /**
     * Calculates estimated wait time for a new queue entry.
     * @param moodType The mood type
     * @param intensity The intensity level
     * @return Estimated wait time in minutes
     */
    int calculateEstimatedWaitTime(MoodType moodType, int intensity);

    /**
     * Checks if two queue entries are compatible for matching.
     * @param queue1 First queue entry
     * @param queue2 Second queue entry
     * @return true if compatible
     */
    boolean areCompatibleForMatching(FriendlyTalkQueue queue1, FriendlyTalkQueue queue2);

    /**
     * Gets queue entries that need retry matching.
     * @return List of queue DTOs that need retry
     */
    List<FriendlyTalkQueueDTO> getQueueNeedingRetry();

    /**
     * Updates matching criteria for a queue entry.
     * @param user The user
     * @param preferredTopics New preferred topics
     * @param avoidTopics New topics to avoid
     * @return true if updated successfully
     */
    boolean updateMatchingCriteria(User user, String preferredTopics, String avoidTopics);

    /**
     * Cancels a queue entry.
     * @param user The user
     * @return true if cancelled successfully
     */
    boolean cancelQueueEntry(User user);

    /**
     * Gets recently matched queue entries for analytics.
     * @param limit Maximum number of entries to return
     * @return List of recently matched queue DTOs
     */
    List<FriendlyTalkQueueDTO> getRecentlyMatched(int limit);
}
