package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyTalkRoom;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendlyTalkRoomRepository extends JpaRepository<FriendlyTalkRoom, Long> {

    Optional<FriendlyTalkRoom> findByRoomId(String roomId);

    List<FriendlyTalkRoom> findByStatus(RoomStatus status);

    List<FriendlyTalkRoom> findByCreator(User creator);

    List<FriendlyTalkRoom> findByAnonymousTrue();

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.status = 'WAITING' OR r.status = 'ACTIVE' ORDER BY r.createdAt ASC")
    List<FriendlyTalkRoom> findActiveRooms();

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.status = 'WAITING' AND r.currentParticipants < r.maxParticipants ORDER BY r.createdAt ASC")
    List<FriendlyTalkRoom> findAvailableRooms();

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.moodFocus = :moodFocus AND (r.status = 'WAITING' OR r.status = 'ACTIVE') ORDER BY r.createdAt ASC")
    List<FriendlyTalkRoom> findRoomsByMoodFocus(@Param("moodFocus") String moodFocus);

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.topicTags LIKE %:topic% AND (r.status = 'WAITING' OR r.status = 'ACTIVE') ORDER BY r.createdAt ASC")
    List<FriendlyTalkRoom> findRoomsByTopic(@Param("topic") String topic);

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.currentParticipants < r.maxParticipants AND (r.status = 'WAITING' OR r.status = 'ACTIVE') ORDER BY r.currentParticipants ASC, r.createdAt ASC")
    List<FriendlyTalkRoom> findLeastCrowdedRooms();

    @Query("SELECT COUNT(r) FROM FriendlyTalkRoom r WHERE r.status = 'ACTIVE'")
    long countActiveRooms();

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<FriendlyTalkRoom> findRecentRooms(@Param("since") LocalDateTime since);

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.endedAt IS NOT NULL AND r.durationMinutes > 0 ORDER BY r.durationMinutes DESC")
    List<FriendlyTalkRoom> findLongestRooms();

    @Query("SELECT r FROM FriendlyTalkRoom r WHERE r.moderator = :moderator ORDER BY r.createdAt DESC")
    List<FriendlyTalkRoom> findRoomsModeratedBy(@Param("moderator") User moderator);
}
