package com.samjhadoo.model.gamification;

import com.samjhadoo.model.enums.gamification.AchievementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an achievement that users can earn by completing specific actions.
 */
@Entity
@Table(name = "achievements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;
    
    @Column(nullable = false)
    private String name;
    
    @Lob
    @Column
    private String description;
    
    @Column(nullable = false)
    private int threshold;
    
    @Column(name = "points_reward")
    private int pointsReward;
    
    @Column(name = "is_repeatable", nullable = false)
    private boolean repeatable = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
