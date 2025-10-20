package com.samjhadoo.controller.api.voice;

import com.samjhadoo.dto.voice.VoiceCommandDTO;
import com.samjhadoo.dto.voice.VoiceConsentDTO;
import com.samjhadoo.dto.voice.VoiceSessionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.voice.VoiceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Voice Commands", description = "Voice command processing and management")
public class VoiceCommandController {

    private final VoiceCommandService voiceCommandService;

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Process voice command from audio",
               description = "Transcribes audio and executes the detected command")
    public ResponseEntity<VoiceCommandDTO> processVoiceCommand(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "language", defaultValue = "en") String language,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceCommandDTO result = voiceCommandService.processVoiceCommand(
                    audioFile, user, sessionId, language);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("Consent required for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (IOException e) {
            log.error("Error processing audio file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error processing voice command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/process-text")
    @Operation(summary = "Process text command",
               description = "Processes a text command (for testing or text fallback)")
    public ResponseEntity<VoiceCommandDTO> processTextCommand(
            @RequestParam("text") String text,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "language", defaultValue = "en") String language,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceCommandDTO result = voiceCommandService.processTextCommand(
                    text, user, sessionId, language);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.warn("Consent required for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error processing text command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get command history",
               description = "Retrieves the user's voice command history")
    public ResponseEntity<Page<VoiceCommandDTO>> getCommandHistory(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            Page<VoiceCommandDTO> history = voiceCommandService.getCommandHistory(user, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting command history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/session/start")
    @Operation(summary = "Start voice session",
               description = "Starts a new voice command session")
    public ResponseEntity<VoiceSessionDTO> startSession(
            @RequestParam(value = "language", defaultValue = "en") String language,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceSessionDTO session = voiceCommandService.startSession(user, language);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error starting voice session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/session/{sessionId}/end")
    @Operation(summary = "End voice session",
               description = "Ends an active voice command session")
    public ResponseEntity<Void> endSession(
            @PathVariable String sessionId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            voiceCommandService.endSession(sessionId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error ending voice session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/session/active")
    @Operation(summary = "Get active session",
               description = "Gets the user's current active voice session")
    public ResponseEntity<VoiceSessionDTO> getActiveSession(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceSessionDTO session = voiceCommandService.getActiveSession(user);
            if (session == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error getting active session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/consent")
    @Operation(summary = "Set voice consent",
               description = "Sets or updates user's consent for voice processing")
    public ResponseEntity<VoiceConsentDTO> setConsent(
            @Valid @RequestBody VoiceConsentDTO consentDTO,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceConsentDTO result = voiceCommandService.setConsent(user, consentDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error setting voice consent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/consent")
    @Operation(summary = "Get voice consent",
               description = "Retrieves the user's current voice processing consent status")
    public ResponseEntity<VoiceConsentDTO> getConsent(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            VoiceConsentDTO consent = voiceCommandService.getConsent(user);
            if (consent == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(consent);
        } catch (Exception e) {
            log.error("Error getting voice consent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/consent")
    @Operation(summary = "Revoke voice consent",
               description = "Revokes user's consent for voice processing")
    public ResponseEntity<Void> revokeConsent(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            voiceCommandService.revokeConsent(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error revoking voice consent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get voice command statistics",
               description = "Retrieves statistics about the user's voice command usage")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        
        try {
            User user = currentUser.getUser();
            Map<String, Object> stats = voiceCommandService.getStatistics(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting voice statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
