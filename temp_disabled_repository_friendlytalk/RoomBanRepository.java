package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.RoomBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomBanRepository extends JpaRepository<RoomBan, Long> {
    
    Optional<RoomBan> findByRoomIdAndUserId(String roomId, String userId);
    
    List<RoomBan> findByRoomId(String roomId);
    
    List<RoomBan> findByRoomIdAndExpiresAtAfter(String roomId, Instant date);
    
    List<RoomBan> findByUserId(String userId);
    
    boolean existsByRoomIdAndUserIdAndExpiresAtAfter(String roomId, String userId, Instant date);
    
    @Query("SELECT b FROM RoomBan b WHERE b.room.id = :roomId AND b.userId = :userId AND (b.expiresAt > :now OR b.isPermanent = true)")
    Optional<RoomBan> findActiveBan(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
    
    @Query("SELECT b FROM RoomBan b WHERE b.expiresAt < :now AND b.isPermanent = false")
    List<RoomBan> findExpiredBans(@Param("now") Instant now);
    
    @Modifying
    @Query("DELETE FROM RoomBan b WHERE b.room.id = :roomId AND b.userId = :userId")
    void deleteByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE RoomBan b SET b.active = false, b.expiresAt = :now WHERE b.room.id = :roomId AND b.userId = :userId")
    void revokeBan(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
    
    @Query("SELECT COUNT(b) > 0 FROM RoomBan b WHERE b.room.id = :roomId AND b.userId = :userId AND (b.expiresAt > :now OR b.isPermanent = true)")
    boolean isUserBanned(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") Instant now);
}
