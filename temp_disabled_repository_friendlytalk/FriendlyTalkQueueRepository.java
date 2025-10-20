package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyTalkQueue;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import com.samjhadoo.model.friendlytalk.FriendlyTalkQueue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FriendlyTalkQueueRepository extends JpaRepository<FriendlyTalkQueue, Long> {

    Optional<FriendlyTalkQueue> findByUser(User user);

    List<FriendlyTalkQueue> findByStatus(QueueStatus status);

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.status = 'WAITING' ORDER BY q.joinedAt ASC")
    List<FriendlyTalkQueue> findActiveQueue();

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.status = 'WAITING' AND q.moodType = :moodType ORDER BY q.joinedAt ASC")
    List<FriendlyTalkQueue> findByMoodTypeInQueue(@Param("moodType") MoodType moodType);

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.status = 'WAITING' AND q.intensity >= :minIntensity ORDER BY q.intensity DESC, q.joinedAt ASC")
    List<FriendlyTalkQueue> findHighIntensityInQueue(@Param("minIntensity") int minIntensity);

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.status = 'WAITING' AND q.expiresAt <= :now")
    List<FriendlyTalkQueue> findExpiredQueue(@Param("now") LocalDateTime now);

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.status = 'WAITING' AND q.anonymous = true ORDER BY q.joinedAt ASC")
    List<FriendlyTalkQueue> findAnonymousInQueue();

    @Query("SELECT COUNT(q) FROM FriendlyTalkQueue q WHERE q.status = 'WAITING'")
    long countActiveQueue();

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.joinedAt >= :since ORDER BY q.joinedAt DESC")
    List<FriendlyTalkQueue> findRecentQueueEntries(@Param("since") LocalDateTime since);

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.retryCount > 0 ORDER BY q.retryCount DESC")
    List<FriendlyTalkQueue> findQueueWithRetries();

    @Query("SELECT AVG(q.estimatedWaitMinutes) FROM FriendlyTalkQueue q WHERE q.status = 'WAITING'")
    Double getAverageEstimatedWait();

    @Query("SELECT q FROM FriendlyTalkQueue q WHERE q.matchedWith IS NOT NULL ORDER BY q.matchedAt DESC")
    List<FriendlyTalkQueue> findRecentlyMatched();
}
