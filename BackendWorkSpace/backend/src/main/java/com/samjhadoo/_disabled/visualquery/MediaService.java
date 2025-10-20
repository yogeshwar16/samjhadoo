package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryMediaDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.enums.visualquery.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service for managing visual query media (photos, videos, documents).
 */
public interface MediaService {

    /**
     * Uploads media for a visual query.
     * @param query The visual query
     * @param file The uploaded file
     * @param mediaType The media type
     * @param isPrimary Whether this is the primary media
     * @param description Media description
     * @param uploader The user uploading
     * @return The created media DTO
     */
    VisualQueryMediaDTO uploadMedia(VisualQuery query, MultipartFile file, MediaType mediaType,
                                   boolean isPrimary, String description, User uploader);

    /**
     * Processes uploaded media (compression, AI analysis, thumbnails).
     * @param mediaId The media ID
     * @return The processed media DTO
     */
    VisualQueryMediaDTO processMedia(Long mediaId);

    /**
     * Gets media for a query.
     * @param queryId The query ID
     * @return List of media DTOs
     */
    List<VisualQueryMediaDTO> getQueryMedia(String queryId);

    /**
     * Gets media by ID.
     * @param mediaId The media ID
     * @return The media DTO or null if not found
     */
    VisualQueryMediaDTO getMediaById(Long mediaId);

    /**
     * Deletes media.
     * @param mediaId The media ID
     * @param user The user deleting
     * @return true if deleted successfully
     */
    boolean deleteMedia(Long mediaId, User user);

    /**
     * Compresses media file.
     * @param mediaId The media ID
     * @return The compression result
     */
    Map<String, Object> compressMedia(Long mediaId);

    /**
     * Generates thumbnail for media.
     * @param mediaId The media ID
     * @return The thumbnail URL
     */
    String generateThumbnail(Long mediaId);

    /**
     * Performs AI analysis on media.
     * @param mediaId The media ID
     * @return AI analysis results
     */
    Map<String, Object> analyzeMedia(Long mediaId);

    /**
     * Suggests category based on AI analysis.
     * @param mediaId The media ID
     * @return Suggested category
     */
    com.samjhadoo.model.enums.visualquery.QueryCategory suggestCategory(Long mediaId);

    /**
     * Extracts tags from media using AI.
     * @param mediaId The media ID
     * @return List of extracted tags
     */
    List<String> extractTags(Long mediaId);

    /**
     * Gets media processing status.
     * @param mediaId The media ID
     * @return Processing status map
     */
    Map<String, Object> getMediaProcessingStatus(Long mediaId);

    /**
     * Validates uploaded file.
     * @param file The uploaded file
     * @param mediaType The expected media type
     * @return Validation result map
     */
    Map<String, Object> validateFile(MultipartFile file, MediaType mediaType);

    /**
     * Gets media statistics.
     * @return Map of media statistics
     */
    Map<String, Object> getMediaStatistics();

    /**
     * Gets media by processing status.
     * @param status The processing status
     * @param limit Maximum number of results
     * @return List of media DTOs with the specified status
     */
    List<VisualQueryMediaDTO> getMediaByProcessingStatus(String status, int limit);

    /**
     * Reprocesses failed media uploads.
     * @return Number of media reprocessed
     */
    int reprocessFailedMedia();

    /**
     * Cleans up orphaned media files.
     * @return Number of files cleaned up
     */
    int cleanupOrphanedMedia();

    /**
     * Gets media storage usage statistics.
     * @return Map of storage statistics
     */
    Map<String, Object> getStorageStatistics();

    /**
     * Updates media metadata.
     * @param mediaId The media ID
     * @param description New description
     * @param user The user updating
     * @return The updated media DTO
     */
    VisualQueryMediaDTO updateMediaMetadata(Long mediaId, String description, User user);

    /**
     * Checks if media belongs to a user's query.
     * @param mediaId The media ID
     * @param user The user
     * @return true if user owns the media
     */
    boolean userOwnsMedia(Long mediaId, User user);

    /**
     * Gets media requiring processing.
     * @param limit Maximum number of media to return
     * @return List of media DTOs needing processing
     */
    List<VisualQueryMediaDTO> getMediaRequiringProcessing(int limit);
}
