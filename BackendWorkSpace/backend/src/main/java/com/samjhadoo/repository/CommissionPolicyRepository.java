package com.samjhadoo.repository.pricing;

import com.samjhadoo.model.pricing.CommissionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionPolicyRepository extends JpaRepository<CommissionPolicy, Long> {
    
    @Query("SELECT cp FROM CommissionPolicy cp WHERE cp.startDate <= :now " +
           "AND (cp.endDate IS NULL OR cp.endDate >= :now) " +
           "AND cp.active = true " +
           "ORDER BY cp.createdAt DESC")
    Optional<CommissionPolicy> findCurrentPolicy(@Param("now") LocalDateTime now);
    
    List<CommissionPolicy> findByActiveTrue();
}
