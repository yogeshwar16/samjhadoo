package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.LiveSessionDTO;
import com.samjhadoo.model.friendlytalk.LiveSession;

import java.time.LocalDateTime;
import java.util.List;

public interface LiveSessionService {

    LiveSessionDTO createSession(LiveSessionDTO.CreateRequest request, Long mentorId);

    LiveSessionDTO updateSession(Long sessionId, LiveSessionDTO.UpdateRequest request, Long mentorId);

    LiveSessionDTO getSessionById(Long sessionId);

    List<LiveSessionDTO> getAllLiveSessions();

    List<LiveSessionDTO> getSessionsByStatus(LiveSession.SessionStatus status);

    List<LiveSessionDTO> getSessionsByType(LiveSession.SessionType type);

    List<LiveSessionDTO> getSessionsByTag(String tag);

    List<LiveSessionDTO> getFeaturedSessions();

    List<LiveSessionDTO> getMentorSessions(Long mentorId);

    List<LiveSessionDTO> getUserParticipatedSessions(Long userId);

    LiveSessionDTO startSession(Long sessionId, Long mentorId);

    LiveSessionDTO endSession(Long sessionId, Long mentorId);

    LiveSessionDTO cancelSession(Long sessionId, Long mentorId);

    LiveSessionDTO joinSession(Long sessionId, Long userId);

    LiveSessionDTO leaveSession(Long sessionId, Long userId);

    void deleteSession(Long sessionId, Long mentorId);

    List<LiveSessionDTO> getUpcomingSessions(LocalDateTime from, LocalDateTime to);

    void checkAndStartScheduledSessions();
}
