package com.samjhadoo.controller.api;

import com.samjhadoo.dto.topic.TopicDTO;
import com.samjhadoo.dto.topic.TopicEngagementDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.TopicCategory;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.service.topic.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API for topics - Mentee/Mentor access
 */
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TopicController {

    private final TopicService topicService;

    /**
     * Get trending topics
     */
    @GetMapping("/trending")
    public ResponseEntity<List<TopicDTO>> getTrendingTopics(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(topicService.getTrendingTopics(limit));
    }

    /**
     * Get topics by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TopicDTO>> getTopicsByCategory(
            @PathVariable TopicCategory category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(topicService.getTopicsByCategory(category, limit));
    }

    /**
     * Get active campaigns (seasonal topics)
     */
    @GetMapping("/campaigns")
    public ResponseEntity<List<TopicDTO>> getActiveCampaigns() {
        return ResponseEntity.ok(topicService.getActiveCampaigns());
    }

    /**
     * Get recommended topics for current user
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<TopicDTO>> getRecommendedTopics(
            @CurrentUser User user,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(topicService.getRecommendedTopics(user, limit));
    }

    /**
     * Get new topics not yet explored by user
     */
    @GetMapping("/new")
    public ResponseEntity<List<TopicDTO>> getNewTopics(
            @CurrentUser User user,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(topicService.getNewTopicsForUser(user, limit));
    }

    /**
     * Get topic details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TopicDTO> getTopicById(@PathVariable Long id) {
        return ResponseEntity.ok(topicService.getTopicById(id));
    }

    /**
     * Track topic view
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> trackView(@CurrentUser User user, @PathVariable Long id) {
        topicService.trackView(user, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Track topic click
     */
    @PostMapping("/{id}/click")
    public ResponseEntity<Void> trackClick(@CurrentUser User user, @PathVariable Long id) {
        topicService.trackClick(user, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Track session booked for topic
     */
    @PostMapping("/{id}/session-booked")
    public ResponseEntity<Void> trackSessionBooked(@CurrentUser User user, @PathVariable Long id) {
        topicService.trackSessionBooked(user, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get user's topic engagement history
     */
    @GetMapping("/engagements")
    public ResponseEntity<List<TopicEngagementDTO>> getMyEngagements(@CurrentUser User user) {
        return ResponseEntity.ok(topicService.getUserEngagements(user.getId()));
    }

    // ============= Mentor-Specific Endpoints =============

    /**
     * Adopt a topic (Mentor only)
     */
    @PostMapping("/{id}/adopt")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Void> adoptTopic(@CurrentUser User mentor, @PathVariable Long id) {
        topicService.adoptTopic(mentor, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Unadopt a topic (Mentor only)
     */
    @PostMapping("/{id}/unadopt")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Void> unadoptTopic(@CurrentUser User mentor, @PathVariable Long id) {
        topicService.unadoptTopic(mentor, id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get my adopted topics (Mentor only)
     */
    @GetMapping("/my-topics")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<TopicDTO>> getMyAdoptedTopics(@CurrentUser User mentor) {
        return ResponseEntity.ok(topicService.getMentorAdoptedTopics(mentor.getId()));
    }

    /**
     * Get topics available for adoption (Mentor only)
     */
    @GetMapping("/available-for-adoption")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<TopicDTO>> getTopicsForAdoption(
            @CurrentUser User mentor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(topicService.getTopicsForMentorAdoption(mentor, limit));
    }

    /**
     * Check if mentor has adopted a topic
     */
    @GetMapping("/{id}/adopted")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Boolean> hasAdoptedTopic(@CurrentUser User mentor, @PathVariable Long id) {
        return ResponseEntity.ok(topicService.hasMentorAdoptedTopic(mentor.getId(), id));
    }
}
