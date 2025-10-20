package com.samjhadoo.repository.mongo;

import com.samjhadoo.model.pricing.SurgeRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurgeRuleRepository extends MongoRepository<SurgeRule, String> {
    
    Optional<SurgeRule> findBySkillIdAndRegionCodeAndActiveTrue(Long skillId, String regionCode);
    
    Optional<SurgeRule> findBySkillIdAndRegionCodeIsNullAndActiveTrue(Long skillId);
    
    List<SurgeRule> findByActiveTrue();
}
