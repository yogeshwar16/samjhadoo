package com.samjhadoo.event.community;

import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
public abstract class CommunityEvent extends ApplicationEvent {
    private final Community community;
    private final User actor;
    private final EventType eventType;

    public CommunityEvent(Object source, Community community, User actor, EventType eventType) {
        super(source);
        this.community = community;
        this.actor = actor;
        this.eventType = eventType;
    }

    public enum EventType {
        COMMUNITY_CREATED,
        COMMUNITY_UPDATED,
        COMMUNITY_DELETED,
        MEMBER_JOINED,
        MEMBER_LEFT,
        MEMBER_ROLE_CHANGED,
        MEMBER_STATUS_CHANGED,
        COMMUNITY_SETTINGS_UPDATED
    }
}
