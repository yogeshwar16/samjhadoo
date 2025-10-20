package com.samjhadoo.repository.ai;

import com.samjhadoo.model.ai.AIConfig;
import com.samjhadoo.model.enums.AITier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIConfigRepository extends JpaRepository<AIConfig, Long> {
    
    Optional<AIConfig> findByTier(AITier tier);
    
    Optional<AIConfig> findByTierAndEnabledTrue(AITier tier);
}
