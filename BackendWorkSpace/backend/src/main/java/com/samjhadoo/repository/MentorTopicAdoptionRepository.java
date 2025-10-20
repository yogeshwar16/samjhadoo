package com.samjhadoo.repository.topic;

import com.samjhadoo.model.topic.MentorTopicAdoption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorTopicAdoptionRepository extends JpaRepository<MentorTopicAdoption, Long> {
    
    Optional<MentorTopicAdoption> findByMentorIdAndTopicId(Long mentorId, Long topicId);
    
    List<MentorTopicAdoption> findByMentorIdAndActiveTrue(Long mentorId);
    
    List<MentorTopicAdoption> findByTopicIdAndActiveTrue(Long topicId);
    
    @Query("SELECT COUNT(m) FROM MentorTopicAdoption m WHERE m.topic.id = :topicId AND m.active = true")
    long countActiveMentorsByTopicId(@Param("topicId") Long topicId);
    
    boolean existsByMentorIdAndTopicIdAndActiveTrue(Long mentorId, Long topicId);
}
