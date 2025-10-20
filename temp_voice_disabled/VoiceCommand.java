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
@Table(name = "voice_commands")
public class VoiceCommand extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "audio_url", length = 500)
    private String audioUrl;
    
    @Column(name = "audio_duration_seconds")
    private Integer audioDurationSeconds;
    
    @Column(name = "transcribed_text", length = 2000)
    private String transcribedText;
    
    @Column(name = "detected_language", length = 10)
    private String detectedLanguage = "en";
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status = CommandStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "detected_intent")
    private CommandIntent detectedIntent;
    
    @Column(name = "intent_confidence")
    private Double intentConfidence;
    
    @Column(name = "extracted_entities", length = 2000)
    private String extractedEntities; // JSON string
    
    @Column(name = "execution_result", length = 1000)
    private String executionResult;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "device_info", length = 200)
    private String deviceInfo;
    
    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;
    
    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous = false;
    
    @Column(name = "feedback_rating")
    private Integer feedbackRating;
    
    @Column(name = "feedback_comment", length = 500)
    private String feedbackComment;
    
    public enum CommandStatus {
        PENDING,        // Awaiting processing
        PROCESSING,     // Being transcribed/analyzed
        EXECUTED,       // Successfully executed
        FAILED,         // Failed to execute
        REJECTED        // Rejected due to consent/policy
    }
    
    public enum CommandIntent {
        // Navigation
        NAVIGATE_HOME,
        NAVIGATE_DASHBOARD,
        NAVIGATE_PROFILE,
        NAVIGATE_SESSIONS,
        NAVIGATE_MESSAGES,
        
        // Session Management
        BOOK_SESSION,
        CANCEL_SESSION,
        RESCHEDULE_SESSION,
        JOIN_SESSION,
        END_SESSION,
        
        // Search
        SEARCH_MENTOR,
        SEARCH_TOPIC,
        SEARCH_SESSION,
        
        // Messaging
        SEND_MESSAGE,
        READ_MESSAGES,
        
        // Profile
        UPDATE_PROFILE,
        VIEW_PROFILE,
        
        // Payments
        CHECK_WALLET,
        ADD_FUNDS,
        VIEW_TRANSACTIONS,
        
        // Help
        GET_HELP,
        FAQ,
        
        // Settings
        CHANGE_LANGUAGE,
        UPDATE_PREFERENCES,
        
        // Friendly Talk
        START_FRIENDLY_TALK,
        JOIN_ROOM,
        SET_MOOD,
        
        // General
        UNKNOWN,
        AMBIGUOUS
    }
}
