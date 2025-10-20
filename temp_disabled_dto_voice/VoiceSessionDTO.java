package com.samjhadoo.dto.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSessionDTO {
    private String sessionId;
    private String preferredLanguage;
    private Instant startedAt;
    private Instant endedAt;
    private boolean active;
    private int commandCount;
    private int successfulCommands;
    private int failedCommands;
    private double successRate;
    private String contextData;
}
