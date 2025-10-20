package com.samjhadoo.service.event;

import com.samjhadoo.event.community.CommunityEvent;
import com.samjhadoo.event.community.CommunityMemberEvent;
import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void publishCommunityCreated(Community community, User creator) {
        log.debug("Publishing community created event for community: {}", community.getId());
        eventPublisher.publishEvent(
            new CommunityEvent(this, community, creator, CommunityEvent.EventType.COMMUNITY_CREATED)
        );
    }

    @Async
    public void publishCommunityUpdated(Community community, User updater) {
        log.debug("Publishing community updated event for community: {}", community.getId());
        eventPublisher.publishEvent(
            new CommunityEvent(this, community, updater, CommunityEvent.EventType.COMMUNITY_UPDATED)
        );
    }

    @Async
    public void publishMemberJoined(Community community, User member) {
        log.debug("Publishing member joined event for community: {}, member: {}", community.getId(), member.getId());
        eventPublisher.publishEvent(
            CommunityMemberEvent.memberJoined(this, community, member, member)
        );
    }

    @Async
    public void publishMemberLeft(Community community, User member) {
        log.debug("Publishing member left event for community: {}, member: {}", community.getId(), member.getId());
        eventPublisher.publishEvent(
            CommunityMemberEvent.memberLeft(this, community, member, member)
        );
    }

    @Async
    public void publishMemberRoleChanged(Community community, User admin, User member, 
                                        MemberRole previousRole, MemberRole newRole) {
        log.debug("Publishing role change event for community: {}, member: {}, new role: {}", 
                community.getId(), member.getId(), newRole);
        eventPublisher.publishEvent(
            CommunityMemberEvent.memberRoleChanged(this, community, admin, member, previousRole, newRole)
        );
    }

    @Async
    public void publishMemberStatusChanged(Community community, User admin, User member,
                                         MemberStatus previousStatus, MemberStatus newStatus) {
        log.debug("Publishing status change event for community: {}, member: {}, new status: {}", 
                community.getId(), member.getId(), newStatus);
        eventPublisher.publishEvent(
            CommunityMemberEvent.memberStatusChanged(this, community, admin, member, previousStatus, newStatus)
        );
    }
}
