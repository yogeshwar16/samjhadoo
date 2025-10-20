package com.samjhadoo.controller.websocket;

import com.samjhadoo.dto.communication.ChatMessageDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.service.communication.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket controller for real-time chat messaging.
 * Handles STOMP messages for chat operations.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload Map<String, Object> payload,
            @AuthenticationPrincipal User user,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            String content = (String) payload.get("content");
            Long replyToMessageId = payload.get("replyToMessageId") != null 
                    ? ((Number) payload.get("replyToMessageId")).longValue() 
                    : null;
            
            ChatMessageDTO message = chatService.sendMessage(roomId, user, content, replyToMessageId);
            log.debug("Message sent via WebSocket: {}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("Error sending message via WebSocket: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/{roomId}/typing")
    public void updateTyping(
            @DestinationVariable Long roomId,
            @Payload Map<String, Object> payload,
            @AuthenticationPrincipal User user) {
        
        try {
            boolean isTyping = (boolean) payload.getOrDefault("isTyping", false);
            chatService.updateTypingStatus(roomId, user, isTyping);
            
        } catch (Exception e) {
            log.error("Error updating typing status: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/{roomId}/read")
    public void markAsRead(
            @DestinationVariable Long roomId,
            @Payload Map<String, Object> payload,
            @AuthenticationPrincipal User user) {
        
        try {
            String upToMessageId = (String) payload.get("upToMessageId");
            chatService.markAsRead(roomId, user, upToMessageId);
            
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/message/{messageId}/react")
    public void reactToMessage(
            @DestinationVariable String messageId,
            @Payload Map<String, Object> payload,
            @AuthenticationPrincipal User user) {
        
        try {
            String emoji = (String) payload.get("emoji");
            chatService.reactToMessage(messageId, user, emoji);
            
        } catch (Exception e) {
            log.error("Error reacting to message: {}", e.getMessage(), e);
        }
    }
}
