package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkRoomDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkSessionDTO;
import com.samjhadoo.dto.friendlytalk.MoodDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkQueueDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;

import java.util.List;
import java.util.Map;

/**
 * Main service for coordinating friendly talk features.
 */
public interface FriendlyTalkService {

    /**
     * Gets the overall friendly talk dashboard for a user.
     * @param user The user
     * @return Map containing user's mood, queue status, active sessions, and available rooms
     */
    Map<String, Object> getUserDashboard(User user);

    /**
     * Initiates a friendly talk request to another user.
     * @param initiator The user initiating the talk
     * @param receiver The user to talk to
     * @param moodType The initiator's mood
     * @param anonymous Whether the talk should be anonymous
     * @param topic The conversation topic
     * @return The created session DTO
     */
    FriendlyTalkSessionDTO initiateTalkRequest(User initiator, User receiver,
                                              MoodType moodType, boolean anonymous, String topic);

    /**
     * Accepts a talk request.
     * @param sessionId The session ID
     * @param receiver The user accepting
     * @return The updated session DTO
     */
    FriendlyTalkSessionDTO acceptTalkRequest(Long sessionId, User receiver);

    /**
     * Starts a talk session.
     * @param sessionId The session ID
     * @param user The user starting the session
     * @return The updated session DTO
     */
    FriendlyTalkSessionDTO startTalkSession(Long sessionId, User user);

    /**
     * Completes a talk session.
     * @param sessionId The session ID
     * @param user The user completing the session
     * @param satisfactionRating Rating (1-5)
     * @param feedback Optional feedback
     * @return The completed session DTO
     */
    FriendlyTalkSessionDTO completeTalkSession(Long sessionId, User user,
                                              int satisfactionRating, String feedback);

    /**
     * Cancels a talk request or session.
     * @param sessionId The session ID
     * @param user The user cancelling
     * @return true if cancelled successfully
     */
    boolean cancelTalkSession(Long sessionId, User user);

    /**
     * Reports a talk session for safety concerns.
     * @param sessionId The session ID
     * @param reporter The user reporting
     * @param reportType The type of report
     * @param severity The severity level
     * @param description Report description
     * @return true if reported successfully
     */
    boolean reportTalkSession(Long sessionId, User reporter,
                             com.samjhadoo.model.friendlytalk.SafetyReport.ReportType reportType,
                             com.samjhadoo.model.friendlytalk.SafetyReport.ReportSeverity severity,
                             String description);

    /**
     * Gets active talk sessions for a user.
     * @param user The user
     * @return List of active session DTOs
     */
    List<FriendlyTalkSessionDTO> getActiveSessions(User user);

    /**
     * Gets talk session history for a user.
     * @param user The user
     * @param limit Maximum number of sessions to return
     * @return List of session DTOs
     */
    List<FriendlyTalkSessionDTO> getSessionHistory(User user, int limit);

    /**
     * Processes mood-based matching and queue management.
     * @return Number of matches created
     */
    int processMoodMatching();

    /**
     * Gets friendly talk statistics.
     * @return Map of overall friendly talk statistics
     */
    Map<String, Object> getFriendlyTalkStatistics();

    /**
     * Expires old sessions and cleans up inactive data.
     * @return Number of items cleaned up
     */
    int cleanupOldData();

    /**
     * Gets emotional check-in suggestions for a user.
     * @param user The user
     * @return List of suggested mood check-in questions
     */
    List<String> getEmotionalCheckInSuggestions(User user);

    /**
     * Analyzes conversation content for safety (AI-powered).
     * @param content The conversation content
     * @return Safety analysis result
     */
    Map<String, Object> analyzeContentForSafety(String content);

    /**
     * Gets conversation starters based on mood compatibility.
     * @param mood1 First user's mood
     * @param mood2 Second user's mood
     * @return List of conversation starter suggestions
     */
    List<String> getConversationStarters(MoodType mood1, MoodType mood2);

    /**
     * Updates user mood and triggers matching if appropriate.
     * @param user The user
     * @param moodType The new mood
     * @param intensity The intensity level
     * @return The updated mood DTO
     */
    MoodDTO updateMoodAndTriggerMatching(User user, MoodType moodType, int intensity);

    /**
     * Gets users who might benefit from friendly talk based on their mood patterns.
     * @param limit Maximum number of users to return
     * @return List of users who might benefit from friendly talk
     */
    List<User> getUsersWhoMightBenefitFromTalk(int limit);

    /**
     * Gets the health status of the friendly talk system.
     * @return Map of system health metrics
     */
    Map<String, Object> getSystemHealth();
}
