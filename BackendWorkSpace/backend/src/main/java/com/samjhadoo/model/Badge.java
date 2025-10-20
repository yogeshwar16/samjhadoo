package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.gamification.BadgeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a badge that can be awarded to users for various achievements.
 */
@Entity
@Table(name = "badges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType type;
    
    @Lob
    @Column
    private String description;
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Column(name = "points_value")
    private Integer pointsValue;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
