package com.samjhadoo.controller.api.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjhadoo.dto.content.ContentDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.content.Content;
import com.samjhadoo.model.content.ContentStatus;
import com.samjhadoo.repository.content.ContentRepository;
import com.samjhadoo.security.JwtTokenProvider;
import com.samjhadoo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ContentRepository contentRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private User testUser;
    private Content testContent;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");

        // Setup test content
        testContent = new Content();
        testContent.setId(1L);
        testContent.setTitle("Test Content");
        testContent.setStatus(ContentStatus.PUBLISHED);
        testContent.setAuthor(testUser);

        // Mock authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(any())).thenReturn("dummy-token");
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
    }

    @Test
    void createContent_ValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setTitle("New Test Content");
        contentDTO.setDescription("Test Description");
        contentDTO.setBody("Test Body");
        contentDTO.setStatus("DRAFT");

        when(contentRepository.save(any(Content.class))).thenReturn(testContent);
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/content")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testContent.getTitle()));
    }

    @Test
    void getContent_ExistingId_ShouldReturnContent() throws Exception {
        // Arrange
        when(contentRepository.findById(1L)).thenReturn(Optional.of(testContent));

        // Act & Assert
        mockMvc.perform(get("/api/content/1")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testContent.getTitle()));
    }

    @Test
    void updateContent_ValidRequest_ShouldReturnUpdatedContent() throws Exception {
        // Arrange
        ContentDTO updateDTO = new ContentDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStatus("PUBLISHED");

        when(contentRepository.findById(1L)).thenReturn(Optional.of(testContent));
        when(contentRepository.save(any(Content.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/content/1")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updateDTO.getTitle()));
    }

    @Test
    void deleteContent_ExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(contentRepository.findById(1L)).thenReturn(Optional.of(testContent));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/content/1")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllContent_ShouldReturnPageOfContent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/content")
                .header("Authorization", "Bearer dummy-token")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
