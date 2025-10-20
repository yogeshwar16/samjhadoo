package com.samjhadoo.model.payment;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payouts")
public class Payout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;
    
    @Column(name = "payout_method", nullable = false)
    private String payoutMethod; // BANK_TRANSFER, UPI, PAYPAL, etc.
    
    @Column(name = "payout_reference")
    private String payoutReference;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "gateway_response")
    private String gatewayResponse;
    
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PayoutStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum PayoutStatus {
        PENDING,        // Payout is created but not yet processed
        PROCESSING,     // Payout is being processed
        COMPLETED,      // Payout is successfully completed
        FAILED,         // Payout failed
        CANCELED,       // Payout was canceled
        REVERSED        // Payout was reversed (e.g., due to a dispute)
    }
}
