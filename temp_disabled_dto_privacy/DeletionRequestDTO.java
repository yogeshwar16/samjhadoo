package com.samjhadoo.dto.privacy;

import com.samjhadoo.model.privacy.AccountDeletionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionRequestDTO {
    private String requestId;
    private AccountDeletionRequest.DeletionStatus status;
    private Instant requestedAt;
    private Instant scheduledFor;
    private Instant processedAt;
    private boolean verified;
    private boolean backupCreated;
    private String backupUrl;
    private int gracePeriodDays;
}
