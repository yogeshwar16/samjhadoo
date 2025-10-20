package com.samjhadoo.dto.voice;

import com.samjhadoo.model.voice.VoiceCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCommandDTO {
    private Long id;
    private String sessionId;
    private String audioUrl;
    private Integer audioDurationSeconds;
    private String transcribedText;
    private String detectedLanguage;
    private Double confidenceScore;
    private VoiceCommand.CommandStatus status;
    private VoiceCommand.CommandIntent detectedIntent;
    private Double intentConfidence;
    private Map<String, Object> extractedEntities;
    private String executionResult;
    private String errorMessage;
    private Instant processedAt;
    private Long executionTimeMs;
    private boolean consentGiven;
    private Instant createdAt;
}
