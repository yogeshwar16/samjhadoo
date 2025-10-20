package com.samjhadoo.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.samjhadoo.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    private String id;
    private String type;
    private String title;
    private String message;
    private String actionUrl;
    private NotificationType notificationType;
    private Map<String, Object> data;
    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();
    
    // Factory methods for common message types
    public static WebSocketMessage notification(String title, String message, String actionUrl, 
                                              NotificationType notificationType, Map<String, Object> data) {
        return WebSocketMessage.builder()
                .type("NOTIFICATION")
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .notificationType(notificationType)
                .data(data)
                .build();
    }
    
    public static WebSocketMessage error(String message, String details) {
        return WebSocketMessage.builder()
                .type("ERROR")
                .message(message)
                .data(Map.of("details", details))
                .build();
    }
    
    public static WebSocketMessage info(String message) {
        return WebSocketMessage.builder()
                .type("INFO")
                .message(message)
                .build();
    }
}
