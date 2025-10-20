package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyTalkSession;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FriendlyTalkSessionRepository extends JpaRepository<FriendlyTalkSession, Long> {

    List<FriendlyTalkSession> findByInitiator(User initiator);

    List<FriendlyTalkSession> findByReceiver(User receiver);

    List<FriendlyTalkSession> findByStatus(SessionStatus status);

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.initiator = :user OR s.receiver = :user ORDER BY s.createdAt DESC")
    List<FriendlyTalkSession> findByParticipant(@Param("user") User user);

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.status = 'ACTIVE' ORDER BY s.startedAt ASC")
    List<FriendlyTalkSession> findActiveSessions();

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.status = 'REQUESTED' AND s.createdAt >= :since ORDER BY s.createdAt ASC")
    List<FriendlyTalkSession> findPendingRequests(@Param("since") LocalDateTime since);

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.initiator = :user AND s.receiver = :receiver")
    List<FriendlyTalkSession> findByParticipants(@Param("user") User initiator, @Param("receiver") User receiver);

    @Query("SELECT COUNT(s) FROM FriendlyTalkSession s WHERE s.status = 'ACTIVE'")
    long countActiveSessions();

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<FriendlyTalkSession> findRecentSessions(@Param("since") LocalDateTime since);

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.status = 'COMPLETED' AND s.durationMinutes > 0 ORDER BY s.durationMinutes DESC")
    List<FriendlyTalkSession> findLongestSessions();

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.satisfactionRating >= :minRating ORDER BY s.satisfactionRating DESC")
    List<FriendlyTalkSession> findHighRatedSessions(@Param("minRating") int minRating);

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.status = 'REPORTED' ORDER BY s.reportedAt ASC")
    List<FriendlyTalkSession> findReportedSessions();

    @Query("SELECT s FROM FriendlyTalkSession s WHERE s.anonymous = true ORDER BY s.createdAt DESC")
    List<FriendlyTalkSession> findAnonymousSessions();

    @Query("SELECT AVG(s.satisfactionRating) FROM FriendlyTalkSession s WHERE s.status = 'COMPLETED' AND s.satisfactionRating > 0")
    Double getAverageSatisfactionRating();

    @Query("SELECT COUNT(s) FROM FriendlyTalkSession s WHERE s.status = 'COMPLETED' AND s.createdAt >= :since")
    long countCompletedSessionsSince(@Param("since") LocalDateTime since);
}
