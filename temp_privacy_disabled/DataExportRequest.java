package com.samjhadoo.model.privacy;

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
@Table(name = "data_export_requests")
public class DataExportRequest extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportStatus status = ExportStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportFormat format = ExportFormat.JSON;
    
    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @Column(name = "download_url", length = 1000)
    private String downloadUrl;
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    
    @Column(name = "download_count", nullable = false)
    private int downloadCount = 0;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        EXPIRED
    }
    
    public enum ExportFormat {
        JSON,
        CSV,
        PDF,
        XML
    }
}
