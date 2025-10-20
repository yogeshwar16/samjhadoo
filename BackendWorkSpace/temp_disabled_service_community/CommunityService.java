package com.samjhadoo.service.community;

import com.samjhadoo.dto.community.CommunityDTO;
import com.samjhadoo.dto.community.CreateCommunityRequest;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.CommunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityService {
    
    CommunityDTO createCommunity(CreateCommunityRequest request, User creator);
    
    CommunityDTO getCommunityById(Long id, User currentUser);
    
    Page<CommunityDTO> findCommunities(String query, CommunityType type, Boolean isPrivate, Pageable pageable, User currentUser);
    
    List<CommunityDTO> findRecommendedCommunities(User currentUser);
    
    CommunityDTO updateCommunity(Long id, CreateCommunityRequest request, User currentUser);
    
    void deleteCommunity(Long id, User currentUser);
    
    void joinCommunity(Long communityId, User user);
    
    void leaveCommunity(Long communityId, User user);
    
    void updateMemberRole(Long communityId, Long userId, String role, User currentUser);
    
    void updateMemberStatus(Long communityId, Long userId, String status, User currentUser);
    
    List<CommunityDTO> getUserCommunities(User user);
    
    boolean isUserMember(Long communityId, Long userId);
    
    boolean isUserAdmin(Long communityId, Long userId);
}
