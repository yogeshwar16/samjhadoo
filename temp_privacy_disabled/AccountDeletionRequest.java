package com.samjhadoo.model.privacy;

import com.samjhadoo.model.User;
import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_deletion_requests")
public class AccountDeletionRequest extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionStatus status = DeletionStatus.PENDING;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
    
    @Column(name = "scheduled_for")
    private Instant scheduledFor; // Grace period (e.g., 30 days)
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "verification_code", length = 100)
    private String verificationCode;
    
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
    
    @Column(name = "backup_created", nullable = false)
    private boolean backupCreated = false;
    
    @Column(name = "backup_url", length = 1000)
    private String backupUrl;
    
    public enum DeletionStatus {
        PENDING,           // Request submitted, awaiting verification
        VERIFIED,          // User verified the request
        SCHEDULED,         // Scheduled for deletion (grace period)
        PROCESSING,        // Deletion in progress
        COMPLETED,         // Account deleted
        CANCELLED,         // User cancelled the request
        FAILED             // Deletion failed
    }
}
