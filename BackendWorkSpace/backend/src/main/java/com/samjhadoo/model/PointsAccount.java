package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a user's points balance and lifetime statistics.
 */
@Entity
@Table(name = "points_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "lifetime_earned", nullable = false, precision = 12, scale = 2)
    private BigDecimal lifetimeEarned;
    
    @Column(name = "lifetime_spent", nullable = false, precision = 12, scale = 2)
    private BigDecimal lifetimeSpent;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Adds points to the account.
     * @param amount The number of points to add (must be positive)
     * @return The new balance
     */
    public BigDecimal addPoints(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        this.balance = this.balance.add(amount);
        this.lifetimeEarned = this.lifetimeEarned.add(amount);
        this.lastActivity = LocalDateTime.now();
        
        return this.balance;
    }
    
    /**
     * Spends points from the account.
     * @param amount The number of points to spend (must be positive and not exceed balance)
     * @return The new balance
     * @throws IllegalArgumentException if amount is invalid or insufficient balance
     */
    public BigDecimal spendPoints(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient points balance");
        }
        
        this.balance = this.balance.subtract(amount);
        this.lifetimeSpent = this.lifetimeSpent.add(amount);
        this.lastActivity = LocalDateTime.now();
        
        return this.balance;
    }
    
    @PrePersist
    protected void onCreate() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (lifetimeEarned == null) {
            lifetimeEarned = BigDecimal.ZERO;
        }
        if (lifetimeSpent == null) {
            lifetimeSpent = BigDecimal.ZERO;
        }
    }
}
