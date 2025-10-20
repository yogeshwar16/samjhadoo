package com.samjhadoo.config.websocket;

import com.samjhadoo.service.monitoring.WebSocketMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts WebSocket messages to collect metrics and monitor WebSocket activity.
 * Tracks connections, disconnections, and message processing times.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMetricsInterceptor implements ChannelInterceptor {

    private final WebSocketMetricsService metricsService;
    private final Map<String, Long> connectionTimestamps = new ConcurrentHashMap<>();
    private final Map<String, String> sessionEndpoints = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            if (sessionId != null) {
                switch (command) {
                    case CONNECT:
                        handleConnect(sessionId, destination);
                        break;
                    case CONNECTED:
                        handleConnected(sessionId, accessor);
                        break;
                    case DISCONNECT:
                        handleDisconnect(sessionId);
                        break;
                    case SUBSCRIBE:
                        handleSubscribe(sessionId, destination);
                        break;
                    case UNSUBSCRIBE:
                        handleUnsubscribe(sessionId, destination);
                        break;
                    case SEND:
                        handleSend(sessionId, destination);
                        break;
                    case ERROR:
                        handleError(sessionId, accessor);
                        break;
                    default:
                        log.debug("Unhandled STOMP command: {}", command);
                        break;
                }
            }
        }

        return message;
    }

    private void handleConnect(String sessionId, String destination) {
        connectionTimestamps.put(sessionId, System.currentTimeMillis());
        log.debug("WebSocket connection initiated: {}", sessionId);
    }

    private void handleConnected(String sessionId, StompHeaderAccessor accessor) {
        String endpoint = getEndpoint(accessor);
        sessionEndpoints.put(sessionId, endpoint);
        metricsService.incrementConnection(endpoint);
        
        long connectionTime = System.currentTimeMillis() - connectionTimestamps.getOrDefault(sessionId, System.currentTimeMillis());
        log.info("WebSocket connected: {} to {} in {}ms", sessionId, endpoint, connectionTime);
    }

    private void handleDisconnect(String sessionId) {
        String endpoint = sessionEndpoints.remove(sessionId);
        if (endpoint != null) {
            metricsService.decrementConnection(endpoint);
            log.debug("WebSocket disconnected: {} from {}", sessionId, endpoint);
        }
        connectionTimestamps.remove(sessionId);
    }

    private void handleSubscribe(String sessionId, String destination) {
        if (destination != null) {
            metricsService.trackSubscription(destination, true);
            log.debug("Subscription started: {} to {}", sessionId, destination);
        }
    }

    private void handleUnsubscribe(String sessionId, String destination) {
        if (destination != null) {
            metricsService.trackSubscription(destination, false);
            log.debug("Subscription ended: {} from {}", sessionId, destination);
        }
    }

    private void handleSend(String sessionId, String destination) {
        if (destination != null) {
            metricsService.trackMessage(destination, 0); // Processing time updated in afterSendCompletion
            log.trace("Message sent to {} by {}", destination, sessionId);
        }
    }

    private void handleError(String sessionId, StompHeaderAccessor accessor) {
        String errorMessage = accessor.getMessage();
        log.error("WebSocket error for session {}: {}", sessionId, errorMessage);
        
        // Clean up resources on error
        String endpoint = sessionEndpoints.remove(sessionId);
        if (endpoint != null) {
            metricsService.decrementConnection(endpoint);
        }
        connectionTimestamps.remove(sessionId);
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && accessor.getCommand() == StompCommand.SEND) {
            String destination = accessor.getDestination();
            if (destination != null) {
                long processingTime = System.currentTimeMillis() - connectionTimestamps
                    .getOrDefault(accessor.getSessionId(), System.currentTimeMillis());
                metricsService.trackMessage(destination, processingTime);
            }
        }
    }

    private String getEndpoint(StompHeaderAccessor accessor) {
        // Extract endpoint from the destination or use a default
        String destination = accessor.getDestination();
        if (destination == null) {
            return "unknown";
        }
        
        // Extract the first part of the destination as the endpoint
        String[] parts = destination.split("/");
        return parts.length > 1 ? parts[1] : "root";
    }
}
