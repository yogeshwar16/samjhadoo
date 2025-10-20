package com.samjhadoo.model.topic;

import com.samjhadoo.model.enums.*;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Topics that mentees can explore and mentors can adopt
 */
@Data
@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicCategory category;

    @ElementCollection
    @CollectionTable(name = "topic_tags", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TopicDifficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "topic_target_communities", joinColumns = @JoinColumn(name = "topic_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "community_tag")
    private Set<CommunityTag> targetCommunities = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "topic_languages", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "language")
    private Set<String> supportedLanguages = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status = TopicStatus.PENDING;

    @Column(nullable = false)
    private boolean aiGenerated = false;

    @Column(nullable = false)
    private boolean seasonal = false; // For campaigns like "Exam Season", "Kharif Crop"

    @Column
    private LocalDateTime campaignStartDate;

    @Column
    private LocalDateTime campaignEndDate;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private long clickCount = 0;

    @Column(nullable = false)
    private long sessionCount = 0; // Sessions booked for this topic

    @Column(nullable = false)
    private long mentorCount = 0; // Mentors who adopted this topic

    @Column(length = 500)
    private String rejectionReason; // If admin rejects

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column(length = 100)
    private String approvedBy; // Admin username
}
