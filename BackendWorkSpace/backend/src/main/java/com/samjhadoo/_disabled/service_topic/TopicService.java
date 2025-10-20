package com.samjhadoo.service.topic;

import com.samjhadoo.dto.topic.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.TopicCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing topics
 */
public interface TopicService {

    // Topic Management (Admin + AI)
    TopicDTO createTopic(CreateTopicRequest request, boolean aiGenerated);
    TopicDTO updateTopic(Long id, CreateTopicRequest request);
    void deleteTopic(Long id);
    TopicDTO getTopicById(Long id);
    Page<TopicDTO> getAllTopics(Pageable pageable);
    
    // Admin Approval Workflow
    Page<TopicDTO> getPendingTopics(Pageable pageable);
    void approveTopic(Long id, String adminUsername);
    void rejectTopic(Long id, String adminUsername, String reason);
    long getPendingTopicsCount();
    
    // Mentee Discovery
    List<TopicDTO> getTrendingTopics(int limit);
    List<TopicDTO> getTopicsByCategory(TopicCategory category, int limit);
    List<TopicDTO> getActiveCampaigns();
    List<TopicDTO> getRecommendedTopics(User user, int limit);
    List<TopicDTO> getNewTopicsForUser(User user, int limit);
    
    // Mentor Adoption
    void adoptTopic(User mentor, Long topicId);
    void unadoptTopic(User mentor, Long topicId);
    List<TopicDTO> getMentorAdoptedTopics(Long mentorId);
    List<TopicDTO> getTopicsForMentorAdoption(User mentor, int limit);
    boolean hasMentorAdoptedTopic(Long mentorId, Long topicId);
    
    // Engagement Tracking
    void trackView(User user, Long topicId);
    void trackClick(User user, Long topicId);
    void trackSessionBooked(User user, Long topicId);
    List<TopicEngagementDTO> getUserEngagements(Long userId);
    
    // Analytics
    void updateTopicMetrics(Long topicId);
}
