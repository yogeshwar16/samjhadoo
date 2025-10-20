package com.samjhadoo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mentor_profiles")
public class MentorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String headline;
    private String bio;
    private String expertise;
    private String experience;
    private String education;
    private String languages;
    private String websiteUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private String githubUrl;
    
    @ElementCollection
    @CollectionTable(name = "mentor_skills", joinColumns = @JoinColumn(name = "mentor_profile_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "mentor_topics", joinColumns = @JoinColumn(name = "mentor_profile_id"))
    @Column(name = "topic")
    private Set<String> topics = new HashSet<>();
    
    private Double hourlyRate;
    private Integer minSessionDuration; // in minutes
    private Integer maxSessionDuration; // in minutes
    private Boolean isAvailable;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Additional mentor-specific fields
    private String timezone;
    private String availability;
    private String meetingPreferences;
    private String cancellationPolicy;
    private String certification;
    private String achievements;
    
    // Rating and reviews
    private Double averageRating;
    private Integer totalSessions;
    private Integer totalReviews;
}
