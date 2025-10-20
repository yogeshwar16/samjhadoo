package com.samjhadoo.service.visualquery;

import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.visualquery.VisualQueryMedia;

import java.util.List;
import java.util.Map;

/**
 * Service for AI-powered categorization and content analysis of visual queries.
 */
public interface CategorizationService {

    /**
     * Analyzes media content and suggests a category.
     * @param media The media to analyze
     * @return Suggested category and confidence score
     */
    Map<String, Object> suggestCategoryFromMedia(VisualQueryMedia media);

    /**
     * Analyzes all media in a query and suggests an overall category.
     * @param query The visual query
     * @return Suggested category and analysis details
     */
    Map<String, Object> suggestCategoryFromQuery(VisualQuery query);

    /**
     * Extracts relevant tags from media content.
     * @param media The media to analyze
     * @return List of extracted tags with confidence scores
     */
    List<Map<String, Object>> extractTagsFromMedia(VisualQueryMedia media);

    /**
     * Extracts tags from all media in a query.
     * @param query The visual query
     * @return Consolidated list of tags
     */
    List<String> extractTagsFromQuery(VisualQuery query);

    /**
     * Analyzes image content for objects, text, and context.
     * @param mediaId The media ID
     * @return Analysis results including detected objects and text
     */
    Map<String, Object> analyzeImageContent(Long mediaId);

    /**
     * Analyzes video content for objects, scenes, and audio.
     * @param mediaId The media ID
     * @return Video analysis results
     */
    Map<String, Object> analyzeVideoContent(Long mediaId);

    /**
     * Extracts text from images using OCR.
     * @param mediaId The media ID
     * @return Extracted text and confidence
     */
    Map<String, Object> extractTextFromImage(Long mediaId);

    /**
     * Detects objects in images.
     * @param mediaId The media ID
     * @return List of detected objects with bounding boxes
     */
    List<Map<String, Object>> detectObjectsInImage(Long mediaId);

    /**
     * Analyzes document content and structure.
     * @param mediaId The media ID
     * @return Document analysis results
     */
    Map<String, Object> analyzeDocumentContent(Long mediaId);

    /**
     * Gets content safety analysis.
     * @param mediaId The media ID
     * @return Safety analysis results
     */
    Map<String, Object> analyzeContentSafety(Long mediaId);

    /**
     * Processes a batch of media for AI analysis.
     * @param mediaIds List of media IDs to process
     * @return Processing results for each media
     */
    Map<Long, Map<String, Object>> processMediaBatch(List<Long> mediaIds);

    /**
     * Gets AI processing queue status.
     * @return Queue status and statistics
     */
    Map<String, Object> getProcessingQueueStatus();

    /**
     * Reprocesses media with updated AI models.
     * @param mediaId The media ID
     * @return Reprocessing results
     */
    Map<String, Object> reprocessMediaWithUpdatedModels(Long mediaId);

    /**
     * Gets categorization confidence threshold.
     * @return Current confidence threshold (0-1)
     */
    double getCategorizationConfidenceThreshold();

    /**
     * Updates categorization confidence threshold.
     * @param threshold New threshold (0-1)
     */
    void updateCategorizationConfidenceThreshold(double threshold);

    /**
     * Gets supported languages for text extraction.
     * @return List of supported language codes
     */
    List<String> getSupportedLanguages();

    /**
     * Validates if content is appropriate for the platform.
     * @param mediaId The media ID
     * @return Validation result with safety flags
     */
    Map<String, Object> validateContentAppropriateness(Long mediaId);

    /**
     * Gets AI model performance metrics.
     * @return Model performance statistics
     */
    Map<String, Object> getAIModelPerformanceMetrics();

    /**
     * Updates AI model for better accuracy.
     * @param modelType The model type to update
     * @param modelVersion New model version
     */
    void updateAIModel(String modelType, String modelVersion);

    /**
     * Gets content analysis history for a media item.
     * @param mediaId The media ID
     * @return Analysis history
     */
    List<Map<String, Object>> getContentAnalysisHistory(Long mediaId);

    /**
     * Exports analysis data for training.
     * @param since Start date for export
     * @param limit Maximum number of records
     * @return Export data
     */
    Map<String, Object> exportAnalysisDataForTraining(java.time.LocalDateTime since, int limit);

    /**
     * Gets trending categories based on recent queries.
     * @param days Number of days to analyze
     * @param limit Maximum number of categories
     * @return List of trending categories with counts
     */
    List<Map<String, Object>> getTrendingCategories(int days, int limit);

    /**
     * Gets category accuracy statistics.
     * @return Accuracy metrics by category
     */
    Map<String, Object> getCategoryAccuracyStatistics();

    /**
     * Manually corrects AI categorization.
     * @param mediaId The media ID
     * @param correctCategory The correct category
     * @param feedback Feedback for improvement
     */
    void correctCategorization(Long mediaId, QueryCategory correctCategory, String feedback);

    /**
     * Gets AI processing costs and usage.
     * @return Cost and usage statistics
     */
    Map<String, Object> getAIProcessingCosts();

    /**
     * Optimizes media for AI processing.
     * @param mediaId The media ID
     * @return Optimization results
     */
    Map<String, Object> optimizeMediaForAI(Long mediaId);
}
