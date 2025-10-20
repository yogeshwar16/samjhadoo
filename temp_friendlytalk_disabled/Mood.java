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
 * Represents a user's current mood status for friendly talk matching.
 */
@Entity
@Table(name = "moods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoodType moodType;

    @Column(name = "intensity", nullable = false)
    private int intensity; // 1-10 scale

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "looking_for_talk", nullable = false)
    private boolean lookingForTalk;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(2); // Default 2 hours
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if the mood is currently active.
     * @return true if mood is active and not expired
     */
    public boolean isActive() {
        return expiresAt != null && !expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Updates the mood with new values.
     * @param moodType New mood type
     * @param intensity New intensity
     * @param description New description
     * @param anonymous New anonymous setting
     */
    public void updateMood(MoodType moodType, int intensity, String description, boolean anonymous) {
        this.moodType = moodType;
        this.intensity = intensity;
        this.description = description;
        this.anonymous = anonymous;
        this.lastUpdated = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(2);
    }
}
