package com.samjhadoo.controller.api;

import com.samjhadoo.dto.topic.CreateTopicRequest;
import com.samjhadoo.dto.topic.TopicApprovalRequest;
import com.samjhadoo.dto.topic.TopicDTO;
import com.samjhadoo.service.topic.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API for topic management
 */
@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTopicController {

    private final TopicService topicService;

    /**
     * Create a new topic (Admin)
     */
    @PostMapping
    public ResponseEntity<TopicDTO> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        TopicDTO topic = topicService.createTopic(request, false); // Not AI-generated
        return ResponseEntity.ok(topic);
    }

    /**
     * Update a topic
     */
    @PutMapping("/{id}")
    public ResponseEntity<TopicDTO> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.ok(topicService.updateTopic(id, request));
    }

    /**
     * Delete a topic
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all topics with pagination
     */
    @GetMapping
    public ResponseEntity<Page<TopicDTO>> getAllTopics(Pageable pageable) {
        return ResponseEntity.ok(topicService.getAllTopics(pageable));
    }

    /**
     * Get pending topics (AI-suggested, awaiting approval)
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<TopicDTO>> getPendingTopics(Pageable pageable) {
        return ResponseEntity.ok(topicService.getPendingTopics(pageable));
    }

    /**
     * Get pending topics count
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", topicService.getPendingTopicsCount()));
    }

    /**
     * Approve a topic
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicApprovalRequest request) {
        
        if (!"APPROVE".equalsIgnoreCase(request.getAction())) {
            return ResponseEntity.badRequest().build();
        }

        topicService.approveTopic(id, request.getAdminUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a topic
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicApprovalRequest request) {
        
        if (!"REJECT".equalsIgnoreCase(request.getAction())) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        topicService.rejectTopic(id, request.getAdminUsername(), request.getReason());
        return ResponseEntity.ok().build();
    }

    /**
     * Update topic metrics manually
     */
    @PostMapping("/{id}/update-metrics")
    public ResponseEntity<Void> updateTopicMetrics(@PathVariable Long id) {
        topicService.updateTopicMetrics(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Simulate AI-generated topic (for testing)
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<TopicDTO> generateAITopic(@Valid @RequestBody CreateTopicRequest request) {
        TopicDTO topic = topicService.createTopic(request, true); // Mark as AI-generated
        return ResponseEntity.ok(topic);
    }
}
