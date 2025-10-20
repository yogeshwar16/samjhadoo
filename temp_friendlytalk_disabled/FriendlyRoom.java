package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.audit.DateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "friendly_rooms")
public class FriendlyRoom extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.ACTIVE;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RoomType roomType = RoomType.GROUP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "friendly_room_participants",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new HashSet<>();

    private boolean isPrivate = false;

    private int maxParticipants = 10;

    private int currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    private Mood mood;

    private String topic;

    private String language = "en";

    private boolean isAnonymous = false;

    private boolean isVoiceOnly = false;

    private String roomCode;

    public enum RoomStatus {
        ACTIVE, INACTIVE, FULL, ARCHIVED, MODERATION
    }

    public enum RoomType {
        GROUP, ONE_ON_ONE, MENTORSHIP, SUPPORT, COMMUNITY, EVENT, DROP_IN
    }
}

public enum Mood {
    LONELY("Feeling Lonely", "Looking for someone to talk to"),
    ANXIOUS("Feeling Anxious", "Need someone to listen"),
    EXCITED("Feeling Excited", "Want to share good news"),
    SAD("Feeling Sad", "Need emotional support"),
    HAPPY("Feeling Happy", "Just want to chat"),
    STRESSED("Feeling Stressed", "Need to unwind"),
    CONFUSED("Feeling Confused", "Need advice"),
    BORED("Feeling Bored", "Looking for interesting conversation"),
    ANGRY("Feeling Angry", "Need to vent"),
    GRATEFUL("Feeling Grateful", "Want to share appreciation");

    private final String displayName;
    private final String description;

    Mood(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
