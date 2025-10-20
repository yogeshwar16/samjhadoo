package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a friendly talk session between two users.
 */
@Entity
@Table(name = "friendly_talk_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendlyTalkSession {

    public enum SessionStatus {
        REQUESTED,      // One user requested a talk
        ACCEPTED,       // Both users accepted
        ACTIVE,         // Session is ongoing
        COMPLETED,      // Session ended normally
        CANCELLED,      // Session was cancelled
        REPORTED        // Session was reported for moderation
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator_mood")
    private MoodType initiatorMood;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_mood")
    private MoodType receiverMood;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "topic")
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "satisfaction_rating")
    private int satisfactionRating; // 1-5 scale

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Column(name = "report_reason")
    private String reportReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

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
        if (status == null) {
            status = SessionStatus.REQUESTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Accepts the talk request.
     * @return true if status was updated to ACCEPTED
     */
    public boolean acceptRequest() {
        if (status == SessionStatus.REQUESTED) {
            status = SessionStatus.ACCEPTED;
            return true;
        }
        return false;
    }

    /**
     * Starts the talk session.
     * @return true if status was updated to ACTIVE
     */
    public boolean startSession() {
        if (status == SessionStatus.ACCEPTED) {
            status = SessionStatus.ACTIVE;
            startedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Completes the talk session.
     * @return true if status was updated to COMPLETED
     */
    public boolean completeSession() {
        if (status == SessionStatus.ACTIVE) {
            status = SessionStatus.COMPLETED;
            endedAt = LocalDateTime.now();
            if (startedAt != null) {
                durationMinutes = (int) java.time.Duration.between(startedAt, endedAt).toMinutes();
            }
            return true;
        }
        return false;
    }

    /**
     * Cancels the talk session.
     * @return true if status was updated to CANCELLED
     */
    public boolean cancelSession() {
        if (status == SessionStatus.REQUESTED || status == SessionStatus.ACCEPTED) {
            status = SessionStatus.CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * Reports the session for moderation.
     * @param reason The reason for reporting
     * @return true if reported successfully
     */
    public boolean reportSession(String reason) {
        if (status != SessionStatus.REPORTED) {
            status = SessionStatus.REPORTED;
            reportReason = reason;
            reportedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Checks if the session is active.
     * @return true if session is currently active
     */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    /**
     * Gets the duration of the session in minutes.
     * @return Duration in minutes, or 0 if not ended
     */
    public int getDurationInMinutes() {
        if (startedAt != null && endedAt != null) {
            return (int) java.time.Duration.between(startedAt, endedAt).toMinutes();
        }
        return 0;
    }
}
