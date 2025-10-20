package com.samjhadoo.dto.friendlytalk;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChatMessageDTO {
    private String messageId;
    private String roomId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    private String content;
    
    private String timestamp;
    private String type = "TEXT"; // TEXT, IMAGE, FILE, SYSTEM, etc.
    private String status = "SENT"; // SENT, DELIVERED, READ, FAILED
    private String replyToMessageId;
    private String attachmentUrl;
    private String attachmentType;
    private String attachmentName;
    private Long attachmentSize;
    private String metadata; // JSON string for additional data
    
    // For system messages
    private String systemMessageType;
    private Object systemMessageData;
    
    // For reactions
    private String reaction;
    private String reactionToMessageId;
    
    // For typing indicators
    private boolean isTyping = false;
    
    // For read receipts
    private String[] readBy;
    private String[] deliveredTo;
    
    // For end-to-end encryption
    private String encryptionKeyId;
    private String encryptedContent;
    private String encryptionIv;
    
    // For message threading
    private String threadId;
    private boolean isThreadStarter = false;
    private int threadMessageCount = 0;
    
    // For moderation
    private boolean isModerated = false;
    private String moderatedBy;
    private String moderationReason;
    private String moderationTimestamp;
    
    // For message editing
    private boolean isEdited = false;
    private String editedAt;
    private int editCount = 0;
    
    // For message deletion
    private boolean isDeleted = false;
    private String deletedAt;
    private String deletedBy;
    private String deleteReason;
    
    // For message translation
    private String originalLanguage;
    private String translatedContent;
    private String targetLanguage;
    
    // For message search
    private String[] searchTerms;
    private double relevanceScore;
    
    // For analytics
    private String clientId;
    private String userAgent;
    private String ipAddress;
    private String location;
    
    // For future extensibility
    private Object customData;
}
