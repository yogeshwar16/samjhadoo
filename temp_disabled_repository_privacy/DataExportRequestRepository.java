package com.samjhadoo.repository.privacy;

import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.DataExportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataExportRequestRepository extends JpaRepository<DataExportRequest, Long> {
    
    Optional<DataExportRequest> findByRequestId(String requestId);
    
    List<DataExportRequest> findByUserOrderByRequestedAtDesc(User user);
    
    List<DataExportRequest> findByUserAndStatus(User user, DataExportRequest.ExportStatus status);
    
    @Query("SELECT r FROM DataExportRequest r WHERE r.status = 'PENDING' ORDER BY r.requestedAt ASC")
    List<DataExportRequest> findPendingRequests();
    
    @Query("SELECT r FROM DataExportRequest r WHERE r.status = 'COMPLETED' AND r.expiresAt < :now")
    List<DataExportRequest> findExpiredExports(@Param("now") Instant now);
    
    boolean existsByUserAndStatusIn(User user, List<DataExportRequest.ExportStatus> statuses);
}
