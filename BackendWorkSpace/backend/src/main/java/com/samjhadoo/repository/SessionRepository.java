package com.samjhadoo.repository;

import com.samjhadoo.model.Session;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    
    List<Session> findByMentorAndStartTimeBetween(
        User mentor, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    List<Session> findByMenteeAndStartTimeBetween(
        User mentee, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    List<Session> findByMentorAndStatus(
        User mentor, 
        SessionStatus status
    );
    
    List<Session> findByMenteeAndStatus(
        User mentee, 
        SessionStatus status
    );
    
    @Query("SELECT s FROM Session s " +
           "WHERE (s.mentor = :user OR s.mentee = :user) " +
           "AND s.status = :status " +
           "AND s.startTime > :now " +
           "ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessionsForUser(
        @Param("user") User user,
        @Param("status") SessionStatus status,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT s FROM Session s " +
           "WHERE (s.mentor = :user OR s.mentee = :user) " +
           "AND s.status = :status " +
           "AND s.startTime <= :endTime " +
           "AND s.endTime >= :startTime")
    List<Session> findSessionsInTimeRange(
        @Param("user") User user,
        @Param("status") SessionStatus status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    Optional<Session> findByIdAndMentorId(String id, Long mentorId);
    Optional<Session> findByIdAndMenteeId(String id, Long menteeId);
    
    @Query("SELECT COUNT(s) > 0 FROM Session s " +
           "WHERE ((s.mentor = :user1 AND s.mentee = :user2) OR " +
           "      (s.mentor = :user2 AND s.mentee = :user1)) " +
           "AND s.status = 'COMPLETED'")
    boolean haveUsersHadSessionTogether(
        @Param("user1") User user1,
        @Param("user2") User user2
    );
    
    long countByStatus(SessionStatus status);
}
