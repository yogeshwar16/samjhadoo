package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks a user's progress toward earning a specific achievement.
 */
@Entity
@Table(name = "user_achievements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(nullable = false)
    private int progress;
    
    @Column(name = "is_completed", nullable = false)
    private boolean completed;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        
        // Check if achievement is completed
        if (!completed && progress >= achievement.getThreshold()) {
            completed = true;
            completedAt = LocalDateTime.now();
        }
    }
}
