package com.samjhadoo.dto.communication;

import com.samjhadoo.model.enums.communication.ChatRoomType;
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
public class ChatRoomDTO {
    private Long id;
    private String roomName;
    private String roomDescription;
    private ChatRoomType roomType;
    private String roomIcon;
    private boolean isActive;
    private boolean isArchived;
    private boolean isEncrypted;
    private int participantCount;
    private List<ParticipantDTO> participants;
    private ChatMessageDTO lastMessage;
    private LocalDateTime lastActivityAt;
    private long unreadCount;
    private LocalDateTime createdAt;
    private String metadata;
}
