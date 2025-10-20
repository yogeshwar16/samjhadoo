package com.samjhadoo.websocket;

import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a private message to a specific user
     */
    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload WebSocketMessage message, Principal principal) {
        String recipient = message.getData() != null ? 
                String.valueOf(message.getData().get("recipientId")) : null;
        
        if (recipient != null) {
            String destination = "/queue/private-messages";
            log.debug("Sending private message to user {}: {}", recipient, message);
            messagingTemplate.convertAndSendToUser(recipient, destination, message);
        }
    }

    /**
     * Handle subscription to user's private notifications
     */
    @SubscribeMapping("/user/queue/notifications")
    public void handleNotificationSubscription(Principal principal) {
        log.debug("User {} subscribed to notifications", principal.getName());
    }

    /**
     * Subscribe to community-specific notifications
     */
    @SubscribeMapping("/topic/communities/{communityId}")
    public void handleCommunitySubscription(
            @DestinationVariable Long communityId, 
            Principal principal) {
        log.debug("User {} subscribed to community {} notifications", 
                principal.getName(), communityId);
    }

    /**
     * Send a notification to all users in a community
     */
    public void sendCommunityNotification(Long communityId, WebSocketMessage message) {
        String destination = "/topic/communities/" + communityId;
        log.debug("Sending notification to community {}: {}", communityId, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Send a notification to a specific user
     */
    public void sendUserNotification(Long userId, WebSocketMessage message) {
        String destination = "/queue/notifications";
        log.debug("Sending notification to user {}: {}", userId, message);
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, message);
    }
}
