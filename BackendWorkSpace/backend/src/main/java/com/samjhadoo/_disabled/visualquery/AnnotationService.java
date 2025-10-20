package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryAnnotationDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQueryAnnotation;
import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.visualquery.VisualQueryResponse;

import java.util.List;
import java.util.Map;

/**
 * Service for managing annotations on visual query media.
 */
public interface AnnotationService {

    /**
     * Creates a new annotation on media.
     * @param response The response the annotation belongs to
     * @param media The media being annotated
     * @param annotationType The type of annotation
     * @param xPosition X position (0-1)
     * @param yPosition Y position (0-1)
     * @param width Width (0-1)
     * @param height Height (0-1)
     * @param rotation Rotation angle
     * @param color Hex color code
     * @param strokeWidth Stroke width
     * @param text Text content (for text annotations)
     * @param fontSize Font size
     * @param fontFamily Font family
     * @param shapeData JSON shape data
     * @param creator The user creating the annotation
     * @return The created annotation DTO
     */
    VisualQueryAnnotationDTO createAnnotation(VisualQueryResponse response, VisualQueryMedia media,
                                             VisualQueryAnnotation.AnnotationType annotationType,
                                             double xPosition, double yPosition, double width, double height,
                                             double rotation, String color, double strokeWidth,
                                             String text, double fontSize, String fontFamily,
                                             String shapeData, User creator);

    /**
     * Updates an existing annotation.
     * @param annotationId The annotation ID
     * @param xPosition New X position
     * @param yPosition New Y position
     * @param width New width
     * @param height New height
     * @param rotation New rotation
     * @param color New color
     * @param strokeWidth New stroke width
     * @param text New text
     * @param user The user updating
     * @return The updated annotation DTO
     */
    VisualQueryAnnotationDTO updateAnnotation(Long annotationId, double xPosition, double yPosition,
                                             double width, double height, double rotation,
                                             String color, double strokeWidth, String text, User user);

    /**
     * Deletes an annotation.
     * @param annotationId The annotation ID
     * @param user The user deleting
     * @return true if deleted successfully
     */
    boolean deleteAnnotation(Long annotationId, User user);

    /**
     * Gets annotations for a response.
     * @param responseId The response ID
     * @return List of annotation DTOs
     */
    List<VisualQueryAnnotationDTO> getResponseAnnotations(Long responseId);

    /**
     * Gets annotations for specific media.
     * @param mediaId The media ID
     * @return List of annotation DTOs
     */
    List<VisualQueryAnnotationDTO> getMediaAnnotations(Long mediaId);

    /**
     * Gets annotations by type.
     * @param annotationType The annotation type
     * @param responseId The response ID (optional)
     * @return List of annotation DTOs
     */
    List<VisualQueryAnnotationDTO> getAnnotationsByType(VisualQueryAnnotation.AnnotationType annotationType,
                                                       Long responseId);

    /**
     * Gets text annotations for a response.
     * @param responseId The response ID
     * @return List of text annotation DTOs
     */
    List<VisualQueryAnnotationDTO> getTextAnnotations(Long responseId);

    /**
     * Gets shape annotations for a response.
     * @param responseId The response ID
     * @return List of shape annotation DTOs
     */
    List<VisualQueryAnnotationDTO> getShapeAnnotations(Long responseId);

    /**
     * Checks if annotations overlap.
     * @param annotation1 First annotation
     * @param annotation2 Second annotation
     * @return true if they overlap
     */
    boolean doAnnotationsOverlap(VisualQueryAnnotation annotation1, VisualQueryAnnotation annotation2);

    /**
     * Gets annotation layer information for rendering.
     * @param responseId The response ID
     * @return Map of annotation layers and their data
     */
    Map<String, Object> getAnnotationLayers(Long responseId);

    /**
     * Updates annotation visibility.
     * @param annotationId The annotation ID
     * @param visible New visibility state
     * @param user The user updating
     * @return The updated annotation DTO
     */
    VisualQueryAnnotationDTO updateAnnotationVisibility(Long annotationId, boolean visible, User user);

    /**
     * Updates annotation z-index (layer order).
     * @param annotationId The annotation ID
     * @param newZIndex New z-index value
     * @param user The user updating
     * @return The updated annotation DTO
     */
    VisualQueryAnnotationDTO updateAnnotationZIndex(Long annotationId, int newZIndex, User user);

    /**
     * Duplicates an annotation.
     * @param annotationId The annotation ID to duplicate
     * @param offsetX X offset for the duplicate
     * @param offsetY Y offset for the duplicate
     * @param user The user duplicating
     * @return The new annotation DTO
     */
    VisualQueryAnnotationDTO duplicateAnnotation(Long annotationId, double offsetX, double offsetY, User user);

    /**
     * Gets annotation statistics.
     * @return Map of annotation statistics
     */
    Map<String, Object> getAnnotationStatistics();

    /**
     * Validates annotation bounds.
     * @param xPosition X position (0-1)
     * @param yPosition Y position (0-1)
     * @param width Width (0-1)
     * @param height Height (0-1)
     * @return Validation result map
     */
    Map<String, Object> validateAnnotationBounds(double xPosition, double yPosition, double width, double height);

    /**
     * Gets annotations created by a user.
     * @param user The user
     * @param limit Maximum number of annotations
     * @return List of annotation DTOs created by the user
     */
    List<VisualQueryAnnotationDTO> getUserAnnotations(User user, int limit);

    /**
     * Gets annotations requiring moderation.
     * @param limit Maximum number of annotations
     * @return List of annotation DTOs needing review
     */
    List<VisualQueryAnnotationDTO> getAnnotationsRequiringModeration(int limit);

    /**
     * Reports an inappropriate annotation.
     * @param annotationId The annotation ID
     * @param reporter The user reporting
     * @param reason Report reason
     * @return true if reported successfully
     */
    boolean reportAnnotation(Long annotationId, User reporter, String reason);

    /**
     * Hides an inappropriate annotation.
     * @param annotationId The annotation ID
     * @param moderator The moderator hiding it
     * @param reason Reason for hiding
     * @return true if hidden successfully
     */
    boolean hideAnnotation(Long annotationId, User moderator, String reason);

    /**
     * Gets annotation usage analytics.
     * @return Map of annotation usage statistics
     */
    Map<String, Object> getAnnotationUsageAnalytics();

    /**
     * Exports annotations for a response as JSON.
     * @param responseId The response ID
     * @return JSON string of all annotations
     */
    String exportAnnotationsAsJson(Long responseId);

    /**
     * Imports annotations from JSON.
     * @param responseId The response ID
     * @param annotationsJson JSON string of annotations
     * @param user The user importing
     * @return Number of annotations imported
     */
    int importAnnotationsFromJson(Long responseId, String annotationsJson, User user);
}
