package com.samjhadoo.controller.api.visualquery;

import com.samjhadoo.dto.visualquery.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;
import com.samjhadoo.service.visualquery.VisualQueryService;
import com.samjhadoo.service.visualquery.MediaService;
import com.samjhadoo.service.visualquery.AnnotationService;
import com.samjhadoo.service.visualquery.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Public API controller for Visual Query features.
 * Provides endpoints for users to submit visual queries, upload media, and view responses.
 */
@RestController
@RequestMapping("/api/visual-queries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visual Queries", description = "User visual query features")
public class VisualQueryController {

    private final VisualQueryService visualQueryService;
    private final MediaService mediaService;
    private final AnnotationService annotationService;
    private final ResponseService responseService;

    // Query Management Endpoints
    @PostMapping
    @Operation(summary = "Create visual query",
               description = "Creates a new visual query with basic information")
    public ResponseEntity<VisualQueryDTO> createQuery(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam QueryCategory category,
            @RequestParam(defaultValue = "3") int urgencyLevel,
            @RequestParam(defaultValue = "false") boolean anonymous,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam(defaultValue = "false") boolean allowMentorBidding,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) Long preferredMentorId,
            @RequestParam(required = false) String locationContext,
            @RequestParam(required = false) String tags) {
        try {
            VisualQueryDTO query = visualQueryService.createQuery(user, title, description, category,
                    urgencyLevel, anonymous, isPublic, allowMentorBidding, maxBudget,
                    preferredMentorId, locationContext, tags);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid query creation for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating query for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/submit")
    @Operation(summary = "Submit query",
               description = "Submits a draft query for mentor review")
    public ResponseEntity<VisualQueryDTO> submitQuery(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            VisualQueryDTO query = visualQueryService.submitQuery(queryId, user);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot submit query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{queryId}")
    @Operation(summary = "Get query details",
               description = "Retrieves detailed information about a specific query")
    public ResponseEntity<VisualQueryDTO> getQuery(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            VisualQueryDTO query = visualQueryService.getQuery(queryId, user);
            if (query == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            log.error("Error getting query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user")
    @Operation(summary = "Get user queries",
               description = "Retrieves queries submitted by the authenticated user")
    public ResponseEntity<List<VisualQueryDTO>> getUserQueries(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(required = false) QueryStatus status,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getUserQueries(user, status, limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting user queries for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/public")
    @Operation(summary = "Get public queries",
               description = "Retrieves publicly available queries for learning")
    public ResponseEntity<List<VisualQueryDTO>> getPublicQueries(
            @RequestParam(required = false) QueryCategory category,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getPublicQueries(category, limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting public queries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search queries",
               description = "Searches for queries by keyword and optional category")
    public ResponseEntity<List<VisualQueryDTO>> searchQueries(
            @RequestParam String keyword,
            @RequestParam(required = false) QueryCategory category,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.searchQueries(keyword, category, limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error searching queries for keyword '{}': {}", keyword, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available queries",
               description = "Retrieves queries available for mentor assignment")
    public ResponseEntity<List<VisualQueryDTO>> getAvailableQueries(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getAvailableQueries(limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting available queries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/resolve")
    @Operation(summary = "Resolve query",
               description = "Marks a query as resolved with rating and feedback")
    public ResponseEntity<VisualQueryDTO> resolveQuery(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int rating,
            @RequestParam(required = false) String feedback) {
        try {
            VisualQueryDTO query = visualQueryService.resolveQuery(queryId, user, rating, feedback);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resolve query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error resolving query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/close")
    @Operation(summary = "Close query",
               description = "Closes a query without resolution")
    public ResponseEntity<Void> closeQuery(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean closed = visualQueryService.closeQuery(queryId, user);
            if (!closed) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cannot close query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error closing query {} for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/view")
    @Operation(summary = "Increment view count",
               description = "Increments the view count for a query")
    public ResponseEntity<Integer> incrementViewCount(@PathVariable String queryId) {
        try {
            int viewCount = visualQueryService.incrementViewCount(queryId);
            return ResponseEntity.ok(viewCount);
        } catch (Exception e) {
            log.error("Error incrementing view count for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/helpful")
    @Operation(summary = "Mark query as helpful",
               description = "Adds a helpful vote to a query")
    public ResponseEntity<Void> markQueryHelpful(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean marked = visualQueryService.addHelpfulVote(queryId, user);
            if (!marked) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking query {} as helpful for user {}: {}", queryId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Media Management Endpoints
    @PostMapping("/{queryId}/media")
    @Operation(summary = "Upload media",
               description = "Uploads media (photo/video/document) for a visual query")
    public ResponseEntity<VisualQueryMediaDTO> uploadMedia(
            @PathVariable String queryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam com.samjhadoo.model.enums.visualquery.MediaType mediaType,
            @RequestParam(defaultValue = "false") boolean isPrimary,
            @RequestParam(required = false) String description,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            // In a real implementation, you'd fetch the query by ID
            // For now, we'll create a mock query
            VisualQueryDTO queryDTO = visualQueryService.getQuery(queryId, user);
            if (queryDTO == null) {
                return ResponseEntity.notFound().build();
            }

            VisualQueryMediaDTO media = mediaService.uploadMedia(null, file, mediaType, isPrimary, description, user);
            return ResponseEntity.ok(media);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid media upload for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading media for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{queryId}/media")
    @Operation(summary = "Get query media",
               description = "Retrieves all media attachments for a query")
    public ResponseEntity<List<VisualQueryMediaDTO>> getQueryMedia(@PathVariable String queryId) {
        try {
            List<VisualQueryMediaDTO> media = mediaService.getQueryMedia(queryId);
            return ResponseEntity.ok(media);
        } catch (Exception e) {
            log.error("Error getting media for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/{mediaId}")
    @Operation(summary = "Get media details",
               description = "Retrieves detailed information about specific media")
    public ResponseEntity<VisualQueryMediaDTO> getMedia(@PathVariable Long mediaId) {
        try {
            VisualQueryMediaDTO media = mediaService.getMediaById(mediaId);
            if (media == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(media);
        } catch (Exception e) {
            log.error("Error getting media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/media/{mediaId}")
    @Operation(summary = "Delete media",
               description = "Deletes media from a query")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean deleted = mediaService.deleteMedia(mediaId, user);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete media {} for user {}: {}", mediaId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting media {} for user {}: {}", mediaId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

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

    // Annotation Endpoints
    @PostMapping("/responses/{responseId}/annotations")
    @Operation(summary = "Create annotation",
               description = "Creates an annotation on visual query media")
    public ResponseEntity<VisualQueryAnnotationDTO> createAnnotation(
            @PathVariable Long responseId,
            @RequestParam Long mediaId,
            @RequestParam com.samjhadoo.model.visualquery.VisualQueryAnnotation.AnnotationType annotationType,
            @RequestParam double xPosition,
            @RequestParam double yPosition,
            @RequestParam double width,
            @RequestParam double height,
            @RequestParam(defaultValue = "0") double rotation,
            @RequestParam(defaultValue = "#FF0000") String color,
            @RequestParam(defaultValue = "2.0") double strokeWidth,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "12.0") double fontSize,
            @RequestParam(defaultValue = "Arial") String fontFamily,
            @RequestParam(required = false) String shapeData,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            VisualQueryAnnotationDTO annotation = annotationService.createAnnotation(
                    null, null, annotationType, xPosition, yPosition, width, height,
                    rotation, color, strokeWidth, text, fontSize, fontFamily, shapeData, user);
            return ResponseEntity.ok(annotation);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot create annotation for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating annotation for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/responses/{responseId}/annotations")
    @Operation(summary = "Get response annotations",
               description = "Retrieves all annotations for a specific response")
    public ResponseEntity<List<VisualQueryAnnotationDTO>> getResponseAnnotations(@PathVariable Long responseId) {
        try {
            List<VisualQueryAnnotationDTO> annotations = annotationService.getResponseAnnotations(responseId);
            return ResponseEntity.ok(annotations);
        } catch (Exception e) {
            log.error("Error getting annotations for response {}: {}", responseId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/{mediaId}/annotations")
    @Operation(summary = "Get media annotations",
               description = "Retrieves all annotations for specific media")
    public ResponseEntity<List<VisualQueryAnnotationDTO>> getMediaAnnotations(@PathVariable Long mediaId) {
        try {
            List<VisualQueryAnnotationDTO> annotations = annotationService.getMediaAnnotations(mediaId);
            return ResponseEntity.ok(annotations);
        } catch (Exception e) {
            log.error("Error getting annotations for media {}: {}", mediaId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Response Management Endpoints
    @PostMapping("/{queryId}/responses")
    @Operation(summary = "Create response",
               description = "Creates a mentor response to a visual query")
    public ResponseEntity<VisualQueryResponseDTO> createResponse(
            @PathVariable String queryId,
            @Parameter(hidden = true) @AuthenticationPrincipal User mentor,
            @RequestParam com.samjhadoo.model.visualquery.VisualQueryResponse.ResponseType responseType,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean isSolution,
            @RequestParam(required = false) Integer estimatedTimeMinutes,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String requiredMaterials,
            @RequestParam(required = false) BigDecimal costEstimate,
            @RequestParam(required = false) String stepByStepGuide,
            @RequestParam(required = false) String alternativeSolutions,
            @RequestParam(required = false) String safetyPrecautions,
            @RequestParam(required = false) String references) {
        try {
            VisualQueryResponseDTO response = responseService.createResponse(
                    null, mentor, responseType, content, isSolution, estimatedTimeMinutes,
                    difficultyLevel, requiredMaterials, costEstimate, stepByStepGuide,
                    alternativeSolutions, safetyPrecautions, references);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating response for query {} by mentor {}: {}", queryId, mentor.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{queryId}/responses")
    @Operation(summary = "Get query responses",
               description = "Retrieves all responses for a specific query")
    public ResponseEntity<List<VisualQueryResponseDTO>> getQueryResponses(@PathVariable String queryId) {
        try {
            List<VisualQueryResponseDTO> responses = responseService.getQueryResponses(queryId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting responses for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/helpful")
    @Operation(summary = "Mark response as helpful",
               description = "Adds a helpful vote to a response")
    public ResponseEntity<Void> markResponseHelpful(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean marked = responseService.addHelpfulVote(responseId, user);
            if (!marked) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking response {} as helpful for user {}: {}", responseId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/not-helpful")
    @Operation(summary = "Mark response as not helpful",
               description = "Adds a not helpful vote to a response")
    public ResponseEntity<Void> markResponseNotHelpful(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean marked = responseService.addNotHelpfulVote(responseId, user);
            if (!marked) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking response {} as not helpful for user {}: {}", responseId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/responses/{responseId}/rate")
    @Operation(summary = "Rate response",
               description = "Rates a mentor's response with feedback")
    public ResponseEntity<VisualQueryResponseDTO> rateResponse(
            @PathVariable Long responseId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam int rating,
            @RequestParam(required = false) String feedback) {
        try {
            VisualQueryResponseDTO response = responseService.rateResponse(responseId, user, rating, feedback);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot rate response {} for user {}: {}", responseId, user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rating response {} for user {}: {}", responseId, user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Analytics and Utility Endpoints
    @GetMapping("/categories")
    @Operation(summary = "Get queries by category",
               description = "Retrieves queries filtered by category")
    public ResponseEntity<List<VisualQueryDTO>> getQueriesByCategory(
            @RequestParam QueryCategory category,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getQueriesByCategory(category, limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting queries by category {}: {}", category, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular queries",
               description = "Retrieves popular queries based on view count and helpfulness")
    public ResponseEntity<List<VisualQueryDTO>> getPopularQueries(
            @RequestParam(defaultValue = "10") int minViews,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getPopularQueries(minViews, limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting popular queries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get query statistics",
               description = "Retrieves statistics about visual queries")
    public ResponseEntity<Map<String, Object>> getQueryStatistics() {
        try {
            Map<String, Object> statistics = visualQueryService.getQueryStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting query statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get mentor queries",
               description = "Retrieves queries assigned to a specific mentor")
    public ResponseEntity<List<VisualQueryDTO>> getMentorQueries(
            @PathVariable Long mentorId,
            @RequestParam(required = false) QueryStatus status) {
        try {
            // In a real implementation, you'd fetch the mentor user by ID
            // For now, we'll return an empty list
            return ResponseEntity.ok(List.of());
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

    @PostMapping("/{queryId}/category")
    @Operation(summary = "Update query category",
               description = "Updates the category of a visual query")
    public ResponseEntity<VisualQueryDTO> updateQueryCategory(
            @PathVariable String queryId,
            @RequestParam QueryCategory newCategory,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            VisualQueryDTO query = visualQueryService.updateQueryCategory(queryId, newCategory, user);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot update category for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating category for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/categorize")
    @Operation(summary = "Process AI categorization",
               description = "Processes AI categorization for a query")
    public ResponseEntity<QueryCategory> processAICategorization(@PathVariable String queryId) {
        try {
            QueryCategory category = visualQueryService.processAICategorization(queryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("Error processing AI categorization for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{queryId}/timeline")
    @Operation(summary = "Get query timeline",
               description = "Retrieves the timeline of events for a query")
    public ResponseEntity<Map<String, Object>> getQueryTimeline(@PathVariable String queryId) {
        try {
            Map<String, Object> timeline = visualQueryService.getQueryTimeline(queryId);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error getting timeline for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/attention")
    @Operation(summary = "Get queries requiring attention",
               description = "Retrieves queries that need attention (overdue, unassigned, etc.)")
    public ResponseEntity<List<VisualQueryDTO>> getQueriesRequiringAttention(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<VisualQueryDTO> queries = visualQueryService.getQueriesRequiringAttention(limit);
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error getting queries requiring attention: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queryId}/urgency")
    @Operation(summary = "Update query urgency",
               description = "Updates the urgency level of a query")
    public ResponseEntity<VisualQueryDTO> updateQueryUrgency(
            @PathVariable String queryId,
            @RequestParam int newUrgency,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            VisualQueryDTO query = visualQueryService.updateQueryUrgency(queryId, newUrgency, user);
            return ResponseEntity.ok(query);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot update urgency for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating urgency for query {}: {}", queryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
