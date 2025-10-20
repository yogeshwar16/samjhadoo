package com.samjhadoo.controller.api.community;

import com.samjhadoo.dto.community.CommunityMemberDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.community.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities/{communityId}/members")
@RequiredArgsConstructor
public class CommunityMemberController {

    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<Page<CommunityMemberDTO>> getCommunityMembers(
            @PathVariable Long communityId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Pageable pageable,
            @CurrentUser User currentUser) {
        
        // Only members can see other members
        if (!communityService.isUserMember(communityId, currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        // Implementation will be added in the service layer
        Page<CommunityMemberDTO> members = communityService.getCommunityMembers(
                communityId, role, status, pageable, currentUser);
        
        return ResponseEntity.ok(members);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long communityId,
            @PathVariable Long userId,
            @RequestParam String role,
            @CurrentUser User currentUser) {
        
        communityService.updateMemberRole(communityId, userId, role, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updateMemberStatus(
            @PathVariable Long communityId,
            @PathVariable Long userId,
            @RequestParam String status,
            @CurrentUser User currentUser) {
        
        communityService.updateMemberStatus(communityId, userId, status, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long communityId,
            @PathVariable Long userId,
            @CurrentUser User currentUser) {
        
        // Implementation will be added in the service layer
        communityService.removeMember(communityId, userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommunityMemberDTO> getMyMembership(
            @PathVariable Long communityId,
            @CurrentUser User currentUser) {
        
        // Implementation will be added in the service layer
        CommunityMemberDTO membership = communityService.getCommunityMember(communityId, currentUser.getId());
        return ResponseEntity.ok(membership);
    }
}
