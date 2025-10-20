package com.samjhadoo.controller.api;

import com.samjhadoo.dto.ai.*;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.ai.AIGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI Gateway API - User endpoints
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AIGatewayController {

    private final AIGatewayService aiGatewayService;

    /**
     * Master AI endpoint (Freemium)
     * Available to all authenticated users
     */
    @PostMapping("/master")
    public ResponseEntity<AIResponse> masterAI(
            @CurrentUser User user,
            @Valid @RequestBody AIRequest request) {
        AIResponse response = aiGatewayService.masterAI(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Agentic AI endpoint (Premium)
     * Available only to premium users
     */
    @PostMapping("/agentic")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<AIResponse> agenticAI(
            @CurrentUser User user,
            @Valid @RequestBody AIRequest request) {
        AIResponse response = aiGatewayService.agenticAI(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate session preparation (Premium)
     * Helps mentors prepare for upcoming sessions
     */
    @PostMapping("/session-prep")
    @PreAuthorize("hasRole('MENTOR') or hasRole('PREMIUM')")
    public ResponseEntity<SessionPrepResponse> generateSessionPrep(
            @CurrentUser User user,
            @Valid @RequestBody SessionPrepRequest request) {
        SessionPrepResponse response = aiGatewayService.generateSessionPrep(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate post-session insights (Premium)
     * Provides actionable insights after a session
     */
    @PostMapping("/post-session-insights")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<PostSessionInsightsResponse> generatePostSessionInsights(
            @CurrentUser User user,
            @Valid @RequestBody PostSessionInsightsRequest request) {
        PostSessionInsightsResponse response = aiGatewayService.generatePostSessionInsights(user, request);
        return ResponseEntity.ok(response);
    }
}
