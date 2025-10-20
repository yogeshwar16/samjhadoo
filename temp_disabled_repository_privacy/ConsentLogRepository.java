package com.samjhadoo.repository.privacy;

import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.ConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {
    
    List<ConsentLog> findByUserOrderByConsentTimestampDesc(User user);
    
    List<ConsentLog> findByUserAndConsentTypeOrderByConsentTimestampDesc(User user, ConsentLog.ConsentType consentType);
    
    @Query("SELECT c FROM ConsentLog c WHERE c.user = :user AND c.consentType = :type AND c.active = true ORDER BY c.consentTimestamp DESC")
    Optional<ConsentLog> findActiveConsentByUserAndType(@Param("user") User user, @Param("type") ConsentLog.ConsentType type);
    
    @Query("SELECT c FROM ConsentLog c WHERE c.user = :user AND c.active = true")
    List<ConsentLog> findActiveConsentsByUser(@Param("user") User user);
    
    boolean existsByUserAndConsentTypeAndActiveTrue(User user, ConsentLog.ConsentType consentType);
    
    @Query("SELECT c FROM ConsentLog c WHERE c.consentTimestamp < :before")
    List<ConsentLog> findOldConsents(@Param("before") Instant before);
    
    @Query("SELECT c.consentType, COUNT(c) FROM ConsentLog c WHERE c.granted = true AND c.active = true GROUP BY c.consentType")
    List<Object[]> countActiveConsentsByType();
}
