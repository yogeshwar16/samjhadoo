package com.samjhadoo.model.visualquery;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a visual query submitted by a user with media attachments.
 */
@Entity
@Table(name = "visual_queries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String queryId; // UUID for external reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private QueryCategory category;

    @Column(name = "ai_suggested_category")
    private String aiSuggestedCategory; // AI-suggested category before mentor confirmation

    @Column(name = "urgency_level", nullable = false)
    private int urgencyLevel; // 1-5 scale (5 = most urgent)

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic; // Whether other users can see this query

    @Column(name = "allow_mentor_bidding", nullable = false)
    private boolean allowMentorBidding; // Whether mentors can bid on this query

    @Column(name = "max_budget")
    private java.math.BigDecimal maxBudget; // Maximum budget for paid queries

    @Column(name = "preferred_mentor_id")
    private Long preferredMentorId; // If user has a preferred mentor

    @Column(name = "location_context")
    private String locationContext; // Geographic context if relevant

    @Column(name = "tags")
    private String tags; // Comma-separated tags for searchability

    @OneToMany(mappedBy = "visualQuery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VisualQueryMedia> mediaAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "visualQuery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VisualQueryResponse> responses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mentor_id")
    private User assignedMentor;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "response_deadline")
    private LocalDateTime responseDeadline; // Expected response time

    @Column(name = "resolution_rating")
    private int resolutionRating; // 1-5 stars after resolution

    @Lob
    @Column
    private String resolutionFeedback; // User's feedback after resolution

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "helpful_votes", nullable = false)
    private int helpfulVotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = QueryStatus.DRAFT;
        }
        if (urgencyLevel == 0) {
            urgencyLevel = 3; // Default medium urgency
        }
        if (viewCount == 0) {
            viewCount = 0;
        }
        if (helpfulVotes == 0) {
            helpfulVotes = 0;
        }
        if (isPublic) {
            isPublic = false; // Default to private
        }
        if (allowMentorBidding) {
            allowMentorBidding = false; // Default to no bidding
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Track first response time
        if (status == QueryStatus.IN_PROGRESS && firstResponseAt == null) {
            firstResponseAt = LocalDateTime.now();
        }

        // Track resolution time
        if (status == QueryStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }

    /**
     * Submits the query for review.
     * @return true if status was updated to SUBMITTED
     */
    public boolean submit() {
        if (status == QueryStatus.DRAFT) {
            status = QueryStatus.SUBMITTED;
            submittedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Assigns a mentor to this query.
     * @param mentor The mentor to assign
     * @return true if assigned successfully
     */
    public boolean assignMentor(User mentor) {
        if (status == QueryStatus.SUBMITTED || status == QueryStatus.UNDER_REVIEW) {
            this.assignedMentor = mentor;
            this.status = QueryStatus.IN_PROGRESS;
            return true;
        }
        return false;
    }

    /**
     * Marks the query as resolved.
     * @param rating User's satisfaction rating (1-5)
     * @param feedback User's feedback
     * @return true if marked as resolved
     */
    public boolean markResolved(int rating, String feedback) {
        if (status == QueryStatus.IN_PROGRESS || status == QueryStatus.RESPONDED) {
            this.status = QueryStatus.RESOLVED;
            this.resolutionRating = rating;
            this.resolutionFeedback = feedback;
            this.resolvedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Closes the query without resolution.
     * @return true if closed successfully
     */
    public boolean close() {
        if (status != QueryStatus.RESOLVED) {
            this.status = QueryStatus.CLOSED;
            this.closedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Increments the view count.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Adds a helpful vote.
     */
    public void addHelpfulVote() {
        this.helpfulVotes++;
    }

    /**
     * Checks if the query is currently active (can receive responses).
     * @return true if query is active
     */
    public boolean isActive() {
        return status == QueryStatus.SUBMITTED ||
               status == QueryStatus.UNDER_REVIEW ||
               status == QueryStatus.IN_PROGRESS;
    }

    /**
     * Gets the response time in hours since submission.
     * @return Hours since submission, or 0 if not submitted
     */
    public long getResponseTimeHours() {
        if (submittedAt != null) {
            return java.time.Duration.between(submittedAt, LocalDateTime.now()).toHours();
        }
        return 0;
    }

    /**
     * Checks if the query is overdue for response.
     * @return true if overdue
     */
    public boolean isOverdue() {
        return responseDeadline != null && responseDeadline.isBefore(LocalDateTime.now());
    }
}
