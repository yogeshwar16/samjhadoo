package com.samjhadoo.dto.voice;

import com.samjhadoo.model.voice.VoiceCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {
    private VoiceCommand.CommandIntent intent;
    private Double confidence;
    private Map<String, Object> entities;
    private boolean isAmbiguous;
    private String originalText;
    private String language;
    private String suggestedAction;
    private String[] clarificationQuestions;
}
