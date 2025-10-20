package com.samjhadoo.controller.api.friendlytalk;

import com.samjhadoo.dto.friendlytalk.LiveSessionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.LiveSession;
import com.samjhadoo.service.friendlytalk.LiveSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/live-sessions")
@RequiredArgsConstructor
@Tag(name = "Live Sessions", description = "Manage live mentor sessions and friendly talk rooms")
public class LiveSessionController {

    private final LiveSessionService liveSessionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "Create a new live session", description = "Mentors can create scheduled or live sessions")
    public ResponseEntity<LiveSessionDTO> createSession(
            @Valid @RequestBody LiveSessionDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.createSession(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "Update a live session", description = "Update session details before it starts")
    public ResponseEntity<LiveSessionDTO> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody LiveSessionDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.updateSession(id, request, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieve detailed information about a specific session")
    public ResponseEntity<LiveSessionDTO> getSession(@PathVariable Long id) {
        LiveSessionDTO session = liveSessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/live")
    @Operation(summary = "Get all live sessions", description = "Retrieve all currently active live sessions")
    public ResponseEntity<List<LiveSessionDTO>> getLiveSessions() {
        List<LiveSessionDTO> sessions = liveSessionService.getAllLiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sessions by status", description = "Filter sessions by status (SCHEDULED, LIVE, ENDED, CANCELLED)")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByStatus(
            @PathVariable LiveSession.SessionStatus status) {
        
        List<LiveSessionDTO> sessions = liveSessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get sessions by type", description = "Filter sessions by type (NIGHT_RADIO, LIVE_MOTIVATION_TALK, etc.)")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByType(
            @PathVariable LiveSession.SessionType type) {
        
        List<LiveSessionDTO> sessions = liveSessionService.getSessionsByType(type);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get sessions by tag", description = "Find sessions with a specific tag (calm, career, love, etc.)")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByTag(@PathVariable String tag) {
        List<LiveSessionDTO> sessions = liveSessionService.getSessionsByTag(tag);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured sessions", description = "Retrieve featured upcoming and live sessions")
    public ResponseEntity<List<LiveSessionDTO>> getFeaturedSessions() {
        List<LiveSessionDTO> sessions = liveSessionService.getFeaturedSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get mentor's sessions", description = "Retrieve all sessions created by a specific mentor")
    public ResponseEntity<List<LiveSessionDTO>> getMentorSessions(@PathVariable Long mentorId) {
        List<LiveSessionDTO> sessions = liveSessionService.getMentorSessions(mentorId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/my-sessions")
    @Operation(summary = "Get my sessions", description = "Retrieve sessions where the current user is a participant")
    public ResponseEntity<List<LiveSessionDTO>> getMyParticipatedSessions(
            @AuthenticationPrincipal User currentUser) {
        
        List<LiveSessionDTO> sessions = liveSessionService.getUserParticipatedSessions(currentUser.getId());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming sessions", description = "Retrieve sessions scheduled within a time range")
    public ResponseEntity<List<LiveSessionDTO>> getUpcomingSessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        LocalDateTime startTime = from != null ? from : LocalDateTime.now();
        LocalDateTime endTime = to != null ? to : LocalDateTime.now().plusDays(7);
        
        List<LiveSessionDTO> sessions = liveSessionService.getUpcomingSessions(startTime, endTime);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "Start a session", description = "Start a scheduled session and make it live")
    public ResponseEntity<LiveSessionDTO> startSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.startSession(id, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "End a session", description = "End a live session")
    public ResponseEntity<LiveSessionDTO> endSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.endSession(id, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "Cancel a session", description = "Cancel a scheduled session")
    public ResponseEntity<LiveSessionDTO> cancelSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.cancelSession(id, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a session", description = "Join a live session as a participant")
    public ResponseEntity<LiveSessionDTO> joinSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.joinSession(id, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave a session", description = "Leave a live session")
    public ResponseEntity<LiveSessionDTO> leaveSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        LiveSessionDTO session = liveSessionService.leaveSession(id, currentUser.getId());
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @Operation(summary = "Delete a session", description = "Delete a session (only if not started)")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        liveSessionService.deleteSession(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
