package com.samjhadoo.dto.friendlytalk;

import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.Mood;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class FriendlyRoomRequest {
    
    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must be less than 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;
    
    @NotNull(message = "Room type is required")
    private FriendlyRoom.RoomType roomType;
    
    @NotNull(message = "Mood is required")
    private Mood mood;
    
    private String topic;
    private String language = "en";
    private boolean isPrivate = false;
    private boolean isAnonymous = false;
    private boolean isVoiceOnly = false;
    private int maxParticipants = 10;
}

@Data
public class FriendlyRoomResponse {
    private Long id;
    private String name;
    private String description;
    private FriendlyRoom.RoomType roomType;
    private Mood mood;
    private String topic;
    private String language;
    private boolean isPrivate;
    private boolean isAnonymous;
    private boolean isVoiceOnly;
    private int maxParticipants;
    private int currentParticipants;
    private String roomCode;
    private Long createdById;
    private String createdByUsername;
    private String createdAt;
    private String updatedAt;
}

@Data
public class JoinRoomRequest {
    @NotBlank(message = "Room code is required")
    private String roomCode;
    
    private boolean joinAnonymously = false;
}

@Data
public class RoomMessageRequest {
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    private String content;
    
    private Long replyToMessageId;
    private String attachmentUrl;
    private String attachmentType;
}

@Data
public class RoomMessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private boolean isAnonymous;
    private Long replyToMessageId;
    private String attachmentUrl;
    private String attachmentType;
    private String sentAt;
    private boolean isModerated;
}
