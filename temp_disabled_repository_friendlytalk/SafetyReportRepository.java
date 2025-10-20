package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.SafetyReport;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.SafetyReport.ReportStatus;
import com.samjhadoo.model.friendlytalk.SafetyReport.ReportType;
import com.samjhadoo.model.friendlytalk.SafetyReport.ReportSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {

    List<SafetyReport> findByReporter(User reporter);

    List<SafetyReport> findByReportedUser(User reportedUser);

    List<SafetyReport> findByStatus(ReportStatus status);

    List<SafetyReport> findByReportType(ReportType reportType);

    List<SafetyReport> findBySeverity(ReportSeverity severity);

    @Query("SELECT r FROM SafetyReport r WHERE r.status = 'PENDING' ORDER BY r.reportedAt ASC")
    List<SafetyReport> findPendingReports();

    @Query("SELECT r FROM SafetyReport r WHERE r.status = 'UNDER_REVIEW' ORDER BY r.reportedAt ASC")
    List<SafetyReport> findUnderReviewReports();

    @Query("SELECT r FROM SafetyReport r WHERE r.status = 'REPORTED' ORDER BY r.reportedAt ASC")
    List<SafetyReport> findReportedReports();

    @Query("SELECT r FROM SafetyReport r WHERE r.reportedAt >= :since ORDER BY r.reportedAt DESC")
    List<SafetyReport> findRecentReports(@Param("since") LocalDateTime since);

    @Query("SELECT r FROM SafetyReport r WHERE r.followUpRequired = true AND r.followUpDate <= :now ORDER BY r.followUpDate ASC")
    List<SafetyReport> findReportsRequiringFollowUp(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM SafetyReport r WHERE r.anonymous = true ORDER BY r.reportedAt DESC")
    List<SafetyReport> findAnonymousReports();

    @Query("SELECT COUNT(r) FROM SafetyReport r WHERE r.status = 'PENDING'")
    long countPendingReports();

    @Query("SELECT COUNT(r) FROM SafetyReport r WHERE r.status = 'RESOLVED' AND r.reportedAt >= :since")
    long countResolvedReportsSince(@Param("since") LocalDateTime since);

    @Query("SELECT r FROM SafetyReport r WHERE r.resolver = :resolver ORDER BY r.resolvedAt DESC")
    List<SafetyReport> findReportsResolvedBy(@Param("resolver") User resolver);

    @Query("SELECT r FROM SafetyReport r WHERE r.severity = 'CRITICAL' AND r.status != 'RESOLVED' ORDER BY r.reportedAt ASC")
    List<SafetyReport> findCriticalUnresolvedReports();

    @Query("SELECT AVG(CAST(r.severity AS int)) FROM SafetyReport r WHERE r.status = 'RESOLVED'")
    Double getAverageResolutionTime();

    @Query("SELECT r FROM SafetyReport r WHERE r.escalatedTo IS NOT NULL ORDER BY r.reportedAt DESC")
    List<SafetyReport> findEscalatedReports();
}
