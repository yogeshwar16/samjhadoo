package com.samjhadoo.repository.topic;

import com.samjhadoo.model.enums.TopicCategory;
import com.samjhadoo.model.enums.TopicStatus;
import com.samjhadoo.model.topic.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    Page<Topic> findByStatus(TopicStatus status, Pageable pageable);
    
    Page<Topic> findByStatusAndCategory(TopicStatus status, TopicCategory category, Pageable pageable);
    
    List<Topic> findByStatusOrderByCreatedAtDesc(TopicStatus status);
    
    @Query("SELECT t FROM Topic t WHERE t.status = :status " +
           "AND t.seasonal = true " +
           "AND t.campaignStartDate <= :now " +
           "AND t.campaignEndDate >= :now")
    List<Topic> findActiveCampaigns(@Param("status") TopicStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Topic t WHERE t.status = 'APPROVED' " +
           "ORDER BY t.clickCount DESC, t.viewCount DESC")
    List<Topic> findTrendingTopics(Pageable pageable);
    
    @Query("SELECT t FROM Topic t WHERE t.status = 'APPROVED' " +
           "AND t.category = :category " +
           "ORDER BY t.sessionCount DESC")
    List<Topic> findPopularTopicsByCategory(@Param("category") TopicCategory category, Pageable pageable);
    
    long countByStatus(TopicStatus status);
}
