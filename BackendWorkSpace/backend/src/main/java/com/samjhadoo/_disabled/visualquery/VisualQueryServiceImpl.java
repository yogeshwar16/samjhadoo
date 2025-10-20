package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryDTO;
import com.samjhadoo.dto.visualquery.VisualQueryMediaDTO;
import com.samjhadoo.dto.visualquery.VisualQueryResponseDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;
import com.samjhadoo.repository.visualquery.VisualQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VisualQueryServiceImpl implements VisualQueryService {

    private final VisualQueryRepository queryRepository;
    private final MediaService mediaService;
    private final CategorizationService categorizationService;

    @Override
    public VisualQueryDTO createQuery(User user, String title, String description, QueryCategory category,
                                     int urgencyLevel, boolean anonymous, boolean isPublic,
                                     boolean allowMentorBidding, BigDecimal maxBudget,
                                     Long preferredMentorId, String locationContext, String tags) {
        // Validate inputs
        if (urgencyLevel < 1 || urgencyLevel > 5) {
            throw new IllegalArgumentException("Urgency level must be between 1 and 5");
        }

        VisualQuery query = VisualQuery.builder()
                .queryId(UUID.randomUUID().toString())
                .user(user)
                .title(title)
                .description(description)
                .status(QueryStatus.DRAFT)
                .category(category)
                .urgencyLevel(urgencyLevel)
                .anonymous(anonymous)
                .isPublic(isPublic)
                .allowMentorBidding(allowMentorBidding)
                .maxBudget(maxBudget)
                .preferredMentorId(preferredMentorId)
                .locationContext(locationContext)
                .tags(tags)
                .build();

        VisualQuery savedQuery = queryRepository.save(query);

        log.info("Created visual query {} for user {}: {}", savedQuery.getQueryId(), user.getId(), title);

        return convertToDTO(savedQuery);
    }

    @Override
    public VisualQueryDTO submitQuery(String queryId, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }

        if (!query.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User not authorized to submit this query");
        }

        if (query.getStatus() != QueryStatus.DRAFT) {
            throw new IllegalArgumentException("Query is not in draft status");
        }

        boolean submitted = query.submit();
        if (!submitted) {
            throw new IllegalStateException("Failed to submit query");
        }

        // Process AI categorization
        QueryCategory suggestedCategory = categorizationService.suggestCategoryFromQuery(query);
        if (suggestedCategory != null) {
            query.setAiSuggestedCategory(suggestedCategory.name());
        }

        VisualQuery savedQuery = queryRepository.save(query);

        log.info("Submitted visual query {} for user {}", queryId, user.getId());

        return convertToDTO(savedQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public VisualQueryDTO getQuery(String queryId, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return null;
        }

        // Check access permissions
        if (!canUserAccessQuery(queryId, user)) {
            return null;
        }

        // Increment view count if not the owner
        if (!query.getUser().getId().equals(user.getId())) {
            query.incrementViewCount();
            queryRepository.save(query);
        }

        return convertToDTO(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getUserQueries(User user, QueryStatus status, int limit) {
        List<VisualQuery> queries;

        if (status != null) {
            queries = queryRepository.findByUser(user).stream()
                    .filter(q -> q.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            queries = queryRepository.findByUser(user);
        }

        return queries.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getPublicQueries(QueryCategory category, int limit) {
        List<VisualQuery> queries;

        if (category != null) {
            queries = queryRepository.findByCategory(category).stream()
                    .filter(VisualQuery::isPublic)
                    .collect(Collectors.toList());
        } else {
            queries = queryRepository.findByIsPublicTrue();
        }

        return queries.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> searchQueries(String keyword, QueryCategory category, int limit) {
        List<VisualQuery> queries;

        if (category != null) {
            queries = queryRepository.findByKeyword(keyword).stream()
                    .filter(q -> q.getCategory() == category)
                    .filter(VisualQuery::isActive)
                    .collect(Collectors.toList());
        } else {
            queries = queryRepository.findByKeyword(keyword).stream()
                    .filter(VisualQuery::isActive)
                    .collect(Collectors.toList());
        }

        return queries.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getAvailableQueries(int limit) {
        return queryRepository.findSubmittedQueries().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean assignMentor(String queryId, User mentor) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return false;
        }

        if (query.getStatus() != QueryStatus.SUBMITTED && query.getStatus() != QueryStatus.UNDER_REVIEW) {
            return false;
        }

        boolean assigned = query.assignMentor(mentor);
        if (assigned) {
            queryRepository.save(query);
            log.info("Assigned mentor {} to query {}", mentor.getId(), queryId);
        }

        return assigned;
    }

    @Override
    public VisualQueryDTO resolveQuery(String queryId, User user, int rating, String feedback) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }

        if (!query.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only query owner can resolve it");
        }

        boolean resolved = query.markResolved(rating, feedback);
        if (!resolved) {
            throw new IllegalStateException("Query cannot be resolved in current state");
        }

        VisualQuery savedQuery = queryRepository.save(query);

        log.info("Resolved query {} with rating {}: {}", queryId, rating, feedback);

        return convertToDTO(savedQuery);
    }

    @Override
    public boolean closeQuery(String queryId, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return false;
        }

        if (!query.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only query owner can close it");
        }

        boolean closed = query.close();
        if (closed) {
            queryRepository.save(query);
            log.info("Closed query {} by user {}", queryId, user.getId());
        }

        return closed;
    }

    @Override
    public int incrementViewCount(String queryId) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query != null) {
            query.incrementViewCount();
            queryRepository.save(query);
            return query.getViewCount();
        }
        return 0;
    }

    @Override
    public boolean addHelpfulVote(String queryId, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null || !query.isActive()) {
            return false;
        }

        query.addHelpfulVote();
        queryRepository.save(query);

        log.info("Added helpful vote to query {} by user {}", queryId, user.getId());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQueryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalQueries = queryRepository.count();
        long submittedQueries = queryRepository.countSubmittedQueries();
        long inProgressQueries = queryRepository.countInProgressQueries();
        long resolvedQueries = queryRepository.countResolvedQueries();

        stats.put("totalQueries", totalQueries);
        stats.put("submittedQueries", submittedQueries);
        stats.put("inProgressQueries", inProgressQueries);
        stats.put("resolvedQueries", resolvedQueries);

        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (QueryStatus status : QueryStatus.values()) {
            long count = queryRepository.findByStatus(status).size();
            statusDistribution.put(status.name(), count);
        }
        stats.put("statusDistribution", statusDistribution);

        // Average resolution rating
        Double avgRating = queryRepository.getAverageResolutionRating();
        stats.put("averageResolutionRating", avgRating != null ? avgRating : 0);

        // Category distribution
        Map<String, Long> categoryDistribution = new HashMap<>();
        for (QueryCategory category : QueryCategory.values()) {
            long count = queryRepository.findByCategory(category).size();
            categoryDistribution.put(category.name(), count);
        }
        stats.put("categoryDistribution", categoryDistribution);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getQueriesByCategory(QueryCategory category, int limit) {
        return queryRepository.findByCategory(category).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getPopularQueries(int minViews, int limit) {
        return queryRepository.findPopularQueries(minViews).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getMentorQueries(User mentor, QueryStatus status) {
        List<VisualQuery> queries;

        if (status != null) {
            queries = queryRepository.findByAssignedMentor(mentor).stream()
                    .filter(q -> q.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            queries = queryRepository.findByAssignedMentor(mentor);
        }

        return queries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getOverdueQueries() {
        return queryRepository.findOverdueQueries(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VisualQueryDTO updateQueryCategory(String queryId, QueryCategory newCategory, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }

        // Check if user is mentor or admin
        boolean isMentor = query.getAssignedMentor() != null &&
                          query.getAssignedMentor().getId().equals(user.getId());
        boolean isOwner = query.getUser().getId().equals(user.getId());

        if (!isMentor && !isOwner) {
            throw new IllegalArgumentException("User not authorized to update category");
        }

        query.setCategory(newCategory);
        VisualQuery savedQuery = queryRepository.save(query);

        log.info("Updated category for query {} to {} by user {}", queryId, newCategory, user.getId());

        return convertToDTO(savedQuery);
    }

    @Override
    public QueryCategory processAICategorization(String queryId) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return null;
        }

        Map<String, Object> analysis = categorizationService.suggestCategoryFromQuery(query);
        String suggestedCategory = (String) analysis.get("category");

        if (suggestedCategory != null) {
            try {
                QueryCategory category = QueryCategory.valueOf(suggestedCategory);
                query.setAiSuggestedCategory(suggestedCategory);
                queryRepository.save(query);

                log.info("AI suggested category {} for query {}", suggestedCategory, queryId);

                return category;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid AI suggested category {} for query {}", suggestedCategory, queryId);
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQueryTimeline(String queryId) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return new HashMap<>();
        }

        Map<String, Object> timeline = new HashMap<>();

        timeline.put("created", query.getCreatedAt());
        timeline.put("submitted", query.getSubmittedAt());
        timeline.put("firstResponse", query.getFirstResponseAt());
        timeline.put("resolved", query.getResolvedAt());
        timeline.put("closed", query.getClosedAt());

        // Calculate durations
        if (query.getSubmittedAt() != null && query.getResolvedAt() != null) {
            long resolutionTimeHours = java.time.Duration.between(query.getSubmittedAt(), query.getResolvedAt()).toHours();
            timeline.put("resolutionTimeHours", resolutionTimeHours);
        }

        if (query.getFirstResponseAt() != null && query.getSubmittedAt() != null) {
            long firstResponseTimeMinutes = java.time.Duration.between(query.getSubmittedAt(), query.getFirstResponseAt()).toMinutes();
            timeline.put("firstResponseTimeMinutes", firstResponseTimeMinutes);
        }

        return timeline;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessQuery(String queryId, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            return false;
        }

        // Owner can always access
        if (query.getUser().getId().equals(user.getId())) {
            return true;
        }

        // Assigned mentor can access
        if (query.getAssignedMentor() != null && query.getAssignedMentor().getId().equals(user.getId())) {
            return true;
        }

        // Public queries can be accessed by anyone
        if (query.isPublic()) {
            return true;
        }

        // Anonymous queries can only be accessed by owner and assigned mentor
        return !query.isAnonymous();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryDTO> getQueriesRequiringAttention(int limit) {
        List<VisualQuery> queries = new ArrayList<>();

        // Add overdue queries
        queries.addAll(queryRepository.findOverdueQueries(LocalDateTime.now()));

        // Add unassigned submitted queries
        queries.addAll(queryRepository.findUnassignedQueries());

        // Add queries with low helpful votes
        queries.addAll(queryRepository.findByStatus(QueryStatus.IN_PROGRESS));

        return queries.stream()
                .distinct()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VisualQueryDTO updateQueryUrgency(String queryId, int newUrgency, User user) {
        VisualQuery query = queryRepository.findByQueryId(queryId).orElse(null);
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }

        if (!query.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only query owner can update urgency");
        }

        if (newUrgency < 1 || newUrgency > 5) {
            throw new IllegalArgumentException("Urgency must be between 1 and 5");
        }

        query.setUrgencyLevel(newUrgency);
        VisualQuery savedQuery = queryRepository.save(query);

        log.info("Updated urgency for query {} to {} by user {}", queryId, newUrgency, user.getId());

        return convertToDTO(savedQuery);
    }

    private VisualQueryDTO convertToDTO(VisualQuery query) {
        List<VisualQueryMediaDTO> mediaDTOs = query.getMediaAttachments().stream()
                .map(media -> mediaService.getMediaById(media.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<VisualQueryResponseDTO> responseDTOs = query.getResponses().stream()
                .map(response -> {
                    // This would need a ResponseService to convert
                    return null; // Placeholder for now
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return VisualQueryDTO.builder()
                .id(query.getId())
                .queryId(query.getQueryId())
                .userName(query.isAnonymous() ? "Anonymous" :
                         query.getUser().getFirstName() + " " + query.getUser().getLastName())
                .title(query.getTitle())
                .description(query.getDescription())
                .status(query.getStatus())
                .category(query.getCategory())
                .aiSuggestedCategory(query.getAiSuggestedCategory())
                .urgencyLevel(query.getUrgencyLevel())
                .anonymous(query.isAnonymous())
                .isPublic(query.isPublic())
                .allowMentorBidding(query.isAllowMentorBidding())
                .maxBudget(query.getMaxBudget())
                .preferredMentorId(query.getPreferredMentorId())
                .preferredMentorName(query.getPreferredMentorId() != null ? "Preferred Mentor" : null)
                .locationContext(query.getLocationContext())
                .tags(query.getTags())
                .assignedMentorName(query.getAssignedMentor() != null ?
                                   (query.isAnonymous() ? "Anonymous" :
                                    query.getAssignedMentor().getFirstName() + " " + query.getAssignedMentor().getLastName()) : null)
                .submittedAt(query.getSubmittedAt())
                .firstResponseAt(query.getFirstResponseAt())
                .resolvedAt(query.getResolvedAt())
                .closedAt(query.getClosedAt())
                .responseDeadline(query.getResponseDeadline())
                .resolutionRating(query.getResolutionRating())
                .resolutionFeedback(query.getResolutionFeedback())
                .viewCount(query.getViewCount())
                .helpfulVotes(query.getHelpfulVotes())
                .createdAt(query.getCreatedAt())
                .updatedAt(query.getUpdatedAt())
                .mediaAttachments(mediaDTOs)
                .responses(responseDTOs)
                .isOverdue(query.isOverdue())
                .responseTimeHours(query.getResponseTimeHours())
                .isActive(query.isActive())
                .build();
    }
}
