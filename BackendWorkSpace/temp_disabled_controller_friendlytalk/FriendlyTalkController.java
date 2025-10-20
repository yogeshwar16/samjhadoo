package com.samjhadoo.controller.api.friendlytalk;

import com.samjhadoo.dto.friendlytalk.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import com.samjhadoo.service.friendlytalk.FriendlyTalkService;
import com.samjhadoo.service.friendlytalk.MoodService;
import com.samjhadoo.service.friendlytalk.RoomService;
import com.samjhadoo.service.friendlytalk.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API controller for Friendly Talk features.
 * Provides endpoints for users to manage moods, join rooms, and participate in talk sessions.
 */
@RestController
@RequestMapping("/api/friendly-talk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friendly Talk", description = "User-friendly talk features")
public class FriendlyTalkController {

    private final FriendlyTalkService friendlyTalkService;
    private final MoodService moodService;
    private final RoomService roomService;
    private final QueueService queueService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get user dashboard",
               description = "Retrieves the user's friendly talk dashboard with mood, queue status, and available rooms")
    public ResponseEntity<Map<String, Object>> getUserDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            Map<String, Object> dashboard = friendlyTalkService.getUserDashboard(user);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error getting dashboard for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Mood Management Endpoints
    @PostMapping("/mood")
    @Operation(summary = "Set user mood",
               description = "Sets or updates the user's current mood for matching")
    public ResponseEntity<MoodDTO> setMood(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam MoodType moodType,
            @RequestParam(defaultValue = "5") int intensity,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean anonymous,
            @RequestParam(defaultValue = "true") boolean lookingForTalk) {
        try {
            MoodDTO mood = moodService.setUserMood(user, moodType, intensity, description, anonymous, lookingForTalk);
            return ResponseEntity.ok(mood);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid mood setting for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error setting mood for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mood")
    @Operation(summary = "Get user mood",
               description = "Retrieves the user's current mood status")
    public ResponseEntity<MoodDTO> getUserMood(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            MoodDTO mood = moodService.getUserMood(user);
            return ResponseEntity.ok(mood);
        } catch (Exception e) {
            log.error("Error getting mood for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/mood")
    @Operation(summary = "Clear user mood",
               description = "Clears the user's current mood")
    public ResponseEntity<Void> clearMood(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean cleared = moodService.clearUserMood(user);
            if (!cleared) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error clearing mood for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/moods/compatible")
    @Operation(summary = "Get compatible moods",
               description = "Finds users with compatible moods for potential matching")
    public ResponseEntity<List<MoodDTO>> getCompatibleMoods(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MoodDTO> compatibleMoods = moodService.findCompatibleMoods(user, limit);
            return ResponseEntity.ok(compatibleMoods);
        } catch (Exception e) {
            log.error("Error getting compatible moods for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Room Management Endpoints
    @PostMapping("/rooms")
    @Operation(summary = "Create room",
               description = "Creates a new friendly talk room")
    public ResponseEntity<FriendlyTalkRoomDTO> createRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "6") int maxParticipants,
            @RequestParam(defaultValue = "false") boolean anonymous,
            @RequestParam(required = false) String topicTags,
            @RequestParam(required = false) String moodFocus) {
        try {
            FriendlyTalkRoomDTO room = roomService.createRoom(user, name, description,
                    maxParticipants, anonymous, topicTags, moodFocus);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid room creation for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating room for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rooms/{roomId}/join")
    @Operation(summary = "Join room",
               description = "Joins an existing friendly talk room")
    public ResponseEntity<FriendlyTalkRoomDTO> joinRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @PathVariable String roomId) {
        try {
            FriendlyTalkRoomDTO room = roomService.joinRoom(user, roomId);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot join room {} for user {}: {}", roomId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error joining room {} for user {}: {}", roomId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "Leave room",
               description = "Leaves a friendly talk room")
    public ResponseEntity<Void> leaveRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @PathVariable String roomId) {
        try {
            boolean left = roomService.leaveRoom(user, roomId);
            if (!left) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error leaving room {} for user {}: {}", roomId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room details",
               description = "Retrieves detailed information about a specific room")
    public ResponseEntity<FriendlyTalkRoomDTO> getRoom(
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            FriendlyTalkRoomDTO room = roomService.getRoom(roomId, user);
            if (room == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            log.error("Error getting room {}: {}", roomId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms")
    @Operation(summary = "Get available rooms",
               description = "Retrieves list of available rooms for joining")
    public ResponseEntity<List<FriendlyTalkRoomDTO>> getAvailableRooms(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<FriendlyTalkRoomDTO> rooms = roomService.getAvailableRooms(limit);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting available rooms: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/mood/{moodFocus}")
    @Operation(summary = "Get rooms by mood focus",
               description = "Retrieves rooms focused on a specific mood")
    public ResponseEntity<List<FriendlyTalkRoomDTO>> getRoomsByMoodFocus(
            @PathVariable String moodFocus,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FriendlyTalkRoomDTO> rooms = roomService.getRoomsByMoodFocus(moodFocus, limit);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting rooms by mood focus {}: {}", moodFocus, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/topic/{topic}")
    @Operation(summary = "Get rooms by topic",
               description = "Retrieves rooms discussing a specific topic")
    public ResponseEntity<List<FriendlyTalkRoomDTO>> getRoomsByTopic(
            @PathVariable String topic,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<FriendlyTalkRoomDTO> rooms = roomService.getRoomsByTopic(topic, limit);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting rooms by topic {}: {}", topic, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/user")
    @Operation(summary = "Get user's rooms",
               description = "Retrieves rooms created by the authenticated user")
    public ResponseEntity<List<FriendlyTalkRoomDTO>> getUserRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<FriendlyTalkRoomDTO> rooms = roomService.getUserRooms(user);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting user rooms for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Queue Management Endpoints
    @PostMapping("/queue/join")
    @Operation(summary = "Join talk queue",
               description = "Adds the user to the friendly talk matching queue")
    public ResponseEntity<FriendlyTalkQueueDTO> joinQueue(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam MoodType moodType,
            @RequestParam(defaultValue = "5") int intensity,
            @RequestParam(defaultValue = "false") boolean anonymous,
            @RequestParam(required = false) String preferredTopics,
            @RequestParam(required = false) String avoidTopics,
            @RequestParam(defaultValue = "30") int maxWaitMinutes) {
        try {
            FriendlyTalkQueueDTO queueEntry = queueService.joinQueue(user, moodType, intensity,
                    anonymous, preferredTopics, avoidTopics, maxWaitMinutes);
            return ResponseEntity.ok(queueEntry);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot join queue for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error joining queue for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/queue/leave")
    @Operation(summary = "Leave talk queue",
               description = "Removes the user from the friendly talk queue")
    public ResponseEntity<Void> leaveQueue(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean left = queueService.leaveQueue(user);
            if (!left) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error leaving queue for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/queue/status")
    @Operation(summary = "Get queue status",
               description = "Retrieves the user's current queue status")
    public ResponseEntity<FriendlyTalkQueueDTO> getQueueStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            FriendlyTalkQueueDTO queueStatus = queueService.getUserQueueStatus(user);
            return ResponseEntity.ok(queueStatus);
        } catch (Exception e) {
            log.error("Error getting queue status for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/queue/find-match")
    @Operation(summary = "Find immediate match",
               description = "Attempts to find an immediate match for the user")
    public ResponseEntity<FriendlyTalkSessionDTO> findMatch(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            FriendlyTalkSessionDTO session = queueService.findMatch(user);
            if (session == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error finding match for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/queue/criteria")
    @Operation(summary = "Update matching criteria",
               description = "Updates the user's matching preferences while in queue")
    public ResponseEntity<Void> updateMatchingCriteria(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(required = false) String preferredTopics,
            @RequestParam(required = false) String avoidTopics) {
        try {
            boolean updated = queueService.updateMatchingCriteria(user, preferredTopics, avoidTopics);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating matching criteria for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Session Management Endpoints
    @PostMapping("/sessions")
    @Operation(summary = "Initiate talk request",
               description = "Sends a talk request to another user")
    public ResponseEntity<FriendlyTalkSessionDTO> initiateTalkRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam Long receiverId,
            @RequestParam MoodType moodType,
            @RequestParam(defaultValue = "false") boolean anonymous,
            @RequestParam(required = false) String topic) {
        try {
            // In a real implementation, you'd fetch the receiver user by ID
            // For now, we'll assume the receiver exists and proceed
            FriendlyTalkSessionDTO session = friendlyTalkService.initiateTalkRequest(user, null,
                    moodType, anonymous, topic != null ? topic : "General conversation");
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error initiating talk request for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/accept")
    @Operation(summary = "Accept talk request",
               description = "Accepts a pending talk request")
    public ResponseEntity<FriendlyTalkSessionDTO> acceptTalkRequest(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            FriendlyTalkSessionDTO session = friendlyTalkService.acceptTalkRequest(sessionId, user);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot accept session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error accepting session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/start")
    @Operation(summary = "Start talk session",
               description = "Starts an accepted talk session")
    public ResponseEntity<FriendlyTalkSessionDTO> startTalkSession(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            FriendlyTalkSessionDTO session = friendlyTalkService.startTalkSession(sessionId, user);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot start session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error starting session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete talk session",
               description = "Completes a talk session with rating and feedback")
    public ResponseEntity<FriendlyTalkSessionDTO> completeTalkSession(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int satisfactionRating,
            @RequestParam(required = false) String feedback) {
        try {
            FriendlyTalkSessionDTO session = friendlyTalkService.completeTalkSession(sessionId, user,
                    satisfactionRating, feedback);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot complete session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error completing session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/cancel")
    @Operation(summary = "Cancel talk session",
               description = "Cancels a talk request or session")
    public ResponseEntity<Void> cancelTalkSession(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean cancelled = friendlyTalkService.cancelTalkSession(sessionId, user);
            if (!cancelled) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sessions/active")
    @Operation(summary = "Get active sessions",
               description = "Retrieves the user's currently active talk sessions")
    public ResponseEntity<List<FriendlyTalkSessionDTO>> getActiveSessions(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<FriendlyTalkSessionDTO> sessions = friendlyTalkService.getActiveSessions(user);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting active sessions for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sessions/history")
    @Operation(summary = "Get session history",
               description = "Retrieves the user's talk session history")
    public ResponseEntity<List<FriendlyTalkSessionDTO>> getSessionHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<FriendlyTalkSessionDTO> sessions = friendlyTalkService.getSessionHistory(user, limit);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting session history for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/report")
    @Operation(summary = "Report session",
               description = "Reports a talk session for safety concerns")
    public ResponseEntity<Void> reportSession(
            @PathVariable Long sessionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam com.samjhadoo.model.friendlytalk.SafetyReport.ReportType reportType,
            @RequestParam com.samjhadoo.model.friendlytalk.SafetyReport.ReportSeverity severity,
            @RequestParam String description) {
        try {
            boolean reported = friendlyTalkService.reportTalkSession(sessionId, user, reportType, severity, description);
            if (!reported) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error reporting session {} for user {}: {}", sessionId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Utility Endpoints
    @GetMapping("/conversation-starters")
    @Operation(summary = "Get conversation starters",
               description = "Retrieves conversation starter suggestions based on mood compatibility")
    public ResponseEntity<List<String>> getConversationStarters(
            @RequestParam MoodType mood1,
            @RequestParam MoodType mood2) {
        try {
            List<String> starters = friendlyTalkService.getConversationStarters(mood1, mood2);
            return ResponseEntity.ok(starters);
        } catch (Exception e) {
            log.error("Error getting conversation starters: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/emotional-checkin")
    @Operation(summary = "Get emotional check-in suggestions",
               description = "Retrieves personalized emotional check-in questions for the user")
    public ResponseEntity<List<String>> getEmotionalCheckInSuggestions(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<String> suggestions = friendlyTalkService.getEmotionalCheckInSuggestions(user);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error getting emotional check-in suggestions for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mood/update-and-match")
    @Operation(summary = "Update mood and trigger matching",
               description = "Updates user mood and immediately attempts to find a match")
    public ResponseEntity<MoodDTO> updateMoodAndTriggerMatching(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam MoodType moodType,
            @RequestParam(defaultValue = "5") int intensity) {
        try {
            MoodDTO mood = friendlyTalkService.updateMoodAndTriggerMatching(user, moodType, intensity);
            return ResponseEntity.ok(mood);
        } catch (Exception e) {
            log.error("Error updating mood and matching for user {}: {}", user.getId(), e.getMessage());
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
}
