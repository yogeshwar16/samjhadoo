package com.samjhadoo.service.ai;

import com.samjhadoo.SamjhadooApplication;
import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.repository.MentorProfileRepository;
import com.samjhadoo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SamjhadooApplication.class)
@ActiveProfiles("test")
@Transactional
public class AIMatchingServiceIntegrationTest {

    @Autowired
    private AIMatchingServiceV2 aiMatchingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MentorProfileRepository mentorProfileRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test.user@example.com");
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);
        
        // Create test mentors
        createTestMentors();
    }
    
    private void createTestMentors() {
        // Mentor 1: Java expert with 5 years experience
        MentorProfile mentor1 = new MentorProfile();
        mentor1.setUser(createUser("java.mentor@example.com", "Java Mentor"));
        mentor1.setSkills(Set.of("Java", "Spring Boot", "Microservices"));
        mentor1.setExperience("5 years");
        mentor1.setActive(true);
        mentorProfileRepository.save(mentor1);
        
        // Mentor 2: JavaScript expert with 3 years experience
        MentorProfile mentor2 = new MentorProfile();
        mentor2.setUser(createUser("js.mentor@example.com", "JS Mentor"));
        mentor2.setSkills(Set.of("JavaScript", "React", "Node.js"));
        mentor2.setExperience("3 years");
        mentor2.setActive(true);
        mentorProfileRepository.save(mentor2);
    }
    
    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }
    
    @Test
    void findMatchingMentors_shouldReturnRelevantMentors() {
        // Given
        Map<String, Object> preferences = Map.of(
            "skills", List.of("Java", "Spring Boot"),
            "minExperience", 3
        );
        
        // When
        var results = aiMatchingService.findMatchingMentors(
            testUser, preferences, SessionType.MENTORSHIP, 10
        );
        
        // Then
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getMatchScore() > 0);
        
        // Verify the first result is the Java mentor
        assertEquals("Java Mentor", results.get(0).getMentorProfile().getUser().getName());
    }
    
    @Test
    void countPotentialMatches_shouldReturnPositiveNumber() {
        // When
        long count = aiMatchingService.countPotentialMatches(testUser);
        
        // Then
        assertTrue(count > 0);
    }
    
    @Test
    void getRecommendedSkills_shouldReturnRelevantSkills() {
        // When
        var skills = aiMatchingService.getRecommendedSkills(testUser);
        
        // Then
        assertFalse(skills.isEmpty());
        assertTrue(skills.contains("Java") || skills.contains("Spring Boot"));
    }
}
