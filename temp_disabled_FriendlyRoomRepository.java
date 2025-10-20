package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendlyRoomRepository extends JpaRepository<FriendlyRoom, Long> {
    
    List<FriendlyRoom> findByStatus(FriendlyRoom.RoomStatus status);
    
    List<FriendlyRoom> findByRoomType(FriendlyRoom.RoomType roomType);
    
    List<FriendlyRoom> findByMood(FriendlyRoom.Mood mood);
    
    List<FriendlyRoom> findByIsPrivate(boolean isPrivate);
    
    List<FriendlyRoom> findByIsAnonymous(boolean isAnonymous);
    
    List<FriendlyRoom> findByIsVoiceOnly(boolean isVoiceOnly);
    
    List<FriendlyRoom> findByLanguage(String language);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.currentParticipants < fr.maxParticipants")
    List<FriendlyRoom> findAvailableRooms();
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.topic LIKE %:topic%")
    List<FriendlyRoom> findByTopicContaining(@Param("topic") String topic);
    
    @Query("SELECT fr FROM FriendlyRoom fr JOIN fr.participants p WHERE p.id = :userId")
    List<FriendlyRoom> findByParticipantId(@Param("userId") Long userId);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.createdBy.id = :userId")
    List<FriendlyRoom> findByCreatorId(@Param("userId") Long userId);
    
    Optional<FriendlyRoom> findByRoomCode(String roomCode);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.currentParticipants < fr.maxParticipants ORDER BY fr.currentParticipants DESC")
    List<FriendlyRoom> findPopularActiveRooms();
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.mood = :mood AND fr.currentParticipants < fr.maxParticipants")
    List<FriendlyRoom> findActiveRoomsByMood(@Param("mood") FriendlyRoom.Mood mood);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.roomType = :roomType AND fr.currentParticipants < fr.maxParticipants")
    List<FriendlyRoom> findActiveRoomsByType(@Param("roomType") FriendlyRoom.RoomType roomType);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.language = :language AND fr.currentParticipants < fr.maxParticipants")
    List<FriendlyRoom> findActiveRoomsByLanguage(@Param("language") String language);
    
    @Query("SELECT fr FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.isPrivate = false AND fr.currentParticipants < fr.maxParticipants")
    List<FriendlyRoom> findActivePublicRooms();
    
    @Query("SELECT COUNT(fr) FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE'")
    long countActiveRooms();
    
    @Query("SELECT COUNT(fr) FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' AND fr.mood = :mood")
    long countActiveRoomsByMood(@Param("mood") FriendlyRoom.Mood mood);
    
    @Query("SELECT fr.mood, COUNT(fr) FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' GROUP BY fr.mood")
    List<Object[]> countActiveRoomsByMood();
    
    @Query("SELECT fr.roomType, COUNT(fr) FROM FriendlyRoom fr WHERE fr.status = 'ACTIVE' GROUP BY fr.roomType")
    List<Object[]> countActiveRoomsByType();
}
