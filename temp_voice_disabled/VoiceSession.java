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
@Table(name = "voice_sessions")
public class VoiceSession extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";
    
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    
    @Column(name = "ended_at")
    private Instant endedAt;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @Column(name = "command_count")
    private int commandCount = 0;
    
    @Column(name = "successful_commands")
    private int successfulCommands = 0;
    
    @Column(name = "failed_commands")
    private int failedCommands = 0;
    
    @Column(name = "device_info", length = 200)
    private String deviceInfo;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "context_data", length = 2000)
    private String contextData; // JSON string for maintaining conversation context
    
    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;
    
    @Column(name = "privacy_mode", nullable = false)
    private boolean privacyMode = false;
    
    public void incrementCommandCount() {
        this.commandCount++;
    }
    
    public void incrementSuccessfulCommands() {
        this.successfulCommands++;
    }
    
    public void incrementFailedCommands() {
        this.failedCommands++;
    }
    
    public double getSuccessRate() {
        if (commandCount == 0) return 0.0;
        return (double) successfulCommands / commandCount * 100;
    }
}
