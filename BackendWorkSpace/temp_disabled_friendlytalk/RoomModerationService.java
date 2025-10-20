package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.exception.UnauthorizedException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.RoomBan;
import com.samjhadoo.model.friendlytalk.RoomModerator;
import com.samjhadoo.model.friendlytalk.RoomMute;
import com.samjhadoo.repository.friendlytalk.RoomBanRepository;
import com.samjhadoo.repository.friendlytalk.RoomModeratorRepository;
import com.samjhadoo.repository.friendlytalk.RoomMuteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomModerationService {

    private final RoomBanRepository roomBanRepository;
    private final RoomMuteRepository roomMuteRepository;
    private final RoomModeratorRepository roomModeratorRepository;
    private final FriendlyRoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Ban a user from a room
    public void banUser(String roomId, String targetUserId, String reason, User moderator) {
        validateModerationRights(roomId, moderator.getId());
        
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Check if already banned
        if (isUserBanned(roomId, targetUserId)) {
            throw new IllegalStateException("User is already banned from this room");
        }
        
        // Create ban record
        RoomBan ban = RoomBan.builder()
                .room(room)
                .userId(targetUserId)
                .bannedBy(moderator.getId())
                .reason(reason)
                .bannedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(7))) // Default 7-day ban
                .build();
        
        roomBanRepository.save(ban);
        
        // Kick the user if they're currently in the room
        kickUser(roomId, targetUserId, "Banned: " + reason, moderator);
        
        // Notify room about the ban
        notifyRoom(roomId, "USER_BANNED", Map.of(
            "userId", targetUserId,
            "moderatorId", moderator.getId(),
            "reason", reason
        ));
    }
    
    // Unban a user from a room
    public void unbanUser(String roomId, String targetUserId, User moderator) {
        validateModerationRights(roomId, moderator.getId());
        
        RoomBan ban = roomBanRepository.findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No active ban found for this user"));
        
        // Mark ban as expired
        ban.setExpiresAt(Instant.now());
        roomBanRepository.save(ban);
        
        // Notify room about the unban
        notifyRoom(roomId, "USER_UNBANNED", Map.of(
            "userId", targetUserId,
            "moderatorId", moderator.getId()
        ));
    }
    
    // Mute a user in a room
    public void muteUser(String roomId, String targetUserId, Duration duration, String reason, User moderator) {
        validateModerationRights(roomId, moderator.getId());
        
        // Remove any existing mutes to prevent duplicates
        roomMuteRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
        
        // Create new mute
        RoomMute mute = RoomMute.builder()
                .room(getRoomOrThrow(roomId))
                .userId(targetUserId)
                .mutedBy(moderator.getId())
                .reason(reason)
                .mutedAt(Instant.now())
                .expiresAt(Instant.now().plus(duration))
                .build();
        
        roomMuteRepository.save(mute);
        
        // Notify user and room about the mute
        notifyUser(targetUserId, "USER_MUTED", Map.of(
            "roomId", roomId,
            "moderatorId", moderator.getId(),
            "reason", reason,
            "expiresAt", mute.getExpiresAt().toString()
        ));
    }
    
    // Unmute a user in a room
    public void unmuteUser(String roomId, String targetUserId, User moderator) {
        validateModerationRights(roomId, moderator.getId());
        
        RoomMute mute = roomMuteRepository.findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No active mute found for this user"));
        
        // Mark mute as expired
        mute.setExpiresAt(Instant.now());
        roomMuteRepository.save(mute);
        
        // Notify user and room about the unmute
        notifyUser(targetUserId, "USER_UNMUTED", Map.of("roomId", roomId));
    }
    
    // Add a moderator to a room
    public void addModerator(String roomId, String targetUserId, User admin) {
        if (!isRoomAdmin(roomId, admin.getId())) {
            throw new UnauthorizedException("Only room admins can add moderators");
        }
        
        // Check if already a moderator
        if (isUserModerator(roomId, targetUserId)) {
            throw new IllegalStateException("User is already a moderator of this room");
        }
        
        // Add moderator
        RoomModerator moderator = RoomModerator.builder()
                .room(getRoomOrThrow(roomId))
                .userId(targetUserId)
                .addedBy(admin.getId())
                .addedAt(Instant.now())
                .build();
        
        roomModeratorRepository.save(moderator);
        
        // Notify user and room about the new moderator
        notifyUser(targetUserId, "MADE_MODERATOR", Map.of("roomId", roomId));
        notifyRoom(roomId, "NEW_MODERATOR", Map.of(
            "userId", targetUserId,
            "adminId", admin.getId()
        ));
    }
    
    // Remove a moderator from a room
    public void removeModerator(String roomId, String targetUserId, User admin) {
        if (!isRoomAdmin(roomId, admin.getId())) {
            throw new UnauthorizedException("Only room admins can remove moderators");
        }
        
        // Remove moderator
        int removed = roomModeratorRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
        
        if (removed > 0) {
            // Notify user and room about the removal
            notifyUser(targetUserId, "REMOVED_AS_MODERATOR", Map.of("roomId", roomId));
            notifyRoom(roomId, "MODERATOR_REMOVED", Map.of(
                "userId", targetUserId,
                "adminId", admin.getId()
            ));
        }
    }
    
    // Kick a user from a room
    public void kickUser(String roomId, String targetUserId, String reason, User moderator) {
        validateModerationRights(roomId, moderator.getId());
        
        // Notify user about the kick
        notifyUser(targetUserId, "KICKED_FROM_ROOM", Map.of(
            "roomId", roomId,
            "reason", reason,
            "moderatorId", moderator.getId()
        ));
        
        // Notify room about the kick
        notifyRoom(roomId, "USER_KICKED", Map.of(
            "userId", targetUserId,
            "moderatorId", moderator.getId(),
            "reason", reason
        ));
        
        // The actual removal from the room will be handled by the WebSocket event listener
    }
    
    // Check if a user is banned from a room
    public boolean isUserBanned(String roomId, String userId) {
        return roomBanRepository.existsByRoomIdAndUserIdAndExpiresAtAfter(
            roomId, userId, Instant.now());
    }
    
    // Check if a user is muted in a room
    public boolean isUserMuted(String roomId, String userId) {
        return roomMuteRepository.existsByRoomIdAndUserIdAndExpiresAtAfter(
            roomId, userId, Instant.now());
    }
    
    // Check if a user is a moderator of a room
    public boolean isUserModerator(String roomId, String userId) {
        return roomModeratorRepository.existsByRoomIdAndUserId(roomId, userId) || 
               isRoomAdmin(roomId, userId);
    }
    
    // Check if a user is an admin of a room
    public boolean isRoomAdmin(String roomId, String userId) {
        return roomRepository.existsByIdAndCreatedById(roomId, userId);
    }
    
    // Get all active bans for a room
    public List<RoomBan> getActiveBans(String roomId) {
        return roomBanRepository.findByRoomIdAndExpiresAtAfter(
            roomId, Instant.now());
    }
    
    // Get all active mutes for a room
    public List<RoomMute> getActiveMutes(String roomId) {
        return roomMuteRepository.findByRoomIdAndExpiresAtAfter(
            roomId, Instant.now());
    }
    
    // Get all moderators for a room
    public List<RoomModerator> getModerators(String roomId) {
        return roomModeratorRepository.findByRoomId(roomId);
    }
    
    // Clean up expired bans and mutes
    public void cleanupExpiredModerations() {
        Instant now = Instant.now();
        
        // Clean up expired bans
        List<RoomBan> expiredBans = roomBanRepository.findByExpiresAtBefore(now);
        roomBanRepository.deleteAll(expiredBans);
        
        // Clean up expired mutes
        List<RoomMute> expiredMutes = roomMuteRepository.findByExpiresAtBefore(now);
        roomMuteRepository.deleteAll(expiredMutes);
        
        log.info("Cleaned up {} expired bans and {} expired mutes", 
                expiredBans.size(), expiredMutes.size());
    }
    
    // Helper methods
    private FriendlyRoom getRoomOrThrow(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
    }
    
    private void validateModerationRights(String roomId, String userId) {
        if (!isUserModerator(roomId, userId)) {
            throw new UnauthorizedException("You don't have moderation rights in this room");
        }
    }
    
    private void notifyRoom(String roomId, String type, Object payload) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/moderation",
            Map.of(
                "type", type,
                "timestamp", Instant.now().toString(),
                "data", payload
            )
        );
    }
    
    private void notifyUser(String userId, String type, Object payload) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            Map.of(
                "type", type,
                "timestamp", Instant.now().toString(),
                "data", payload
            )
        );
    }
}
