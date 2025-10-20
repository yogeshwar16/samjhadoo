package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendly_room_events")
public class RoomEvent extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private FriendlyRoom room;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    private String userId;
    
    private String details;
    
    public enum EventType {
        ROOM_CREATED,
        ROOM_ENDED,
        USER_JOINED,
        USER_LEFT,
        USER_MUTED,
        USER_UNMUTED,
        USER_BANNED,
        MESSAGE_SENT,
        MESSAGE_DELETED,
        ROOM_SETTINGS_UPDATED
    }
}
