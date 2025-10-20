package com.samjhadoo.repository;

import com.samjhadoo.model.WebRTCSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebRTCSessionRepository extends JpaRepository<WebRTCSession, String> {
    
    Optional<WebRTCSession> findBySessionIdAndUserId(String sessionId, String userId);
    
    void deleteBySessionIdAndUserId(String sessionId, String userId);
    
    void deleteBySessionId(String sessionId);
}
