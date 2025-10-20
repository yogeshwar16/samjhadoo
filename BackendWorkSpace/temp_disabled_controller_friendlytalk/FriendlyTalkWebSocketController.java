package com.samjhadoo.controller.api.friendlytalk;

import com.samjhadoo.dto.friendlytalk.ChatMessageDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.RoomEvent;
import com.samjhadoo.repository.friendlytalk.RoomEventRepository;
import com.samjhadoo.service.friendlytalk.FriendlyRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FriendlyTalkWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FriendlyRoomService roomService;
    private final RoomEventRepository roomEventRepository;

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(
            @DestinationVariable String roomId,
            @Payload ChatMessageDTO message,
            @AuthenticationPrincipal User sender,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // Validate user can send messages to this room
        if (!roomService.canUserSendMessages(roomId, sender.getId())) {
            log.warn("User {} is not allowed to send messages to room {}", sender.getId(), roomId);
            return;
        }

        // Create and save room event
        RoomEvent event = RoomEvent.builder()
                .room(roomService.getRoomById(roomId))
                .eventType(RoomEvent.EventType.MESSAGE_SENT)
                .userId(sender.getId())
                .details(message.getContent())
                .build();
        roomEventRepository.save(event);

        // Add metadata to the message
        message.setSenderId(sender.getId());
        message.setSenderName(sender.getUsername());
        message.setTimestamp(Instant.now().toString());
        message.setMessageId(event.getId().toString());

        // Broadcast the message to all subscribers of the room
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/messages",
            message
        );
    }

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(
            @DestinationVariable String roomId,
            @AuthenticationPrincipal User user,
            @Header("simpSessionId") String sessionId) {
        
        // Add user to the room
        roomService.addParticipant(roomId, user.getId());
        
        // Notify others in the room
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/participants",
            Map.of(
                "type", "USER_JOINED",
                "userId", user.getId(),
                "username", user.getUsername(),
                "timestamp", Instant.now().toString()
            )
        );
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable String roomId,
            @AuthenticationPrincipal User user,
            @Header("simpSessionId") String sessionId) {
        
        // Remove user from the room
        roomService.removeParticipant(roomId, user.getId());
        
        // Notify others in the room
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/participants",
            Map.of(
                "type", "USER_LEFT",
                "userId", user.getId(),
                "username", user.getUsername(),
                "timestamp", Instant.now().toString()
            )
        );
    }

    @MessageMapping("/room/{roomId}/typing")
    public void typing(
            @DestinationVariable String roomId,
            @AuthenticationPrincipal User user,
            @Header("simpSessionId") String sessionId) {
        
        // Notify others that the user is typing
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/activity",
            Map.of(
                "type", "TYPING",
                "userId", user.getId(),
                "username", user.getUsername(),
                "timestamp", Instant.now().toString()
            )
        );
    }

    @MessageMapping("/room/{roomId}/update-settings")
    public void updateRoomSettings(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> settings,
            @AuthenticationPrincipal User user) {
        
        // Validate user has permission to update room settings
        if (!roomService.isRoomAdmin(roomId, user.getId())) {
            log.warn("User {} is not authorized to update settings for room {}", user.getId(), roomId);
            return;
        }

        // Update room settings
        roomService.updateRoomSettings(roomId, settings, user.getId());

        // Notify all room participants about the update
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/settings",
            Map.of(
                "type", "SETTINGS_UPDATED",
                "updatedBy", user.getUsername(),
                "settings", settings,
                "timestamp", Instant.now().toString()
            )
        );
    }
}
