package com.samjhadoo.controller.api;

import com.samjhadoo.dto.notification.NotificationDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(@CurrentUser User user, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId(), pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationDTO>> getUnreadNotifications(@CurrentUser User user, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId(), pageable));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@CurrentUser User user) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, @CurrentUser User user) {
        notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
