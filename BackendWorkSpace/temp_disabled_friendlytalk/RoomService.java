package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkRoomDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkRoom;

import java.util.List;

/**
 * Service for managing friendly talk rooms.
 */
public interface RoomService {

    /**
     * Creates a new friendly talk room.
     * @param creator The room creator
     * @param name Room name
     * @param description Room description
     * @param maxParticipants Maximum number of participants
     * @param anonymous Whether the room should be anonymous
     * @param topicTags Comma-separated topic tags
     * @param moodFocus Primary mood focus
     * @return The created room DTO
     */
    FriendlyTalkRoomDTO createRoom(User creator, String name, String description,
                                  int maxParticipants, boolean anonymous,
                                  String topicTags, String moodFocus);

    /**
     * Joins a room.
     * @param user The user joining
     * @param roomId The room ID
     * @return The updated room DTO
     */
    FriendlyTalkRoomDTO joinRoom(User user, String roomId);

    /**
     * Leaves a room.
     * @param user The user leaving
     * @param roomId The room ID
     * @return true if left successfully
     */
    boolean leaveRoom(User user, String roomId);

    /**
     * Gets a room by ID.
     * @param roomId The room ID
     * @param user The requesting user (for participant info)
     * @return The room DTO or null if not found
     */
    FriendlyTalkRoomDTO getRoom(String roomId, User user);

    /**
     * Gets all active rooms.
     * @param limit Maximum number of rooms to return
     * @return List of active room DTOs
     */
    List<FriendlyTalkRoomDTO> getActiveRooms(int limit);

    /**
     * Gets available rooms (not full).
     * @param limit Maximum number of rooms to return
     * @return List of available room DTOs
     */
    List<FriendlyTalkRoomDTO> getAvailableRooms(int limit);

    /**
     * Gets rooms by mood focus.
     * @param moodFocus The mood focus
     * @param limit Maximum number of rooms to return
     * @return List of room DTOs with the specified mood focus
     */
    List<FriendlyTalkRoomDTO> getRoomsByMoodFocus(String moodFocus, int limit);

    /**
     * Gets rooms by topic.
     * @param topic The topic to search for
     * @param limit Maximum number of rooms to return
     * @return List of room DTOs containing the topic
     */
    List<FriendlyTalkRoomDTO> getRoomsByTopic(String topic, int limit);

    /**
     * Gets rooms created by a user.
     * @param user The user
     * @return List of rooms created by the user
     */
    List<FriendlyTalkRoomDTO> getUserRooms(User user);

    /**
     * Closes a room.
     * @param roomId The room ID
     * @param user The user closing the room (must be creator or moderator)
     * @return true if closed successfully
     */
    boolean closeRoom(String roomId, User user);

    /**
     * Assigns a moderator to a room.
     * @param roomId The room ID
     * @param moderator The moderator user
     * @return true if assigned successfully
     */
    boolean assignModerator(String roomId, User moderator);

    /**
     * Removes a participant from a room (moderator action).
     * @param roomId The room ID
     * @param participantId The participant to remove
     * @param moderator The moderator performing the action
     * @return true if removed successfully
     */
    boolean removeParticipant(String roomId, Long participantId, User moderator);

    /**
     * Gets room statistics.
     * @return Map of room statistics
     */
    java.util.Map<String, Object> getRoomStatistics();

    /**
     * Gets the least crowded rooms.
     * @param limit Maximum number of rooms to return
     * @return List of least crowded room DTOs
     */
    List<FriendlyTalkRoomDTO> getLeastCrowdedRooms(int limit);

    /**
     * Updates room settings.
     * @param roomId The room ID
     * @param user The user updating (must be creator)
     * @param name New name
     * @param description New description
     * @param topicTags New topic tags
     * @return The updated room DTO
     */
    FriendlyTalkRoomDTO updateRoom(String roomId, User user, String name,
                                  String description, String topicTags);

    /**
     * Checks if a user is a participant in a room.
     * @param user The user
     * @param roomId The room ID
     * @return true if user is a participant
     */
    boolean isUserInRoom(User user, String roomId);

    /**
     * Gets rooms where a user is a moderator.
     * @param user The user
     * @return List of rooms where user is moderator
     */
    List<FriendlyTalkRoomDTO> getModeratedRooms(User user);
}
