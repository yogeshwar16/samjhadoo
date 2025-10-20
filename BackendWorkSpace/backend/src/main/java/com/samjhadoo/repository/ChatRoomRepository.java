package com.samjhadoo.repository;

import com.samjhadoo.model.ChatRoom;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByType(ChatRoomType type);

    List<ChatRoom> findByActiveTrue();

    List<ChatRoom> findByIsPublicTrue();

    List<ChatRoom> findByCreatedBy(User createdBy);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.active = true ORDER BY cr.lastActivityAt DESC")
    List<ChatRoom> findActiveOrderByActivity();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isPublic = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findPublicRooms();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.name LIKE %:name% AND cr.isPublic = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findByNameContaining(@Param("name") String name);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.description LIKE %:keyword% AND cr.isPublic = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.currentParticipants < cr.maxParticipants AND cr.active = true ORDER BY cr.currentParticipants ASC")
    List<ChatRoom> findAvailableRooms();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.currentParticipants >= cr.maxParticipants AND cr.active = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findFullRooms();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.lastActivityAt >= :since ORDER BY cr.lastActivityAt DESC")
    List<ChatRoom> findActiveSince(@Param("since") LocalDateTime since);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.lastActivityAt < :cutoff ORDER BY cr.lastActivityAt ASC")
    List<ChatRoom> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.allowFileSharing = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findFileSharingEnabled();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.allowVoiceNotes = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findVoiceNoteEnabled();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.allowScreenShare = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findScreenShareEnabled();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.allowWhiteboard = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findWhiteboardEnabled();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isEncrypted = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findEncryptedRooms();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.requireApproval = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findApprovalRequired();

    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.active = true")
    long countActiveRooms();

    @Query("SELECT SUM(cr.currentParticipants) FROM ChatRoom cr WHERE cr.active = true")
    Long getTotalActiveParticipants();

    @Query("SELECT AVG(cr.currentParticipants) FROM ChatRoom cr WHERE cr.active = true")
    Double getAverageParticipantsPerRoom();

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.createdAt >= :since ORDER BY cr.createdAt DESC")
    List<ChatRoom> findRecentRooms(@Param("since") LocalDateTime since);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.lastActivityAt < :cutoffDate AND cr.currentParticipants = 0 AND cr.isPublic = false ORDER BY cr.lastActivityAt ASC")
    List<ChatRoom> findRoomsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    // TODO: healthScore is a computed method, not a field - these queries need to be implemented differently
    // @Query("SELECT cr FROM ChatRoom cr WHERE cr.healthScore >= :minScore ORDER BY cr.healthScore DESC")
    // List<ChatRoom> findHealthyRooms(@Param("minScore") int minScore);

    // @Query("SELECT cr FROM ChatRoom cr WHERE cr.healthScore < :maxScore ORDER BY cr.healthScore ASC")
    // List<ChatRoom> findUnhealthyRooms(@Param("maxScore") int maxScore);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = :type AND cr.isPublic = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findPublicByType(@Param("type") ChatRoomType type);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.createdBy = :user AND cr.active = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findActiveByCreator(@Param("user") User user);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isArchived = true ORDER BY cr.createdAt DESC")
    List<ChatRoom> findArchivedRooms();
}
