package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkRoomDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkSessionDTO;
import com.samjhadoo.dto.friendlytalk.MoodDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkQueueDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import com.samjhadoo.repository.friendlytalk.FriendlyTalkSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendlyTalkServiceImpl implements FriendlyTalkService {

    private final MoodService moodService;
    private final RoomService roomService;
    private final QueueService queueService;
    private final SafetyService safetyService;
    private final FriendlyTalkSessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDashboard(User user) {
        Map<String, Object> dashboard = new HashMap<>();

        // User's current mood
        MoodDTO userMood = moodService.getUserMood(user);
        dashboard.put("userMood", userMood);

        // User's queue status
        FriendlyTalkQueueDTO queueStatus = queueService.getUserQueueStatus(user);
        dashboard.put("queueStatus", queueStatus);

        // User's active sessions
        List<FriendlyTalkSessionDTO> activeSessions = getActiveSessions(user);
        dashboard.put("activeSessions", activeSessions);

        // Available rooms
        List<FriendlyTalkRoomDTO> availableRooms = roomService.getAvailableRooms(10);
        dashboard.put("availableRooms", availableRooms);

        // Mood-based conversation starters
        if (userMood != null) {
            List<String> conversationStarters = getConversationStarters(userMood.getMoodType(), userMood.getMoodType());
            dashboard.put("conversationStarters", conversationStarters);
        }

        // Emotional check-in suggestions
        List<String> checkInSuggestions = getEmotionalCheckInSuggestions(user);
        dashboard.put("checkInSuggestions", checkInSuggestions);

        return dashboard;
    }

    @Override
    public FriendlyTalkSessionDTO initiateTalkRequest(User initiator, User receiver,
                                                     MoodType moodType, boolean anonymous, String topic) {
        // Create session
        FriendlyTalkSession session = FriendlyTalkSession.builder()
                .initiator(initiator)
                .receiver(receiver)
                .status(FriendlyTalkSession.SessionStatus.REQUESTED)
                .initiatorMood(moodType)
                .anonymous(anonymous)
                .topic(topic)
                .build();

        FriendlyTalkSession savedSession = sessionRepository.save(session);

        log.info("Initiated talk request from {} to {}: {}",
                initiator.getId(), receiver.getId(), topic);

        return convertSessionToDTO(savedSession);
    }

    @Override
    public FriendlyTalkSessionDTO acceptTalkRequest(Long sessionId, User receiver) {
        FriendlyTalkSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || !session.getReceiver().getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Session not found or user not authorized");
        }

        boolean accepted = session.acceptRequest();
        if (!accepted) {
            throw new IllegalArgumentException("Session cannot be accepted in current state");
        }

        FriendlyTalkSession savedSession = sessionRepository.save(session);

        log.info("User {} accepted talk request {}", receiver.getId(), sessionId);

        return convertSessionToDTO(savedSession);
    }

    @Override
    public FriendlyTalkSessionDTO startTalkSession(Long sessionId, User user) {
        FriendlyTalkSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || (!session.getInitiator().getId().equals(user.getId()) &&
                                 !session.getReceiver().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("Session not found or user not authorized");
        }

        boolean started = session.startSession();
        if (!started) {
            throw new IllegalArgumentException("Session cannot be started in current state");
        }

        FriendlyTalkSession savedSession = sessionRepository.save(session);

        log.info("Started talk session {} by user {}", sessionId, user.getId());

        return convertSessionToDTO(savedSession);
    }

    @Override
    public FriendlyTalkSessionDTO completeTalkSession(Long sessionId, User user,
                                                     int satisfactionRating, String feedback) {
        FriendlyTalkSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || (!session.getInitiator().getId().equals(user.getId()) &&
                                 !session.getReceiver().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("Session not found or user not authorized");
        }

        boolean completed = session.completeSession();
        if (!completed) {
            throw new IllegalArgumentException("Session cannot be completed in current state");
        }

        session.setSatisfactionRating(satisfactionRating);
        session.setFeedback(feedback);

        FriendlyTalkSession savedSession = sessionRepository.save(session);

        log.info("Completed talk session {} by user {} (rating: {})",
                sessionId, user.getId(), satisfactionRating);

        return convertSessionToDTO(savedSession);
    }

    @Override
    public boolean cancelTalkSession(Long sessionId, User user) {
        FriendlyTalkSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || (!session.getInitiator().getId().equals(user.getId()) &&
                                 !session.getReceiver().getId().equals(user.getId()))) {
            return false;
        }

        boolean cancelled = session.cancelSession();
        if (cancelled) {
            sessionRepository.save(session);
            log.info("Cancelled talk session {} by user {}", sessionId, user.getId());
        }

        return cancelled;
    }

    @Override
    public boolean reportTalkSession(Long sessionId, User reporter,
                                   com.samjhadoo.model.friendlytalk.SafetyReport.ReportType reportType,
                                   com.samjhadoo.model.friendlytalk.SafetyReport.ReportSeverity severity,
                                   String description) {
        FriendlyTalkSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }

        // Determine reported user (the other participant)
        User reportedUser = session.getInitiator().getId().equals(reporter.getId()) ?
                           session.getReceiver() : session.getInitiator();

        safetyService.createReport(reporter, reportedUser, session, null,
                                  reportType, severity, description, null, false);

        log.info("Reported talk session {} by user {}: {}", sessionId, reporter.getId(), reportType);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkSessionDTO> getActiveSessions(User user) {
        return sessionRepository.findByParticipant(user).stream()
                .filter(FriendlyTalkSession::isActive)
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkSessionDTO> getSessionHistory(User user, int limit) {
        return sessionRepository.findByParticipant(user).stream()
                .filter(s -> s.getStatus() == FriendlyTalkSession.SessionStatus.COMPLETED ||
                           s.getStatus() == FriendlyTalkSession.SessionStatus.CANCELLED)
                .limit(limit)
                .map(this::convertSessionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int processMoodMatching() {
        return queueService.processQueueMatching();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFriendlyTalkStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Session statistics
        long totalSessions = sessionRepository.count();
        long activeSessions = sessionRepository.countActiveSessions();
        long completedSessions = sessionRepository.findByStatus(FriendlyTalkSession.SessionStatus.COMPLETED).size();

        stats.put("totalSessions", totalSessions);
        stats.put("activeSessions", activeSessions);
        stats.put("completedSessions", completedSessions);

        // Queue statistics
        stats.putAll(queueService.getQueueStatistics());

        // Room statistics
        stats.putAll(roomService.getRoomStatistics());

        // Safety statistics
        stats.putAll(safetyService.getSafetyStatistics());

        // Mood statistics
        stats.putAll(moodService.getMoodStatistics());

        return stats;
    }

    @Override
    public int cleanupOldData() {
        int totalCleaned = 0;

        // Clean up old moods
        totalCleaned += moodService.expireOldMoods();

        // Clean up old queue entries
        totalCleaned += queueService.expireOldQueueEntries();

        // Process follow-up requirements
        totalCleaned += safetyService.processFollowUpRequirements();

        log.info("Cleaned up {} old data entries", totalCleaned);

        return totalCleaned;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getEmotionalCheckInSuggestions(User user) {
        MoodDTO mood = moodService.getUserMood(user);
        if (mood == null) {
            return List.of("How are you feeling today?", "What's on your mind?");
        }

        return switch (mood.getMoodType()) {
            case LONELY -> List.of(
                "What would help you feel more connected today?",
                "Is there someone specific you'd like to talk to?",
                "What activities usually help when you're feeling lonely?"
            );
            case ANXIOUS -> List.of(
                "What specific thoughts are making you anxious?",
                "What has helped you manage anxiety in the past?",
                "Would talking about your concerns help?"
            );
            case STRESSED -> List.of(
                "What are the main sources of stress right now?",
                "What usually helps you relax?",
                "Would breaking things down into smaller steps help?"
            );
            case HAPPY -> List.of(
                "What's making you feel happy today?",
                "How can you share this positive feeling?",
                "What would make this moment even better?"
            );
            default -> List.of(
                "How are you feeling right now?",
                "What's been on your mind lately?",
                "Is there anything you'd like to talk about?"
            );
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeContentForSafety(String content) {
        Map<String, Object> analysis = new HashMap<>();

        // Simple keyword-based analysis (in a real system, this would use AI)
        String[] unsafeKeywords = {"hate", "kill", "hurt", "suicide", "threat", "bomb", "gun"};
        String[] concerningKeywords = {"sad", "lonely", "depressed", "anxious", "worried"};

        boolean hasUnsafeContent = false;
        boolean hasConcerningContent = false;

        String lowerContent = content.toLowerCase();
        for (String keyword : unsafeKeywords) {
            if (lowerContent.contains(keyword)) {
                hasUnsafeContent = true;
                break;
            }
        }

        if (!hasUnsafeContent) {
            for (String keyword : concerningKeywords) {
                if (lowerContent.contains(keyword)) {
                    hasConcerningContent = true;
                    break;
                }
            }
        }

        analysis.put("hasUnsafeContent", hasUnsafeContent);
        analysis.put("hasConcerningContent", hasConcerningContent);
        analysis.put("riskLevel", hasUnsafeContent ? "HIGH" : hasConcerningContent ? "MEDIUM" : "LOW");
        analysis.put("confidence", 0.8); // Mock confidence score

        return analysis;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getConversationStarters(MoodType mood1, MoodType mood2) {
        if (mood1 == mood2) {
            return switch (mood1) {
                case LONELY -> List.of(
                    "What do you usually do when you're feeling this way?",
                    "Is there something specific that's making you feel lonely?",
                    "Would talking about your day help?"
                );
                case ANXIOUS -> List.of(
                    "What thoughts are going through your mind?",
                    "Have you experienced this kind of anxiety before?",
                    "What usually helps when you're feeling anxious?"
                );
                case HAPPY -> List.of(
                    "What's making you feel happy today?",
                    "How can we celebrate this positive feeling?",
                    "What would make this moment even better?"
                );
                default -> List.of(
                    "How are you feeling about things right now?",
                    "What's been on your mind lately?",
                    "Is there anything you'd like to share?"
                );
            };
        } else {
            return List.of(
                "It's interesting that we're feeling different things. How are you experiencing this?",
                "What would help bridge the gap between how we're both feeling?",
                "Sometimes different perspectives can be really helpful. What's your take on this?"
            );
        }
    }

    @Override
    public MoodDTO updateMoodAndTriggerMatching(User user, MoodType moodType, int intensity) {
        // Update mood
        MoodDTO updatedMood = moodService.setUserMood(user, moodType, intensity, null, false, true);

        // If user is looking for talk, try to find a match
        if (updatedMood.isLookingForTalk()) {
            FriendlyTalkSessionDTO match = queueService.findMatch(user);
            if (match != null) {
                log.info("Found immediate match for user {} after mood update", user.getId());
            }
        }

        return updatedMood;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersWhoMightBenefitFromTalk(int limit) {
        // Get users with high-intensity negative moods
        List<MoodDTO> highIntensityMoods = moodService.findHighIntensityMoods(7, limit * 2);

        return highIntensityMoods.stream()
                .filter(m -> m.getMoodType() == MoodType.LONELY ||
                           m.getMoodType() == MoodType.ANXIOUS ||
                           m.getMoodType() == MoodType.STRESSED)
                .filter(m -> m.isLookingForTalk())
                .limit(limit)
                .map(m -> {
                    // In a real implementation, you'd fetch the user by ID
                    // For now, return null as this would need user lookup
                    return null;
                })
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // Queue health
        long activeQueue = queueService.getActiveQueue(1).size();
        health.put("activeQueueSize", activeQueue);

        // Room health
        long activeRooms = roomService.getActiveRooms(1).size();
        health.put("activeRooms", activeRooms);

        // Session health
        long activeSessions = sessionRepository.countActiveSessions();
        health.put("activeSessions", activeSessions);

        // Safety health
        long pendingReports = safetyService.getPendingReports(1).size();
        health.put("pendingSafetyReports", pendingReports);

        // Overall health score
        int healthScore = 100;
        if (pendingReports > 10) healthScore -= 20;
        if (activeQueue > 50) healthScore -= 10;
        if (activeSessions == 0) healthScore -= 5;

        health.put("overallHealthScore", Math.max(0, healthScore));

        return health;
    }

    private FriendlyTalkSessionDTO convertSessionToDTO(FriendlyTalkSession session) {
        return FriendlyTalkSessionDTO.builder()
                .id(session.getId())
                .initiatorName(session.isAnonymous() ? "Anonymous" :
                              session.getInitiator().getFirstName() + " " + session.getInitiator().getLastName())
                .receiverName(session.isAnonymous() ? "Anonymous" :
                             session.getReceiver().getFirstName() + " " + session.getReceiver().getLastName())
                .status(session.getStatus().name())
                .initiatorMood(session.getInitiatorMood())
                .receiverMood(session.getReceiverMood())
                .anonymous(session.isAnonymous())
                .topic(session.getTopic())
                .notes(session.getNotes())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationMinutes(session.getDurationMinutes())
                .satisfactionRating(session.getSatisfactionRating())
                .feedback(session.getFeedback())
                .reportedAt(session.getReportedAt())
                .reportReason(session.getReportReason())
                .moderatorName(session.getModerator() != null ?
                              (session.isAnonymous() ? "Anonymous" :
                               session.getModerator().getFirstName() + " " + session.getModerator().getLastName()) : null)
                .moderationNotes(session.getModerationNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
