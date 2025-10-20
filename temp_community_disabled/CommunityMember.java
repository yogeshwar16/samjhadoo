package com.samjhadoo.model.community;

import com.samjhadoo.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "community_members")
@IdClass(CommunityMemberId.class)
public class CommunityMember {
    
    @Id
    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.MEMBER;
    
    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.PENDING;
    
    @Column(name = "joined_at")
    private Long joinedAt = System.currentTimeMillis();
    
    @Column(name = "last_active")
    private Long lastActive = System.currentTimeMillis();
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "is_moderator")
    private boolean isModerator = false;
    
    @Column(name = "receive_notifications")
    private boolean receiveNotifications = true;
}
