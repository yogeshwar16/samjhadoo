package com.samjhadoo.service.communication;

import com.samjhadoo.dto.communication.ChatMessageDTO;
import com.samjhadoo.dto.communication.ChatRoomDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.communication.ChatMessage;
import com.samjhadoo.model.communication.ChatParticipant;
import com.samjhadoo.model.communication.ChatRoom;
import com.samjhadoo.model.enums.communication.ChatRoomType;
import com.samjhadoo.model.enums.communication.MessageType;
import com.samjhadoo.repository.communication.ChatMessageRepository;
import com.samjhadoo.repository.communication.ChatParticipantRepository;
import com.samjhadoo.repository.communication.ChatRoomRepository;
import com.samjhadoo.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatParticipantRepository participantRepository;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;

    @Override
    public ChatMessageDTO sendMessage(Long roomId, User sender, String content, Long replyToMessageId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, sender);
        
        ChatMessage replyTo = null;
        if (replyToMessageId != null) {
            replyTo = messageRepository.findById(replyToMessageId).orElse(null);
        }
        
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .chatRoom(room)
                .sender(sender)
                .type(MessageType.TEXT)
                .content(content)
                .replyToMessage(replyTo)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .isEdited(false)
                .isDeleted(false)
                .build();
        
        message = messageRepository.save(message);
        
        // Update room last activity
        room.setLastActivityAt(LocalDateTime.now());
        roomRepository.save(room);
        
        // Broadcast via WebSocket
        ChatMessageDTO messageDTO = convertToDTO(message);
        broadcastMessage(room, messageDTO);
        
        log.info("User {} sent message in room {}", sender.getId(), roomId);
        return messageDTO;
    }

    @Override
    public ChatMessageDTO sendFileMessage(Long roomId, User sender, MultipartFile file, String caption) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, sender);
        
        // Upload file
        String fileUrl = fileStorageService.storeFile(file, "chat-files");
        String thumbnailUrl = null;
        
        // Generate thumbnail for images
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            thumbnailUrl = fileStorageService.generateThumbnail(fileUrl);
        }
        
        MessageType messageType = determineMessageType(file.getContentType());
        
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .chatRoom(room)
                .sender(sender)
                .type(messageType)
                .content(caption)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSizeBytes(file.getSize())
                .fileType(file.getContentType())
                .thumbnailUrl(thumbnailUrl)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .isEdited(false)
                .isDeleted(false)
                .build();
        
        message = messageRepository.save(message);
        
        // Update room
        room.setLastActivityAt(LocalDateTime.now());
        roomRepository.save(room);
        
        ChatMessageDTO messageDTO = convertToDTO(message);
        broadcastMessage(room, messageDTO);
        
        log.info("User {} sent file message in room {}", sender.getId(), roomId);
        return messageDTO;
    }

    @Override
    public ChatMessageDTO sendVoiceNote(Long roomId, User sender, MultipartFile voiceFile) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, sender);
        
        // Upload voice file
        String voiceUrl = fileStorageService.storeFile(voiceFile, "voice-notes");
        
        // TODO: Transcribe voice note using speech-to-text service
        String transcript = ""; // voiceTranscriptionService.transcribe(voiceFile);
        
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .chatRoom(room)
                .sender(sender)
                .type(MessageType.VOICE)
                .fileUrl(voiceUrl)
                .fileName(voiceFile.getOriginalFilename())
                .fileSizeBytes(voiceFile.getSize())
                .fileType(voiceFile.getContentType())
                .voiceTranscript(transcript)
                .durationSeconds(0) // TODO: Calculate duration
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .isDeleted(false)
                .build();
        
        message = messageRepository.save(message);
        
        room.setLastActivityAt(LocalDateTime.now());
        roomRepository.save(room);
        
        ChatMessageDTO messageDTO = convertToDTO(message);
        broadcastMessage(room, messageDTO);
        
        log.info("User {} sent voice note in room {}", sender.getId(), roomId);
        return messageDTO;
    }

    @Override
    public ChatMessageDTO sendLocation(Long roomId, User sender, Double latitude, Double longitude, String locationName) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, sender);
        
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .chatRoom(room)
                .sender(sender)
                .type(MessageType.LOCATION)
                .locationLatitude(latitude)
                .locationLongitude(longitude)
                .locationName(locationName)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .isDeleted(false)
                .build();
        
        message = messageRepository.save(message);
        
        room.setLastActivityAt(LocalDateTime.now());
        roomRepository.save(room);
        
        ChatMessageDTO messageDTO = convertToDTO(message);
        broadcastMessage(room, messageDTO);
        
        return messageDTO;
    }

    @Override
    public ChatMessageDTO editMessage(String messageId, User user, String newContent) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Verify sender
        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalAccessError("You can only edit your own messages");
        }
        
        message.setContent(newContent);
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());
        
        message = messageRepository.save(message);
        
        ChatMessageDTO messageDTO = convertToDTO(message);
        broadcastMessageUpdate(message.getChatRoom(), messageDTO);
        
        log.info("User {} edited message {}", user.getId(), messageId);
        return messageDTO;
    }

    @Override
    public void deleteMessage(String messageId, User user) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Verify sender or admin
        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalAccessError("You can only delete your own messages");
        }
        
        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
        
        // Broadcast deletion
        messagingTemplate.convertAndSend(
            "/topic/room/" + message.getChatRoom().getId() + "/messages/deleted",
            messageId
        );
        
        log.info("User {} deleted message {}", user.getId(), messageId);
    }

    @Override
    public void reactToMessage(String messageId, User user, String emoji) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // TODO: Implement reaction tracking (could use a separate Reaction entity or JSON field)
        // For now, just broadcast the reaction
        messagingTemplate.convertAndSend(
            "/topic/room/" + message.getChatRoom().getId() + "/reactions",
            Map.of("messageId", messageId, "userId", user.getId(), "emoji", emoji)
        );
    }

    @Override
    public void pinMessage(String messageId, User user) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // TODO: Verify user has permission to pin
        
        message.setIsPinned(true);
        message.setPinnedAt(LocalDateTime.now());
        message.setPinnedBy(user.getId());
        messageRepository.save(message);
        
        log.info("User {} pinned message {}", user.getId(), messageId);
    }

    @Override
    public void unpinMessage(String messageId, User user) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        message.setIsPinned(false);
        messageRepository.save(message);
        
        log.info("User {} unpinned message {}", user.getId(), messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessageDTO getMessage(String messageId, User user) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Verify user is in room
        validateParticipant(message.getChatRoom(), user);
        
        return convertToDTO(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getChatHistory(Long roomId, User user, Pageable pageable) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, user);
        
        Page<ChatMessage> messages = messageRepository.findByChatRoomOrderBySentAtDesc(room, pageable);
        return messages.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessagesAfter(Long roomId, User user, LocalDateTime afterTimestamp) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, user);
        
        List<ChatMessage> messages = messageRepository.findByChatRoomAndSentAtAfterOrderBySentAtAsc(
                room, afterTimestamp);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> searchMessages(Long roomId, User user, String searchTerm, Pageable pageable) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, user);
        
        Page<ChatMessage> messages = messageRepository.searchInRoom(room, searchTerm, pageable);
        return messages.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getPinnedMessages(Long roomId, User user) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, user);
        
        List<ChatMessage> messages = messageRepository.findPinnedMessages(room);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long roomId, User user) {
        ChatRoom room = getRoomOrThrow(roomId);
        return messageRepository.countUnreadMessages(room, user);
    }

    @Override
    public void markAsRead(Long roomId, User user, String upToMessageId) {
        ChatRoom room = getRoomOrThrow(roomId);
        
        // Get unread messages
        List<ChatMessage> unreadMessages = messageRepository.findUnreadMessagesForUser(room, user);
        
        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
            if (upToMessageId != null && message.getMessageId().equals(upToMessageId)) {
                break;
            }
        }
        
        messageRepository.saveAll(unreadMessages);
        log.debug("Marked messages as read for user {} in room {}", user.getId(), roomId);
    }

    @Override
    public void markAllAsRead(Long roomId, User user) {
        markAsRead(roomId, user, null);
    }

    @Override
    public void updateTypingStatus(Long roomId, User user, boolean isTyping) {
        // Broadcast typing status via WebSocket
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/typing",
            Map.of("userId", user.getId(), "userName", user.getUsername(), "isTyping", isTyping)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomDTO getRoom(Long roomId, User user) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateParticipant(room, user);
        return modelMapper.map(room, ChatRoomDTO.class);
    }

    @Override
    public ChatRoomDTO createDirectChat(User user1, User user2) {
        // Check if direct chat already exists
        // TODO: Implement check for existing direct chat
        
        ChatRoom room = ChatRoom.builder()
                .roomName("Direct Chat")
                .roomType(ChatRoomType.DIRECT)
                .isActive(true)
                .isArchived(false)
                .lastActivityAt(LocalDateTime.now())
                .build();
        
        room = roomRepository.save(room);
        
        // Add participants
        ChatParticipant participant1 = ChatParticipant.builder()
                .chatRoom(room)
                .user(user1)
                .joinedAt(LocalDateTime.now())
                .build();
        
        ChatParticipant participant2 = ChatParticipant.builder()
                .chatRoom(room)
                .user(user2)
                .joinedAt(LocalDateTime.now())
                .build();
        
        participantRepository.save(participant1);
        participantRepository.save(participant2);
        
        log.info("Created direct chat between users {} and {}", user1.getId(), user2.getId());
        return modelMapper.map(room, ChatRoomDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getUserRooms(User user) {
        List<ChatParticipant> participants = participantRepository.findByUser(user);
        return participants.stream()
                .map(p -> modelMapper.map(p.getChatRoom(), ChatRoomDTO.class))
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private ChatRoom getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
    }
    
    private void validateParticipant(ChatRoom room, User user) {
        if (!participantRepository.existsByChatRoomAndUser(room, user)) {
            throw new IllegalAccessError("User is not a participant of this chat room");
        }
    }
    
    private MessageType determineMessageType(String contentType) {
        if (contentType == null) return MessageType.FILE;
        
        if (contentType.startsWith("image/")) return MessageType.IMAGE;
        if (contentType.startsWith("video/")) return MessageType.VIDEO;
        if (contentType.startsWith("audio/")) return MessageType.VOICE;
        
        return MessageType.FILE;
    }
    
    private void broadcastMessage(ChatRoom room, ChatMessageDTO messageDTO) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + room.getId() + "/messages",
            messageDTO
        );
    }
    
    private void broadcastMessageUpdate(ChatRoom room, ChatMessageDTO messageDTO) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + room.getId() + "/messages/updated",
            messageDTO
        );
    }
    
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        return modelMapper.map(message, ChatMessageDTO.class);
    }
}
