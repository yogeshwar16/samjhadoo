package com.samjhadoo.controller.api.v2;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.ai.AIMatchingServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v2/ai/matching")
@RequiredArgsConstructor
public class AIMatchingControllerV2 {

    private final AIMatchingServiceV2 aiMatchingService;

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('USER')")
    public CompletableFuture<ResponseEntity<?>> findMatchingMentors(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(defaultValue = "MENTORSHIP") SessionType sessionType,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> preferences = Map.of(
            "skills", skills != null ? skills : List.of(),
            "minExperience", minExperience != null ? minExperience : 0
        );
        
        return CompletableFuture.completedFuture(
            ResponseEntity.ok(aiMatchingService.findMatchingMentors(
                currentUser.getUser(), preferences, sessionType, limit
            ))
        );
    }
    
    @GetMapping("/mentors/popular")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPopularMentors(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "5") int limit) {
        
        return ResponseEntity.ok(
            aiMatchingService.getPopularMentors(currentUser.getUser(), limit)
        );
    }
    
    @GetMapping("/skills/recommended")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getRecommendedSkills(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(
            aiMatchingService.getRecommendedSkills(currentUser.getUser())
        );
    }
    
    @GetMapping("/matches/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> countPotentialMatches(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(
            Map.of("count", aiMatchingService.countPotentialMatches(currentUser.getUser()))
        );
    }
    
    @GetMapping("/matches/quality/{mentorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMatchQuality(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String mentorId,
            @RequestParam(defaultValue = "MENTORSHIP") SessionType sessionType) {
        
        // In a real implementation, fetch the mentor by ID first
        // For now, returning a placeholder response
        return ResponseEntity.ok(
            aiMatchingService.getMatchQuality(
                currentUser.getUser(), 
                new com.samjhadoo.model.MentorProfile(), // Replace with actual mentor
                sessionType
            )
        );
    }
}
