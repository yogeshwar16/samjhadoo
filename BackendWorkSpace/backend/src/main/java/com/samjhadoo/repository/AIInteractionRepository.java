package com.samjhadoo.repository.ai;

import com.samjhadoo.model.ai.AIInteraction;
import com.samjhadoo.model.enums.AITier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, Long> {
    
    Page<AIInteraction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<AIInteraction> findByTierOrderByCreatedAtDesc(AITier tier, Pageable pageable);
    
    List<AIInteraction> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(i) FROM AIInteraction i WHERE i.user.id = :userId " +
           "AND i.tier = :tier AND i.createdAt >= :since")
    long countByUserAndTierSince(@Param("userId") Long userId, 
                                  @Param("tier") AITier tier, 
                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT COALESCE(SUM(i.cost), 0) FROM AIInteraction i WHERE i.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalCostBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(i.totalTokens), 0) FROM AIInteraction i WHERE i.createdAt BETWEEN :start AND :end")
    long getTotalTokensBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    long countBySuccessfulFalseAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
