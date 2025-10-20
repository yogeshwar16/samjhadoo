package com.samjhadoo.dto.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceConsentDTO {
    
    @NotNull(message = "Consent decision is required")
    private Boolean consentGiven;
    
    private String consentVersion;
    private boolean allowStorage;
    private boolean allowAnalytics;
    private boolean allowThirdPartyProcessing;
    private Integer dataRetentionDays;
    private Instant consentTimestamp;
    private boolean active;
}
