package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a safety report for inappropriate content or behavior in friendly talk.
 */
@Entity
@Table(name = "safety_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyReport {

    public enum ReportType {
        HARASSMENT,
        INAPPROPRIATE_CONTENT,
        SPAM,
        HATE_SPEECH,
        VIOLENCE,
        SEXUAL_CONTENT,
        SCAM,
        IMPERSONATION,
        OTHER
    }

    public enum ReportStatus {
        PENDING,
        UNDER_REVIEW,
        RESOLVED,
        DISMISSED,
        ESCALATED
    }

    public enum ReportSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private FriendlyTalkSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private FriendlyTalkRoom room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportSeverity severity;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "evidence_url")
    private String evidenceUrl; // URL to recorded evidence if available

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "follow_up_required", nullable = false)
    private boolean followUpRequired;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "escalated_to")
    private String escalatedTo; // Email or system of escalation

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReportStatus.PENDING;
        }
        if (severity == null) {
            severity = ReportSeverity.MEDIUM;
        }
        if (!followUpRequired) {
            followUpRequired = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == ReportStatus.RESOLVED || status == ReportStatus.DISMISSED) {
            if (resolvedAt == null) {
                resolvedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Marks the report as under review.
     */
    public void markUnderReview() {
        if (status == ReportStatus.PENDING) {
            status = ReportStatus.UNDER_REVIEW;
        }
    }

    /**
     * Resolves the report.
     * @param resolver The user who resolved it
     * @param notes Resolution notes
     * @param action Action taken
     */
    public void resolve(User resolver, String notes, String action) {
        this.status = ReportStatus.RESOLVED;
        this.resolver = resolver;
        this.resolutionNotes = notes;
        this.actionTaken = action;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Dismisses the report.
     * @param resolver The user who dismissed it
     * @param notes Dismissal notes
     */
    public void dismiss(User resolver, String notes) {
        this.status = ReportStatus.DISMISSED;
        this.resolver = resolver;
        this.resolutionNotes = notes;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Escalates the report.
     * @param escalatedTo The system/email to escalate to
     */
    public void escalate(String escalatedTo) {
        this.status = ReportStatus.ESCALATED;
        this.escalatedTo = escalatedTo;
    }

    /**
     * Checks if the report requires follow-up.
     * @return true if follow-up is required
     */
    public boolean requiresFollowUp() {
        return followUpRequired && followUpDate != null && followUpDate.isBefore(LocalDateTime.now());
    }

    /**
     * Gets the age of the report in hours.
     * @return Hours since report was created
     */
    public long getAgeInHours() {
        if (reportedAt != null) {
            return java.time.Duration.between(reportedAt, LocalDateTime.now()).toHours();
        }
        return 0;
    }
}
