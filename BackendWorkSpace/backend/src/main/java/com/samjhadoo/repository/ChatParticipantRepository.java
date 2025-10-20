package com.samjhadoo.repository;

import com.samjhadoo.model.ChatParticipant;
import com.samjhadoo.model.ChatRoom;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.ChatParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    List<ChatParticipant> findByUser(User user);

    List<ChatParticipant> findByRole(ChatParticipantRole role);

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom = :room ORDER BY cp.role ASC, cp.joinedAt ASC")
    List<ChatParticipant> findByChatRoomOrderByRole(@Param("room") ChatRoom room);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.user = :user AND cp.pinned = true ORDER BY cp.lastMessageAt DESC")
    List<ChatParticipant> findPinnedByUser(@Param("user") User user);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.online = true ORDER BY cp.lastSeenAt DESC")
    List<ChatParticipant> findOnlineParticipants();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.typing = true ORDER BY cp.lastSeenAt DESC")
    List<ChatParticipant> findTypingParticipants();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.lastSeenAt >= :since ORDER BY cp.lastSeenAt DESC")
    List<ChatParticipant> findActiveSince(@Param("since") LocalDateTime since);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.lastSeenAt < :cutoff ORDER BY cp.lastSeenAt ASC")
    List<ChatParticipant> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.role IN ('ADMIN', 'MODERATOR') ORDER BY cp.role ASC, cp.joinedAt ASC")
    List<ChatParticipant> findModerators();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.role = 'OWNER' ORDER BY cp.joinedAt ASC")
    List<ChatParticipant> findOwners();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.notificationEnabled = false ORDER BY cp.joinedAt DESC")
    List<ChatParticipant> findWithNotificationsDisabled();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.isMuted = true ORDER BY cp.muteUntil ASC")
    List<ChatParticipant> findMutedParticipants();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.messagesSent >= :minMessages ORDER BY cp.messagesSent DESC")
    List<ChatParticipant> findByMinMessages(@Param("minMessages") long minMessages);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.shouldAutoRemove = true ORDER BY cp.lastSeenAt ASC")
    List<ChatParticipant> findForAutoRemoval();

    @Query("SELECT COUNT(cp) FROM ChatParticipant cp WHERE cp.chatRoom = :room")
    long countByChatRoom(@Param("room") ChatRoom room);

    @Query("SELECT COUNT(cp) FROM ChatParticipant cp WHERE cp.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(cp) FROM ChatParticipant cp WHERE cp.online = true")
    long countOnlineParticipants();

    @Query("SELECT AVG(cp.engagementScore) FROM ChatParticipant cp WHERE cp.engagementScore > 0")
    Double getAverageEngagementScore();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom = :room AND cp.role IN ('ADMIN', 'MODERATOR', 'OWNER') ORDER BY cp.role ASC")
    List<ChatParticipant> findRoomModerators(@Param("room") ChatRoom room);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.user = :user AND cp.chatRoom.active = true ORDER BY cp.lastMessageAt DESC")
    List<ChatParticipant> findActiveRoomsByUser(@Param("user") User user);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.lastMessageAt >= :since ORDER BY cp.lastMessageAt DESC")
    List<ChatParticipant> findRecentlyActive(@Param("since") LocalDateTime since);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.unreadMessageCount > 0 ORDER BY cp.unreadMessageCount DESC")
    List<ChatParticipant> findWithUnreadMessages();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.customNickname IS NOT NULL ORDER BY cp.joinedAt DESC")
    List<ChatParticipant> findWithCustomNicknames();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom.type = :type ORDER BY cp.joinedAt DESC")
    List<ChatParticipant> findByRoomType(@Param("type") com.samjhadoo.model.enums.communication.ChatRoomType type);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.role = :role AND cp.chatRoom = :room")
    List<ChatParticipant> findByRoleAndRoom(@Param("role") ChatParticipantRole role, @Param("room") ChatRoom room);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.joinedAt >= :since ORDER BY cp.joinedAt DESC")
    List<ChatParticipant> findRecentParticipants(@Param("since") LocalDateTime since);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom.active = true AND cp.online = true ORDER BY cp.lastSeenAt DESC")
    List<ChatParticipant> findOnlineInActiveRooms();

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.engagementScore >= :minScore ORDER BY cp.engagementScore DESC")
    List<ChatParticipant> findHighEngagementParticipants(@Param("minScore") int minScore);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.engagementScore < :maxScore ORDER BY cp.engagementScore ASC")
    List<ChatParticipant> findLowEngagementParticipants(@Param("maxScore") int maxScore);
}
