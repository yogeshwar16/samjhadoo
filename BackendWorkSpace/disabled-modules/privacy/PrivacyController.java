package com.samjhadoo.controller.api.privacy;

import com.samjhadoo.dto.privacy.ConsentDTO;
import com.samjhadoo.dto.privacy.DataExportDTO;
import com.samjhadoo.dto.privacy.DeletionRequestDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.ConsentLog;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.privacy.PrivacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/privacy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Privacy & Compliance", description = "GDPR/DPDP compliance endpoints")
public class PrivacyController {

    private final PrivacyService privacyService;

    // Consent Management
    
    @PostMapping("/consent")
    @Operation(summary = "Record consent", description = "Records user consent for data processing")
    public ResponseEntity<ConsentDTO> recordConsent(
            @RequestParam @NotNull String consentType,
            @RequestParam @NotNull boolean granted,
            @RequestParam(defaultValue = "1.0") String version,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser,
            HttpServletRequest request) {
        try {
            User user = currentUser.getUser();
            ConsentLog.ConsentType type = ConsentLog.ConsentType.valueOf(consentType);
            
            ConsentDTO consent = privacyService.recordConsent(
                user, type, granted, version, 
                request.getRemoteAddr(), request.getHeader("User-Agent")
            );
            
            return ResponseEntity.ok(consent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error recording consent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/consents")
    @Operation(summary = "Get consents", description = "Gets all user consents")
    public ResponseEntity<List<ConsentDTO>> getUserConsents(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            List<ConsentDTO> consents = privacyService.getUserConsents(user);
            return ResponseEntity.ok(consents);
        } catch (Exception e) {
            log.error("Error getting consents: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/consent/{consentType}")
    @Operation(summary = "Revoke consent", description = "Revokes user consent")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable String consentType,
            @RequestParam(required = false) String reason,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            ConsentLog.ConsentType type = ConsentLog.ConsentType.valueOf(consentType);
            privacyService.revokeConsent(user, type, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error revoking consent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Data Export (GDPR Right to Data Portability)
    
    @PostMapping("/export")
    @Operation(summary = "Request data export", description = "Requests export of user data (GDPR Article 20)")
    public ResponseEntity<DataExportDTO> requestDataExport(
            @RequestParam(defaultValue = "JSON") String format,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            DataExportDTO export = privacyService.requestDataExport(user, format);
            return ResponseEntity.ok(export);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // Conflict
        } catch (Exception e) {
            log.error("Error requesting data export: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/{requestId}")
    @Operation(summary = "Get export status", description = "Gets the status of a data export request")
    public ResponseEntity<DataExportDTO> getExportStatus(
            @PathVariable String requestId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            DataExportDTO export = privacyService.getExportRequest(requestId, user);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            log.error("Error getting export status: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/export/{requestId}/download")
    @Operation(summary = "Download exported data", description = "Gets download URL for completed export")
    public ResponseEntity<Map<String, String>> getExportDownload(
            @PathVariable String requestId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            String downloadUrl = privacyService.getExportDownloadUrl(requestId, user);
            return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
        } catch (Exception e) {
            log.error("Error getting download URL: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Account Deletion (GDPR Right to be Forgotten)
    
    @PostMapping("/delete-account")
    @Operation(summary = "Request account deletion", description = "Requests account deletion (GDPR Article 17)")
    public ResponseEntity<DeletionRequestDTO> requestAccountDeletion(
            @RequestParam String password,
            @RequestParam(required = false) String reason,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            DeletionRequestDTO deletion = privacyService.requestAccountDeletion(user, reason, password);
            return ResponseEntity.ok(deletion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build(); // Unauthorized
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // Conflict
        } catch (Exception e) {
            log.error("Error requesting account deletion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/delete-account/{requestId}/verify")
    @Operation(summary = "Verify deletion request", description = "Verifies account deletion with code")
    public ResponseEntity<DeletionRequestDTO> verifyDeletion(
            @PathVariable String requestId,
            @RequestParam @NotNull String verificationCode,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            DeletionRequestDTO deletion = privacyService.verifyDeletionRequest(requestId, verificationCode, user);
            return ResponseEntity.ok(deletion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error verifying deletion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete-account/{requestId}")
    @Operation(summary = "Cancel deletion request", description = "Cancels a pending deletion request")
    public ResponseEntity<Void> cancelDeletion(
            @PathVariable String requestId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            privacyService.cancelDeletionRequest(requestId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling deletion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Privacy Settings
    
    @GetMapping("/settings")
    @Operation(summary = "Get privacy settings", description = "Gets user's privacy settings")
    public ResponseEntity<Map<String, Object>> getPrivacySettings(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Map<String, Object> settings = privacyService.getPrivacySettings(user);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("Error getting privacy settings: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/settings")
    @Operation(summary = "Update privacy settings", description = "Updates user's privacy settings")
    public ResponseEntity<Void> updatePrivacySettings(
            @RequestBody Map<String, Object> settings,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            privacyService.updatePrivacySettings(user, settings);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating privacy settings: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/compliance-report")
    @Operation(summary = "Get compliance report", description = "Gets user's compliance report")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Map<String, Object> report = privacyService.generateComplianceReport(user);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating compliance report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
