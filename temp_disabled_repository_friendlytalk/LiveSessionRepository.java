package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    List<LiveSession> findByStatus(LiveSession.SessionStatus status);

    List<LiveSession> findByMentorAndStatus(User mentor, LiveSession.SessionStatus status);

    List<LiveSession> findByType(LiveSession.SessionType type);

    List<LiveSession> findByTypeAndStatus(LiveSession.SessionType type, LiveSession.SessionStatus status);

    @Query("SELECT ls FROM LiveSession ls WHERE :tag MEMBER OF ls.tags")
    List<LiveSession> findByTag(@Param("tag") String tag);

    @Query("SELECT ls FROM LiveSession ls WHERE :tag MEMBER OF ls.tags AND ls.status = :status")
    List<LiveSession> findByTagAndStatus(@Param("tag") String tag, @Param("status") LiveSession.SessionStatus status);

    List<LiveSession> findByScheduledStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'LIVE' ORDER BY ls.currentParticipants DESC")
    List<LiveSession> findLiveSessionsOrderedByParticipants();

    @Query("SELECT ls FROM LiveSession ls WHERE ls.isFeatured = true AND ls.status IN ('SCHEDULED', 'LIVE') ORDER BY ls.scheduledStartTime")
    List<LiveSession> findFeaturedSessions();

    @Query("SELECT ls FROM LiveSession ls WHERE :user MEMBER OF ls.participants")
    List<LiveSession> findByParticipant(@Param("user") User user);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.mentor = :mentor ORDER BY ls.createdAt DESC")
    List<LiveSession> findByMentorOrderByCreatedAtDesc(@Param("mentor") User mentor);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'SCHEDULED' AND ls.scheduledStartTime <= :time")
    List<LiveSession> findScheduledSessionsReadyToStart(@Param("time") LocalDateTime time);

    @Query("SELECT COUNT(ls) FROM LiveSession ls WHERE ls.mentor = :mentor AND ls.status = 'LIVE'")
    long countLiveSessionsByMentor(@Param("mentor") User mentor);

    Optional<LiveSession> findByIdAndStatus(Long id, LiveSession.SessionStatus status);
}
