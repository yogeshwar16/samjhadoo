package com.samjhadoo.repository.privacy;

import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.AccountDeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequest, Long> {
    
    Optional<AccountDeletionRequest> findByRequestId(String requestId);
    
    Optional<AccountDeletionRequest> findByUserAndStatusIn(User user, List<AccountDeletionRequest.DeletionStatus> statuses);
    
    List<AccountDeletionRequest> findByUserOrderByRequestedAtDesc(User user);
    
    @Query("SELECT r FROM AccountDeletionRequest r WHERE r.status = 'SCHEDULED' AND r.scheduledFor <= :now")
    List<AccountDeletionRequest> findScheduledForDeletion(@Param("now") Instant now);
    
    @Query("SELECT r FROM AccountDeletionRequest r WHERE r.status = 'PENDING' AND r.verified = false ORDER BY r.requestedAt ASC")
    List<AccountDeletionRequest> findUnverifiedRequests();
    
    boolean existsByUserAndStatusIn(User user, List<AccountDeletionRequest.DeletionStatus> statuses);
}
