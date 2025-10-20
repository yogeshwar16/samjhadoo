package com.samjhadoo.dto.notification;

import com.samjhadoo.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
