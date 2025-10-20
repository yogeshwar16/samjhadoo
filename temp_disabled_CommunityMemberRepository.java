package com.samjhadoo.repository;

import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.community.CommunityMember;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    
    Optional<CommunityMember> findByCommunityAndUser(Community community, User user);
    
    List<CommunityMember> findByCommunity(Community community);
    
    List<CommunityMember> findByUser(User user);
    
    List<CommunityMember> findByCommunityAndRole(Community community, MemberRole role);
    
    List<CommunityMember> findByCommunityAndStatus(Community community, MemberStatus status);
    
    @Query("SELECT cm FROM CommunityMember cm WHERE cm.community.id = :communityId AND cm.user.id = :userId")
    Optional<CommunityMember> findByCommunityIdAndUserId(@Param("communityId") Long communityId, 
                                                       @Param("userId") Long userId);
    
    @Query("SELECT cm FROM CommunityMember cm WHERE cm.community.id = :communityId AND cm.role = 'ADMIN'")
    List<CommunityMember> findAdminsByCommunityId(@Param("communityId") Long communityId);
    
    @Modifying
    @Query("UPDATE CommunityMember cm SET cm.status = :status WHERE cm.community.id = :communityId AND cm.user.id = :userId")
    void updateMemberStatus(@Param("communityId") Long communityId, 
                           @Param("userId") Long userId, 
                           @Param("status") MemberStatus status);
    
    @Modifying
    @Query("UPDATE CommunityMember cm SET cm.role = :role WHERE cm.community.id = :communityId AND cm.user.id = :userId")
    void updateMemberRole(@Param("communityId") Long communityId, 
                         @Param("userId") Long userId, 
                         @Param("role") MemberRole role);
    
    @Query("SELECT COUNT(cm) > 0 FROM CommunityMember cm WHERE cm.community.id = :communityId AND cm.user.id = :userId AND cm.status = 'ACTIVE'")
    boolean isUserActiveMember(@Param("communityId") Long communityId, 
                              @Param("userId") Long userId);
    
    @Query("SELECT cm.user FROM CommunityMember cm WHERE cm.community.id = :communityId AND cm.status = 'ACTIVE'")
    List<User> findActiveMembers(@Param("communityId") Long communityId);
}
