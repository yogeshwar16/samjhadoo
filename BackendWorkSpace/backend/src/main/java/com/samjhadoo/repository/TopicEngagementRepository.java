package com.samjhadoo.repository.topic;

import com.samjhadoo.model.topic.TopicEngagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicEngagementRepository extends JpaRepository<TopicEngagement, Long> {
    
    Optional<TopicEngagement> findByUserIdAndTopicId(Long userId, Long topicId);
    
    List<TopicEngagement> findByUserId(Long userId);
    
    @Query("SELECT COUNT(e) FROM TopicEngagement e WHERE e.topic.id = :topicId AND e.viewed = true")
    long countViewsByTopicId(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(e) FROM TopicEngagement e WHERE e.topic.id = :topicId AND e.clicked = true")
    long countClicksByTopicId(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(e) FROM TopicEngagement e WHERE e.topic.id = :topicId AND e.sessionBooked = true")
    long countSessionsByTopicId(@Param("topicId") Long topicId);
}
