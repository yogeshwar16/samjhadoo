package com.samjhadoo.model.ai;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.AITier;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Rate limiting for AI requests per user
 */
@Data
@Entity
@Table(name = "ai_rate_limits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "tier"})
})
public class AIRateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AITier tier;

    @Column(nullable = false)
    private int requestCount = 0;

    @Column(nullable = false)
    private LocalDateTime windowStart;

    @Column(nullable = false)
    private LocalDateTime windowEnd;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
