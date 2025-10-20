package com.samjhadoo.controller.api.communication;

import com.samjhadoo.dto.communication.ChatMessageDTO;
import com.samjhadoo.dto.communication.ChatRoomDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.communication.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Chat and messaging endpoints")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Send message", description = "Sends a text message to a chat room")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable Long roomId,
            @RequestParam @NotNull String content,
            @RequestParam(required = false) Long replyToMessageId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatMessageDTO message = chatService.sendMessage(roomId, user, content, replyToMessageId);
            return ResponseEntity.ok(message);
        } catch (IllegalAccessError e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/rooms/{roomId}/messages/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Send file", description = "Sends a file message to a chat room")
    public ResponseEntity<ChatMessageDTO> sendFile(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String caption,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatMessageDTO message = chatService.sendFileMessage(roomId, user, file, caption);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error sending file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/rooms/{roomId}/messages/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Send voice note", description = "Sends a voice note to a chat room")
    public ResponseEntity<ChatMessageDTO> sendVoiceNote(
            @PathVariable Long roomId,
            @RequestParam("voice") MultipartFile voiceFile,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatMessageDTO message = chatService.sendVoiceNote(roomId, user, voiceFile);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error sending voice note: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rooms/{roomId}/messages/location")
    @Operation(summary = "Send location", description = "Sends a location message")
    public ResponseEntity<ChatMessageDTO> sendLocation(
            @PathVariable Long roomId,
            @RequestParam @NotNull Double latitude,
            @RequestParam @NotNull Double longitude,
            @RequestParam(required = false) String locationName,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatMessageDTO message = chatService.sendLocation(roomId, user, latitude, longitude, locationName);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error sending location: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Edit message", description = "Edits an existing message")
    public ResponseEntity<ChatMessageDTO> editMessage(
            @PathVariable String messageId,
            @RequestParam @NotNull String content,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatMessageDTO message = chatService.editMessage(messageId, user, content);
            return ResponseEntity.ok(message);
        } catch (IllegalAccessError e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error editing message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete message", description = "Deletes a message")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String messageId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            chatService.deleteMessage(messageId, user);
            return ResponseEntity.ok().build();
        } catch (IllegalAccessError e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/messages/{messageId}/react")
    @Operation(summary = "React to message", description = "Adds an emoji reaction to a message")
    public ResponseEntity<Void> reactToMessage(
            @PathVariable String messageId,
            @RequestParam @NotNull String emoji,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            chatService.reactToMessage(messageId, user, emoji);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error reacting to message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/messages/{messageId}/pin")
    @Operation(summary = "Pin message", description = "Pins a message in the chat room")
    public ResponseEntity<Void> pinMessage(
            @PathVariable String messageId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            chatService.pinMessage(messageId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error pinning message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/messages/{messageId}/pin")
    @Operation(summary = "Unpin message", description = "Unpins a message")
    public ResponseEntity<Void> unpinMessage(
            @PathVariable String messageId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            chatService.unpinMessage(messageId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error unpinning message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Get chat history", description = "Retrieves chat history for a room")
    public ResponseEntity<Page<ChatMessageDTO>> getChatHistory(
            @PathVariable Long roomId,
            @PageableDefault(size = 50) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Page<ChatMessageDTO> messages = chatService.getChatHistory(roomId, user, pageable);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting chat history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/{roomId}/messages/after")
    @Operation(summary = "Get messages after timestamp", description = "Gets messages after a specific timestamp")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesAfter(
            @PathVariable Long roomId,
            @RequestParam @NotNull String after,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            LocalDateTime afterTime = LocalDateTime.parse(after);
            List<ChatMessageDTO> messages = chatService.getMessagesAfter(roomId, user, afterTime);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting messages after timestamp: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms/{roomId}/messages/search")
    @Operation(summary = "Search messages", description = "Searches for messages in a room")
    public ResponseEntity<Page<ChatMessageDTO>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam @NotNull String query,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Page<ChatMessageDTO> messages = chatService.searchMessages(roomId, user, query, pageable);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error searching messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/{roomId}/messages/pinned")
    @Operation(summary = "Get pinned messages", description = "Retrieves all pinned messages in a room")
    public ResponseEntity<List<ChatMessageDTO>> getPinnedMessages(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            List<ChatMessageDTO> messages = chatService.getPinnedMessages(roomId, user);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting pinned messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/{roomId}/unread-count")
    @Operation(summary = "Get unread count", description = "Gets the number of unread messages")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            long count = chatService.getUnreadCount(roomId, user);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rooms/{roomId}/mark-read")
    @Operation(summary = "Mark as read", description = "Marks messages as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @RequestParam(required = false) String upToMessageId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            if (upToMessageId != null) {
                chatService.markAsRead(roomId, user, upToMessageId);
            } else {
                chatService.markAllAsRead(roomId, user);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking as read: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/rooms/{roomId}/typing")
    @Operation(summary = "Update typing status", description = "Updates user's typing status")
    public ResponseEntity<Void> updateTyping(
            @PathVariable Long roomId,
            @RequestParam boolean isTyping,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            chatService.updateTypingStatus(roomId, user, isTyping);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating typing status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room", description = "Gets chat room details")
    public ResponseEntity<ChatRoomDTO> getRoom(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ChatRoomDTO room = chatService.getRoom(roomId, user);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            log.error("Error getting room: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/rooms/direct")
    @Operation(summary = "Create direct chat", description = "Creates a direct chat with another user")
    public ResponseEntity<ChatRoomDTO> createDirectChat(
            @RequestParam @NotNull Long otherUserId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            // TODO: Get other user by ID
            // ChatRoomDTO room = chatService.createDirectChat(user, otherUser);
            return ResponseEntity.status(501).build(); // Not implemented yet
        } catch (Exception e) {
            log.error("Error creating direct chat: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms")
    @Operation(summary = "Get user rooms", description = "Gets all chat rooms for the user")
    public ResponseEntity<List<ChatRoomDTO>> getUserRooms(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            List<ChatRoomDTO> rooms = chatService.getUserRooms(user);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error getting user rooms: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
