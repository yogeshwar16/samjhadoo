package com.samjhadoo.model;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.ChatParticipantRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a participant's membership and role in a chat room.
 */
@Entity
@Table(name = "chat_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatParticipantRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_online", nullable = false)
    private boolean online;

    @Column(name = "is_typing", nullable = false)
    private boolean typing;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    @Column(name = "mute_until")
    private LocalDateTime muteUntil;

    @Column(name = "messages_sent", nullable = false)
    private long messagesSent;

    @Column(name = "messages_read", nullable = false)
    private long messagesRead;

    @Column(name = "last_read_message_id")
    private String lastReadMessageId;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned; // Whether this conversation is pinned

    @Column(name = "custom_nickname")
    private String customNickname; // Custom display name in this room

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (messagesSent == 0) {
            messagesSent = 0;
        }
        if (messagesRead == 0) {
            messagesRead = 0;
        }
        if (notificationEnabled) {
            notificationEnabled = true; // Default to notifications enabled
        }
        if (online) {
            online = false; // Default to offline
        }
        if (typing) {
            typing = false; // Default to not typing
        }
        if (pinned) {
            pinned = false; // Default to not pinned
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the last seen timestamp.
     */
    public void updateLastSeen() {
        this.lastSeenAt = LocalDateTime.now();
        this.online = true;
    }

    /**
     * Updates the last message timestamp.
     */
    public void updateLastMessage() {
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * Marks the participant as offline.
     */
    public void markOffline() {
        this.online = false;
        this.typing = false;
    }

    /**
     * Sets the typing status.
     * @param typing Whether the user is typing
     */
    public void setTypingStatus(boolean typing) {
        this.typing = typing;
        if (typing) {
            this.online = true;
        }
    }

    /**
     * Increments the messages sent count.
     */
    public void incrementMessagesSent() {
        this.messagesSent++;
    }

    /**
     * Updates the messages read count and last read message.
     * @param messageId The last message ID that was read
     */
    public void updateMessagesRead(String messageId) {
        this.messagesRead++;
        this.lastReadMessageId = messageId;
    }

    /**
     * Mutes notifications for a period of time.
     * @param until Mute until this time
     */
    public void muteUntil(LocalDateTime until) {
        this.muteUntil = until;
        this.notificationEnabled = false;
    }

    /**
     * Unmutes notifications.
     */
    public void unmute() {
        this.muteUntil = null;
        this.notificationEnabled = true;
    }

    /**
     * Checks if notifications are currently muted.
     * @return true if muted
     */
    public boolean isMuted() {
        return muteUntil != null && muteUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the participant should receive notifications.
     * @return true if should receive notifications
     */
    public boolean shouldReceiveNotifications() {
        return notificationEnabled && !isMuted() && online;
    }

    /**
     * Gets the unread message count for this participant.
     * @return Number of unread messages
     */
    public long getUnreadMessageCount() {
        return messagesSent - messagesRead;
    }

    /**
     * Checks if the participant is currently active (online and not away).
     * @return true if active
     */
    public boolean isActive() {
        return online && (lastSeenAt == null ||
               lastSeenAt.isAfter(LocalDateTime.now().minusMinutes(5)));
    }

    /**
     * Gets the participant's activity status.
     * @return Activity status string
     */
    public String getActivityStatus() {
        if (!online) {
            if (lastSeenAt != null) {
                long minutesSinceLastSeen = java.time.Duration.between(lastSeenAt, LocalDateTime.now()).toMinutes();
                if (minutesSinceLastSeen < 60) {
                    return "Away for " + minutesSinceLastSeen + " minutes";
                } else {
                    long hours = minutesSinceLastSeen / 60;
                    return "Away for " + hours + " hours";
                }
            }
            return "Offline";
        }

        if (typing) {
            return "Typing...";
        }

        return "Online";
    }

    /**
     * Checks if the participant has admin rights.
     * @return true if owner, admin, or moderator
     */
    public boolean hasAdminRights() {
        return role == ChatParticipantRole.OWNER ||
               role == ChatParticipantRole.ADMIN ||
               role == ChatParticipantRole.MODERATOR;
    }

    /**
     * Checks if the participant can moderate the room.
     * @return true if can moderate
     */
    public boolean canModerate() {
        return role == ChatParticipantRole.OWNER ||
               role == ChatParticipantRole.ADMIN;
    }

    /**
     * Gets the participant's display name for this room.
     * @return Display name (custom nickname or real name)
     */
    public String getDisplayName() {
        return customNickname != null ? customNickname : user.getFirstName() + " " + user.getLastName();
    }

    /**
     * Updates the participant's custom nickname.
     * @param nickname New nickname
     */
    public void updateNickname(String nickname) {
        this.customNickname = nickname;
    }

    /**
     * Pins or unpins this conversation.
     * @param pinned Whether to pin
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Gets the participant's engagement score.
     * @return Engagement score based on activity
     */
    public int getEngagementScore() {
        int score = 0;

        // Online status bonus
        if (online) score += 10;

        // Message activity bonus
        if (messagesSent > 0) score += Math.min(30, messagesSent);

        // Read rate bonus
        if (messagesSent > 0) {
            double readRate = (double) messagesRead / messagesSent;
            score += (int) (readRate * 20);
        }

        // Recent activity bonus
        if (lastMessageAt != null && lastMessageAt.isAfter(LocalDateTime.now().minusHours(1))) {
            score += 15;
        }

        // Pinned conversations get bonus
        if (pinned) score += 5;

        return Math.min(100, score);
    }

    /**
     * Checks if the participant is eligible for auto-removal due to inactivity.
     * @return true if should be auto-removed
     */
    public boolean shouldAutoRemove() {
        if (role == ChatParticipantRole.OWNER) {
            return false; // Never auto-remove owners
        }

        if (lastSeenAt == null) {
            return false; // No activity data
        }

        // Auto-remove after 90 days of inactivity for non-admin participants
        return lastSeenAt.isBefore(LocalDateTime.now().minusDays(90)) &&
               role != ChatParticipantRole.ADMIN &&
               role != ChatParticipantRole.MODERATOR;
    }
}
