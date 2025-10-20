package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.visualquery.VisualQueryDTO;
import com.samjhadoo.dto.visualquery.VisualQueryResponseDTO;
import com.samjhadoo.dto.visualquery.VisualQueryMediaDTO;
import com.samjhadoo.dto.visualquery.VisualQueryAnnotationDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.service.visualquery.VisualQueryService;
import com.samjhadoo.service.visualquery.MediaService;
import com.samjhadoo.service.visualquery.AnnotationService;
import com.samjhadoo.service.visualquery.ResponseService;
import com.samjhadoo.service.visualquery.CategorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API controller for Visual Query moderation and management.
 * Provides endpoints for administrators to manage queries, responses, and system health.
 */
@RestController
@RequestMapping("/api/admin/visual-queries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Visual Queries", description = "Admin visual query management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVisualQueryController {

    private final VisualQueryService visualQueryService;
    private final MediaService mediaService;
    private final AnnotationService annotationService;
    private final ResponseService responseService;
    private final CategorizationService categorizationService;

    // Query Management
    @PostMapping("/{queryId}/assign-mentor")
    @Operation(summary = "Assign mentor to query",
               description = "Manually assigns a mentor to a visual query")
    public ResponseEntity<Void> assignMentor(
            @PathVariable String queryId,
            @RequestParam Long mentorId) {
        try {
            // In a real implementation, you'd fetch the mentor user by ID
            // For now, we'll assume the mentor exists and proceed
            boolean assigned = visualQueryService.assignMentor(queryId, null);
            if (!assigned) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error assigning mentor to query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get mentor's queries",
               description = "Retrieves all queries assigned to a specific mentor")
    public ResponseEntity<List<VisualQueryDTO>> getMentorQueries(
            @PathVariable Long mentorId,
            @RequestParam(required = false) com.samjhadoo.model.enums.visualquery.QueryStatus status) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getMentorQueries(null, status);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting queries for mentor {}: {}", mentorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue queries",
               description = "Retrieves queries that are overdue for response")
    public ResponseEntity<List<VisualQueryDTO>> getOverdueQueries() {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getOverdueQueries();
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting overdue queries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/escalate")
    @Operation(summary = "Escalate query",
               description = "Escalates a query to higher-level support")
    public ResponseEntity<Void> escalateQuery(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin) {
        try {
            // In a real implementation, this would escalate the query
            // For now, we'll return success
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error escalating query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Media Management
    @PostMapping("/media/{mediaId}/process")
    @Operation(summary = "Process media",
               description = "Processes uploaded media (compression, AI analysis)")
    public ResponseEntity<VisualQueryMediaDTO> processMedia(@PathVariable Long mediaId) {
        try {
            VisualQueryMediaDTO media = mediaService.processMedia(mediaId);
            return ResponseEntity.ok(media);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot process media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/reprocess-failed")
    @Operation(summary = "Reprocess failed media",
               description = "Reprocesses media uploads that failed")
    public ResponseEntity<Integer> reprocessFailedMedia() {
        try {
            int reprocessed = mediaService.reprocessFailedMedia();
            return ResponseEntity.ok(reprocessed);
        } catch (Exception e) {
            log.error("Error reprocessing failed media: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/cleanup-orphaned")
    @Operation(summary = "Cleanup orphaned media",
               description = "Removes media files not associated with any query")
    public ResponseEntity<Integer> cleanupOrphanedMedia() {
        try {
            int cleaned = mediaService.cleanupOrphanedMedia();
            return ResponseEntity.ok(cleaned);
        } catch (Exception e) {
            log.error("Error cleaning up orphaned media: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/processing-status/{mediaId}")
    @Operation(summary = "Get media processing status",
               description = "Retrieves the processing status of media")
    public ResponseEntity<Map<String, Object>> getMediaProcessingStatus(@PathVariable Long mediaId) {
        try {
            Map<String, Object> status = mediaService.getMediaProcessingStatus(mediaId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting processing status for media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/statistics")
    @Operation(summary = "Get media statistics",
               description = "Retrieves statistics about media usage and processing")
    public ResponseEntity<Map<String, Object>> getMediaStatistics() {
        try {
            Map<String, Object> statistics = mediaService.getMediaStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting media statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/storage")
    @Operation(summary = "Get storage statistics",
               description = "Retrieves storage usage statistics for media")
    public ResponseEntity<Map<String, Object>> getStorageStatistics() {
        try {
            Map<String, Object> statistics = mediaService.getStorageStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting storage statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Response Moderation
    @GetMapping("/responses/requiring-moderation")
    @Operation(summary = "Get responses requiring moderation",
               description = "Retrieves responses that need administrative review")
    public ResponseEntity<List<VisualQueryResponseDTO>> getResponsesRequiringModeration(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<VisualQueryResponseDTO> responses = responseService.getResponsesRequiringModeration(limit);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting responses requiring moderation: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/hide")
    @Operation(summary = "Hide response",
               description = "Hides an inappropriate response")
    public ResponseEntity<Void> hideResponse(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User moderator,
            @RequestParam String reason) {
        try {
            boolean hidden = responseService.hideResponse(responseId, moderator, reason);
            if (!hidden) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error hiding response {}: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/responses/statistics")
    @Operation(summary = "Get response statistics",
               description = "Retrieves statistics about mentor responses")
    public ResponseEntity<Map<String, Object>> getResponseStatistics() {
        try {
            Map<String, Object> statistics = responseService.getResponseStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting response statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mentor/{mentorId}/performance")
    @Operation(summary = "Get mentor performance",
               description = "Retrieves performance metrics for a specific mentor")
    public ResponseEntity<Map<String, Object>> getMentorPerformance(@PathVariable Long mentorId) {
        try {
            // In a real implementation, you'd fetch the mentor user by ID
            // For now, we'll return mock data
            return ResponseEntity.ok(Map.of("totalResponses", 25, "averageRating", 4.2));
        } catch (Exception e) {
            log.error("Error getting performance for mentor {}: {}", mentorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Annotation Moderation
    @GetMapping("/annotations/requiring-moderation")
    @Operation(summary = "Get annotations requiring moderation",
               description = "Retrieves annotations that need administrative review")
    public ResponseEntity<List<VisualQueryAnnotationDTO>> getAnnotationsRequiringModeration(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<VisualQueryAnnotationDTO> annotations = annotationService.getAnnotationsRequiringModeration(limit);
            return ResponseEntity.ok(annotations);
        } catch (Exception e) {
            log.error("Error getting annotations requiring moderation: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/annotations/{annotationId}/hide")
    @Operation(summary = "Hide annotation",
               description = "Hides an inappropriate annotation")
    public ResponseEntity<Void> hideAnnotation(
            @PathVariable Long annotationId,
            @Parameter(hidden = true) @AuthenticationPrincipal User moderator,
            @RequestParam String reason) {
        try {
            boolean hidden = annotationService.hideAnnotation(annotationId, moderator, reason);
            if (!hidden) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error hiding annotation {}: {}", annotationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/annotations/{annotationId}/report")
    @Operation(summary = "Report annotation",
               description = "Reports an inappropriate annotation")
    public ResponseEntity<Void> reportAnnotation(
            @PathVariable Long annotationId,
            @Parameter(hidden = true) @AuthenticationPrincipal User reporter,
            @RequestParam String reason) {
        try {
            boolean reported = annotationService.reportAnnotation(annotationId, reporter, reason);
            if (!reported) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error reporting annotation {}: {}", annotationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/annotations/statistics")
    @Operation(summary = "Get annotation statistics",
               description = "Retrieves statistics about annotations")
    public ResponseEntity<Map<String, Object>> getAnnotationStatistics() {
        try {
            Map<String, Object> statistics = annotationService.getAnnotationStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting annotation statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/annotations/usage")
    @Operation(summary = "Get annotation usage analytics",
               description = "Retrieves usage analytics for annotations")
    public ResponseEntity<Map<String, Object>> getAnnotationUsageAnalytics() {
        try {
            Map<String, Object> analytics = annotationService.getAnnotationUsageAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting annotation usage analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // AI and Categorization Management
    @GetMapping("/categorization/statistics")
    @Operation(summary = "Get categorization statistics",
               description = "Retrieves AI categorization accuracy and performance metrics")
    public ResponseEntity<Map<String, Object>> getCategorizationStatistics() {
        try {
            Map<String, Object> statistics = categorizationService.getCategoryAccuracyStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting categorization statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/categorization/threshold")
    @Operation(summary = "Update categorization threshold",
               description = "Updates the confidence threshold for automatic categorization")
    public ResponseEntity<Void> updateCategorizationThreshold(@RequestParam double threshold) {
        try {
            categorizationService.updateCategorizationConfidenceThreshold(threshold);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid categorization threshold {}: {}", threshold, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating categorization threshold: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categorization/trending")
    @Operation(summary = "Get trending categories",
               description = "Retrieves trending query categories based on recent activity")
    public ResponseEntity<List<Map<String, Object>>> getTrendingCategories(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> trending = categorizationService.getTrendingCategories(days, limit);
            return ResponseEntity.ok(trending);
        } catch (Exception e) {
            log.error("Error getting trending categories: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/categorization/correct")
    @Operation(summary = "Correct AI categorization",
               description = "Manually corrects AI categorization for training improvement")
    public ResponseEntity<Void> correctCategorization(
            @RequestParam Long mediaId,
            @RequestParam QueryCategory correctCategory,
            @RequestParam(required = false) String feedback) {
        try {
            categorizationService.correctCategorization(mediaId, correctCategory, feedback);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error correcting categorization for media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ai/models")
    @Operation(summary = "Get AI model metrics",
               description = "Retrieves performance metrics for AI models")
    public ResponseEntity<Map<String, Object>> getAIModelMetrics() {
        try {
            Map<String, Object> metrics = categorizationService.getAIModelPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting AI model metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ai/models/update")
    @Operation(summary = "Update AI model",
               description = "Updates AI models for better accuracy")
    public ResponseEntity<Void> updateAIModel(
            @RequestParam String modelType,
            @RequestParam String modelVersion) {
        try {
            categorizationService.updateAIModel(modelType, modelVersion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating AI model {} to {}: {}", modelType, modelVersion, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ai/costs")
    @Operation(summary = "Get AI processing costs",
               description = "Retrieves cost and usage statistics for AI processing")
    public ResponseEntity<Map<String, Object>> getAIProcessingCosts() {
        try {
            Map<String, Object> costs = categorizationService.getAIProcessingCosts();
            return ResponseEntity.ok(costs);
        } catch (Exception e) {
            log.error("Error getting AI processing costs: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // System Management
    @GetMapping("/statistics")
    @Operation(summary = "Get comprehensive statistics",
               description = "Retrieves comprehensive statistics for the visual query system")
    public ResponseEntity<Map<String, Object>> getComprehensiveStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            statistics.put("queries", visualQueryService.getQueryStatistics());
            statistics.put("media", mediaService.getMediaStatistics());
            statistics.put("responses", responseService.getResponseStatistics());
            statistics.put("annotations", annotationService.getAnnotationStatistics());
            statistics.put("categorization", categorizationService.getCategoryAccuracyStatistics());
            statistics.put("storage", mediaService.getStorageStatistics());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting comprehensive statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup old data",
               description = "Removes expired queries, old media, and cleans up inactive data")
    public ResponseEntity<Integer> cleanupOldData() {
        try {
            int cleaned = 0; // In a real implementation, this would call cleanup methods
            return ResponseEntity.ok(cleaned);
        } catch (Exception e) {
            log.error("Error cleaning up old data: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/system/health")
    @Operation(summary = "Get system health",
               description = "Retrieves the current health status of the visual query system")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();

            // Mock health metrics
            health.put("queriesPerDay", 150);
            health.put("mediaProcessingQueue", 5);
            health.put("aiProcessingQueue", 2);
            health.put("overdueQueries", 3);
            health.put("systemLoad", 0.3);

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting system health: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/optimize-for-ai")
    @Operation(summary = "Optimize media for AI",
               description = "Optimizes media files for better AI processing")
    public ResponseEntity<Map<String, Object>> optimizeMediaForAI(@RequestParam Long mediaId) {
        try {
            Map<String, Object> optimization = categorizationService.optimizeMediaForAI(mediaId);
            return ResponseEntity.ok(optimization);
        } catch (Exception e) {
            log.error("Error optimizing media {} for AI: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/processing-queue")
    @Operation(summary = "Get processing queue status",
               description = "Retrieves the status of media processing queues")
    public ResponseEntity<List<VisualQueryMediaDTO>> getMediaProcessingQueue(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<VisualQueryMediaDTO> queue = mediaService.getMediaRequiringProcessing(limit);
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            log.error("Error getting media processing queue: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/batch-process")
    @Operation(summary = "Process media batch",
               description = "Processes a batch of media files for AI analysis")
    public ResponseEntity<Map<Long, Map<String, Object>>> processMediaBatch(@RequestBody List<Long> mediaIds) {
        try {
            Map<Long, Map<String, Object>> results = categorizationService.processMediaBatch(mediaIds);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error processing media batch: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ai/queue-status")
    @Operation(summary = "Get AI processing queue status",
               description = "Retrieves the status of AI processing queues")
    public ResponseEntity<Map<String, Object>> getAIProcessingQueueStatus() {
        try {
            Map<String, Object> status = categorizationService.getProcessingQueueStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting AI processing queue status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/{mediaId}/reprocess")
    @Operation(summary = "Reprocess media with updated models",
               description = "Reprocesses media using updated AI models")
    public ResponseEntity<Map<String, Object>> reprocessMediaWithUpdatedModels(@PathVariable Long mediaId) {
        try {
            Map<String, Object> result = categorizationService.reprocessMediaWithUpdatedModels(mediaId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error reprocessing media {} with updated models: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/responses/analytics")
    @Operation(summary = "Get response analytics",
               description = "Retrieves analytics about response quality and patterns")
    public ResponseEntity<Map<String, Object>> getResponseAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            analytics.put("byCategory", responseService.getResponseAnalyticsByCategory());
            analytics.put("averageTime", responseService.getAverageResponseTimeByCategory());
            analytics.put("qualityScores", Map.of("average", 0.85)); // Mock data

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting response analytics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/export/responses")
    @Operation(summary = "Export responses for analysis",
               description = "Exports response data for quality analysis and training")
    public ResponseEntity<Map<String, Object>> exportResponsesForAnalysis(
            @RequestParam String since,
            @RequestParam(defaultValue = "1000") int limit) {
        try {
            java.time.LocalDateTime sinceDate = java.time.LocalDateTime.parse(since);
            Map<String, Object> export = responseService.exportResponsesForAnalysis(sinceDate, limit);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            log.error("Error exporting responses for analysis: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/trending/approaches")
    @Operation(summary = "Get trending solution approaches",
               description = "Retrieves trending solution approaches by category")
    public ResponseEntity<List<Map<String, Object>>> getTrendingSolutionApproaches(
            @RequestParam(required = false) QueryCategory category,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> approaches = responseService.getTrendingSolutionApproaches(category, limit);
            return ResponseEntity.ok(approaches);
        } catch (Exception e) {
            log.error("Error getting trending solution approaches: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/responses/{responseId}/quality")
    @Operation(summary = "Get response quality score",
               description = "Retrieves quality analysis for a specific response")
    public ResponseEntity<Map<String, Object>> getResponseQualityScore(@PathVariable Long responseId) {
        try {
            Map<String, Object> quality = responseService.getResponseQualityScore(responseId);
            return ResponseEntity.ok(quality);
        } catch (Exception e) {
            log.error("Error getting quality score for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/format")
    @Operation(summary = "Update response formatting",
               description = "Updates a response with better formatting")
    public ResponseEntity<VisualQueryResponseDTO> updateResponseFormatting(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User mentor,
            @RequestParam String formattedContent) {
        try {
            VisualQueryResponseDTO response = responseService.updateResponseFormatting(responseId, mentor, formattedContent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot update formatting for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating formatting for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/duplicate")
    @Operation(summary = "Duplicate response",
               description = "Duplicates a successful response to another query")
    public ResponseEntity<VisualQueryResponseDTO> duplicateResponse(
            @RequestParam Long sourceResponseId,
            @RequestParam String targetQueryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User mentor) {
        try {
            VisualQueryResponseDTO response = responseService.duplicateResponse(sourceResponseId, targetQueryId, mentor);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot duplicate response {}: {}", sourceResponseId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error duplicating response {}: {}", sourceResponseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mentor/{mentorId}/templates")
    @Operation(summary = "Get mentor response templates",
               description = "Retrieves reusable response templates for a mentor")
    public ResponseEntity<List<VisualQueryResponseDTO>> getResponseTemplates(
            @PathVariable Long mentorId,
            @RequestParam(required = false) QueryCategory category) {
        try {
            // In a real implementation, you'd fetch the mentor user by ID
            // For now, we'll return an empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error getting response templates for mentor {}: {}", mentorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/save-template")
    @Operation(summary = "Save response as template",
               description = "Saves a response as a reusable template")
    public ResponseEntity<Void> saveResponseAsTemplate(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User mentor,
            @RequestParam String templateName,
            @RequestParam QueryCategory category) {
        try {
            boolean saved = responseService.saveResponseAsTemplate(responseId, mentor, templateName, category);
            if (!saved) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving response {} as template: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
