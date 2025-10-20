package com.samjhadoo.model.ai;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.AIRequestType;
import com.samjhadoo.model.enums.AITier;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Logs all AI interactions for auditing and cost tracking
 */
@Data
@Entity
@Table(name = "ai_interactions")
public class AIInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AITier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIRequestType requestType;

    @Lob
    @Column
    private String prompt;

    @Lob
    @Column
    private String response;

    @Column(nullable = false)
    private int promptTokens = 0;

    @Column(nullable = false)
    private int completionTokens = 0;

    @Column(nullable = false)
    private int totalTokens = 0;

    @Column(precision = 10, scale = 6)
    private BigDecimal cost; // Cost in USD

    @Column(nullable = false)
    private boolean successful = true;

    @Lob
    @Column
    private String errorMessage;

    @Column
    private Long sessionId; // Related session if applicable

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private Integer responseTimeMs; // Response time in milliseconds
}
