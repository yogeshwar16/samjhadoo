package com.samjhadoo.model.ai;

import com.samjhadoo.model.enums.AITier;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Configuration for AI services
 */
@Data
@Entity
@Table(name = "ai_configs")
public class AIConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AITier tier;

    @Column(nullable = false)
    private String modelName; // e.g., "gpt-4", "gpt-3.5-turbo"

    @Column(nullable = false)
    private int maxTokens = 1000;

    @Column(nullable = false)
    private int requestLimitPerHour = 10;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 1000)
    private String systemPrompt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
