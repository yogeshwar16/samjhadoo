package com.samjhadoo.dto.communication;

import com.samjhadoo.model.enums.communication.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private MessageType type;
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSizeBytes;
    private String fileType;
    private String thumbnailUrl;
    private Integer durationSeconds;
    private String voiceTranscript;
    private Double locationLatitude;
    private Double locationLongitude;
    private String locationName;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private boolean isEdited;
    private boolean isDeleted;
    private boolean isRead;
    private boolean isPinned;
    private boolean isForwarded;
    private boolean isSystemMessage;
    private String replyToMessageId;
    private String replyToContent;
    private String replyToSenderName;
    private List<ReactionDTO> reactions;
    private List<String> readBy;
    private String metadata;
}
