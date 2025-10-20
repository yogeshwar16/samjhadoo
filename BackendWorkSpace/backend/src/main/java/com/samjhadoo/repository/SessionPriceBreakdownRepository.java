package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.SessionPriceBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionPriceBreakdownRepository extends JpaRepository<SessionPriceBreakdown, Long> {
    
    Optional<SessionPriceBreakdown> findByBreakdownToken(String breakdownToken);
    
    Optional<SessionPriceBreakdown> findBySessionId(String sessionId);
}
