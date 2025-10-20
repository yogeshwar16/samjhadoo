package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Main service for managing visual queries and their lifecycle.
 */
public interface VisualQueryService {

    /**
     * Creates a new visual query.
     * @param user The user creating the query
     * @param title Query title
     * @param description Query description
     * @param category Query category
     * @param urgencyLevel Urgency level (1-5)
     * @param anonymous Whether query should be anonymous
     * @param isPublic Whether query should be public
     * @param allowMentorBidding Whether to allow mentor bidding
     * @param maxBudget Maximum budget for paid queries
     * @param preferredMentorId Preferred mentor ID (optional)
     * @param locationContext Location context (optional)
     * @param tags Query tags (optional)
     * @return The created query DTO
     */
    VisualQueryDTO createQuery(User user, String title, String description, QueryCategory category,
                              int urgencyLevel, boolean anonymous, boolean isPublic,
                              boolean allowMentorBidding, BigDecimal maxBudget,
                              Long preferredMentorId, String locationContext, String tags);

    /**
     * Submits a draft query for review.
     * @param queryId The query ID
     * @param user The user submitting
     * @return The updated query DTO
     */
    VisualQueryDTO submitQuery(String queryId, User user);

    /**
     * Gets a query by ID.
     * @param queryId The query ID
     * @param user The requesting user
     * @return The query DTO or null if not found
     */
    VisualQueryDTO getQuery(String queryId, User user);

    /**
     * Gets queries for a user.
     * @param user The user
     * @param status Filter by status (optional)
     * @param limit Maximum number of queries
     * @return List of query DTOs
     */
    List<VisualQueryDTO> getUserQueries(User user, QueryStatus status, int limit);

    /**
     * Gets all public queries.
     * @param category Filter by category (optional)
     * @param limit Maximum number of queries
     * @return List of public query DTOs
     */
    List<VisualQueryDTO> getPublicQueries(QueryCategory category, int limit);

    /**
     * Searches queries by keyword.
     * @param keyword Search keyword
     * @param category Filter by category (optional)
     * @param limit Maximum number of results
     * @return List of matching query DTOs
     */
    List<VisualQueryDTO> searchQueries(String keyword, QueryCategory category, int limit);

    /**
     * Gets queries available for mentor assignment.
     * @param limit Maximum number of queries
     * @return List of available query DTOs
     */
    List<VisualQueryDTO> getAvailableQueries(int limit);

    /**
     * Assigns a mentor to a query.
     * @param queryId The query ID
     * @param mentor The mentor to assign
     * @return true if assigned successfully
     */
    boolean assignMentor(String queryId, User mentor);

    /**
     * Marks a query as resolved.
     * @param queryId The query ID
     * @param user The user marking as resolved
     * @param rating Resolution rating (1-5)
     * @param feedback Resolution feedback
     * @return The updated query DTO
     */
    VisualQueryDTO resolveQuery(String queryId, User user, int rating, String feedback);

    /**
     * Closes a query without resolution.
     * @param queryId The query ID
     * @param user The user closing
     * @return true if closed successfully
     */
    boolean closeQuery(String queryId, User user);

    /**
     * Increments view count for a query.
     * @param queryId The query ID
     * @return The updated view count
     */
    int incrementViewCount(String queryId);

    /**
     * Adds a helpful vote to a query.
     * @param queryId The query ID
     * @param user The user voting
     * @return true if vote was added
     */
    boolean addHelpfulVote(String queryId, User user);

    /**
     * Gets query statistics.
     * @return Map of query statistics
     */
    Map<String, Object> getQueryStatistics();

    /**
     * Gets queries by category.
     * @param category The category
     * @param limit Maximum number of queries
     * @return List of query DTOs in the category
     */
    List<VisualQueryDTO> getQueriesByCategory(QueryCategory category, int limit);

    /**
     * Gets popular queries.
     * @param minViews Minimum view count
     * @param limit Maximum number of queries
     * @return List of popular query DTOs
     */
    List<VisualQueryDTO> getPopularQueries(int minViews, int limit);

    /**
     * Gets queries by mentor.
     * @param mentor The mentor
     * @param status Filter by status (optional)
     * @return List of query DTOs assigned to the mentor
     */
    List<VisualQueryDTO> getMentorQueries(User mentor, QueryStatus status);

    /**
     * Gets overdue queries.
     * @return List of overdue query DTOs
     */
    List<VisualQueryDTO> getOverdueQueries();

    /**
     * Updates query category (admin/mentor action).
     * @param queryId The query ID
     * @param newCategory The new category
     * @param user The user updating
     * @return The updated query DTO
     */
    VisualQueryDTO updateQueryCategory(String queryId, QueryCategory newCategory, User user);

    /**
     * Processes AI categorization for a query.
     * @param queryId The query ID
     * @return The suggested category
     */
    QueryCategory processAICategorization(String queryId);

    /**
     * Gets query timeline for analytics.
     * @param queryId The query ID
     * @return Map of timeline events
     */
    Map<String, Object> getQueryTimeline(String queryId);

    /**
     * Checks if a user can access a query.
     * @param queryId The query ID
     * @param user The user
     * @return true if user can access the query
     */
    boolean canUserAccessQuery(String queryId, User user);

    /**
     * Gets queries requiring attention (overdue, unassigned, etc.).
     * @param limit Maximum number of queries
     * @return List of queries requiring attention
     */
    List<VisualQueryDTO> getQueriesRequiringAttention(int limit);

    /**
     * Updates query urgency level.
     * @param queryId The query ID
     * @param newUrgency New urgency level (1-5)
     * @param user The user updating
     * @return The updated query DTO
     */
    VisualQueryDTO updateQueryUrgency(String queryId, int newUrgency, User user);
}
