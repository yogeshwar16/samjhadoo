package com.samjhadoo.dto.privacy;

import com.samjhadoo.model.privacy.ConsentLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDTO {
    private Long id;
    private ConsentLog.ConsentType consentType;
    private boolean granted;
    private String consentVersion;
    private Instant consentTimestamp;
    private Instant revokedAt;
    private boolean active;
}
