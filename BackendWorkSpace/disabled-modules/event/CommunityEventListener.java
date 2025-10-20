package com.samjhadoo.service.event;

import com.samjhadoo.event.community.CommunityEvent;
import com.samjhadoo.event.community.CommunityMemberEvent;
import com.samjhadoo.model.Notification;
import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.enums.NotificationType;
import com.samjhadoo.repository.NotificationRepository;
import com.samjhadoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommunityEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommunityEvent(CommunityEvent event) {
        log.debug("Handling community event: {} for community: {}", 
                event.getEventType(), event.getCommunity().getId());

        switch (event.getEventType()) {
            case COMMUNITY_CREATED:
                handleCommunityCreated(event);
                break;
            case COMMUNITY_UPDATED:
                handleCommunityUpdated(event);
                break;
            // Handle other community events
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberEvent(CommunityMemberEvent event) {
        log.debug("Handling member event: {} for community: {}, member: {}", 
                event.getEventType(), event.getCommunity().getId(), event.getMember().getId());

        switch (event.getEventType()) {
            case MEMBER_JOINED:
                handleMemberJoined(event);
                break;
            case MEMBER_LEFT:
                handleMemberLeft(event);
                break;
            case MEMBER_ROLE_CHANGED:
                handleMemberRoleChanged(event);
                break;
            case MEMBER_STATUS_CHANGED:
                handleMemberStatusChanged(event);
                break;
        }
    }

    private void handleCommunityCreated(CommunityEvent event) {
        Community community = event.getCommunity();
        User creator = event.getActor();
        
        // Create welcome notification for the creator
        createNotification(
            creator,
            "community.created",
            "You've created the community " + community.getName(),
            "/communities/" + community.getId(),
            NotificationType.COMMUNITY_CREATED,
            Map.of("communityId", community.getId().toString())
        );
        
        log.info("Community created: {} by user: {}", community.getName(), creator.getId());
    }

    private void handleMemberJoined(CommunityMemberEvent event) {
        Community community = event.getCommunity();
        User member = event.getMember();
        
        // Notify the member
        createNotification(
            member,
            "community.joined",
            "You've joined the community " + community.getName(),
            "/communities/" + community.getId(),
            NotificationType.COMMUNITY_JOINED,
            Map.of("communityId", community.getId().toString())
        );
        
        // Notify admins (except the joining member if they are admin)
        community.getMembers().stream()
            .filter(cm -> cm.getRole() == MemberRole.ADMIN && !cm.getUser().equals(member))
            .forEach(adminMember -> {
                createNotification(
                    adminMember.getUser(),
                    "community.member.joined",
                    member.getUsername() + " has joined the community " + community.getName(),
                    "/communities/" + community.getId() + "/members",
                    NotificationType.COMMUNITY_MEMBER_JOINED,
                    Map.of(
                        "communityId", community.getId().toString(),
                        "memberId", member.getId().toString()
                    )
                );
            });
    }

    private void handleMemberRoleChanged(CommunityMemberEvent event) {
        // Implementation for handling role changes
    }

    private void handleMemberStatusChanged(CommunityMemberEvent event) {
        // Implementation for handling status changes
    }

    private void handleCommunityUpdated(CommunityEvent event) {
        // Implementation for handling community updates
    }

    private void handleMemberLeft(CommunityMemberEvent event) {
        // Implementation for handling member leaving
    }

    private void createNotification(User recipient, String type, String message, 
                                   String actionUrl, NotificationType notificationType,
                                   Map<String, String> metadata) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(notificationType);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notification.setMetadata(metadata);
        notification.setRead(false);
        
        notificationRepository.save(notification);
        
        // Here you could also integrate with WebSocket or push notification service
        log.debug("Created notification for user: {}, type: {}", recipient.getId(), type);
    }
}
