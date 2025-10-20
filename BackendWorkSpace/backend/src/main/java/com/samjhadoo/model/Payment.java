package com.samjhadoo.model.payment;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_gateway_id")
    private String paymentGatewayId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(name = "gateway_response")
    private String gatewayResponse;
    
    @Column(name = "invoice_url")
    private String invoiceUrl;
    
    @Column(name = "receipt_url")
    private String receiptUrl;
    
    @Column(name = "is_refunded")
    private boolean isRefunded;
    
    @Column(name = "refund_amount")
    private BigDecimal refundAmount;
    
    @Column(name = "refund_reason")
    private String refundReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        DISPUTED,
        CANCELED
    }
}
