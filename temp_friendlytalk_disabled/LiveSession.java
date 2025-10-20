package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "live_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @ElementCollection
    @CollectionTable(name = "live_session_tags", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    @Column(name = "scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;

    @Column(name = "max_participants")
    private Integer maxParticipants = 50;

    @Column(name = "current_participants")
    private Integer currentParticipants = 0;

    @Column(name = "meeting_url")
    private String meetingUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_recorded")
    private Boolean isRecorded = false;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @ManyToMany
    @JoinTable(
        name = "live_session_participants",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        SCHEDULED,
        LIVE,
        ENDED,
        CANCELLED
    }

    public enum SessionType {
        NIGHT_RADIO,           // Nighttime Video Radio
        NIGHT_LORI,            // Nighttime Lori (lullabies/stories)
        ON_DEMAND_MOTIVATION,  // On-Demand Motivation
        LIVE_MOTIVATION_TALK,  // Live Motivation Talks
        LIVE_TOPIC_SESSION,    // Live Topic Sessions (career, love, etc.)
        LOVE_LIFE_TIPS,        // Love & Life Tips
        GENERAL_TALK           // General Friendly Talk
    }

    // Helper methods
    public boolean isLive() {
        return status == SessionStatus.LIVE;
    }

    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    public boolean canJoin() {
        return isLive() && !isFull();
    }

    public void addParticipant(User user) {
        if (participants.add(user)) {
            currentParticipants = participants.size();
        }
    }

    public void removeParticipant(User user) {
        if (participants.remove(user)) {
            currentParticipants = participants.size();
        }
    }
}
