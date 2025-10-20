package com.samjhadoo.service.community;

import com.samjhadoo.dto.community.CommunityDTO;
import com.samjhadoo.dto.community.CreateCommunityRequest;
import com.samjhadoo.exception.OperationNotAllowedException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.mapper.CommunityMapper;
import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.community.CommunityMember;
import com.samjhadoo.model.enums.CommunityType;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import com.samjhadoo.repository.CommunityMemberRepository;
import com.samjhadoo.repository.CommunityRepository;
import com.samjhadoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;
    private final CommunityMapper communityMapper;

    @Override
    @Transactional
    public CommunityDTO createCommunity(CreateCommunityRequest request, User creator) {
        if (communityRepository.existsByName(request.getName())) {
            throw new OperationNotAllowedException("Community with this name already exists");
        }

        Community community = communityMapper.toEntity(request, creator);
        Community savedCommunity = communityRepository.save(community);
        
        // Add creator as admin
        addCommunityMember(savedCommunity, creator, MemberRole.ADMIN, MemberStatus.ACTIVE);
        
        return getCommunityWithMemberInfo(savedCommunity, creator);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityDTO getCommunityById(Long id, User currentUser) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + id));
        
        if (community.isPrivate() && !isUserMember(community.getId(), currentUser.getId())) {
            throw new OperationNotAllowedException("This is a private community");
        }
        
        return getCommunityWithMemberInfo(community, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityDTO> findCommunities(String query, CommunityType type, Boolean isPrivate, Pageable pageable, User currentUser) {
        return communityRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<javax.persistence.criteria.Predicate>();
            
            if (query != null && !query.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    "%" + query.toLowerCase() + "%"
                ));
            }
            
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            
            if (isPrivate != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPrivate"), isPrivate));
            }
            
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            
            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        }, pageable)
        .map(community -> getCommunityWithMemberInfo(community, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDTO> findRecommendedCommunities(User currentUser) {
        return communityRepository.findRecommendedCommunities(currentUser.getId())
                .stream()
                .map(community -> getCommunityWithMemberInfo(community, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void joinCommunity(Long communityId, User user) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
                
        if (isUserMember(communityId, user.getId())) {
            throw new OperationNotAllowedException("You are already a member of this community");
        }
        
        MemberStatus status = community.isRequiresApproval() ? 
                MemberStatus.PENDING : MemberStatus.ACTIVE;
                
        addCommunityMember(community, user, MemberRole.MEMBER, status);
    }

    @Override
    @Transactional
    public void leaveCommunity(Long communityId, User user) {
        CommunityMember member = communityMemberRepository.findByCommunityIdAndUserId(communityId, user.getId())
                .orElseThrow(() -> new OperationNotAllowedException("You are not a member of this community"));
        
        if (member.getRole() == MemberRole.ADMIN) {
            // Check if this is the last admin
            long adminCount = communityMemberRepository.countByCommunityIdAndRole(communityId, MemberRole.ADMIN);
            if (adminCount <= 1) {
                throw new OperationNotAllowedException("Cannot leave as you are the only admin. Assign another admin first.");
            }
        }
        
        communityMemberRepository.delete(member);
    }

    // Helper methods
    private CommunityDTO getCommunityWithMemberInfo(Community community, User currentUser) {
        CommunityDTO dto = communityMapper.toDTO(community);
        
        if (currentUser != null) {
            communityMemberRepository.findByCommunityIdAndUserId(community.getId(), currentUser.getId())
                    .ifPresent(member -> {
                        dto.setMember(true);
                        dto.setMemberRole(member.getRole());
                        dto.setMemberStatus(member.getStatus());
                    });
        }
        
        return dto;
    }
    
    private void addCommunityMember(Community community, User user, MemberRole role, MemberStatus status) {
        CommunityMember member = new CommunityMember();
        member.setCommunity(community);
        member.setUser(user);
        member.setRole(role);
        member.setStatus(status);
        communityMemberRepository.save(member);
    }
    
    @Override
    public boolean isUserMember(Long communityId, Long userId) {
        return communityMemberRepository.isUserActiveMember(communityId, userId);
    }
    
    @Override
    public boolean isUserAdmin(Long communityId, Long userId) {
        return communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .map(member -> member.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }
    
    // Other implemented methods...
    @Override
    public CommunityDTO updateCommunity(Long id, CreateCommunityRequest request, User currentUser) {
        // Implementation for updating community details
        return null;
    }

    @Override
    public void deleteCommunity(Long id, User currentUser) {
        // Implementation for deleting a community
    }

    @Override
    public void updateMemberRole(Long communityId, Long userId, String role, User currentUser) {
        // Implementation for updating member role
    }

    @Override
    public void updateMemberStatus(Long communityId, Long userId, String status, User currentUser) {
        // Implementation for updating member status
    }

    @Override
    public List<CommunityDTO> getUserCommunities(User user) {
        // Implementation to get all communities of a user
        return null;
    }
}
