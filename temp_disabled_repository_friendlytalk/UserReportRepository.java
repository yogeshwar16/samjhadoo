package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
    // Basic CRUD operations
    Page<UserReport> findByStatus(UserReport.ReportStatus status, Pageable pageable);
    
    Page<UserReport> findByReporter(User reporter, Pageable pageable);
    
    Page<UserReport> findByReportedUserId(String reportedUserId, Pageable pageable);
    
    Page<UserReport> findByRoomId(String roomId, Pageable pageable);
    
    // Status-based queries
    long countByStatus(UserReport.ReportStatus status);
    
    @Query("SELECT r FROM UserReport r WHERE r.status IN :statuses")
    Page<UserReport> findByStatusIn(@Param("statuses") List<UserReport.ReportStatus> statuses, Pageable pageable);
    
    // Complex queries
    @Query("SELECT r FROM UserReport r WHERE " +
           "(:reporterId IS NULL OR r.reporter.id = :reporterId) AND " +
           "(:reportedUserId IS NULL OR r.reportedUserId = :reportedUserId) AND " +
           "(:roomId IS NULL OR r.room.id = :roomId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(cast(:startDate AS timestamp) IS NULL OR r.createdAt >= :startDate) AND " +
           "(cast(:endDate AS timestamp) IS NULL OR r.createdAt <= :endDate)")
    Page<UserReport> searchReports(
            @Param("reporterId") Long reporterId,
            @Param("reportedUserId") String reportedUserId,
            @Param("roomId") String roomId,
            @Param("status") UserReport.ReportStatus status,
            @Param("type") UserReport.ReportType type,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
    
    // For dashboard statistics
    @Query("SELECT r.type, COUNT(r) FROM UserReport r GROUP BY r.type")
    List<Object[]> countReportsByType();
    
    @Query("SELECT r.status, COUNT(r) FROM UserReport r GROUP BY r.status")
    List<Object[]> countReportsByStatus();
    
    // For cleanup tasks
    @Modifying
    @Query("DELETE FROM UserReport r WHERE r.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    // For batch operations
    @Modifying
    @Query("UPDATE UserReport r SET r.status = :status, r.resolvedAt = :resolvedAt, r.resolvedBy = :resolvedBy WHERE r.id IN :ids")
    int updateStatusForReports(
            @Param("ids") List<Long> ids,
            @Param("status") UserReport.ReportStatus status,
            @Param("resolvedAt") Instant resolvedAt,
            @Param("resolvedBy") String resolvedBy
    );
    
    // For finding reports needing notification
    @Query("SELECT r FROM UserReport r WHERE " +
           "(r.isReporterNotified = false AND r.status IN ('RESOLVED', 'DISMISSED')) OR " +
           "(r.isReportedUserNotified = false AND r.status = 'RESOLVED')")
    List<UserReport> findReportsNeedingNotification();
}
