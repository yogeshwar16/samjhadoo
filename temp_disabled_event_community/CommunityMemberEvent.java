package com.samjhadoo.event.community;

import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import lombok.Getter;

@Getter
public class CommunityMemberEvent extends CommunityEvent {
    private final User member;
    private final MemberRole previousRole;
    private final MemberRole newRole;
    private final MemberStatus previousStatus;
    private final MemberStatus newStatus;

    public CommunityMemberEvent(Object source, Community community, User actor, User member,
                              MemberRole previousRole, MemberRole newRole,
                              MemberStatus previousStatus, MemberStatus newStatus,
                              EventType eventType) {
        super(source, community, actor, eventType);
        this.member = member;
        this.previousRole = previousRole;
        this.newRole = newRole;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public static CommunityMemberEvent memberJoined(Object source, Community community, User actor, User member) {
        return new CommunityMemberEvent(
                source, community, actor, member,
                null, MemberRole.MEMBER,
                null, MemberStatus.ACTIVE,
                EventType.MEMBER_JOINED
        );
    }

    public static CommunityMemberEvent memberLeft(Object source, Community community, User actor, User member) {
        return new CommunityMemberEvent(
                source, community, actor, member,
                null, null,
                null, null,
                EventType.MEMBER_LEFT
        );
    }

    public static CommunityMemberEvent memberRoleChanged(
            Object source, Community community, User actor, User member,
            MemberRole previousRole, MemberRole newRole) {
        return new CommunityMemberEvent(
                source, community, actor, member,
                previousRole, newRole,
                null, null,
                EventType.MEMBER_ROLE_CHANGED
        );
    }

    public static CommunityMemberEvent memberStatusChanged(
            Object source, Community community, User actor, User member,
            MemberStatus previousStatus, MemberStatus newStatus) {
        return new CommunityMemberEvent(
                source, community, actor, member,
                null, null,
                previousStatus, newStatus,
                EventType.MEMBER_STATUS_CHANGED
        );
    }
}
