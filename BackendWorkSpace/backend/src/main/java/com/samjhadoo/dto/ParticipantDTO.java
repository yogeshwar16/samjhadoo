package com.samjhadoo.dto.communication;

import com.samjhadoo.model.enums.communication.ChatParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {
    private Long userId;
    private String userName;
    private String userAvatar;
    private ChatParticipantRole role;
    private boolean isOnline;
    private boolean isTyping;
    private LocalDateTime lastSeenAt;
    private LocalDateTime joinedAt;
}
