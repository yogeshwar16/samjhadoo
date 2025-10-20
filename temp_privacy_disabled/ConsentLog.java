package com.samjhadoo.model.privacy;

import com.samjhadoo.model.User;
import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consent_logs")
public class ConsentLog extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentType consentType;
    
    @Column(nullable = false)
    private boolean granted;
    
    @Column(name = "consent_version", length = 20)
    private String consentVersion;
    
    @Column(name = "consent_text", columnDefinition = "TEXT")
    private String consentText;
    
    @Column(name = "consent_timestamp", nullable = false)
    private Instant consentTimestamp;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "device_info", length = 500)
    private String deviceInfo;
    
    @Column(name = "geolocation", length = 200)
    private String geolocation;
    
    @Column(name = "revoked_at")
    private Instant revokedAt;
    
    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    public enum ConsentType {
        TERMS_OF_SERVICE,
        PRIVACY_POLICY,
        DATA_PROCESSING,
        MARKETING_COMMUNICATIONS,
        ANALYTICS_TRACKING,
        COOKIE_CONSENT,
        VOICE_RECORDING,
        VIDEO_RECORDING,
        LOCATION_TRACKING,
        THIRD_PARTY_SHARING,
        AI_PROCESSING,
        MEDICAL_DATA_PROCESSING,  // HIPAA
        BIOMETRIC_DATA,
        SENSITIVE_PERSONAL_DATA   // DPDP (India)
    }
}
