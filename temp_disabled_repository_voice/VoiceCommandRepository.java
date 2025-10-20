package com.samjhadoo.repository.voice;

import com.samjhadoo.model.User;
import com.samjhadoo.model.voice.VoiceCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceCommandRepository extends JpaRepository<VoiceCommand, Long> {
    
    Page<VoiceCommand> findByUser(User user, Pageable pageable);
    
    Page<VoiceCommand> findByUserAndStatus(User user, VoiceCommand.CommandStatus status, Pageable pageable);
    
    List<VoiceCommand> findBySessionId(String sessionId);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user.id = :userId AND vc.status = :status ORDER BY vc.createdAt DESC")
    List<VoiceCommand> findRecentCommandsByUserAndStatus(@Param("userId") Long userId, 
                                                          @Param("status") VoiceCommand.CommandStatus status);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user AND vc.createdAt >= :since ORDER BY vc.createdAt DESC")
    List<VoiceCommand> findRecentCommandsByUser(@Param("user") User user, @Param("since") Instant since);
    
    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.status = :status AND vc.createdAt < :before")
    List<VoiceCommand> findOldCommandsByStatus(@Param("status") VoiceCommand.CommandStatus status, 
                                                @Param("before") Instant before);
    
    @Query("SELECT COUNT(vc) FROM VoiceCommand vc WHERE vc.user = :user AND vc.status = 'EXECUTED'")
    long countSuccessfulCommandsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(vc) FROM VoiceCommand vc WHERE vc.user = :user AND vc.status = 'FAILED'")
    long countFailedCommandsByUser(@Param("user") User user);
    
    @Query("SELECT vc.detectedIntent, COUNT(vc) FROM VoiceCommand vc WHERE vc.user = :user GROUP BY vc.detectedIntent")
    List<Object[]> countCommandsByIntent(@Param("user") User user);
    
    @Query("SELECT vc.detectedLanguage, COUNT(vc) FROM VoiceCommand vc GROUP BY vc.detectedLanguage")
    List<Object[]> countCommandsByLanguage();
    
    @Query("SELECT AVG(vc.confidenceScore) FROM VoiceCommand vc WHERE vc.user = :user")
    Double getAverageConfidenceScore(@Param("user") User user);
    
    @Query("SELECT AVG(vc.executionTimeMs) FROM VoiceCommand vc WHERE vc.status = 'EXECUTED'")
    Double getAverageExecutionTime();
}
