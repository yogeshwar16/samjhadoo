package com.samjhadoo.dto.privacy;

import com.samjhadoo.model.privacy.DataExportRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataExportDTO {
    private String requestId;
    private DataExportRequest.ExportStatus status;
    private DataExportRequest.ExportFormat format;
    private Instant requestedAt;
    private Instant processedAt;
    private Instant expiresAt;
    private String downloadUrl;
    private Long fileSizeBytes;
    private int downloadCount;
}
