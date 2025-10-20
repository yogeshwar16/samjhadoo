package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.SafetyReportDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkRoom;
import com.samjhadoo.model.friendlytalk.FriendlyTalkSession;
import com.samjhadoo.model.friendlytalk.SafetyReport;

import java.util.List;

/**
 * Service for managing safety reports and moderation in friendly talk.
 */
public interface SafetyService {

    /**
     * Creates a safety report.
     * @param reporter The user reporting
     * @param reportedUser The user being reported (optional)
     * @param session The session being reported (optional)
     * @param room The room being reported (optional)
     * @param reportType The type of report
     * @param severity The severity level
     * @param description Report description
     * @param evidenceUrl URL to evidence (optional)
     * @param anonymous Whether the report is anonymous
     * @return The created safety report DTO
     */
    SafetyReportDTO createReport(User reporter, User reportedUser, FriendlyTalkSession session,
                                FriendlyTalkRoom room, SafetyReport.ReportType reportType,
                                SafetyReport.ReportSeverity severity, String description,
                                String evidenceUrl, boolean anonymous);

    /**
     * Gets all pending safety reports.
     * @param limit Maximum number of reports to return
     * @return List of pending safety report DTOs
     */
    List<SafetyReportDTO> getPendingReports(int limit);

    /**
     * Gets safety reports under review.
     * @param limit Maximum number of reports to return
     * @return List of under review safety report DTOs
     */
    List<SafetyReportDTO> getUnderReviewReports(int limit);

    /**
     * Gets safety reports for a specific user (as reporter).
     * @param user The user
     * @return List of user's safety reports
     */
    List<SafetyReportDTO> getUserReports(User user);

    /**
     * Gets safety reports about a specific user.
     * @param user The reported user
     * @return List of reports about the user
     */
    List<SafetyReportDTO> getReportsAboutUser(User user);

    /**
     * Marks a report as under review.
     * @param reportId The report ID
     * @param reviewer The user reviewing
     * @return true if marked successfully
     */
    boolean markReportUnderReview(Long reportId, User reviewer);

    /**
     * Resolves a safety report.
     * @param reportId The report ID
     * @param resolver The user resolving
     * @param notes Resolution notes
     * @param action Action taken
     * @return true if resolved successfully
     */
    boolean resolveReport(Long reportId, User resolver, String notes, String action);

    /**
     * Dismisses a safety report.
     * @param reportId The report ID
     * @param resolver The user dismissing
     * @param notes Dismissal notes
     * @return true if dismissed successfully
     */
    boolean dismissReport(Long reportId, User resolver, String notes);

    /**
     * Escalates a safety report.
     * @param reportId The report ID
     * @param escalatedTo The system/email to escalate to
     * @return true if escalated successfully
     */
    boolean escalateReport(Long reportId, String escalatedTo);

    /**
     * Gets reports requiring follow-up.
     * @return List of safety report DTOs requiring follow-up
     */
    List<SafetyReportDTO> getReportsRequiringFollowUp();

    /**
     * Gets critical unresolved reports.
     * @return List of critical safety report DTOs
     */
    List<SafetyReportDTO> getCriticalUnresolvedReports();

    /**
     * Gets safety statistics.
     * @return Map of safety statistics
     */
    java.util.Map<String, Object> getSafetyStatistics();

    /**
     * Processes follow-up requirements for reports.
     * @return Number of follow-ups processed
     */
    int processFollowUpRequirements();

    /**
     * Gets reports resolved by a moderator.
     * @param moderator The moderator
     * @param limit Maximum number of reports to return
     * @return List of resolved safety report DTOs
     */
    List<SafetyReportDTO> getModeratorResolvedReports(User moderator, int limit);

    /**
     * Gets anonymous safety reports.
     * @param limit Maximum number of reports to return
     * @return List of anonymous safety report DTOs
     */
    List<SafetyReportDTO> getAnonymousReports(int limit);

    /**
     * Gets recent safety reports.
     * @param since The start date
     * @param limit Maximum number of reports to return
     * @return List of recent safety report DTOs
     */
    List<SafetyReportDTO> getRecentReports(java.time.LocalDateTime since, int limit);

    /**
     * Checks if a user has recent safety violations.
     * @param user The user to check
     * @return true if user has recent violations
     */
    boolean hasRecentViolations(User user);

    /**
     * Gets the safety score for a user (lower is riskier).
     * @param user The user
     * @return Safety score (0-100, 100 is safest)
     */
    int getUserSafetyScore(User user);

    /**
     * Updates a user's safety score based on reports.
     * @param user The user
     * @param report The related safety report
     */
    void updateUserSafetyScore(User user, SafetyReport report);

    /**
     * Gets escalated safety reports.
     * @return List of escalated safety report DTOs
     */
    List<SafetyReportDTO> getEscalatedReports();
}
