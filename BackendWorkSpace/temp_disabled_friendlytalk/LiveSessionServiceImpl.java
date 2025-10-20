package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.LiveSessionDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.exception.UnauthorizedException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.LiveSession;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.friendlytalk.LiveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveSessionServiceImpl implements LiveSessionService {

    private final LiveSessionRepository liveSessionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public LiveSessionDTO createSession(LiveSessionDTO.CreateRequest request, Long mentorId) {
        User mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        LiveSession session = LiveSession.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .mentor(mentor)
            .type(request.getType())
            .tags(request.getTags())
            .scheduledStartTime(request.getScheduledStartTime())
            .scheduledDurationMinutes(request.getScheduledDurationMinutes())
            .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 50)
            .thumbnailUrl(request.getThumbnailUrl())
            .isRecorded(request.getIsRecorded() != null ? request.getIsRecorded() : false)
            .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
            .status(LiveSession.SessionStatus.SCHEDULED)
            .build();

        LiveSession savedSession = liveSessionRepository.save(session);
        log.info("Created new live session: {} by mentor: {}", savedSession.getId(), mentorId);

        // Broadcast session creation
        broadcastSessionUpdate(savedSession, "SESSION_CREATED");

        return convertToDTO(savedSession);
    }

    @Override
    @Transactional
    public LiveSessionDTO updateSession(Long sessionId, LiveSessionDTO.UpdateRequest request, Long mentorId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getMentor().getId().equals(mentorId)) {
            throw new UnauthorizedException("Only the mentor can update this session");
        }

        if (request.getTitle() != null) session.setTitle(request.getTitle());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getTags() != null) session.setTags(request.getTags());
        if (request.getScheduledStartTime() != null) session.setScheduledStartTime(request.getScheduledStartTime());
        if (request.getScheduledDurationMinutes() != null) session.setScheduledDurationMinutes(request.getScheduledDurationMinutes());
        if (request.getMaxParticipants() != null) session.setMaxParticipants(request.getMaxParticipants());
        if (request.getThumbnailUrl() != null) session.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsFeatured() != null) session.setIsFeatured(request.getIsFeatured());

        LiveSession updatedSession = liveSessionRepository.save(session);
        log.info("Updated live session: {}", sessionId);

        broadcastSessionUpdate(updatedSession, "SESSION_UPDATED");

        return convertToDTO(updatedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public LiveSessionDTO getSessionById(Long sessionId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return convertToDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getAllLiveSessions() {
        return liveSessionRepository.findByStatus(LiveSession.SessionStatus.LIVE)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getSessionsByStatus(LiveSession.SessionStatus status) {
        return liveSessionRepository.findByStatus(status)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getSessionsByType(LiveSession.SessionType type) {
        return liveSessionRepository.findByType(type)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getSessionsByTag(String tag) {
        return liveSessionRepository.findByTag(tag)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getFeaturedSessions() {
        return liveSessionRepository.findFeaturedSessions()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getMentorSessions(Long mentorId) {
        User mentor = userRepository.findById(mentorId)
            .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        return liveSessionRepository.findByMentorOrderByCreatedAtDesc(mentor)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getUserParticipatedSessions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return liveSessionRepository.findByParticipant(user)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LiveSessionDTO startSession(Long sessionId, Long mentorId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getMentor().getId().equals(mentorId)) {
            throw new UnauthorizedException("Only the mentor can start this session");
        }

        if (session.getStatus() != LiveSession.SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Session cannot be started from current status: " + session.getStatus());
        }

        session.setStatus(LiveSession.SessionStatus.LIVE);
        session.setStartTime(LocalDateTime.now());

        // Generate meeting URL (integrate with your video service)
        session.setMeetingUrl(generateMeetingUrl(session));

        LiveSession startedSession = liveSessionRepository.save(session);
        log.info("Started live session: {}", sessionId);

        broadcastSessionUpdate(startedSession, "SESSION_STARTED");
        notifyParticipants(startedSession, "Session has started! Join now.");

        return convertToDTO(startedSession);
    }

    @Override
    @Transactional
    public LiveSessionDTO endSession(Long sessionId, Long mentorId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getMentor().getId().equals(mentorId)) {
            throw new UnauthorizedException("Only the mentor can end this session");
        }

        session.setStatus(LiveSession.SessionStatus.ENDED);
        session.setEndTime(LocalDateTime.now());

        LiveSession endedSession = liveSessionRepository.save(session);
        log.info("Ended live session: {}", sessionId);

        broadcastSessionUpdate(endedSession, "SESSION_ENDED");

        return convertToDTO(endedSession);
    }

    @Override
    @Transactional
    public LiveSessionDTO cancelSession(Long sessionId, Long mentorId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getMentor().getId().equals(mentorId)) {
            throw new UnauthorizedException("Only the mentor can cancel this session");
        }

        session.setStatus(LiveSession.SessionStatus.CANCELLED);

        LiveSession cancelledSession = liveSessionRepository.save(session);
        log.info("Cancelled live session: {}", sessionId);

        broadcastSessionUpdate(cancelledSession, "SESSION_CANCELLED");
        notifyParticipants(cancelledSession, "Session has been cancelled.");

        return convertToDTO(cancelledSession);
    }

    @Override
    @Transactional
    public LiveSessionDTO joinSession(Long sessionId, Long userId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!session.canJoin()) {
            throw new IllegalStateException("Cannot join session. It may be full or not live.");
        }

        session.addParticipant(user);
        LiveSession updatedSession = liveSessionRepository.save(session);
        log.info("User {} joined session {}", userId, sessionId);

        broadcastSessionUpdate(updatedSession, "PARTICIPANT_JOINED");

        return convertToDTO(updatedSession);
    }

    @Override
    @Transactional
    public LiveSessionDTO leaveSession(Long sessionId, Long userId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        session.removeParticipant(user);
        LiveSession updatedSession = liveSessionRepository.save(session);
        log.info("User {} left session {}", userId, sessionId);

        broadcastSessionUpdate(updatedSession, "PARTICIPANT_LEFT");

        return convertToDTO(updatedSession);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long mentorId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getMentor().getId().equals(mentorId)) {
            throw new UnauthorizedException("Only the mentor can delete this session");
        }

        liveSessionRepository.delete(session);
        log.info("Deleted live session: {}", sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDTO> getUpcomingSessions(LocalDateTime from, LocalDateTime to) {
        return liveSessionRepository.findByScheduledStartTimeBetween(from, to)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkAndStartScheduledSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<LiveSession> sessionsToStart = liveSessionRepository.findScheduledSessionsReadyToStart(now);

        for (LiveSession session : sessionsToStart) {
            try {
                startSession(session.getId(), session.getMentor().getId());
            } catch (Exception e) {
                log.error("Failed to auto-start session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    private LiveSessionDTO convertToDTO(LiveSession session) {
        return LiveSessionDTO.builder()
            .id(session.getId())
            .title(session.getTitle())
            .description(session.getDescription())
            .mentor(LiveSessionDTO.MentorInfo.builder()
                .id(session.getMentor().getId())
                .name(session.getMentor().getName())
                .email(session.getMentor().getEmail())
                .build())
            .status(session.getStatus().name())
            .type(session.getType().name())
            .tags(session.getTags())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .scheduledStartTime(session.getScheduledStartTime())
            .scheduledDurationMinutes(session.getScheduledDurationMinutes())
            .maxParticipants(session.getMaxParticipants())
            .currentParticipants(session.getCurrentParticipants())
            .meetingUrl(session.getMeetingUrl())
            .thumbnailUrl(session.getThumbnailUrl())
            .isRecorded(session.getIsRecorded())
            .recordingUrl(session.getRecordingUrl())
            .isFeatured(session.getIsFeatured())
            .isLive(session.isLive())
            .isFull(session.isFull())
            .canJoin(session.canJoin())
            .createdAt(session.getCreatedAt())
            .updatedAt(session.getUpdatedAt())
            .build();
    }

    private void broadcastSessionUpdate(LiveSession session, String eventType) {
        try {
            messagingTemplate.convertAndSend("/topic/live-sessions", 
                new SessionUpdateMessage(eventType, convertToDTO(session)));
        } catch (Exception e) {
            log.error("Failed to broadcast session update: {}", e.getMessage());
        }
    }

    private void notifyParticipants(LiveSession session, String message) {
        session.getParticipants().forEach(participant -> {
            try {
                messagingTemplate.convertAndSendToUser(
                    participant.getEmail(),
                    "/queue/notifications",
                    new NotificationMessage(session.getId(), message)
                );
            } catch (Exception e) {
                log.error("Failed to notify participant {}: {}", participant.getId(), e.getMessage());
            }
        });
    }

    private String generateMeetingUrl(LiveSession session) {
        // Integrate with your video conferencing service (Jitsi, Zoom, etc.)
        return "https://meet.samjhadoo.com/session/" + session.getId();
    }

    // Helper classes for WebSocket messages
    private record SessionUpdateMessage(String type, LiveSessionDTO session) {}
    private record NotificationMessage(Long sessionId, String message) {}
}
