package com.samjhadoo.dto.friendlytalk;

import com.samjhadoo.model.enums.friendlytalk.RoomStatus;
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
public class FriendlyTalkRoomDTO {
    private Long id;
    private String roomId;
    private String name;
    private String description;
    private String creatorName;
    private RoomStatus status;
    private int maxParticipants;
    private int currentParticipants;
    private boolean anonymous;
    private boolean recorded;
    private boolean allowScreenShare;
    private boolean requireModeratorApproval;
    private String moderatorName;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int durationMinutes;
    private String topicTags;
    private String moodFocus;
    private List<String> participantNames;
    private boolean userIsParticipant;
    private boolean userIsModerator;
}
