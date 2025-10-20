package com.samjhadoo.dto.friendlytalk;

import com.samjhadoo.model.friendlytalk.UserReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class ReportDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRequestDTO {
        @NotBlank(message = "Reported user ID is required")
        private String reportedUserId;
        
        private String roomId;
        
        @NotNull(message = "Report type is required")
        private UserReport.ReportType type;
        
        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must be less than 1000 characters")
        private String description;
        
        private String messageId;
        
        @Size(max = 2000, message = "Message snapshot is too long")
        private String messageSnapshot;
        
        // For attachments or additional evidence
        private String[] attachmentUrls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportResponseDTO {
        private Long id;
        private Long reporterId;
        private String reportedUserId;
        private String roomId;
        private UserReport.ReportType type;
        private String description;
        private UserReport.ReportStatus status;
        private Instant createdAt;
        private Instant resolvedAt;
        private String resolutionNotes;
        private boolean isReportedUserNotified;
        private boolean isReporterNotified;
        private String moderatorActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportReviewDTO {
        @NotBlank(message = "Action is required")
        private String action; // RESOLVE, DISMISS, ESCALATE
        
        @Size(max = 1000, message = "Notes must be less than 1000 characters")
        private String notes;
        
        // For RESOLVE action, specify moderation actions taken
        private String moderatorActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportStatisticsDTO {
        private long totalReports;
        private long pendingReports;
        private long inReviewReports;
        private long resolvedReports;
        private long dismissedReports;
        private Map<String, Long> reportsByType;
        private Map<String, Long> reportsByStatus;
        private Map<String, Long> reportsByDay; // Last 30 days
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportNotificationDTO {
        private Long reportId;
        private String type; // NEW_REPORT, REPORT_UPDATED, etc.
        private String message;
        private Instant timestamp;
        private Map<String, Object> data;
    }
}
