package com.samjhadoo.service;

import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.Session;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionStatus;
import com.samjhadoo.repository.SessionRepository;
import com.samjhadoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Session createSession(Session session) {
        validateSessionTime(session.getStartTime(), session.getEndTime());
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Session getSessionById(String id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    @Transactional
    public Session updateSession(String sessionId, Session updatedSession) {
        Session existingSession = getSessionById(sessionId);
        
        // Only allow updating certain fields
        existingSession.setTitle(updatedSession.getTitle());
        existingSession.setDescription(updatedSession.getDescription());
        existingSession.setStartTime(updatedSession.getStartTime());
        existingSession.setEndTime(updatedSession.getEndTime());
        existingSession.setNotes(updatedSession.getNotes());
        
        return sessionRepository.save(existingSession);
    }

    @Transactional
    public void cancelSession(String sessionId, String reason) {
        Session session = getSessionById(sessionId);
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled sessions can be cancelled");
        }
        session.setStatus(SessionStatus.CANCELLED);
        session.setNotes((session.getNotes() != null ? session.getNotes() + "\n" : "") + 
                        "Cancelled. Reason: " + reason);
        sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<Session> getUpcomingSessionsForUser(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return sessionRepository.findUpcomingSessionsForUser(
                user, 
                SessionStatus.SCHEDULED, 
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public List<Session> getSessionsInTimeRange(
            String userId, 
            LocalDateTime startTime, 
            LocalDateTime endTime
    ) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return sessionRepository.findSessionsInTimeRange(
                user,
                SessionStatus.SCHEDULED,
                startTime,
                endTime
        );
    }

    @Transactional
    public Session startSession(String sessionId) {
        Session session = getSessionById(sessionId);
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled sessions can be started");
        }
        if (session.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Session cannot be started before its scheduled time");
        }
        
        session.setStatus(SessionStatus.IN_PROGRESS);
        return sessionRepository.save(session);
    }

    @Transactional
    public Session endSession(String sessionId) {
        Session session = getSessionById(sessionId);
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress sessions can be ended");
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    private void validateSessionTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot create session in the past");
        }
        
        // Check minimum session duration (15 minutes)
        if (java.time.Duration.between(startTime, endTime).toMinutes() < 15) {
            throw new IllegalArgumentException("Minimum session duration is 15 minutes");
        }
        
        // Check maximum session duration (4 hours)
        if (java.time.Duration.between(startTime, endTime).toHours() > 4) {
            throw new IllegalArgumentException("Maximum session duration is 4 hours");
        }
    }
}
