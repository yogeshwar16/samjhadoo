package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.RoomEvent;
import com.samjhadoo.repository.friendlytalk.FriendlyRoomRepository;
import com.samjhadoo.repository.friendlytalk.RoomEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendlyTalkEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final FriendlyRoomRepository roomRepository;
    private final RoomEventRepository roomEventRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new WebSocket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket Disconnected: " + sessionId);
        
        // Handle user leaving rooms
        // You can add logic to clean up room participants here
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        String sessionId = headers.getSessionId();
        
        if (destination != null && destination.startsWith("/topic/room/")) {
            String roomId = destination.substring("/topic/room/".length());
            log.info("User subscribed to room: {} (Session: {})", roomId, sessionId);
            
            // Record room join event
            roomRepository.findById(roomId).ifPresent(room -> {
                RoomEvent roomEvent = RoomEvent.builder()
                    .room(room)
                    .eventType(RoomEvent.EventType.USER_JOINED)
                    .userId(getUserIdFromSession(sessionId)) // Implement this based on your auth
                    .timestamp(Instant.now())
                    .build();
                roomEventRepository.save(roomEvent);
                
                // Update room participant count
                room.setCurrentParticipants(room.getCurrentParticipants() + 1);
                roomRepository.save(room);
                
                // Notify other users in the room
                messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/participants",
                    Map.of(
                        "type", "USER_JOINED",
                        "roomId", roomId,
                        "participantCount", room.getCurrentParticipants()
                    )
                );
            });
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        
        // Handle user leaving rooms
        // You can add logic to clean up room participants here
    }
    
    private String getUserIdFromSession(String sessionId) {
        // Implement this method to get user ID from session
        // This is a placeholder - you'll need to implement based on your auth system
        return "user-" + sessionId;
    }
}
