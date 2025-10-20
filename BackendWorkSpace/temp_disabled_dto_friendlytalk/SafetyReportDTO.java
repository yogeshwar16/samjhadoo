package com.samjhadoo.dto.friendlytalk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyReportDTO {
    private Long id;
    private String reporterName;
    private String reportedUserName;
    private String sessionInfo;
    private String roomInfo;
    private String reportType;
    private String status;
    private String severity;
    private String description;
    private String evidenceUrl;
    private boolean anonymous;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String resolverName;
    private String resolutionNotes;
    private String actionTaken;
    private boolean followUpRequired;
    private LocalDateTime followUpDate;
    private String escalatedTo;
    private long ageInHours;
}
