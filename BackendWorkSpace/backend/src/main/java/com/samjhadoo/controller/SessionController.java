package com.samjhadoo.controller;

import com.samjhadoo.model.Session;
import com.samjhadoo.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Session Management", description = "APIs for managing mentoring sessions")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Create a new session")
    public ResponseEntity<Session> createSession(
            @RequestBody Session session,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Add validation and DTO mapping
        Session createdSession = sessionService.createSession(session);
        return ResponseEntity.ok(createdSession);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<Session> getSession(@PathVariable String id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update session details")
    public ResponseEntity<Session> updateSession(
            @PathVariable String id,
            @RequestBody Session session,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Add authorization check
        return ResponseEntity.ok(sessionService.updateSession(id, session));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a session")
    public ResponseEntity<Void> cancelSession(
            @PathVariable String id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Add authorization check
        sessionService.cancelSession(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get user's upcoming sessions")
    public ResponseEntity<List<Session>> getUpcomingSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Get user ID from authentication
        String userId = ""; // Get from authentication
        return ResponseEntity.ok(sessionService.getUpcomingSessionsForUser(userId));
    }

    @GetMapping
    @Operation(summary = "Get sessions in time range")
    public ResponseEntity<List<Session>> getSessionsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Get user ID from authentication
        String userId = ""; // Get from authentication
        return ResponseEntity.ok(sessionService.getSessionsInTimeRange(userId, start, end));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a session")
    public ResponseEntity<Session> startSession(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Add authorization check
        return ResponseEntity.ok(sessionService.startSession(id));
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "End a session")
    public ResponseEntity<Session> endSession(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Add authorization check
        return ResponseEntity.ok(sessionService.endSession(id));
    }
}
