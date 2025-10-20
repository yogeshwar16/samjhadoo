package com.samjhadoo.security;

import com.samjhadoo.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void doFilterInternal_ValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "valid.token.here";
        String username = "testuser";
        
        request.addHeader("Authorization", "Bearer " + token);
        
        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(request.getAttribute("claims"));
    }

    @Test
    void doFilterInternal_InvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "invalid.token.here";
        request.addHeader("Authorization", "Bearer " + token);
        
        when(tokenProvider.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(request.getAttribute("claims"));
    }

    @Test
    void doFilterInternal_NoToken_ShouldContinueFilterChain() throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - No exception means filter chain continued
        assertNull(request.getAttribute("claims"));
    }
}
