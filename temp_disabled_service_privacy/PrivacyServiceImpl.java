package com.samjhadoo.service.privacy;

import com.samjhadoo.dto.privacy.ConsentDTO;
import com.samjhadoo.dto.privacy.DataExportDTO;
import com.samjhadoo.dto.privacy.DeletionRequestDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.AccountDeletionRequest;
import com.samjhadoo.model.privacy.ConsentLog;
import com.samjhadoo.model.privacy.DataExportRequest;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.privacy.AccountDeletionRequestRepository;
import com.samjhadoo.repository.privacy.ConsentLogRepository;
import com.samjhadoo.repository.privacy.DataExportRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivacyServiceImpl implements PrivacyService {

    private final ConsentLogRepository consentLogRepository;
    private final DataExportRequestRepository exportRequestRepository;
    private final AccountDeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    
    private static final int EXPORT_EXPIRY_DAYS = 30;
    private static final int DELETION_GRACE_PERIOD_DAYS = 30;

    @Override
    public ConsentDTO recordConsent(User user, ConsentLog.ConsentType consentType, boolean granted, 
                                   String version, String ipAddress, String userAgent) {
        // Deactivate previous consents of this type
        List<ConsentLog> existingConsents = consentLogRepository
                .findByUserAndConsentTypeOrderByConsentTimestampDesc(user, consentType);
        existingConsents.forEach(c -> c.setActive(false));
        consentLogRepository.saveAll(existingConsents);
        
        // Create new consent
        ConsentLog consent = ConsentLog.builder()
                .user(user)
                .consentType(consentType)
                .granted(granted)
                .consentVersion(version)
                .consentTimestamp(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .active(true)
                .build();
        
        consent = consentLogRepository.save(consent);
        log.info("Recorded {} consent for user {}: {}", consentType, user.getId(), granted);
        
        return modelMapper.map(consent, ConsentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConsent(User user, ConsentLog.ConsentType consentType) {
        return consentLogRepository.findActiveConsentByUserAndType(user, consentType)
                .map(ConsentLog::isGranted)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentDTO> getUserConsents(User user) {
        List<ConsentLog> consents = consentLogRepository.findActiveConsentsByUser(user);
        return consents.stream()
                .map(c -> modelMapper.map(c, ConsentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void revokeConsent(User user, ConsentLog.ConsentType consentType, String reason) {
        consentLogRepository.findActiveConsentByUserAndType(user, consentType).ifPresent(consent -> {
            consent.setActive(false);
            consent.setRevokedAt(Instant.now());
            consent.setRevocationReason(reason);
            consentLogRepository.save(consent);
            
            log.info("Revoked {} consent for user {}", consentType, user.getId());
        });
    }

    @Override
    public DataExportDTO requestDataExport(User user, String format) {
        // Check for existing pending/processing requests
        List<DataExportRequest.ExportStatus> activeStatuses = Arrays.asList(
                DataExportRequest.ExportStatus.PENDING,
                DataExportRequest.ExportStatus.PROCESSING
        );
        
        if (exportRequestRepository.existsByUserAndStatusIn(user, activeStatuses)) {
            throw new IllegalStateException("You already have a pending data export request");
        }
        
        DataExportRequest request = DataExportRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(user)
                .status(DataExportRequest.ExportStatus.PENDING)
                .format(DataExportRequest.ExportFormat.valueOf(format.toUpperCase()))
                .requestedAt(Instant.now())
                .build();
        
        request = exportRequestRepository.save(request);
        log.info("Created data export request {} for user {}", request.getRequestId(), user.getId());
        
        return modelMapper.map(request, DataExportDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DataExportDTO getExportRequest(String requestId, User user) {
        DataExportRequest request = exportRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Export request not found"));
        
        if (!request.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessError("Unauthorized access");
        }
        
        return modelMapper.map(request, DataExportDTO.class);
    }

    @Override
    @Scheduled(cron = "0 0 */6 * * ?") // Every 6 hours
    public void processPendingExports() {
        List<DataExportRequest> pendingRequests = exportRequestRepository.findPendingRequests();
        
        for (DataExportRequest request : pendingRequests) {
            try {
                request.setStatus(DataExportRequest.ExportStatus.PROCESSING);
                exportRequestRepository.save(request);
                
                // Generate export data
                String exportData = generateUserDataExport(request.getUser(), request.getFormat());
                
                // In production: Upload to cloud storage and get URL
                String downloadUrl = "https://storage.example.com/exports/" + request.getRequestId();
                
                request.setStatus(DataExportRequest.ExportStatus.COMPLETED);
                request.setProcessedAt(Instant.now());
                request.setExpiresAt(Instant.now().plus(EXPORT_EXPIRY_DAYS, ChronoUnit.DAYS));
                request.setDownloadUrl(downloadUrl);
                request.setFileSizeBytes((long) exportData.length());
                
                exportRequestRepository.save(request);
                log.info("Completed export request {}", request.getRequestId());
                
            } catch (Exception e) {
                request.setStatus(DataExportRequest.ExportStatus.FAILED);
                request.setErrorMessage(e.getMessage());
                exportRequestRepository.save(request);
                log.error("Failed to process export request {}: {}", request.getRequestId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getExportDownloadUrl(String requestId, User user) {
        DataExportRequest request = exportRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Export request not found"));
        
        if (!request.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessError("Unauthorized access");
        }
        
        if (request.getStatus() != DataExportRequest.ExportStatus.COMPLETED) {
            throw new IllegalStateException("Export not completed yet");
        }
        
        if (request.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Export has expired");
        }
        
        // Increment download count
        request.setDownloadCount(request.getDownloadCount() + 1);
        exportRequestRepository.save(request);
        
        return request.getDownloadUrl();
    }

    @Override
    public DeletionRequestDTO requestAccountDeletion(User user, String reason, String password) {
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        // Check for existing active request
        List<AccountDeletionRequest.DeletionStatus> activeStatuses = Arrays.asList(
                AccountDeletionRequest.DeletionStatus.PENDING,
                AccountDeletionRequest.DeletionStatus.VERIFIED,
                AccountDeletionRequest.DeletionStatus.SCHEDULED,
                AccountDeletionRequest.DeletionStatus.PROCESSING
        );
        
        if (deletionRequestRepository.existsByUserAndStatusIn(user, activeStatuses)) {
            throw new IllegalStateException("You already have an active deletion request");
        }
        
        String verificationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(user)
                .status(AccountDeletionRequest.DeletionStatus.PENDING)
                .reason(reason)
                .requestedAt(Instant.now())
                .verificationCode(verificationCode)
                .verified(false)
                .build();
        
        request = deletionRequestRepository.save(request);
        log.warn("Account deletion requested for user {}", user.getId());
        
        // TODO: Send verification email/SMS
        
        DeletionRequestDTO dto = modelMapper.map(request, DeletionRequestDTO.class);
        dto.setGracePeriodDays(DELETION_GRACE_PERIOD_DAYS);
        return dto;
    }

    @Override
    public DeletionRequestDTO verifyDeletionRequest(String requestId, String verificationCode, User user) {
        AccountDeletionRequest request = deletionRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deletion request not found"));
        
        if (!request.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessError("Unauthorized access");
        }
        
        if (!request.getVerificationCode().equals(verificationCode)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        
        request.setVerified(true);
        request.setStatus(AccountDeletionRequest.DeletionStatus.SCHEDULED);
        request.setScheduledFor(Instant.now().plus(DELETION_GRACE_PERIOD_DAYS, ChronoUnit.DAYS));
        
        request = deletionRequestRepository.save(request);
        log.warn("Account deletion verified and scheduled for user {}", user.getId());
        
        DeletionRequestDTO dto = modelMapper.map(request, DeletionRequestDTO.class);
        dto.setGracePeriodDays(DELETION_GRACE_PERIOD_DAYS);
        return dto;
    }

    @Override
    public void cancelDeletionRequest(String requestId, User user) {
        AccountDeletionRequest request = deletionRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deletion request not found"));
        
        if (!request.getUser().getId().equals(user.getId())) {
            throw new IllegalAccessError("Unauthorized access");
        }
        
        request.setStatus(AccountDeletionRequest.DeletionStatus.CANCELLED);
        request.setCancelledAt(Instant.now());
        deletionRequestRepository.save(request);
        
        log.info("Account deletion cancelled for user {}", user.getId());
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void processScheduledDeletions() {
        List<AccountDeletionRequest> scheduledDeletions = 
                deletionRequestRepository.findScheduledForDeletion(Instant.now());
        
        for (AccountDeletionRequest request : scheduledDeletions) {
            try {
                request.setStatus(AccountDeletionRequest.DeletionStatus.PROCESSING);
                deletionRequestRepository.save(request);
                
                // Create backup before deletion
                String backupData = generateUserDataExport(request.getUser(), DataExportRequest.ExportFormat.JSON);
                // In production: Upload backup to archive storage
                String backupUrl = "https://archive.example.com/backups/" + request.getRequestId();
                
                request.setBackupCreated(true);
                request.setBackupUrl(backupUrl);
                
                // Delete user data (implement cascade deletion or anonymization)
                deleteOrAnonymizeUserData(request.getUser());
                
                request.setStatus(AccountDeletionRequest.DeletionStatus.COMPLETED);
                request.setProcessedAt(Instant.now());
                deletionRequestRepository.save(request);
                
                log.warn("Completed account deletion for user {}", request.getUser().getId());
                
            } catch (Exception e) {
                request.setStatus(AccountDeletionRequest.DeletionStatus.FAILED);
                deletionRequestRepository.save(request);
                log.error("Failed to delete account {}: {}", request.getRequestId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPrivacySettings(User user) {
        Map<String, Object> settings = new HashMap<>();
        
        // Get all active consents
        List<ConsentLog> consents = consentLogRepository.findActiveConsentsByUser(user);
        Map<String, Boolean> consentMap = consents.stream()
                .collect(Collectors.toMap(
                    c -> c.getConsentType().name(),
                    ConsentLog::isGranted
                ));
        
        settings.put("consents", consentMap);
        settings.put("dataExportAvailable", true);
        settings.put("accountDeletionAvailable", true);
        
        return settings;
    }

    @Override
    public void updatePrivacySettings(User user, Map<String, Object> settings) {
        // Update consents based on settings
        if (settings.containsKey("consents")) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> consents = (Map<String, Boolean>) settings.get("consents");
            
            for (Map.Entry<String, Boolean> entry : consents.entrySet()) {
                try {
                    ConsentLog.ConsentType type = ConsentLog.ConsentType.valueOf(entry.getKey());
                    recordConsent(user, type, entry.getValue(), "1.0", null, null);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid consent type: {}", entry.getKey());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateComplianceReport(User user) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("userId", user.getId());
        report.put("username", user.getUsername());
        report.put("email", user.getEmail());
        report.put("accountCreated", user.getCreatedAt());
        
        // Consents
        List<ConsentDTO> consents = getUserConsents(user);
        report.put("activeConsents", consents);
        
        // Data exports
        List<DataExportRequest> exports = exportRequestRepository.findByUserOrderByRequestedAtDesc(user);
        report.put("dataExportHistory", exports.stream()
                .map(e -> modelMapper.map(e, DataExportDTO.class))
                .collect(Collectors.toList()));
        
        // Deletion requests
        List<AccountDeletionRequest> deletions = deletionRequestRepository.findByUserOrderByRequestedAtDesc(user);
        report.put("deletionHistory", deletions.stream()
                .map(d -> modelMapper.map(d, DeletionRequestDTO.class))
                .collect(Collectors.toList()));
        
        report.put("generatedAt", Instant.now());
        
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getConsentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> consentCounts = consentLogRepository.countActiveConsentsByType();
        Map<String, Long> countMap = consentCounts.stream()
                .collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> (Long) row[1]
                ));
        
        stats.put("consentsByType", countMap);
        stats.put("totalActiveConsents", consentCounts.stream().mapToLong(row -> (Long) row[1]).sum());
        
        return stats;
    }
    
    // Helper methods
    
    private String generateUserDataExport(User user, DataExportRequest.ExportFormat format) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("accountCreated", user.getCreatedAt());
            // Add more user data as needed
            
            return objectMapper.writeValueAsString(userData);
        } catch (Exception e) {
            log.error("Error generating user data export: {}", e.getMessage());
            return "{}";
        }
    }
    
    private void deleteOrAnonymizeUserData(User user) {
        // Implement data deletion or anonymization
        // This is a placeholder - implement based on your data retention policies
        log.info("Deleting/anonymizing data for user {}", user.getId());
        
        // Options:
        // 1. Hard delete (GDPR "right to be forgotten")
        // 2. Anonymize (replace PII with random data)
        // 3. Soft delete (mark as deleted, retain for legal/audit purposes)
    }
}
