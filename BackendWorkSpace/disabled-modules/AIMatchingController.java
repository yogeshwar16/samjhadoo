package com.samjhadoo.controller.api;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.SessionType;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.ai.AIMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/matching")
@RequiredArgsConstructor
public class AIMatchingController {

    private final AIMatchingService aiMatchingService;

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> findMatchingMentors(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(defaultValue = "MENTORSHIP") SessionType sessionType,
            @RequestParam(defaultValue = "10") int limit) {
        
        // Prepare preferences map
        Map<String, Object> preferences = new HashMap<>();
        if (skills != null && !skills.isEmpty()) {
            preferences.put("skills", skills);
        }
        if (minExperience != null) {
            preferences.put("minExperience", minExperience);
        }
        
        // Get matching mentors
        List<AIMatchingService.MentorMatchResult> results = aiMatchingService.findMatchingMentors(
                currentUser.getUser(), preferences, sessionType, limit);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", results,
            "count", results.size()
        ));
    }
    
    @GetMapping("/mentors/{mentorId}/score")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMentorMatchScore(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String mentorId,
            @RequestParam(defaultValue = "MENTORSHIP") SessionType sessionType) {
        
        // This is a simplified example - in a real app, you'd fetch the mentor by ID
        // and calculate the match score
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "mentorId", mentorId,
                "matchScore", 0.85, // Example score
                "explanation", "This mentor matches 85% of your preferences"
            )
        ));
    }
}
