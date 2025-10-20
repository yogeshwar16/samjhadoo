package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.SafetyReportDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkRoom;
import com.samjhadoo.model.friendlytalk.FriendlyTalkSession;
import com.samjhadoo.model.friendlytalk.SafetyReport;
import com.samjhadoo.repository.friendlytalk.SafetyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SafetyServiceImpl implements SafetyService {

    private final SafetyReportRepository safetyReportRepository;

    @Override
    public SafetyReportDTO createReport(User reporter, User reportedUser, FriendlyTalkSession session,
                                       FriendlyTalkRoom room, SafetyReport.ReportType reportType,
                                       SafetyReport.ReportSeverity severity, String description,
                                       String evidenceUrl, boolean anonymous) {
        SafetyReport report = SafetyReport.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .session(session)
                .room(room)
                .reportType(reportType)
                .severity(severity)
                .description(description)
                .evidenceUrl(evidenceUrl)
                .anonymous(anonymous)
                .build();

        SafetyReport savedReport = safetyReportRepository.save(report);

        log.info("Created safety report {} by user {}: {} ({})",
                savedReport.getId(), reporter.getId(), reportType, severity);

        return convertToDTO(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getPendingReports(int limit) {
        return safetyReportRepository.findPendingReports().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getUnderReviewReports(int limit) {
        return safetyReportRepository.findUnderReviewReports().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getUserReports(User user) {
        return safetyReportRepository.findByReporter(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getReportsAboutUser(User user) {
        return safetyReportRepository.findByReportedUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markReportUnderReview(Long reportId, User reviewer) {
        SafetyReport report = safetyReportRepository.findById(reportId).orElse(null);
        if (report == null || report.getStatus() != SafetyReport.ReportStatus.PENDING) {
            return false;
        }

        report.markUnderReview();
        safetyReportRepository.save(report);

        log.info("Marked report {} as under review by {}", reportId, reviewer.getId());

        return true;
    }

    @Override
    public boolean resolveReport(Long reportId, User resolver, String notes, String action) {
        SafetyReport report = safetyReportRepository.findById(reportId).orElse(null);
        if (report == null || report.getStatus() != SafetyReport.ReportStatus.UNDER_REVIEW) {
            return false;
        }

        report.resolve(resolver, notes, action);
        safetyReportRepository.save(report);

        // Update user safety score if applicable
        if (report.getReportedUser() != null) {
            updateUserSafetyScore(report.getReportedUser(), report);
        }

        log.info("Resolved report {} by {}: {}", reportId, resolver.getId(), action);

        return true;
    }

    @Override
    public boolean dismissReport(Long reportId, User resolver, String notes) {
        SafetyReport report = safetyReportRepository.findById(reportId).orElse(null);
        if (report == null || report.getStatus() != SafetyReport.ReportStatus.UNDER_REVIEW) {
            return false;
        }

        report.dismiss(resolver, notes);
        safetyReportRepository.save(report);

        log.info("Dismissed report {} by {}", reportId, resolver.getId());

        return true;
    }

    @Override
    public boolean escalateReport(Long reportId, String escalatedTo) {
        SafetyReport report = safetyReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return false;
        }

        report.escalate(escalatedTo);
        safetyReportRepository.save(report);

        log.info("Escalated report {} to {}", reportId, escalatedTo);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getReportsRequiringFollowUp() {
        return safetyReportRepository.findReportsRequiringFollowUp(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getCriticalUnresolvedReports() {
        return safetyReportRepository.findCriticalUnresolvedReports().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSafetyStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalReports = safetyReportRepository.count();
        long pendingReports = safetyReportRepository.countPendingReports();
        long resolvedReports = safetyReportRepository.findByStatus(SafetyReport.ReportStatus.RESOLVED).size();

        stats.put("totalReports", totalReports);
        stats.put("pendingReports", pendingReports);
        stats.put("resolvedReports", resolvedReports);

        // Report type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (SafetyReport.ReportType type : SafetyReport.ReportType.values()) {
            long count = safetyReportRepository.findByReportType(type).size();
            typeDistribution.put(type.name(), count);
        }
        stats.put("typeDistribution", typeDistribution);

        // Severity distribution
        Map<String, Long> severityDistribution = new HashMap<>();
        for (SafetyReport.ReportSeverity severity : SafetyReport.ReportSeverity.values()) {
            long count = safetyReportRepository.findBySeverity(severity).size();
            severityDistribution.put(severity.name(), count);
        }
        stats.put("severityDistribution", severityDistribution);

        return stats;
    }

    @Override
    public int processFollowUpRequirements() {
        List<SafetyReport> reportsNeedingFollowUp = safetyReportRepository.findReportsRequiringFollowUp(LocalDateTime.now());
        int processed = 0;

        for (SafetyReport report : reportsNeedingFollowUp) {
            // In a real implementation, you'd send notifications or escalate
            report.setFollowUpRequired(false);
            safetyReportRepository.save(report);
            processed++;
        }

        if (processed > 0) {
            log.info("Processed {} follow-up requirements", processed);
        }

        return processed;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getModeratorResolvedReports(User moderator, int limit) {
        return safetyReportRepository.findReportsResolvedBy(moderator).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getAnonymousReports(int limit) {
        return safetyReportRepository.findAnonymousReports().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getRecentReports(LocalDateTime since, int limit) {
        return safetyReportRepository.findRecentReports(since).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRecentViolations(User user) {
        LocalDateTime since = LocalDateTime.now().minusDays(7); // Check last 7 days
        long recentReports = safetyReportRepository.findByReportedUser(user).stream()
                .filter(r -> r.getReportedAt().isAfter(since))
                .filter(r -> r.getStatus() == SafetyReport.ReportStatus.RESOLVED)
                .count();

        return recentReports > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserSafetyScore(User user) {
        // Simple safety score calculation based on resolved reports
        List<SafetyReport> resolvedReports = safetyReportRepository.findByReportedUser(user).stream()
                .filter(r -> r.getStatus() == SafetyReport.ReportStatus.RESOLVED)
                .collect(Collectors.toList());

        if (resolvedReports.isEmpty()) {
            return 100; // Perfect score if no violations
        }

        // Calculate score based on severity and recency
        int score = 100;
        LocalDateTime now = LocalDateTime.now();

        for (SafetyReport report : resolvedReports) {
            int penalty = switch (report.getSeverity()) {
                case LOW -> 5;
                case MEDIUM -> 15;
                case HIGH -> 30;
                case CRITICAL -> 50;
            };

            // Increase penalty for recent violations
            long daysSinceReport = java.time.Duration.between(report.getReportedAt(), now).toDays();
            if (daysSinceReport < 30) {
                penalty = (int) (penalty * 1.5); // 50% more penalty for recent violations
            }

            score -= penalty;
        }

        return Math.max(0, score); // Ensure score doesn't go below 0
    }

    @Override
    public void updateUserSafetyScore(User user, SafetyReport report) {
        // This method is called when a report is resolved
        // The safety score will be recalculated when getUserSafetyScore is called
        log.info("Updated safety score for user {} after report resolution", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SafetyReportDTO> getEscalatedReports() {
        return safetyReportRepository.findEscalatedReports().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SafetyReportDTO convertToDTO(SafetyReport report) {
        String reporterName = report.isAnonymous() ? "Anonymous" :
                             report.getReporter().getFirstName() + " " + report.getReporter().getLastName();

        String reportedUserName = report.getReportedUser() != null ?
                                 (report.isAnonymous() ? "Anonymous" :
                                  report.getReportedUser().getFirstName() + " " + report.getReportedUser().getLastName()) : null;

        String sessionInfo = report.getSession() != null ?
                           "Session #" + report.getSession().getId() : null;

        String roomInfo = report.getRoom() != null ?
                         "Room: " + report.getRoom().getName() : null;

        return SafetyReportDTO.builder()
                .id(report.getId())
                .reporterName(reporterName)
                .reportedUserName(reportedUserName)
                .sessionInfo(sessionInfo)
                .roomInfo(roomInfo)
                .reportType(report.getReportType().name())
                .status(report.getStatus().name())
                .severity(report.getSeverity().name())
                .description(report.getDescription())
                .evidenceUrl(report.getEvidenceUrl())
                .anonymous(report.isAnonymous())
                .reportedAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .resolverName(report.getResolver() != null ?
                             (report.isAnonymous() ? "Anonymous" :
                              report.getResolver().getFirstName() + " " + report.getResolver().getLastName()) : null)
                .resolutionNotes(report.getResolutionNotes())
                .actionTaken(report.getActionTaken())
                .followUpRequired(report.isFollowUpRequired())
                .followUpDate(report.getFollowUpDate())
                .escalatedTo(report.getEscalatedTo())
                .ageInHours(report.getAgeInHours())
                .build();
    }
}
