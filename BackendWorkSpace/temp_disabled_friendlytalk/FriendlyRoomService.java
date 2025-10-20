package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyRoomRequest;
import com.samjhadoo.dto.friendlytalk.FriendlyRoomResponse;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface FriendlyRoomService {
    
    // Room Management
    FriendlyRoomResponse createRoom(FriendlyRoomRequest request, User creator);
    
    FriendlyRoomResponse updateRoom(String roomId, FriendlyRoomRequest request, User updater);
    
    void deleteRoom(String roomId, User requester);
    
    FriendlyRoomResponse getRoomById(String roomId, User requester);
    
    Page<FriendlyRoomResponse> listRooms(Pageable pageable, User requester);
    
    Page<FriendlyRoomResponse> searchRooms(String query, Pageable pageable, User requester);
    
    // Room Participation
    FriendlyRoomResponse joinRoom(String roomId, User user, boolean joinAnonymously);
    
    void leaveRoom(String roomId, User user);
    
    void kickUser(String roomId, String userId, User requester);
    
    void banUser(String roomId, String userId, String reason, User requester);
    
    void unbanUser(String roomId, String userId, User requester);
    
    // Room Moderation
    void muteUser(String roomId, String userId, long durationMinutes, User requester);
    
    void unmuteUser(String roomId, String userId, User requester);
    
    void promoteToModerator(String roomId, String userId, User requester);
    
    void demoteFromModerator(String roomId, String userId, User requester);
    
    // Room Settings
    void updateRoomSettings(String roomId, Map<String, Object> settings, User requester);
    
    // Room Discovery
    List<FriendlyRoomResponse> getPopularRooms(int limit);
    
    List<FriendlyRoomResponse> getRoomsByMood(String mood, int limit);
    
    List<FriendlyRoomResponse> getRoomsByTopic(String topic, int limit);
    
    List<FriendlyRoomResponse> getRecommendedRooms(User user, int limit);
    
    // Room State
    boolean isUserInRoom(String roomId, String userId);
    
    boolean isRoomFull(String roomId);
    
    boolean isRoomPrivate(String roomId);
    
    boolean isUserBanned(String roomId, String userId);
    
    boolean isUserModerator(String roomId, String userId);
    
    boolean isRoomAdmin(String roomId, String userId);
    
    // Helper methods for WebSocket
    boolean canUserSendMessages(String roomId, String userId);
    
    void addParticipant(String roomId, String userId);
    
    void removeParticipant(String roomId, String userId);
    
    FriendlyRoom getRoomEntity(String roomId);
    
    // Room Cleanup
    void cleanupInactiveRooms();
    
    // Room Analytics
    Map<String, Object> getRoomAnalytics(String roomId, User requester);
    
    // Room Invitations
    String generateInviteCode(String roomId, User requester);
    
    boolean validateInviteCode(String roomId, String code);
    
    // Room Media
    String generateMediaToken(String roomId, User user);
    
    boolean validateMediaToken(String roomId, String token, User user);
}
