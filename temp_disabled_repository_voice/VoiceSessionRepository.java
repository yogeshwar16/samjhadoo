package com.samjhadoo.repository.voice;

import com.samjhadoo.model.User;
import com.samjhadoo.model.voice.VoiceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceSessionRepository extends JpaRepository<VoiceSession, Long> {
    
    Optional<VoiceSession> findBySessionId(String sessionId);
    
    Optional<VoiceSession> findBySessionIdAndUser(String sessionId, User user);
    
    List<VoiceSession> findByUserAndActiveTrue(User user);
    
    @Query("SELECT vs FROM VoiceSession vs WHERE vs.user = :user AND vs.active = true ORDER BY vs.startedAt DESC")
    Optional<VoiceSession> findActiveSessionByUser(@Param("user") User user);
    
    @Query("SELECT vs FROM VoiceSession vs WHERE vs.active = true AND vs.startedAt < :timeout")
    List<VoiceSession> findTimedOutSessions(@Param("timeout") Instant timeout);
    
    @Modifying
    @Query("UPDATE VoiceSession vs SET vs.active = false, vs.endedAt = :endTime WHERE vs.sessionId = :sessionId")
    int endSession(@Param("sessionId") String sessionId, @Param("endTime") Instant endTime);
    
    @Modifying
    @Query("UPDATE VoiceSession vs SET vs.active = false, vs.endedAt = :endTime WHERE vs.active = true AND vs.startedAt < :timeout")
    int endTimedOutSessions(@Param("timeout") Instant timeout, @Param("endTime") Instant endTime);
    
    @Query("SELECT COUNT(vs) FROM VoiceSession vs WHERE vs.user = :user")
    long countSessionsByUser(@Param("user") User user);
    
    @Query("SELECT AVG(vs.commandCount) FROM VoiceSession vs WHERE vs.user = :user")
    Double getAverageCommandsPerSession(@Param("user") User user);
}
