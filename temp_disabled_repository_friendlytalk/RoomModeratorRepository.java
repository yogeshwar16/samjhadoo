package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.RoomModerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomModeratorRepository extends JpaRepository<RoomModerator, Long> {
    
    Optional<RoomModerator> findByRoomIdAndUserId(String roomId, String userId);
    
    List<RoomModerator> findByRoomId(String roomId);
    
    List<RoomModerator> findByUserId(String userId);
    
    boolean existsByRoomIdAndUserId(String roomId, String userId);
    
    @Modifying
    @Query("DELETE FROM RoomModerator m WHERE m.room.id = :roomId AND m.userId = :userId")
    int deleteByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM RoomModerator m WHERE m.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") String roomId);
    
    @Query("SELECT m FROM RoomModerator m WHERE m.room.id = :roomId AND m.userId = :userId")
    Optional<RoomModerator> findModerator(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Query("SELECT COUNT(m) > 0 FROM RoomModerator m WHERE m.room.id = :roomId AND m.userId = :userId")
    boolean isUserModerator(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Query("SELECT m.userId FROM RoomModerator m WHERE m.room.id = :roomId")
    List<String> findModeratorIdsByRoomId(@Param("roomId") String roomId);
    
    @Query("SELECT m FROM RoomModerator m WHERE m.room.id = :roomId AND m.permissions LIKE %:permission%")
    List<RoomModerator> findModeratorsWithPermission(@Param("roomId") String roomId, @Param("permission") String permission);
    
    @Modifying
    @Query("UPDATE RoomModerator m SET m.permissions = :permissions WHERE m.room.id = :roomId AND m.userId = :userId")
    void updateModeratorPermissions(
        @Param("roomId") String roomId,
        @Param("userId") String userId,
        @Param("permissions") String permissions
    );
}
