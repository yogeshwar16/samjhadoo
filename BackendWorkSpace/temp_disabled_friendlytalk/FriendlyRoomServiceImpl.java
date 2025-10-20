package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyRoomRequest;
import com.samjhadoo.dto.friendlytalk.FriendlyRoomResponse;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.exception.UnauthorizedException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.RoomEvent;
import com.samjhadoo.repository.friendlytalk.FriendlyRoomRepository;
import com.samjhadoo.repository.friendlytalk.RoomEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FriendlyRoomServiceImpl implements FriendlyRoomService {

    private final FriendlyRoomRepository roomRepository;
    private final RoomEventRepository roomEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private final SafetyService safetyService;

    @Override
    public FriendlyRoomResponse createRoom(FriendlyRoomRequest request, User creator) {
        // Validate room creation request
        safetyService.validateRoomCreation(creator, request);
        
        // Create new room
        FriendlyRoom room = new FriendlyRoom();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setRoomType(request.getRoomType());
        room.setMood(request.getMood());
        room.setTopic(request.getTopic());
        room.setLanguage(request.getLanguage());
        room.setPrivate(request.isPrivate());
        room.setAnonymous(request.isAnonymous());
        room.setVoiceOnly(request.isVoiceOnly());
        room.setMaxParticipants(request.getMaxParticipants());
        room.setCreatedBy(creator);
        room.setRoomCode(generateRoomCode());
        
        // Add creator as first participant
        room.getParticipants().add(creator);
        room.setCurrentParticipants(1);
        
        // Save room
        room = roomRepository.save(room);
        
        // Log room creation event
        logRoomEvent(room, RoomEvent.EventType.ROOM_CREATED, creator.getId(), "Room created");
        
        return convertToDto(room);
    }

    @Override
    public FriendlyRoomResponse updateRoom(String roomId, FriendlyRoomRequest request, User updater) {
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Check if user has permission to update
        if (!isRoomAdmin(roomId, updater.getId())) {
            throw new AccessDeniedException("Only room admins can update room settings");
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            room.setName(request.getName());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getTopic() != null) {
            room.setTopic(request.getTopic());
        }
        if (request.getLanguage() != null) {
            room.setLanguage(request.getLanguage());
        }
        
        // Save updates
        room = roomRepository.save(room);
        
        // Notify room participants
        notifyRoomUpdate(room, updater);
        
        return convertToDto(room);
    }

    @Override
    public void deleteRoom(String roomId, User requester) {
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Check if user has permission to delete
        if (!isRoomAdmin(roomId, requester.getId())) {
            throw new AccessDeniedException("Only room admins can delete the room");
        }
        
        // Log room deletion event
        logRoomEvent(room, RoomEvent.EventType.ROOM_ENDED, requester.getId(), "Room deleted");
        
        // Notify participants before deletion
        notifyRoomDeletion(room, requester);
        
        // Delete the room
        roomRepository.delete(room);
    }

    @Override
    public FriendlyRoomResponse getRoomById(String roomId, User requester) {
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Check if room is private and user is a participant
        if (room.isPrivate() && !isUserInRoom(roomId, requester.getId())) {
            throw new AccessDeniedException("This is a private room");
        }
        
        return convertToDto(room);
    }

    @Override
    public Page<FriendlyRoomResponse> listRooms(Pageable pageable, User requester) {
        Page<FriendlyRoom> rooms = roomRepository.findAll(pageable);
        List<FriendlyRoomResponse> content = rooms.getContent().stream()
                .filter(room -> !room.isPrivate() || isUserInRoom(room.getId(), requester.getId()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public Page<FriendlyRoomResponse> searchRooms(String query, Pageable pageable, User requester) {
        // This is a simplified search implementation
        // In a real app, you'd use a full-text search engine like Elasticsearch
        Page<FriendlyRoom> rooms = roomRepository.searchRooms(query, pageable);
        List<FriendlyRoomResponse> content = rooms.getContent().stream()
                .filter(room -> !room.isPrivate() || isUserInRoom(room.getId(), requester.getId()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public FriendlyRoomResponse joinRoom(String roomId, User user, boolean joinAnonymously) {
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Check if room is full
        if (isRoomFull(roomId)) {
            throw new IllegalStateException("Room is full");
        }
        
        // Check if user is banned
        if (isUserBanned(roomId, user.getId())) {
            throw new AccessDeniedException("You are banned from this room");
        }
        
        // Add user to participants if not already joined
        if (!isUserInRoom(roomId, user.getId())) {
            room.getParticipants().add(user);
            room.setCurrentParticipants(room.getCurrentParticipants() + 1);
            room = roomRepository.save(room);
            
            // Log join event
            logRoomEvent(room, RoomEvent.EventType.USER_JOINED, user.getId(), 
                joinAnonymously ? "Joined anonymously" : "Joined the room");
        }
        
        return convertToDto(room);
    }

    @Override
    public void leaveRoom(String roomId, User user) {
        FriendlyRoom room = getRoomOrThrow(roomId);
        
        // Remove user from participants
        if (room.getParticipants().removeIf(p -> p.getId().equals(user.getId()))) {
            room.setCurrentParticipants(room.getCurrentParticipants() - 1);
            room = roomRepository.save(room);
            
            // Log leave event
            logRoomEvent(room, RoomEvent.EventType.USER_LEFT, user.getId(), "Left the room");
            
            // If room is empty, consider cleaning it up
            if (room.getCurrentParticipants() == 0) {
                cleanupEmptyRoom(room);
            }
        }
    }

    @Override
    public boolean isUserInRoom(String roomId, String userId) {
        return roomRepository.existsByIdAndParticipants_Id(roomId, userId);
    }

    @Override
    public boolean isRoomFull(String roomId) {
        return roomRepository.findById(roomId)
                .map(room -> room.getCurrentParticipants() >= room.getMaxParticipants())
                .orElse(true);
    }

    @Override
    public boolean isRoomPrivate(String roomId) {
        return roomRepository.findById(roomId)
                .map(FriendlyRoom::isPrivate)
                .orElse(true);
    }

    @Override
    public boolean isUserBanned(String roomId, String userId) {
        // Implement ban check logic
        return false;
    }

    @Override
    public boolean isUserModerator(String roomId, String userId) {
        // Implement moderator check logic
        return false;
    }

    @Override
    public boolean isRoomAdmin(String roomId, String userId) {
        return roomRepository.existsByIdAndCreatedById(roomId, userId);
    }

    @Override
    public boolean canUserSendMessages(String roomId, String userId) {
        // Check if user is in the room and not muted/banned
        return isUserInRoom(roomId, userId) && 
               !isUserBanned(roomId, userId);
               // Add additional checks for mute status if needed
    }

    @Override
    public void addParticipant(String roomId, String userId) {
        // This is a simplified implementation
        // In a real app, you'd fetch the user and add to participants
        log.debug("Adding user {} to room {}", userId, roomId);
    }

    @Override
    public void removeParticipant(String roomId, String userId) {
        // This is a simplified implementation
        log.debug("Removing user {} from room {}", userId, roomId);
    }

    @Override
    public FriendlyRoom getRoomEntity(String roomId) {
        return getRoomOrThrow(roomId);
    }

    @Override
    public void cleanupInactiveRooms() {
        // Implement logic to clean up inactive rooms
        // This would typically be called by a scheduled task
    }

    // Helper methods
    private FriendlyRoom getRoomOrThrow(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
    }

    private String generateRoomCode() {
        // Generate a random 6-character alphanumeric code
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void logRoomEvent(FriendlyRoom room, RoomEvent.EventType eventType, String userId, String details) {
        RoomEvent event = RoomEvent.builder()
                .room(room)
                .eventType(eventType)
                .userId(userId)
                .details(details)
                .build();
        roomEventRepository.save(event);
    }

    private void notifyRoomUpdate(FriendlyRoom room, User updater) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "ROOM_UPDATED");
        update.put("roomId", room.getId());
        update.put("updatedBy", updater.getUsername());
        update.put("updatedAt", Instant.now().toString());
        
        messagingTemplate.convertAndSend(
            "/topic/room/" + room.getId() + "/updates",
            update
        );
    }

    private void notifyRoomDeletion(FriendlyRoom room, User deleter) {
        Map<String, Object> deletion = new HashMap<>();
        deletion.put("type", "ROOM_DELETED");
        deletion.put("roomId", room.getId());
        deletion.put("deletedBy", deleter.getUsername());
        deletion.put("deletedAt", Instant.now().toString());
        
        messagingTemplate.convertAndSend(
            "/topic/room/" + room.getId() + "/updates",
            deletion
        );
    }

    private void cleanupEmptyRoom(FriendlyRoom room) {
        // Implement logic to clean up empty rooms
        // For example, schedule for deletion if empty for too long
    }

    private FriendlyRoomResponse convertToDto(FriendlyRoom room) {
        FriendlyRoomResponse dto = modelMapper.map(room, FriendlyRoomResponse.class);
        dto.setCreatedById(room.getCreatedBy().getId());
        dto.setCreatedByUsername(room.getCreatedBy().getUsername());
        return dto;
    }
}
