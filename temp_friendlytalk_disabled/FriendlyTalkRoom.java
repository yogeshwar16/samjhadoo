package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.RoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a friendly talk audio room where users can join for conversations.
 */
@Entity
@Table(name = "friendly_talk_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendlyTalkRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId; // UUID for room identification

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private int currentParticipants;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "is_recorded", nullable = false)
    private boolean recorded;

    @Column(name = "allow_screen_share", nullable = false)
    private boolean allowScreenShare;

    @Column(name = "require_moderator_approval", nullable = false)
    private boolean requireModeratorApproval;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "room_participants",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> participants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "topic_tags")
    private String topicTags; // Comma-separated topics

    @Column(name = "mood_focus")
    private String moodFocus; // Primary mood this room focuses on

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RoomStatus.WAITING;
        }
        if (maxParticipants == 0) {
            maxParticipants = 6; // Default max participants
        }
        if (currentParticipants == 0) {
            currentParticipants = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update participant count when participants list changes
        this.currentParticipants = this.participants.size();
    }

    /**
     * Checks if the room is full.
     * @return true if room has reached max participants
     */
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    /**
     * Checks if the room is active (has participants and is not closed).
     * @return true if room is active
     */
    public boolean isActive() {
        return status == RoomStatus.ACTIVE && !participants.isEmpty();
    }

    /**
     * Adds a participant to the room.
     * @param user The user to add
     * @return true if added successfully, false if room is full
     */
    public boolean addParticipant(User user) {
        if (isFull()) {
            return false;
        }

        if (!participants.contains(user)) {
            participants.add(user);
            currentParticipants = participants.size();

            if (currentParticipants == 1 && status == RoomStatus.WAITING) {
                status = RoomStatus.ACTIVE;
                startedAt = LocalDateTime.now();
            }
        }

        return true;
    }

    /**
     * Removes a participant from the room.
     * @param user The user to remove
     * @return true if removed successfully
     */
    public boolean removeParticipant(User user) {
        boolean removed = participants.remove(user);
        if (removed) {
            currentParticipants = participants.size();

            if (currentParticipants == 0 && status == RoomStatus.ACTIVE) {
                status = RoomStatus.CLOSED;
                endedAt = LocalDateTime.now();
                if (startedAt != null) {
                    durationMinutes = (int) java.time.Duration.between(startedAt, endedAt).toMinutes();
                }
            }
        }
        return removed;
    }

    /**
     * Closes the room.
     */
    public void closeRoom() {
        this.status = RoomStatus.CLOSED;
        this.endedAt = LocalDateTime.now();
        if (startedAt != null) {
            this.durationMinutes = (int) java.time.Duration.between(startedAt, endedAt).toMinutes();
        }
    }
}
