package com.samjhadoo.model.payment;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "plan_id", nullable = false)
    private String planId;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;
    
    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;
    
    @Column(name = "ends_at")
    private LocalDate endsAt;
    
    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;
    
    @Column(name = "is_auto_renew", nullable = false)
    private boolean isAutoRenew = true;
    
    @Column(name = "payment_gateway_subscription_id")
    private String paymentGatewaySubscriptionId;
    
    @Column(name = "payment_gateway_customer_id")
    private String paymentGatewayCustomerId;
    
    @Column(name = "trial_ends_at")
    private LocalDate trialEndsAt;
    
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    
    @Column(name = "cancel_reason")
    private String cancelReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        if (startsAt == null) {
            startsAt = LocalDate.now();
        }
        if (nextBillingDate == null && billingCycle != null) {
            nextBillingDate = calculateNextBillingDate();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private LocalDate calculateNextBillingDate() {
        if (billingCycle == null) return null;
        
        return switch (billingCycle) {
            case MONTHLY -> startsAt.plusMonths(1);
            case QUARTERLY -> startsAt.plusMonths(3);
            case BIANNUAL -> startsAt.plusMonths(6);
            case ANNUAL -> startsAt.plusYears(1);
        };
    }
    
    public enum SubscriptionStatus {
        ACTIVE,
        TRIALING,
        PAST_DUE,
        CANCELED,
        UNPAID,
        INCOMPLETE,
        INCOMPLETE_EXPIRED,
        PAUSED
    }
    
    public enum BillingCycle {
        MONTHLY,
        QUARTERLY,
        BIANNUAL,
        ANNUAL
    }
}
