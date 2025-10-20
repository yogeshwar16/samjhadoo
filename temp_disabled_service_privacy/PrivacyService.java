package com.samjhadoo.service.privacy;

import com.samjhadoo.dto.privacy.ConsentDTO;
import com.samjhadoo.dto.privacy.DataExportDTO;
import com.samjhadoo.dto.privacy.DeletionRequestDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.ConsentLog;

import java.util.List;
import java.util.Map;

/**
 * Service for privacy and compliance operations (GDPR, DPDP, HIPAA).
 */
public interface PrivacyService {
    
    // Consent Management
    
    /**
     * Records user consent
     * @param user User
     * @param consentType Type of consent
     * @param granted Whether consent is granted
     * @param version Consent version
     * @param ipAddress User's IP
     * @param userAgent User agent
     * @return Consent log
     */
    ConsentDTO recordConsent(User user, ConsentLog.ConsentType consentType, boolean granted, 
                            String version, String ipAddress, String userAgent);
    
    /**
     * Gets user's consent status for a type
     * @param user User
     * @param consentType Consent type
     * @return true if consent is granted
     */
    boolean hasConsent(User user, ConsentLog.ConsentType consentType);
    
    /**
     * Gets all consents for user
     * @param user User
     * @return List of consents
     */
    List<ConsentDTO> getUserConsents(User user);
    
    /**
     * Revokes consent
     * @param user User
     * @param consentType Consent type
     * @param reason Revocation reason
     */
    void revokeConsent(User user, ConsentLog.ConsentType consentType, String reason);
    
    // Data Export (GDPR Article 20 - Right to Data Portability)
    
    /**
     * Requests data export for user
     * @param user User
     * @param format Export format
     * @return Export request
     */
    DataExportDTO requestDataExport(User user, String format);
    
    /**
     * Gets export request status
     * @param requestId Request ID
     * @param user User
     * @return Export request
     */
    DataExportDTO getExportRequest(String requestId, User user);
    
    /**
     * Processes pending data exports
     */
    void processPendingExports();
    
    /**
     * Gets download URL for completed export
     * @param requestId Request ID
     * @param user User
     * @return Download URL
     */
    String getExportDownloadUrl(String requestId, User user);
    
    // Account Deletion (GDPR Article 17 - Right to be Forgotten)
    
    /**
     * Requests account deletion
     * @param user User
     * @param reason Deletion reason
     * @param password Password for verification
     * @return Deletion request
     */
    DeletionRequestDTO requestAccountDeletion(User user, String reason, String password);
    
    /**
     * Verifies deletion request
     * @param requestId Request ID
     * @param verificationCode Verification code
     * @param user User
     * @return Updated deletion request
     */
    DeletionRequestDTO verifyDeletionRequest(String requestId, String verificationCode, User user);
    
    /**
     * Cancels deletion request
     * @param requestId Request ID
     * @param user User
     */
    void cancelDeletionRequest(String requestId, User user);
    
    /**
     * Processes scheduled account deletions
     */
    void processScheduledDeletions();
    
    // Privacy Settings
    
    /**
     * Gets user's privacy settings
     * @param user User
     * @return Privacy settings map
     */
    Map<String, Object> getPrivacySettings(User user);
    
    /**
     * Updates privacy settings
     * @param user User
     * @param settings Settings to update
     */
    void updatePrivacySettings(User user, Map<String, Object> settings);
    
    // Compliance Reports
    
    /**
     * Generates compliance report for user
     * @param user User
     * @return Compliance report
     */
    Map<String, Object> generateComplianceReport(User user);
    
    /**
     * Gets consent statistics for admin
     * @return Consent statistics
     */
    Map<String, Object> getConsentStatistics();
}
