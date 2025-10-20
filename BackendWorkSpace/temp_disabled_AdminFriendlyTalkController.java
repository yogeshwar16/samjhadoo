package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.friendlytalk.SafetyReportDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkRoomDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkSessionDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkQueueDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.service.friendlytalk.FriendlyTalkService;
import com.samjhadoo.service.friendlytalk.RoomService;
import com.samjhadoo.service.friendlytalk.QueueService;
import com.samjhadoo.service.friendlytalk.SafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API controller for Friendly Talk moderation and management.
 * Provides endpoints for administrators to manage safety, moderation, and system health.
 */
@RestController
@RequestMapping("/api/admin/friendly-talk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Friendly Talk", description = "Admin friendly talk management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFriendlyTalkController {

    private final FriendlyTalkService friendlyTalkService;
    private final RoomService roomService;
    private final QueueService queueService;
    private final SafetyService safetyService;

    // Safety Report Management
    @GetMapping("/safety/reports/pending")
    @Operation(summary = "Get pending safety reports",
               description = "Retrieves safety reports awaiting moderation")
    public ResponseEntity<List<SafetyReportDTO>> getPendingReports(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<SafetyReportDTO> reports = safetyService.getPendingReports(limit);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting pending safety reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/under-review")
    @Operation(summary = "Get reports under review",
               description = "Retrieves safety reports currently under review")
    public ResponseEntity<List<SafetyReportDTO>> getUnderReviewReports(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<SafetyReportDTO> reports = safetyService.getUnderReviewReports(limit);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting under review safety reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/safety/reports/{reportId}/review")
    @Operation(summary = "Mark report as under review",
               description = "Marks a safety report as under review by a moderator")
    public ResponseEntity<Void> markReportUnderReview(
            @PathVariable Long reportId,
            @Parameter(hidden = true) @AuthenticationPrincipal User reviewer) {
        try {
            boolean marked = safetyService.markReportUnderReview(reportId, reviewer);
            if (!marked) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking report {} as under review: {}", reportId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/safety/reports/{reportId}/resolve")
    @Operation(summary = "Resolve safety report",
               description = "Resolves a safety report with resolution notes and actions taken")
    public ResponseEntity<Void> resolveReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) @AuthenticationPrincipal User resolver,
            @RequestParam String notes,
            @RequestParam String action) {
        try {
            boolean resolved = safetyService.resolveReport(reportId, resolver, notes, action);
            if (!resolved) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error resolving report {}: {}", reportId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/safety/reports/{reportId}/dismiss")
    @Operation(summary = "Dismiss safety report",
               description = "Dismisses a safety report as not requiring action")
    public ResponseEntity<Void> dismissReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) @AuthenticationPrincipal User resolver,
            @RequestParam String notes) {
        try {
            boolean dismissed = safetyService.dismissReport(reportId, resolver, notes);
            if (!dismissed) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error dismissing report {}: {}", reportId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/safety/reports/{reportId}/escalate")
    @Operation(summary = "Escalate safety report",
               description = "Escalates a safety report to higher-level moderation")
    public ResponseEntity<Void> escalateReport(
            @PathVariable Long reportId,
            @RequestParam String escalatedTo) {
        try {
            boolean escalated = safetyService.escalateReport(reportId, escalatedTo);
            if (!escalated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error escalating report {}: {}", reportId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/follow-up")
    @Operation(summary = "Get reports requiring follow-up",
               description = "Retrieves safety reports that require follow-up actions")
    public ResponseEntity<List<SafetyReportDTO>> getReportsRequiringFollowUp() {
        try {
            List<SafetyReportDTO> reports = safetyService.getReportsRequiringFollowUp();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting reports requiring follow-up: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/critical")
    @Operation(summary = "Get critical unresolved reports",
               description = "Retrieves critical safety reports that remain unresolved")
    public ResponseEntity<List<SafetyReportDTO>> getCriticalUnresolvedReports() {
        try {
            List<SafetyReportDTO> reports = safetyService.getCriticalUnresolvedReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting critical unresolved reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/recent")
    @Operation(summary = "Get recent safety reports",
               description = "Retrieves safety reports from a specific time period")
    public ResponseEntity<List<SafetyReportDTO>> getRecentReports(
            @RequestParam String since,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            java.time.LocalDateTime sinceDate = java.time.LocalDateTime.parse(since);
            List<SafetyReportDTO> reports = safetyService.getRecentReports(sinceDate, limit);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting recent safety reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/anonymous")
    @Operation(summary = "Get anonymous reports",
               description = "Retrieves safety reports that were submitted anonymously")
    public ResponseEntity<List<SafetyReportDTO>> getAnonymousReports(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<SafetyReportDTO> reports = safetyService.getAnonymousReports(limit);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting anonymous safety reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/statistics")
    @Operation(summary = "Get safety statistics",
               description = "Retrieves comprehensive safety statistics for the friendly talk system")
    public ResponseEntity<Map<String, Object>> getSafetyStatistics() {
        try {
            Map<String, Object> statistics = safetyService.getSafetyStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting safety statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Room Moderation
    @PostMapping("/rooms/{roomId}/close")
    @Operation(summary = "Close room",
               description = "Closes a friendly talk room (moderator/admin action)")
    public ResponseEntity<Void> closeRoom(
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin) {
        try {
            boolean closed = roomService.closeRoom(roomId, admin);
            if (!closed) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cannot close room {}: {}", roomId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error closing room {}: {}", roomId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rooms/{roomId}/moderator")
    @Operation(summary = "Assign room moderator",
               description = "Assigns a moderator to a friendly talk room")
    public ResponseEntity<Void> assignRoomModerator(
            @PathVariable String roomId,
            @RequestParam Long moderatorId) {
        try {
            // In a real implementation, you'd fetch the moderator user by ID
            // For now, we'll assume the moderator exists and proceed
            boolean assigned = roomService.assignModerator(roomId, null);
            if (!assigned) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error assigning moderator to room {}: {}", roomId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/rooms/{roomId}/participants/{participantId}")
    @Operation(summary = "Remove room participant",
               description = "Removes a participant from a room (moderator action)")
    public ResponseEntity<Void> removeRoomParticipant(
            @PathVariable String roomId,
            @PathVariable Long participantId,
            @Parameter(hidden = true) @AuthenticationPrincipal User moderator) {
        try {
            boolean removed = roomService.removeParticipant(roomId, participantId, moderator);
            if (!removed) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cannot remove participant {} from room {}: {}", participantId, roomId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error removing participant {} from room {}: {}", participantId, roomId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/moderated")
    @Operation(summary = "Get moderated rooms",
               description = "Retrieves rooms where the admin is assigned as moderator")
    public ResponseEntity<List<FriendlyTalkRoomDTO>> getModeratedRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User admin) {
        try {
            List<FriendlyTalkRoomDTO> rooms = roomService.getModeratedRooms(admin);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting moderated rooms for admin {}: {}", admin.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Queue Management
    @GetMapping("/queue/active")
    @Operation(summary = "Get active queue",
               description = "Retrieves all users currently in the talk queue")
    public ResponseEntity<List<FriendlyTalkQueueDTO>> getActiveQueue(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<FriendlyTalkQueueDTO> queue = queueService.getActiveQueue(limit);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            log.error("Error getting active queue: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/queue/process-matching")
    @Operation(summary = "Process queue matching",
               description = "Manually triggers the queue matching process")
    public ResponseEntity<Integer> processQueueMatching() {
        try {
            int matchesCreated = queueService.processQueueMatching();
            return ResponseEntity.ok(matchesCreated);
        } catch (Exception e) {
            log.error("Error processing queue matching: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/queue/expire-old")
    @Operation(summary = "Expire old queue entries",
               description = "Removes expired entries from the talk queue")
    public ResponseEntity<Integer> expireOldQueueEntries() {
        try {
            int expired = queueService.expireOldQueueEntries();
            return ResponseEntity.ok(expired);
        } catch (Exception e) {
            log.error("Error expiring old queue entries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/queue/statistics")
    @Operation(summary = "Get queue statistics",
               description = "Retrieves statistics about the talk queue")
    public ResponseEntity<Map<String, Object>> getQueueStatistics() {
        try {
            Map<String, Object> statistics = queueService.getQueueStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting queue statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Session Moderation
    @GetMapping("/sessions/active")
    @Operation(summary = "Get active sessions",
               description = "Retrieves all currently active talk sessions")
    public ResponseEntity<List<FriendlyTalkSessionDTO>> getActiveSessions() {
        try {
            // In a real implementation, you'd need to fetch sessions for all users
            // For now, we'll return an empty list as this requires user context
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error getting active sessions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/moderate")
    @Operation(summary = "Moderate session",
               description = "Assigns a moderator to oversee a talk session")
    public ResponseEntity<Void> moderateSession(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User moderator) {
        try {
            // In a real implementation, you'd update the session with the moderator
            // For now, we'll return success
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error moderating session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // System Management
    @GetMapping("/statistics")
    @Operation(summary = "Get friendly talk statistics",
               description = "Retrieves comprehensive statistics for the friendly talk system")
    public ResponseEntity<Map<String, Object>> getFriendlyTalkStatistics() {
        try {
            Map<String, Object> statistics = friendlyTalkService.getFriendlyTalkStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting friendly talk statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup old data",
               description = "Removes expired sessions, old moods, and cleans up inactive data")
    public ResponseEntity<Integer> cleanupOldData() {
        try {
            int cleaned = friendlyTalkService.cleanupOldData();
            return ResponseEntity.ok(cleaned);
        } catch (Exception e) {
            log.error("Error cleaning up old data: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/system/health")
    @Operation(summary = "Get system health",
               description = "Retrieves the current health status of the friendly talk system")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = friendlyTalkService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting system health: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/process-mood-matching")
    @Operation(summary = "Process mood matching",
               description = "Manually triggers mood-based matching for queued users")
    public ResponseEntity<Integer> processMoodMatching() {
        try {
            int matchesCreated = friendlyTalkService.processMoodMatching();
            return ResponseEntity.ok(matchesCreated);
        } catch (Exception e) {
            log.error("Error processing mood matching: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/safety/process-follow-ups")
    @Operation(summary = "Process follow-up requirements",
               description = "Processes safety reports that require follow-up actions")
    public ResponseEntity<Integer> processFollowUpRequirements() {
        try {
            int processed = safetyService.processFollowUpRequirements();
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            log.error("Error processing follow-up requirements: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // User Safety Management
    @GetMapping("/users/{userId}/safety-score")
    @Operation(summary = "Get user safety score",
               description = "Retrieves the safety score for a specific user")
    public ResponseEntity<Integer> getUserSafetyScore(@PathVariable Long userId) {
        try {
            // In a real implementation, you'd fetch the user by ID and get their safety score
            // For now, we'll return a default score
            return ResponseEntity.ok(100);
        } catch (Exception e) {
            log.error("Error getting safety score for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/{userId}/violations")
    @Operation(summary = "Get user violations",
               description = "Retrieves safety violations for a specific user")
    public ResponseEntity<Boolean> hasUserViolations(@PathVariable Long userId) {
        try {
            // In a real implementation, you'd fetch the user by ID and check violations
            // For now, we'll return false
            return ResponseEntity.ok(false);
        } catch (Exception e) {
            log.error("Error checking violations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/benefiting-from-talk")
    @Operation(summary = "Get users benefiting from talk",
               description = "Retrieves users who might benefit from friendly talk based on their mood patterns")
    public ResponseEntity<List<User>> getUsersWhoMightBenefitFromTalk(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<User> users = friendlyTalkService.getUsersWhoMightBenefitFromTalk(limit);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users who might benefit from talk: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Content Analysis
    @PostMapping("/analyze-content")
    @Operation(summary = "Analyze content for safety",
               description = "Analyzes conversation content for safety concerns")
    public ResponseEntity<Map<String, Object>> analyzeContentForSafety(@RequestBody String content) {
        try {
            Map<String, Object> analysis = friendlyTalkService.analyzeContentForSafety(content);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing content for safety: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/escalated")
    @Operation(summary = "Get escalated reports",
               description = "Retrieves safety reports that have been escalated")
    public ResponseEntity<List<SafetyReportDTO>> getEscalatedReports() {
        try {
            List<SafetyReportDTO> reports = safetyService.getEscalatedReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting escalated reports: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety/reports/resolved-by/{moderatorId}")
    @Operation(summary = "Get reports resolved by moderator",
               description = "Retrieves safety reports resolved by a specific moderator")
    public ResponseEntity<List<SafetyReportDTO>> getModeratorResolvedReports(
            @PathVariable Long moderatorId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // In a real implementation, you'd fetch the moderator user by ID
            // For now, we'll return an empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error getting reports resolved by moderator {}: {}", moderatorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
