package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.RoomEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RoomEventRepository extends JpaRepository<RoomEvent, Long> {
    
    List<RoomEvent> findByRoomIdOrderByCreatedAtDesc(String roomId);
    
    List<RoomEvent> findByRoomIdAndEventTypeOrderByCreatedAtDesc(
        String roomId, RoomEvent.EventType eventType);
    
    List<RoomEvent> findByUserIdAndRoomIdOrderByCreatedAtDesc(
        String userId, String roomId);
    
    @Query("SELECT e FROM RoomEvent e WHERE e.room.id = :roomId AND e.createdAt >= :since")
    List<RoomEvent> findRecentRoomEvents(
        @Param("roomId") String roomId,
        @Param("since") Instant since);
    
    @Query("SELECT e FROM RoomEvent e WHERE e.room.id = :roomId AND e.eventType = :eventType AND e.createdAt >= :since")
    List<RoomEvent> findRecentRoomEventsByType(
        @Param("roomId") String roomId,
        @Param("eventType") RoomEvent.EventType eventType,
        @Param("since") Instant since);
    
    @Query("SELECT e FROM RoomEvent e WHERE e.room.id = :roomId AND e.userId = :userId ORDER BY e.createdAt DESC")
    Page<RoomEvent> findUserActivityInRoom(
        @Param("roomId") String roomId,
        @Param("userId") String userId,
        Pageable pageable);
    
    long countByRoomId(String roomId);
    
    long countByRoomIdAndEventType(String roomId, RoomEvent.EventType eventType);
    
    @Query("SELECT COUNT(DISTINCT e.userId) FROM RoomEvent e WHERE e.room.id = :roomId")
    long countUniqueUsersInRoom(@Param("roomId") String roomId);
}
