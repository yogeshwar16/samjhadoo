package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.gamification.PointsReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records all point transactions for auditing and history.
 */
@Entity
@Table(name = "points_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private PointsAccount account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal delta;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointsReason reason;
    
    @Column(name = "reference_id")
    private String referenceId; // For linking to the source entity (e.g., session ID, achievement ID)
    
    @Lob
    @Column
    private String description;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;
    
    @Column(name = "reversal_reason")
    private String reversalReason;
    
    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;
    
    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (delta == null) {
            delta = BigDecimal.ZERO;
        }
    }
    
    /**
     * Creates a reversal transaction for this transaction.
     * @param reason The reason for the reversal
     * @return A new transaction that reverses this one
     */
    public PointsTransaction createReversal(String reason) {
        if (reversed) {
            throw new IllegalStateException("Transaction already reversed");
        }
        
        PointsTransaction reversal = PointsTransaction.builder()
            .account(this.account)
            .user(this.user)
            .delta(this.delta.negate()) // Reverse the sign
            .reason(this.reason)
            .referenceId("REVERSAL_OF_" + this.id)
            .description("Reversal: " + (this.description != null ? this.description : ""))
            .reversalReason(reason)
            .reversedAt(LocalDateTime.now())
            .build();
        
        // Mark this transaction as reversed
        this.reversed = true;
        this.reversalReason = reason;
        this.reversedAt = LocalDateTime.now();
        
        return reversal;
    }
}
