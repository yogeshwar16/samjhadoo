package com.samjhadoo.dto.community;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class CommunityMemberDTO {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private MemberRole role;
    private MemberStatus status;
    private Instant joinedAt;
    private Instant lastActive;
    private String bio;
    private boolean isModerator;
    private boolean receiveNotifications;
    
    // Additional user profile information
    private String fullName;
    private String title;
    private String company;
    private int mentorRating;
    private int menteeRating;
}
