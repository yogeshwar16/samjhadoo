package com.samjhadoo.service.topic;

import com.samjhadoo.dto.topic.*;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.TopicCategory;
import com.samjhadoo.model.enums.TopicStatus;
import com.samjhadoo.model.topic.*;
import com.samjhadoo.repository.topic.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final MentorTopicAdoptionRepository adoptionRepository;
    private final TopicEngagementRepository engagementRepository;

    // ============= Topic Management =============

    @Override
    @Transactional
    public TopicDTO createTopic(CreateTopicRequest request, boolean aiGenerated) {
        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setCategory(request.getCategory());
        topic.setTags(request.getTags());
        topic.setDifficulty(request.getDifficulty());
        topic.setTargetCommunities(request.getTargetCommunities());
        topic.setSupportedLanguages(request.getSupportedLanguages());
        topic.setSeasonal(request.isSeasonal());
        topic.setCampaignStartDate(request.getCampaignStartDate());
        topic.setCampaignEndDate(request.getCampaignEndDate());
        topic.setAiGenerated(aiGenerated);
        topic.setStatus(aiGenerated ? TopicStatus.PENDING : TopicStatus.APPROVED);

        topic = topicRepository.save(topic);
        log.info("Created topic: {} (AI: {})", topic.getId(), aiGenerated);

        return toDTO(topic);
    }

    @Override
    @Transactional
    public TopicDTO updateTopic(Long id, CreateTopicRequest request) {
        Topic topic = findTopicById(id);

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setCategory(request.getCategory());
        topic.setTags(request.getTags());
        topic.setDifficulty(request.getDifficulty());
        topic.setTargetCommunities(request.getTargetCommunities());
        topic.setSupportedLanguages(request.getSupportedLanguages());
        topic.setSeasonal(request.isSeasonal());
        topic.setCampaignStartDate(request.getCampaignStartDate());
        topic.setCampaignEndDate(request.getCampaignEndDate());

        topic = topicRepository.save(topic);
        return toDTO(topic);
    }

    @Override
    @Transactional
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDTO getTopicById(Long id) {
        return toDTO(findTopicById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TopicDTO> getAllTopics(Pageable pageable) {
        return topicRepository.findAll(pageable).map(this::toDTO);
    }

    // ============= Admin Approval Workflow =============

    @Override
    @Transactional(readOnly = true)
    public Page<TopicDTO> getPendingTopics(Pageable pageable) {
        return topicRepository.findByStatus(TopicStatus.PENDING, pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public void approveTopic(Long id, String adminUsername) {
        Topic topic = findTopicById(id);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setApprovedAt(LocalDateTime.now());
        topic.setApprovedBy(adminUsername);

        topicRepository.save(topic);
        log.info("Topic {} approved by {}", id, adminUsername);
    }

    @Override
    @Transactional
    public void rejectTopic(Long id, String adminUsername, String reason) {
        Topic topic = findTopicById(id);
        topic.setStatus(TopicStatus.REJECTED);
        topic.setRejectionReason(reason);
        topic.setApprovedBy(adminUsername);

        topicRepository.save(topic);
        log.info("Topic {} rejected by {}: {}", id, adminUsername, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingTopicsCount() {
        return topicRepository.countByStatus(TopicStatus.PENDING);
    }

    // ============= Mentee Discovery =============

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getTrendingTopics(int limit) {
        return topicRepository.findTrendingTopics(PageRequest.of(0, limit))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getTopicsByCategory(TopicCategory category, int limit) {
        return topicRepository.findPopularTopicsByCategory(category, PageRequest.of(0, limit))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getActiveCampaigns() {
        return topicRepository.findActiveCampaigns(TopicStatus.APPROVED, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getRecommendedTopics(User user, int limit) {
        // TODO: Implement AI-based recommendation based on user's community tag, interests, past queries
        // For now, return trending topics
        return getTrendingTopics(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getNewTopicsForUser(User user, int limit) {
        // Get topics user hasn't engaged with yet
        List<Long> engagedTopicIds = engagementRepository.findByUserId(user.getId())
                .stream()
                .map(e -> e.getTopic().getId())
                .collect(Collectors.toList());

        return topicRepository.findByStatusOrderByCreatedAtDesc(TopicStatus.APPROVED)
                .stream()
                .filter(t -> !engagedTopicIds.contains(t.getId()))
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============= Mentor Adoption =============

    @Override
    @Transactional
    public void adoptTopic(User mentor, Long topicId) {
        Topic topic = findTopicById(topicId);

        // Check if already adopted
        if (adoptionRepository.existsByMentorIdAndTopicIdAndActiveTrue(mentor.getId(), topicId)) {
            log.warn("Mentor {} already adopted topic {}", mentor.getId(), topicId);
            return;
        }

        MentorTopicAdoption adoption = new MentorTopicAdoption();
        adoption.setMentor(mentor);
        adoption.setTopic(topic);
        adoption.setActive(true);

        adoptionRepository.save(adoption);

        // Update topic mentor count
        topic.setMentorCount(topic.getMentorCount() + 1);
        topicRepository.save(topic);

        log.info("Mentor {} adopted topic {}", mentor.getId(), topicId);
    }

    @Override
    @Transactional
    public void unadoptTopic(User mentor, Long topicId) {
        MentorTopicAdoption adoption = adoptionRepository.findByMentorIdAndTopicId(mentor.getId(), topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Adoption not found"));

        adoption.setActive(false);
        adoptionRepository.save(adoption);

        // Update topic mentor count
        Topic topic = findTopicById(topicId);
        topic.setMentorCount(Math.max(0, topic.getMentorCount() - 1));
        topicRepository.save(topic);

        log.info("Mentor {} unadopted topic {}", mentor.getId(), topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getMentorAdoptedTopics(Long mentorId) {
        return adoptionRepository.findByMentorIdAndActiveTrue(mentorId)
                .stream()
                .map(adoption -> toDTO(adoption.getTopic()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getTopicsForMentorAdoption(User mentor, int limit) {
        // Get trending topics that mentor hasn't adopted yet
        List<Long> adoptedTopicIds = adoptionRepository.findByMentorIdAndActiveTrue(mentor.getId())
                .stream()
                .map(a -> a.getTopic().getId())
                .collect(Collectors.toList());

        return topicRepository.findTrendingTopics(PageRequest.of(0, limit * 2)) // Get more to filter
                .stream()
                .filter(t -> !adoptedTopicIds.contains(t.getId()))
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMentorAdoptedTopic(Long mentorId, Long topicId) {
        return adoptionRepository.existsByMentorIdAndTopicIdAndActiveTrue(mentorId, topicId);
    }

    // ============= Engagement Tracking =============

    @Override
    @Transactional
    public void trackView(User user, Long topicId) {
        TopicEngagement engagement = getOrCreateEngagement(user, topicId);
        if (!engagement.isViewed()) {
            engagement.setViewed(true);
            engagementRepository.save(engagement);

            // Update topic metrics
            Topic topic = findTopicById(topicId);
            topic.setViewCount(topic.getViewCount() + 1);
            topicRepository.save(topic);
        }
    }

    @Override
    @Transactional
    public void trackClick(User user, Long topicId) {
        TopicEngagement engagement = getOrCreateEngagement(user, topicId);
        if (!engagement.isClicked()) {
            engagement.setClicked(true);
            engagementRepository.save(engagement);

            // Update topic metrics
            Topic topic = findTopicById(topicId);
            topic.setClickCount(topic.getClickCount() + 1);
            topicRepository.save(topic);
        }
    }

    @Override
    @Transactional
    public void trackSessionBooked(User user, Long topicId) {
        TopicEngagement engagement = getOrCreateEngagement(user, topicId);
        if (!engagement.isSessionBooked()) {
            engagement.setSessionBooked(true);
            engagementRepository.save(engagement);

            // Update topic metrics
            Topic topic = findTopicById(topicId);
            topic.setSessionCount(topic.getSessionCount() + 1);
            topicRepository.save(topic);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicEngagementDTO> getUserEngagements(Long userId) {
        return engagementRepository.findByUserId(userId)
                .stream()
                .map(this::toEngagementDTO)
                .collect(Collectors.toList());
    }

    // ============= Analytics =============

    @Override
    @Transactional
    public void updateTopicMetrics(Long topicId) {
        Topic topic = findTopicById(topicId);

        long views = engagementRepository.countViewsByTopicId(topicId);
        long clicks = engagementRepository.countClicksByTopicId(topicId);
        long sessions = engagementRepository.countSessionsByTopicId(topicId);
        long mentors = adoptionRepository.countActiveMentorsByTopicId(topicId);

        topic.setViewCount(views);
        topic.setClickCount(clicks);
        topic.setSessionCount(sessions);
        topic.setMentorCount(mentors);

        topicRepository.save(topic);
        log.info("Updated metrics for topic {}: V={}, C={}, S={}, M={}", topicId, views, clicks, sessions, mentors);
    }

    // ============= Helper Methods =============

    private Topic findTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id));
    }

    private TopicEngagement getOrCreateEngagement(User user, Long topicId) {
        return engagementRepository.findByUserIdAndTopicId(user.getId(), topicId)
                .orElseGet(() -> {
                    TopicEngagement engagement = new TopicEngagement();
                    engagement.setUser(user);
                    engagement.setTopic(findTopicById(topicId));
                    return engagement;
                });
    }

    private TopicDTO toDTO(Topic topic) {
        return TopicDTO.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .category(topic.getCategory())
                .tags(topic.getTags())
                .difficulty(topic.getDifficulty())
                .targetCommunities(topic.getTargetCommunities())
                .supportedLanguages(topic.getSupportedLanguages())
                .status(topic.getStatus())
                .aiGenerated(topic.isAiGenerated())
                .seasonal(topic.isSeasonal())
                .campaignStartDate(topic.getCampaignStartDate())
                .campaignEndDate(topic.getCampaignEndDate())
                .viewCount(topic.getViewCount())
                .clickCount(topic.getClickCount())
                .sessionCount(topic.getSessionCount())
                .mentorCount(topic.getMentorCount())
                .createdAt(topic.getCreatedAt())
                .approvedAt(topic.getApprovedAt())
                .approvedBy(topic.getApprovedBy())
                .build();
    }

    private TopicEngagementDTO toEngagementDTO(TopicEngagement engagement) {
        return TopicEngagementDTO.builder()
                .topicId(engagement.getTopic().getId())
                .topicTitle(engagement.getTopic().getTitle())
                .viewed(engagement.isViewed())
                .clicked(engagement.isClicked())
                .sessionBooked(engagement.isSessionBooked())
                .engagedAt(engagement.getEngagedAt())
                .build();
    }
}
