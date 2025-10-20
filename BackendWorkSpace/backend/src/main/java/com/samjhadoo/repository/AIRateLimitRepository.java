package com.samjhadoo.repository.ai;

import com.samjhadoo.model.ai.AIRateLimit;
import com.samjhadoo.model.enums.AITier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIRateLimitRepository extends JpaRepository<AIRateLimit, Long> {
    
    Optional<AIRateLimit> findByUserIdAndTier(Long userId, AITier tier);
}
