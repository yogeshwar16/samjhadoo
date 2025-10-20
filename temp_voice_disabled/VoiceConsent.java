package com.samjhadoo.model.voice;

import com.samjhadoo.model.User;
import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "voice_consents")
public class VoiceConsent extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;
    
    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;
    
    @Column(name = "consent_version", length = 10)
    private String consentVersion = "1.0";
    
    @Column(name = "allow_storage", nullable = false)
    private boolean allowStorage = false;
    
    @Column(name = "allow_analytics", nullable = false)
    private boolean allowAnalytics = false;
    
    @Column(name = "allow_third_party_processing", nullable = false)
    private boolean allowThirdPartyProcessing = false;
    
    @Column(name = "data_retention_days")
    private Integer dataRetentionDays = 30;
    
    @Column(name = "revoked_at")
    private Instant revokedAt;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "device_info", length = 200)
    private String deviceInfo;
    
    public void revoke() {
        this.active = false;
        this.consentGiven = false;
        this.revokedAt = Instant.now();
    }
    
    public boolean isValid() {
        return active && consentGiven && revokedAt == null;
    }
}
