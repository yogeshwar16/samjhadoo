package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a badge that has been awarded to a specific user.
 */
@Entity
@Table(name = "user_badges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;
    
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
    
    @Column(name = "awarded_for")
    private String awardedFor; // Description of why the badge was awarded
    
    @Column(name = "is_notified", nullable = false)
    private boolean notified = false;
    
    @PrePersist
    protected void onCreate() {
        if (awardedAt == null) {
            awardedAt = LocalDateTime.now();
        }
    }
}
