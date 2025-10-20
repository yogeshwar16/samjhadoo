package com.samjhadoo.service.ai;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.repository.MentorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIMatchingServiceTest {

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    @InjectMocks
    private AIMatchingServiceImpl aiMatchingService;

    private User testUser;
    private MentorProfile mentor1;
    private MentorProfile mentor2;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId("user1");
        testUser.setName("Test User");

        // Setup mentor 1 (good match)
        mentor1 = new MentorProfile();
        mentor1.setId("mentor1");
        mentor1.setExperience("5 years");
        mentor1.setSkills(Set.of("Java", "Spring Boot", "Microservices"));
        mentor1.setActive(true);

        // Setup mentor 2 (less relevant)
        mentor2 = new MentorProfile();
        mentor2.setId("mentor2");
        mentor2.setExperience("2 years");
        mentor2.setSkills(Set.of("JavaScript", "React"));
        mentor2.setActive(true);

        // Mock repository
        when(mentorProfileRepository.findByIsActiveTrue())
                .thenReturn(Arrays.asList(mentor1, mentor2));
    }

    @Test
    void findMatchingMentors_ShouldReturnMentorsOrderedByScore() {
        // Given
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("skills", Set.of("Java", "Spring Boot"));
        preferences.put("minExperience", 3);

        // When
        var results = aiMatchingService.findMatchingMentors(
                testUser, preferences, SessionType.MENTORSHIP, 10);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getMatchScore() > 0);
        
        // Verify mentor1 (Java/Spring expert) is ranked higher than mentor2
        if (results.size() > 1) {
            assertTrue(results.get(0).getMatchScore() >= results.get(1).getMatchScore());
        }
    }

    @Test
    void calculateMatchScore_ShouldReturnValidScore() {
        // Given
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("skills", Set.of("Java", "Spring Boot"));
        preferences.put("minExperience", 3);

        // When
        double score = aiMatchingService.calculateMatchScore(
                testUser, mentor1, preferences, SessionType.MENTORSHIP);

        // Then
        assertTrue(score > 0 && score <= 1.0);
    }

    @Test
    void getMatchExplanation_ShouldReturnNonEmptyString() {
        // When
        String explanation = aiMatchingService.getMatchExplanation(
                testUser, mentor1, SessionType.MENTORSHIP);

        // Then
        assertNotNull(explanation);
        assertFalse(explanation.isEmpty());
    }
}
