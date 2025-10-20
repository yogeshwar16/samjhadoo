package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a queue entry for users waiting to be matched for friendly talk.
 */
@Entity
@Table(name = "friendly_talk_queue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendlyTalkQueue {

    public enum QueueStatus {
        WAITING,        // Waiting for match
        MATCHING,       // Currently being matched
        MATCHED,        // Successfully matched
        EXPIRED,        // Queue entry expired
        CANCELLED       // User cancelled the request
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type")
    private MoodType moodType;

    @Column(name = "intensity")
    private int intensity;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "preferred_topics")
    private String preferredTopics; // Comma-separated topics

    @Column(name = "avoid_topics")
    private String avoidTopics; // Comma-separated topics to avoid

    @Column(name = "max_wait_minutes", nullable = false)
    private int maxWaitMinutes;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "estimated_wait_minutes")
    private int estimatedWaitMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_with_id")
    private User matchedWith;

    @Column(name = "matching_criteria", columnDefinition = "TEXT")
    private String matchingCriteria; // JSON string of matching preferences

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(maxWaitMinutes > 0 ? maxWaitMinutes : 30);
        }
        if (status == null) {
            status = QueueStatus.WAITING;
        }
        if (retryCount == 0) {
            retryCount = 0;
        }
        if (maxWaitMinutes == 0) {
            maxWaitMinutes = 30; // Default 30 minutes
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update expiration if max wait time changes
        if (status == QueueStatus.WAITING && maxWaitMinutes > 0) {
            expiresAt = joinedAt.plusMinutes(maxWaitMinutes);
        }
    }

    /**
     * Checks if the queue entry is expired.
     * @return true if expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Marks the queue entry as matched.
     * @param matchedWith The user they were matched with
     */
    public void markAsMatched(User matchedWith) {
        this.status = QueueStatus.MATCHED;
        this.matchedWith = matchedWith;
        this.matchedAt = LocalDateTime.now();
    }

    /**
     * Marks the queue entry as expired.
     */
    public void markAsExpired() {
        if (status == QueueStatus.WAITING) {
            this.status = QueueStatus.EXPIRED;
        }
    }

    /**
     * Cancels the queue entry.
     */
    public void cancel() {
        if (status == QueueStatus.WAITING || status == QueueStatus.MATCHING) {
            this.status = QueueStatus.CANCELLED;
        }
    }

    /**
     * Increments retry count for matching attempts.
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * Checks if the queue entry is currently active.
     * @return true if waiting for match
     */
    public boolean isActive() {
        return status == QueueStatus.WAITING && !isExpired();
    }

    /**
     * Gets the time spent waiting in minutes.
     * @return Minutes spent waiting
     */
    public int getWaitingTimeMinutes() {
        if (joinedAt != null) {
            return (int) java.time.Duration.between(joinedAt, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }
}
