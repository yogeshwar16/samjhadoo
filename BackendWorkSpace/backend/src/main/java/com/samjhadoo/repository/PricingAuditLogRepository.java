package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.PricingAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingAuditLogRepository extends JpaRepository<PricingAuditLog, Long> {
    
    Page<PricingAuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);
    
    Page<PricingAuditLog> findByActorOrderByTimestampDesc(String actor, Pageable pageable);
}
