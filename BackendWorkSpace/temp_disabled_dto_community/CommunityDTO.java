package com.samjhadoo.dto.community;

import com.samjhadoo.model.enums.CommunityType;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class CommunityDTO {
    private Long id;
    private String name;
    private CommunityType type;
    private String description;
    private boolean isPrivate;
    private boolean requiresApproval;
    private Long memberCount;
    private Long createdBy;
    private Instant createdAt;
    private boolean isMember;
    private MemberRole memberRole;
    private MemberStatus memberStatus;
}
