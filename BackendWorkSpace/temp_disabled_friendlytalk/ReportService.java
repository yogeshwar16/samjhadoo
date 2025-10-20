package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.ReportRequestDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyRoom;
import com.samjhadoo.model.friendlytalk.UserReport;
import com.samjhadoo.repository.friendlytalk.FriendlyRoomRepository;
import com.samjhadoo.repository.friendlytalk.UserReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserReportRepository reportRepository;
    private final FriendlyRoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserActivityService userActivityService;
    private final ModerationActionService moderationActionService;
    
    // In-memory cache for report statistics
    private final Map<String, Object> reportStatsCache = new ConcurrentHashMap<>();
    private final AtomicInteger activeReportCount = new AtomicInteger(0);
    private static final long CACHE_TTL_MINUTES = 5;
    private Instant lastCacheUpdate = Instant.MIN;

    @Transactional
    public UserReport submitReport(ReportRequestDTO request, User reporter) {
        // Validate the report
        validateReportRequest(request, reporter);
        
        // Create and save the report
        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reportedUserId(request.getReportedUserId())
                .room(request.getRoomId() != null ? 
                        roomRepository.findById(request.getRoomId()).orElse(null) : null)
                .type(request.getType())
                .description(request.getDescription())
                .messageId(request.getMessageId())
                .messageSnapshot(request.getMessageSnapshot())
                .status(UserReport.ReportStatus.PENDING)
                .build();
        
        report = reportRepository.save(report);
        
        // Update cache
        incrementActiveReportCount();
        
        // Notify moderators
        notifyModerators(report);
        
        // Log the report
        log.info("New report submitted: {}", report.getId());
        
        return report;
    }
    
    @Transactional
    public UserReview reviewReport(Long reportId, String moderatorId, String action, String notes) {
        UserReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        switch (action.toUpperCase()) {
            case "RESOLVE":
                report.resolve(moderatorId, notes);
                // Apply any necessary moderation actions
                applyModerationActions(report);
                break;
                
            case "DISMISS":
                report.dismiss(moderatorId, notes);
                break;
                
            case "ESCALATE":
                report.escalate(notes, "Escalated for further review");
                notifyAdmins(report);
                break;
                
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
        
        report = reportRepository.save(report);
        
        // Update cache if needed
        if (report.getStatus() == UserReport.ReportStatus.RESOLVED || 
            report.getStatus() == UserReport.ReportStatus.DISMISSED) {
            decrementActiveReportCount();
        }
        
        // Notify relevant parties
        notifyReportUpdate(report);
        
        return report;
    }
    
    // Cache-related methods
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void updateReportStatistics() {
        if (shouldUpdateCache()) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReports", reportRepository.count());
            stats.put("pendingReports", reportRepository.countByStatus(UserReport.ReportStatus.PENDING));
            stats.put("inReviewReports", reportRepository.countByStatus(UserReport.ReportStatus.IN_REVIEW));
            
            // Update cache
            reportStatsCache.clear();
            reportStatsCache.putAll(stats);
            lastCacheUpdate = Instant.now();
            
            log.debug("Updated report statistics cache");
        }
    }
    
    private boolean shouldUpdateCache() {
        return lastCacheUpdate.plus(CACHE_TTL_MINUTES, ChronoUnit.MINUTES).isBefore(Instant.now());
    }
    
    private void incrementActiveReportCount() {
        activeReportCount.incrementAndGet();
    }
    
    private void decrementActiveReportCount() {
        activeReportCount.updateAndGet(count -> Math.max(0, count - 1));
    }
    
    // Notification methods
    private void notifyModerators(UserReport report) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_REPORT");
        payload.put("reportId", report.getId());
        payload.put("reportType", report.getType());
        payload.put("reportedUserId", report.getReportedUserId());
        
        messagingTemplate.convertAndSend("/topic/moderation/reports", payload);
    }
    
    private void notifyAdmins(UserReport report) {
        // Similar to notifyModerators but with higher priority
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ESCALATED_REPORT");
        payload.put("reportId", report.getId());
        payload.put("escalationReason", report.getEscalationReason());
        
        messagingTemplate.convertAndSend("/topic/admin/moderation", payload);
    }
    
    private void notifyReportUpdate(UserReport report) {
        // Notify the reporter
        Map<String, Object> reporterPayload = new HashMap<>();
        reporterPayload.put("type", "REPORT_UPDATE");
        reporterPayload.put("reportId", report.getId());
        reporterPayload.put("status", report.getStatus());
        
        messagingTemplate.convertAndSendToUser(
            report.getReporter().getId().toString(),
            "/queue/report-updates",
            reporterPayload
        );
        
        // If resolved, notify the reported user if applicable
        if (report.getStatus() == UserReport.ReportStatus.RESOLVED) {
            Map<String, Object> reportedUserPayload = new HashMap<>();
            reportedUserPayload.put("type", "REPORT_RESOLVED");
            reportedUserPayload.put("reportId", report.getId());
            reportedUserPayload.put("actionsTaken", report.getModeratorActions());
            
            messagingTemplate.convertAndSendToUser(
                report.getReportedUserId(),
                "/queue/account/notifications",
                reportedUserPayload
            );
        }
    }
    
    // Validation and helper methods
    private void validateReportRequest(ReportRequestDTO request, User reporter) {
        // Basic validation
        if (reporter.getId().equals(request.getReportedUserId())) {
            throw new IllegalArgumentException("Cannot report yourself");
        }
        
        // Check for duplicate reports
        if (reportRepository.existsByReporterIdAndReportedUserIdAndStatusIn(
                reporter.getId(),
                request.getReportedUserId(),
                List.of(UserReport.ReportStatus.PENDING, UserReport.ReportStatus.IN_REVIEW)
        )) {
            throw new IllegalStateException("You already have an active report for this user");
        }
        
        // Additional validation logic...
    }
    
    private void applyModerationActions(UserReport report) {
        // Apply appropriate moderation actions based on report type and severity
        switch (report.getType()) {
            case HARASSMENT:
            case HATE_SPEECH:
                // Apply stronger penalties for serious violations
                moderationActionService.banUser(
                    report.getRoom() != null ? report.getRoom().getId() : null,
                    report.getReportedUserId(),
                    "Automatic ban for " + report.getType(),
                    "system"
                );
                break;
                
            case SPAM:
                // Mute the user temporarily
                moderationActionService.muteUser(
                    report.getRoom() != null ? report.getRoom().getId() : null,
                    report.getReportedUserId(),
                    java.time.Duration.ofHours(24),
                    "Temporary mute for spamming",
                    "system"
                );
                break;
                
            // Handle other report types...
        }
    }
    
    // Additional service methods for querying reports
    @Transactional(readOnly = true)
    public Page<UserReport> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public UserReport getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getReportStatistics() {
        if (shouldUpdateCache()) {
            updateReportStatistics();
        }
        return new HashMap<>(reportStatsCache);
    }
    
    // Scheduled tasks for report management
    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void cleanupOldReports() {
        Instant cutoffDate = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = reportRepository.deleteOlderThan(cutoffDate);
        log.info("Cleaned up {} old reports", deleted);
    }
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void processPendingReports() {
        // Process pending reports that haven't been reviewed
        // This could involve auto-escalation, notifications, etc.
    }
}
