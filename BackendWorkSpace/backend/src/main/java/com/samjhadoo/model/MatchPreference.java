package com.samjhadoo.model.ai;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "match_preferences")
public class MatchPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType preferredSessionType;
    
    @ElementCollection
    @CollectionTable(name = "preferred_skills", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "skill")
    private Set<String> preferredSkills = new HashSet<>();
    
    private Integer minMentorExperience;
    private Double maxHourlyRate;
    private String timezone;
    
    @ElementCollection
    @CollectionTable(name = "preferred_languages", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "language")
    private Set<String> preferredLanguages = new HashSet<>();
    
    private Boolean onlyVerifiedMentors;
    private Double minMentorRating;
    
    // For friendly talk specific preferences
    private String preferredGender;
    private String preferredAgeRange;
    private Set<String> interests = new HashSet<>();
    
    // For mentor preferences (if user is a mentor looking for mentees)
    private String preferredExperienceLevel;
    private Set<String> preferredIndustries = new HashSet<>();
}
