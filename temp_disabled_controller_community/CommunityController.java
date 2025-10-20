package com.samjhadoo.controller.api.community;

import com.samjhadoo.dto.community.CommunityDTO;
import com.samjhadoo.dto.community.CreateCommunityRequest;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.community.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommunityDTO> createCommunity(
            @Valid @RequestBody CreateCommunityRequest request,
            @CurrentUser User currentUser) {
        
        CommunityDTO createdCommunity = communityService.createCommunity(request, currentUser);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCommunity.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdCommunity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getCommunity(
            @PathVariable Long id,
            @CurrentUser User currentUser) {
        
        CommunityDTO community = communityService.getCommunityById(id, currentUser);
        return ResponseEntity.ok(community);
    }

    @GetMapping
    public ResponseEntity<Page<CommunityDTO>> searchCommunities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isPrivate,
            @PageableDefault(size = 20) Pageable pageable,
            @CurrentUser User currentUser) {
        
        CommunityType communityType = type != null ? CommunityType.valueOf(type.toUpperCase()) : null;
        Page<CommunityDTO> communities = communityService.findCommunities(
                query, communityType, isPrivate, pageable, currentUser);
        
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/recommended")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommunityDTO>> getRecommendedCommunities(
            @CurrentUser User currentUser) {
        
        List<CommunityDTO> communities = communityService.findRecommendedCommunities(currentUser);
        return ResponseEntity.ok(communities);
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> joinCommunity(
            @PathVariable Long id,
            @CurrentUser User currentUser) {
        
        communityService.joinCommunity(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveCommunity(
            @PathVariable Long id,
            @CurrentUser User currentUser) {
        
        communityService.leaveCommunity(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommunityDTO> updateCommunity(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommunityRequest request,
            @CurrentUser User currentUser) {
        
        CommunityDTO updatedCommunity = communityService.updateCommunity(id, request, currentUser);
        return ResponseEntity.ok(updatedCommunity);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable Long id,
            @CurrentUser User currentUser) {
        
        communityService.deleteCommunity(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
