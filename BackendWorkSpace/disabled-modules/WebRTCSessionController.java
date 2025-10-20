package com.samjhadoo.controller.api;

import com.samjhadoo.dto.response.WebRTCSessionResponse;
import com.samjhadoo.service.WebRTCService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webrtc")
@RequiredArgsConstructor
@Tag(name = "WebRTC", description = "APIs for managing WebRTC sessions")
@SecurityRequirement(name = "bearerAuth")
public class WebRTCSessionController {

    private final WebRTCService webRTCService;

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get WebRTC session information")
    public ResponseEntity<WebRTCSessionResponse> getSessionInfo(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "false") boolean isInitiator,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        WebRTCSessionResponse sessionInfo = webRTCService.getSessionInfo(
            sessionId, 
            userDetails.getUsername(), 
            isInitiator
        );
        
        return ResponseEntity.ok(sessionInfo);
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Clean up WebRTC session")
    public ResponseEntity<Void> cleanupSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        webRTCService.cleanupSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
