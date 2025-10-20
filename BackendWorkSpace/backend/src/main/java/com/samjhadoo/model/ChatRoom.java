package com.samjhadoo.model;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.ChatRoomType;
import com.samjhadoo.model.enums.communication.ChatParticipantRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat room for real-time communication.
 */
@Entity
@Table(name = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId; // UUID for external reference

    @Column(nullable = false)
    private String name;

    @Lob
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic; // Whether room is discoverable

    @Column(name = "is_archived", nullable = false)
    private boolean archived;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private int currentParticipants;

    @Column(name = "is_encrypted", nullable = false)
    private boolean encrypted; // End-to-end encryption

    @Column(name = "allow_file_sharing", nullable = false)
    private boolean allowFileSharing;

    @Column(name = "allow_voice_notes", nullable = false)
    private boolean allowVoiceNotes;

    @Column(name = "allow_screen_share", nullable = false)
    private boolean allowScreenShare;

    @Column(name = "allow_whiteboard", nullable = false)
    private boolean allowWhiteboard;

    @Column(name = "require_approval", nullable = false)
    private boolean requireApproval; // New participants need approval

    @Column(name = "auto_delete_messages", nullable = false)
    private boolean autoDeleteMessages;

    @Column(name = "message_retention_days")
    private Integer messageRetentionDays; // Days to keep messages

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (maxParticipants == 0) {
            maxParticipants = type == ChatRoomType.DIRECT ? 2 : 50; // Default limits
        }
        if (currentParticipants == 0) {
            currentParticipants = 0;
        }
        if (messageRetentionDays == null) {
            messageRetentionDays = 90; // Default 90 days
        }
        if (autoDeleteMessages) {
            autoDeleteMessages = false; // Default to keep messages
        }
        if (requireApproval) {
            requireApproval = false; // Default to no approval required
        }
        if (encrypted) {
            encrypted = false; // Default to not encrypted
        }
        if (allowFileSharing) {
            allowFileSharing = true; // Default to allow file sharing
        }
        if (allowVoiceNotes) {
            allowVoiceNotes = true; // Default to allow voice notes
        }
        if (allowScreenShare) {
            allowScreenShare = false; // Default to no screen share
        }
        if (allowWhiteboard) {
            allowWhiteboard = false; // Default to no whiteboard
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
    }

    /**
     * Adds a participant to the chat room.
     * @param user The user to add
     * @param role The participant's role
     * @return true if added successfully
     */
    public boolean addParticipant(User user, ChatParticipantRole role) {
        if (currentParticipants >= maxParticipants) {
            return false;
        }

        if (participants.stream().anyMatch(p -> p.getUser().getId().equals(user.getId()))) {
            return false; // Already a participant
        }

        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(this)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        participants.add(participant);
        currentParticipants = participants.size();

        return true;
    }

    /**
     * Removes a participant from the chat room.
     * @param user The user to remove
     * @return true if removed successfully
     */
    public boolean removeParticipant(User user) {
        ChatParticipant participant = participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            return false;
        }

        participants.remove(participant);
        currentParticipants = participants.size();

        return true;
    }

    /**
     * Updates a participant's role.
     * @param user The user
     * @param newRole The new role
     * @return true if updated successfully
     */
    public boolean updateParticipantRole(User user, ChatParticipantRole newRole) {
        ChatParticipant participant = participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            return false;
        }

        participant.setRole(newRole);
        return true;
    }

    /**
     * Checks if a user is a participant in this room.
     * @param user The user
     * @return true if participant
     */
    public boolean isParticipant(User user) {
        return participants.stream().anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    /**
     * Gets the participant's role in this room.
     * @param user The user
     * @return The role or null if not a participant
     */
    public ChatParticipantRole getParticipantRole(User user) {
        return participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .map(ChatParticipant::getRole)
                .orElse(null);
    }

    /**
     * Checks if a user can send messages in this room.
     * @param user The user
     * @return true if can send messages
     */
    public boolean canUserSendMessages(User user) {
        ChatParticipantRole role = getParticipantRole(user);
        if (role == null) {
            return false;
        }

        return role != ChatParticipantRole.MUTED && role != ChatParticipantRole.BANNED;
    }

    /**
     * Checks if a user has moderation rights in this room.
     * @param user The user
     * @return true if has moderation rights
     */
    public boolean hasModerationRights(User user) {
        ChatParticipantRole role = getParticipantRole(user);
        if (role == null) {
            return false;
        }

        return role == ChatParticipantRole.OWNER ||
               role == ChatParticipantRole.ADMIN ||
               role == ChatParticipantRole.MODERATOR;
    }

    /**
     * Archives the chat room.
     */
    public void archive() {
        this.archived = true;
        this.active = false;
    }

    /**
     * Unarchives the chat room.
     */
    public void unarchive() {
        this.archived = false;
        this.active = true;
    }

    /**
     * Updates the last message timestamp.
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * Checks if the room is currently active.
     * @return true if room is active and not archived
     */
    public boolean isCurrentlyActive() {
        return active && !archived;
    }

    /**
     * Checks if the room is full.
     * @return true if at maximum capacity
     */
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    /**
     * Gets the room's activity level based on recent messages.
     * @return Activity level (LOW, MEDIUM, HIGH)
     */
    public String getActivityLevel() {
        if (lastMessageAt == null) {
            return "INACTIVE";
        }

        long hoursSinceLastMessage = java.time.Duration.between(lastMessageAt, LocalDateTime.now()).toHours();

        if (hoursSinceLastMessage < 1) {
            return "HIGH";
        } else if (hoursSinceLastMessage < 24) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Checks if the room supports file sharing.
     * @return true if file sharing is allowed
     */
    public boolean supportsFileSharing() {
        return allowFileSharing && isCurrentlyActive();
    }

    /**
     * Checks if the room supports voice notes.
     * @return true if voice notes are allowed
     */
    public boolean supportsVoiceNotes() {
        return allowVoiceNotes && isCurrentlyActive();
    }

    /**
     * Checks if the room supports screen sharing.
     * @return true if screen sharing is allowed
     */
    public boolean supportsScreenShare() {
        return allowScreenShare && isCurrentlyActive();
    }

    /**
     * Checks if the room supports collaborative whiteboard.
     * @return true if whiteboard is allowed
     */
    public boolean supportsWhiteboard() {
        return allowWhiteboard && isCurrentlyActive();
    }

    /**
     * Gets the room's message count.
     * @return Number of messages in the room
     */
    public long getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    /**
     * Checks if the room is a direct message (1-on-1).
     * @return true if direct message
     */
    public boolean isDirectMessage() {
        return type == ChatRoomType.DIRECT;
    }

    /**
     * Checks if the room is a group chat.
     * @return true if group chat
     */
    public boolean isGroupChat() {
        return type == ChatRoomType.GROUP || type == ChatRoomType.SESSION || type == ChatRoomType.COMMUNITY;
    }

    /**
     * Gets the room's privacy level.
     * @return Privacy level description
     */
    public String getPrivacyLevel() {
        if (isPublic) {
            return "PUBLIC";
        } else if (requireApproval) {
            return "PRIVATE_APPROVAL";
        } else {
            return "PRIVATE";
        }
    }

    /**
     * Checks if the room requires new participant approval.
     * @return true if approval required
     */
    public boolean requiresParticipantApproval() {
        return requireApproval && isCurrentlyActive();
    }

    /**
     * Gets the room's security features summary.
     * @return Map of security features
     */
    public java.util.Map<String, Object> getSecurityFeatures() {
        java.util.Map<String, Object> features = new java.util.HashMap<>();
        features.put("encrypted", encrypted);
        features.put("requireApproval", requireApproval);
        features.put("autoDeleteMessages", autoDeleteMessages);
        features.put("messageRetentionDays", messageRetentionDays);
        return features;
    }

    /**
     * Checks if the room is eligible for auto-cleanup.
     * @return true if should be cleaned up
     */
    public boolean shouldAutoCleanup() {
        if (lastActivityAt == null) {
            return false;
        }

        // Cleanup inactive rooms after 30 days
        return lastActivityAt.isBefore(LocalDateTime.now().minusDays(30)) &&
               currentParticipants == 0 &&
               !isPublic;
    }

    /**
     * Gets the room's health score based on activity and engagement.
     * @return Health score (0-100)
     */
    public int getHealthScore() {
        int score = 50; // Base score

        // Activity level bonus
        String activity = getActivityLevel();
        switch (activity) {
            case "HIGH" -> score += 30;
            case "MEDIUM" -> score += 15;
            case "LOW" -> score -= 10;
        }

        // Participant count bonus
        if (currentParticipants > 0) {
            score += Math.min(20, currentParticipants * 2);
        }

        // Public rooms get bonus for discoverability
        if (isPublic) {
            score += 10;
        }

        // Encrypted rooms get security bonus
        if (encrypted) {
            score += 5;
        }

        return Math.min(100, score);
    }

    /**
     * Updates room settings.
     * @param name New name
     * @param description New description
     * @param allowFileSharing New file sharing setting
     * @param allowVoiceNotes New voice notes setting
     * @param allowScreenShare New screen share setting
     * @param allowWhiteboard New whiteboard setting
     */
    public void updateSettings(String name, String description, boolean allowFileSharing,
                              boolean allowVoiceNotes, boolean allowScreenShare, boolean allowWhiteboard) {
        this.name = name;
        this.description = description;
        this.allowFileSharing = allowFileSharing;
        this.allowVoiceNotes = allowVoiceNotes;
        this.allowScreenShare = allowScreenShare;
        this.allowWhiteboard = allowWhiteboard;
    }
}
