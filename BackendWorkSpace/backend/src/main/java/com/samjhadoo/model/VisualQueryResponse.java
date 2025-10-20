package com.samjhadoo.model.visualquery;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a mentor's response to a visual query with annotations and media.
 */
@Entity
@Table(name = "visual_query_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryResponse {

    public enum ResponseType {
        TEXT,           // Text-only response
        ANNOTATED,      // Response with image annotations
        VIDEO,          // Video response
        AUDIO,          // Audio response
        COMPREHENSIVE   // Multiple media types
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id", nullable = false)
    private VisualQuery visualQuery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseType responseType;

    @Lob
    @Column(nullable = false)
    private String content; // Main response content

    @Column(name = "is_solution", nullable = false)
    private boolean isSolution; // Whether this response provides the solution

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes; // Time to implement solution

    @Column(name = "difficulty_level")
    private String difficultyLevel; // EASY, MEDIUM, HARD

    @Column(name = "required_materials")
    private String requiredMaterials; // Comma-separated list

    @Column(name = "cost_estimate")
    private BigDecimal costEstimate; // Estimated cost

    @Column(name = "step_by_step_guide")
    private String stepByStepGuide; // Detailed steps in JSON format

    @Column(name = "alternative_solutions")
    private String alternativeSolutions; // Alternative approaches

    @Column(name = "safety_precautions")
    private String safetyPrecautions; // Safety warnings

    @Column(name = "references")
    private String references; // External references or sources

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VisualQueryAnnotation> annotations = new ArrayList<>();

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VisualQueryResponseMedia> responseMedia = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_helpful", nullable = false)
    private boolean isHelpful;

    @Column(name = "helpful_votes", nullable = false)
    private int helpfulVotes;

    @Column(name = "not_helpful_votes", nullable = false)
    private int notHelpfulVotes;

    @Column(name = "mentor_rating")
    private int mentorRating; // Rating given by user (1-5)

    @Lob
    @Column
    private String mentorFeedback; // Feedback from user about this response

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (helpfulVotes == 0) {
            helpfulVotes = 0;
        }
        if (notHelpfulVotes == 0) {
            notHelpfulVotes = 0;
        }
        if (!isHelpful) {
            isHelpful = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a helpful vote to this response.
     */
    public void addHelpfulVote() {
        this.helpfulVotes++;
        this.isHelpful = this.helpfulVotes > this.notHelpfulVotes;
    }

    /**
     * Adds a not helpful vote to this response.
     */
    public void addNotHelpfulVote() {
        this.notHelpfulVotes++;
        this.isHelpful = this.helpfulVotes > this.notHelpfulVotes;
    }

    /**
     * Sets the mentor rating from the user.
     * @param rating Rating (1-5)
     * @param feedback User feedback
     */
    public void setMentorRating(int rating, String feedback) {
        this.mentorRating = rating;
        this.mentorFeedback = feedback;
    }

    /**
     * Gets the total votes for this response.
     * @return Total votes (helpful + not helpful)
     */
    public int getTotalVotes() {
        return helpfulVotes + notHelpfulVotes;
    }

    /**
     * Gets the helpfulness ratio as a percentage.
     * @return Helpfulness percentage (0-100)
     */
    public double getHelpfulnessRatio() {
        int totalVotes = getTotalVotes();
        if (totalVotes == 0) {
            return 0;
        }
        return ((double) helpfulVotes / totalVotes) * 100;
    }

    /**
     * Checks if this response is considered highly helpful.
     * @return true if helpfulness ratio >= 80%
     */
    public boolean isHighlyHelpful() {
        return getHelpfulnessRatio() >= 80 && getTotalVotes() >= 3;
    }

    /**
     * Checks if this response provides a complete solution.
     * @return true if marked as solution
     */
    public boolean providesSolution() {
        return isSolution;
    }

    /**
     * Gets the response complexity score based on content length and media.
     * @return Complexity score (1-10)
     */
    public int getComplexityScore() {
        int score = 1;

        // Base score from content length
        if (content != null) {
            int contentLength = content.length();
            if (contentLength > 1000) score += 2;
            else if (contentLength > 500) score += 1;
        }

        // Add score for media attachments
        score += Math.min(3, responseMedia.size());

        // Add score for annotations
        score += Math.min(2, annotations.size());

        // Add score for step-by-step guide
        if (stepByStepGuide != null && !stepByStepGuide.trim().isEmpty()) {
            score += 2;
        }

        return Math.min(10, score);
    }
}
