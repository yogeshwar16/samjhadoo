package com.samjhadoo.model.friendlytalk;

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
@Table(name = "user_reports")
public class UserReport extends DateAudit {
    
    public enum ReportStatus {
        PENDING,    // New report, awaiting review
        IN_REVIEW,  // Currently being reviewed
        RESOLVED,   // Action taken and resolved
        DISMISSED,  // No action needed
        ESCALATED   // Needs higher-level review
    }
    
    public enum ReportType {
        HARASSMENT,     // Bullying, threats, or personal attacks
        HATE_SPEECH,    // Discriminatory or hateful content
        SPAM,           // Unwanted promotional content
        INAPPROPRIATE,  // NSFW or otherwise inappropriate content
        IMPERSONATION,  // Pretending to be someone else
        OTHER           // Other types of violations
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    @Column(name = "reported_user_id", nullable = false)
    private String reportedUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private FriendlyRoom room;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType type;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(name = "message_id")
    private String messageId;
    
    @Column(name = "message_snapshot", length = 2000)
    private String messageSnapshot;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;
    
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    
    @Column(name = "resolved_by")
    private String resolvedBy;
    
    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;
    
    @Column(name = "admin_notes", length = 2000)
    private String adminNotes;
    
    @Column(name = "is_reported_user_notified")
    private boolean isReportedUserNotified = false;
    
    @Column(name = "is_reporter_notified")
    private boolean isReporterNotified = false;
    
    @Column(name = "is_escalated")
    private boolean isEscalated = false;
    
    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;
    
    @Column(name = "moderator_actions", length = 1000)
    private String moderatorActions; // JSON string of actions taken
    
    // Helper methods
    public void resolve(String moderatorId, String notes) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        this.resolvedBy = moderatorId;
        this.resolutionNotes = notes;
    }
    
    public void dismiss(String moderatorId, String reason) {
        this.status = ReportStatus.DISMISSED;
        this.resolvedAt = Instant.now();
        this.resolvedBy = moderatorId;
        this.resolutionNotes = reason;
    }
    
    public void escalate(String reason, String notes) {
        this.status = ReportStatus.ESCALATED;
        this.isEscalated = true;
        this.escalationReason = reason;
        this.adminNotes = notes;
    }
}
