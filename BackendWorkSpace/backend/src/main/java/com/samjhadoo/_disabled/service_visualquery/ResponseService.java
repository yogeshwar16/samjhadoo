package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryResponseDTO;
import com.samjhadoo.dto.visualquery.VisualQueryResponseMediaDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.model.enums.visualquery.MediaType;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.visualquery.VisualQueryResponse.ResponseType;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for managing visual query responses from mentors.
 */
public interface ResponseService {

    /**
     * Creates a new response to a visual query.
     * @param query The visual query
     * @param mentor The responding mentor
     * @param responseType Type of response
     * @param content Response content
     * @param isSolution Whether this is a complete solution
     * @param estimatedTimeMinutes Estimated time to implement
     * @param difficultyLevel Difficulty level
     * @param requiredMaterials Required materials
     * @param costEstimate Cost estimate
     * @param stepByStepGuide Step-by-step instructions
     * @param alternativeSolutions Alternative approaches
     * @param safetyPrecautions Safety warnings
     * @param references External references
     * @return The created response DTO
     */
    VisualQueryResponseDTO createResponse(VisualQuery query, User mentor, ResponseType responseType,
                                         String content, boolean isSolution, Integer estimatedTimeMinutes,
                                         String difficultyLevel, String requiredMaterials,
                                         BigDecimal costEstimate, String stepByStepGuide,
                                         String alternativeSolutions, String safetyPrecautions,
                                         String references);

    /**
     * Updates an existing response.
     * @param responseId The response ID
     * @param mentor The mentor updating
     * @param content New content
     * @param isSolution New solution status
     * @param estimatedTimeMinutes New time estimate
     * @param difficultyLevel New difficulty level
     * @param requiredMaterials New materials
     * @param costEstimate New cost estimate
     * @param stepByStepGuide New step-by-step guide
     * @param alternativeSolutions New alternatives
     * @param safetyPrecautions New safety precautions
     * @param references New references
     * @return The updated response DTO
     */
    VisualQueryResponseDTO updateResponse(Long responseId, User mentor, String content,
                                        boolean isSolution, Integer estimatedTimeMinutes,
                                        String difficultyLevel, String requiredMaterials,
                                        BigDecimal costEstimate, String stepByStepGuide,
                                        String alternativeSolutions, String safetyPrecautions,
                                        String references);

    /**
     * Deletes a response.
     * @param responseId The response ID
     * @param mentor The mentor deleting
     * @return true if deleted successfully
     */
    boolean deleteResponse(Long responseId, User mentor);

    /**
     * Gets responses for a query.
     * @param queryId The query ID
     * @return List of response DTOs
     */
    List<VisualQueryResponseDTO> getQueryResponses(String queryId);

    /**
     * Gets a response by ID.
     * @param responseId The response ID
     * @return The response DTO or null if not found
     */
    VisualQueryResponseDTO getResponseById(Long responseId);

    /**
     * Gets responses by a mentor.
     * @param mentor The mentor
     * @param limit Maximum number of responses
     * @return List of response DTOs by the mentor
     */
    List<VisualQueryResponseDTO> getMentorResponses(User mentor, int limit);

    /**
     * Gets helpful responses.
     * @param minHelpfulnessRatio Minimum helpfulness ratio (0-1)
     * @param minVotes Minimum number of votes
     * @param limit Maximum number of responses
     * @return List of highly helpful response DTOs
     */
    List<VisualQueryResponseDTO> getHelpfulResponses(double minHelpfulnessRatio, int minVotes, int limit);

    /**
     * Adds media to a response.
     * @param responseId The response ID
     * @param file The uploaded file
     * @param mediaType The media type
     * @param description Media description
     * @param isDemo Whether this is a demo
     * @param isSolutionMedia Whether this shows the solution
     * @param stepNumber Step number for step-by-step guides
     * @param mentor The mentor adding media
     * @return The created media DTO
     */
    VisualQueryResponseMediaDTO addResponseMedia(Long responseId, MultipartFile file, MediaType mediaType,
                                               String description, boolean isDemo, boolean isSolutionMedia,
                                               Integer stepNumber, User mentor);

    /**
     * Removes media from a response.
     * @param mediaId The media ID
     * @param mentor The mentor removing
     * @return true if removed successfully
     */
    boolean removeResponseMedia(Long mediaId, User mentor);

    /**
     * Adds a helpful vote to a response.
     * @param responseId The response ID
     * @param user The user voting
     * @return true if vote was added
     */
    boolean addHelpfulVote(Long responseId, User user);

    /**
     * Adds a not helpful vote to a response.
     * @param responseId The response ID
     * @param user The user voting
     * @return true if vote was added
     */
    boolean addNotHelpfulVote(Long responseId, User user);

    /**
     * Rates a mentor's response.
     * @param responseId The response ID
     * @param user The user rating
     * @param rating Rating (1-5)
     * @param feedback Rating feedback
     * @return The updated response DTO
     */
    VisualQueryResponseDTO rateResponse(Long responseId, User user, int rating, String feedback);

    /**
     * Gets response statistics.
     * @return Map of response statistics
     */
    Map<String, Object> getResponseStatistics();

    /**
     * Gets responses requiring moderation.
     * @param limit Maximum number of responses
     * @return List of response DTOs needing review
     */
    List<VisualQueryResponseDTO> getResponsesRequiringModeration(int limit);

    /**
     * Reports inappropriate response content.
     * @param responseId The response ID
     * @param reporter The user reporting
     * @param reason Report reason
     * @return true if reported successfully
     */
    boolean reportResponse(Long responseId, User reporter, String reason);

    /**
     * Hides inappropriate response.
     * @param responseId The response ID
     * @param moderator The moderator hiding it
     * @param reason Reason for hiding
     * @return true if hidden successfully
     */
    boolean hideResponse(Long responseId, User moderator, String reason);

    /**
     * Gets mentor performance metrics.
     * @param mentor The mentor
     * @return Performance statistics
     */
    Map<String, Object> getMentorPerformanceMetrics(User mentor);

    /**
     * Gets response analytics by category.
     * @return Analytics by query category
     */
    Map<String, Object> getResponseAnalyticsByCategory();

    /**
     * Gets average response time by category.
     * @return Average response times by category
     */
    Map<String, Object> getAverageResponseTimeByCategory();

    /**
     * Exports responses for quality analysis.
     * @param since Start date
     * @param limit Maximum number of responses
     * @return Export data
     */
    Map<String, Object> exportResponsesForAnalysis(java.time.LocalDateTime since, int limit);

    /**
     * Gets trending solution approaches.
     * @param category Filter by category (optional)
     * @param limit Maximum number of approaches
     * @return List of trending solution approaches
     */
    List<Map<String, Object>> getTrendingSolutionApproaches(QueryCategory category, int limit);

    /**
     * Gets response quality scores.
     * @param responseId The response ID
     * @return Quality analysis
     */
    Map<String, Object> getResponseQualityScore(Long responseId);

    /**
     * Updates response with better formatting.
     * @param responseId The response ID
     * @param mentor The mentor updating
     * @param formattedContent Formatted content
     * @return The updated response DTO
     */
    VisualQueryResponseDTO updateResponseFormatting(Long responseId, User mentor, String formattedContent);

    /**
     * Duplicates a successful response template.
     * @param sourceResponseId The source response ID
     * @param targetQueryId The target query ID
     * @param mentor The mentor duplicating
     * @return The new response DTO
     */
    VisualQueryResponseDTO duplicateResponse(Long sourceResponseId, String targetQueryId, User mentor);

    /**
     * Gets response templates for a mentor.
     * @param mentor The mentor
     * @param category Filter by category (optional)
     * @return List of reusable response templates
     */
    List<VisualQueryResponseDTO> getResponseTemplates(User mentor, QueryCategory category);

    /**
     * Saves a response as a template.
     * @param responseId The response ID
     * @param mentor The mentor saving
     * @param templateName Template name
     * @param category Template category
     * @return true if saved successfully
     */
    boolean saveResponseAsTemplate(Long responseId, User mentor, String templateName, QueryCategory category);
}
