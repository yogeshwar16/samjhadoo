package com.samjhadoo.controller.api;

import com.samjhadoo.dto.community.CommunityProfileRequest;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.CommunityTag;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.community.CommunityManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Public API for community tag management
 */
@RestController
@RequestMapping("/api/community-management")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommunityManagementController {

    private final CommunityManagementService communityManagementService;

    /**
     * Update user's community tag
     */
    @PostMapping("/tag")
    public ResponseEntity<Void> updateCommunityTag(
            @CurrentUser User user,
            @Valid @RequestBody CommunityProfileRequest request) {
        communityManagementService.updateUserCommunityTag(user, request.getCommunityTag(), request.getDocumentUrl());
        return ResponseEntity.ok().build();
    }

    /**
     * Get current user's community tag
     */
    @GetMapping("/tag")
    public ResponseEntity<CommunityTag> getMyCommunityTag(@CurrentUser User user) {
        CommunityTag tag = communityManagementService.getUserCommunityTag(user.getId());
        return ResponseEntity.ok(tag);
    }

    /**
     * Request verification for community tag
     */
    @PostMapping("/verification/request")
    public ResponseEntity<Void> requestVerification(
            @CurrentUser User user,
            @RequestParam CommunityTag tag,
            @RequestParam String documentUrl) {
        communityManagementService.requestVerification(user, tag, documentUrl);
        return ResponseEntity.ok().build();
    }
}
