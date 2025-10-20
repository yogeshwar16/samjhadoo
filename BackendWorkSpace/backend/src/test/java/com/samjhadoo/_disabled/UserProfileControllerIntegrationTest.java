package com.samjhadoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjhadoo.dto.user.ProfileUpdateRequest;
import com.samjhadoo.model.User;
import com.samjhadoo.model.user.UserProfile;
import com.samjhadoo.repository.UserProfileRepository;
import com.samjhadoo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(testUser);
        userProfile.setDisplayName("Test User");
        userProfileRepository.save(userProfile);
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void getMyProfile_WhenAuthenticated_ReturnsProfile() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateMyProfile_WithValidData_ReturnsUpdatedProfile() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setDisplayName("Updated Test User");
        updateRequest.setHeadline("New Headline");

        mockMvc.perform(put("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Test User"))
                .andExpect(jsonPath("$.headline").value("New Headline"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void uploadProfileImage_WithValidImage_ReturnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/profiles/me/profile-image").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void searchProfiles_WithCriteria_ReturnsMatchingProfiles() throws Exception {
        mockMvc.perform(get("/api/profiles/search").param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayName").value("Test User"));
    }
}
