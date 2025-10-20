package com.samjhadoo.repository;

import com.samjhadoo.model.User;
import com.samjhadoo.model.ChatMessage;
import com.samjhadoo.model.ChatRoom;
import com.samjhadoo.model.enums.communication.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Optional<ChatMessage> findByMessageId(String messageId);
    
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);
    
    List<ChatMessage> findByChatRoomAndCreatedAtAfterOrderByCreatedAtAsc(ChatRoom chatRoom, LocalDateTime after);
    
    Page<ChatMessage> findByChatRoomAndTypeOrderByCreatedAtDesc(ChatRoom chatRoom, MessageType type, Pageable pageable);
    
    List<ChatMessage> findByChatRoomAndSenderOrderByCreatedAtDesc(ChatRoom chatRoom, User sender);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.content LIKE %:searchTerm% ORDER BY m.createdAt DESC")
    Page<ChatMessage> searchInRoom(@Param("room") ChatRoom room, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.pinned = true ORDER BY m.createdAt DESC")
    List<ChatMessage> findPinnedMessages(@Param("room") ChatRoom room);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.deleted = false AND m.createdAt >= :since ORDER BY m.createdAt ASC")
    List<ChatMessage> findRecentMessages(@Param("room") ChatRoom room, @Param("since") LocalDateTime since);
    
    long countByChatRoomAndDeletedFalse(ChatRoom chatRoom);
    
    long countByChatRoomAndSenderAndDeletedFalse(ChatRoom chatRoom, User sender);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :room AND m.sender <> :user AND m.isRead = false")
    long countUnreadMessages(@Param("room") ChatRoom room, @Param("user") User user);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.replyToMessageId = :messageId ORDER BY m.createdAt ASC")
    List<ChatMessage> findReplies(@Param("room") ChatRoom room, @Param("messageId") String messageId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.type = :type AND m.deleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findByRoomAndType(@Param("room") ChatRoom room, @Param("type") MessageType type);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room AND m.sender <> :excludeUser AND m.isRead = false ORDER BY m.sentAt ASC")
    List<ChatMessage> findUnreadMessagesForUser(@Param("room") ChatRoom room, @Param("excludeUser") User excludeUser);
}
