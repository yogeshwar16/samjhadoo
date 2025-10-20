package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.RoomMute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMuteRepository extends JpaRepository<RoomMute, Long> {
    
    Optional<RoomMute> findByRoomIdAndUserId(String roomId, String userId);
    
    List<RoomMute> findByRoomId(String roomId);
    
    List<RoomMute> findByRoomIdAndExpiresAtAfter(String roomId, Instant date);
    
    List<RoomMute> findByUserId(String userId);
    
    boolean existsByRoomIdAndUserIdAndExpiresAtAfter(String roomId, String userId, Instant date);
    
    @Query("SELECT m FROM RoomMute m WHERE m.room.id = :roomId AND m.userId = :userId AND m.expiresAt > :now")
    Optional<RoomMute> findActiveMute(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
    
    @Query("SELECT m FROM RoomMute m WHERE m.expiresAt < :now")
    List<RoomMute> findExpiredMutes(@Param("now") Instant now);
    
    @Modifying
    @Query("DELETE FROM RoomMute m WHERE m.room.id = :roomId AND m.userId = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE RoomMute m SET m.expiresAt = :now WHERE m.room.id = :roomId AND m.userId = :userId")
    void unmuteUser(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
    
    @Query("SELECT COUNT(m) > 0 FROM RoomMute m WHERE m.room.id = :roomId AND m.userId = :userId AND m.expiresAt > :now")
    boolean isUserMuted(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
}
