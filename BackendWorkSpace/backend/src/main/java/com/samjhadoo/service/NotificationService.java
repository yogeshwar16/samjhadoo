package com.samjhadoo.service;

import com.samjhadoo.dto.notification.NotificationDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationService {

    NotificationDTO createNotification(User recipient, NotificationType type, String message, 
                                      String actionUrl, Map<String, String> metadata);

    Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable);

    Page<NotificationDTO> getUnreadNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, User user);

    void markAllAsRead(Long userId);

    void sendEmailNotification(User recipient, String subject, String body);

    void sendVerificationRequestNotification(String userId);
}
