package com.samjhadoo.service.communication;

import com.samjhadoo.dto.communication.ChatMessageDTO;
import com.samjhadoo.dto.communication.ChatRoomDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for managing chat messages and rooms.
 */
public interface ChatService {
    
    // Message operations
    
    /**
     * Sends a text message
     * @param roomId Chat room ID
     * @param sender Sender user
     * @param content Message content
     * @param replyToMessageId Reply to message ID (optional)
     * @return Sent message
     */
    ChatMessageDTO sendMessage(Long roomId, User sender, String content, Long replyToMessageId);
    
    /**
     * Sends a file message
     * @param roomId Chat room ID
     * @param sender Sender user
     * @param file File to send
     * @param caption Caption for file
     * @return Sent message
     */
    ChatMessageDTO sendFileMessage(Long roomId, User sender, MultipartFile file, String caption);
    
    /**
     * Sends a voice note
     * @param roomId Chat room ID
     * @param sender Sender user
     * @param voiceFile Voice audio file
     * @return Sent message
     */
    ChatMessageDTO sendVoiceNote(Long roomId, User sender, MultipartFile voiceFile);
    
    /**
     * Sends a location message
     * @param roomId Chat room ID
     * @param sender Sender user
     * @param latitude Latitude
     * @param longitude Longitude
     * @param locationName Location name
     * @return Sent message
     */
    ChatMessageDTO sendLocation(Long roomId, User sender, Double latitude, Double longitude, String locationName);
    
    /**
     * Edits a message
     * @param messageId Message ID
     * @param user User editing
     * @param newContent New content
     * @return Updated message
     */
    ChatMessageDTO editMessage(String messageId, User user, String newContent);
    
    /**
     * Deletes a message
     * @param messageId Message ID
     * @param user User deleting
     */
    void deleteMessage(String messageId, User user);
    
    /**
     * Reacts to a message
     * @param messageId Message ID
     * @param user User reacting
     * @param emoji Emoji reaction
     */
    void reactToMessage(String messageId, User user, String emoji);
    
    /**
     * Pins a message
     * @param messageId Message ID
     * @param user User pinning (must have permission)
     */
    void pinMessage(String messageId, User user);
    
    /**
     * Unpins a message
     * @param messageId Message ID
     * @param user User unpinning
     */
    void unpinMessage(String messageId, User user);
    
    // Message retrieval
    
    /**
     * Gets message by ID
     * @param messageId Message ID
     * @param user Requesting user
     * @return Message details
     */
    ChatMessageDTO getMessage(String messageId, User user);
    
    /**
     * Gets chat history for a room
     * @param roomId Room ID
     * @param user Requesting user
     * @param pageable Pagination params
     * @return Page of messages
     */
    Page<ChatMessageDTO> getChatHistory(Long roomId, User user, Pageable pageable);
    
    /**
     * Gets messages after a specific time
     * @param roomId Room ID
     * @param user Requesting user
     * @param afterTimestamp Timestamp to get messages after
     * @return List of messages
     */
    List<ChatMessageDTO> getMessagesAfter(Long roomId, User user, java.time.LocalDateTime afterTimestamp);
    
    /**
     * Searches messages in a room
     * @param roomId Room ID
     * @param user Requesting user
     * @param searchTerm Search term
     * @param pageable Pagination params
     * @return Page of matching messages
     */
    Page<ChatMessageDTO> searchMessages(Long roomId, User user, String searchTerm, Pageable pageable);
    
    /**
     * Gets pinned messages for a room
     * @param roomId Room ID
     * @param user Requesting user
     * @return List of pinned messages
     */
    List<ChatMessageDTO> getPinnedMessages(Long roomId, User user);
    
    /**
     * Gets unread message count
     * @param roomId Room ID
     * @param user User
     * @return Unread count
     */
    long getUnreadCount(Long roomId, User user);
    
    // Read receipts
    
    /**
     * Marks messages as read
     * @param roomId Room ID
     * @param user User marking as read
     * @param upToMessageId Mark all messages up to this ID as read
     */
    void markAsRead(Long roomId, User user, String upToMessageId);
    
    /**
     * Marks all messages in room as read
     * @param roomId Room ID
     * @param user User marking as read
     */
    void markAllAsRead(Long roomId, User user);
    
    // Typing indicators
    
    /**
     * Updates typing status
     * @param roomId Room ID
     * @param user User typing
     * @param isTyping Typing status
     */
    void updateTypingStatus(Long roomId, User user, boolean isTyping);
    
    // Room operations (basic)
    
    /**
     * Gets room details
     * @param roomId Room ID
     * @param user Requesting user
     * @return Room details
     */
    ChatRoomDTO getRoom(Long roomId, User user);
    
    /**
     * Creates a direct chat room between two users
     * @param user1 First user
     * @param user2 Second user
     * @return Created room
     */
    ChatRoomDTO createDirectChat(User user1, User user2);
    
    /**
     * Gets user's chat rooms
     * @param user User
     * @return List of rooms
     */
    List<ChatRoomDTO> getUserRooms(User user);
}
