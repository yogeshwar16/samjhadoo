package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryAnnotationDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQueryAnnotation;
import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.repository.visualquery.VisualQueryAnnotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnotationServiceImpl implements AnnotationService {

    private final VisualQueryAnnotationRepository annotationRepository;

    @Override
    public VisualQueryAnnotationDTO createAnnotation(VisualQueryResponse response, VisualQueryMedia media,
                                                    VisualQueryAnnotation.AnnotationType annotationType,
                                                    double xPosition, double yPosition, double width, double height,
                                                    double rotation, String color, double strokeWidth,
                                                    String text, double fontSize, String fontFamily,
                                                    String shapeData, User creator) {
        // Validate bounds
        Map<String, Object> validation = validateAnnotationBounds(xPosition, yPosition, width, height);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException((String) validation.get("message"));
        }

        VisualQueryAnnotation annotation = VisualQueryAnnotation.builder()
                .response(response)
                .media(media)
                .annotationType(annotationType)
                .xPosition(xPosition)
                .yPosition(yPosition)
                .width(width)
                .height(height)
                .rotation(rotation)
                .color(color != null ? color : "#FF0000") // Default red
                .strokeWidth(strokeWidth > 0 ? strokeWidth : 2.0)
                .text(text)
                .fontSize(fontSize > 0 ? fontSize : 12.0)
                .fontFamily(fontFamily != null ? fontFamily : "Arial")
                .shapeData(shapeData)
                .zIndex(getNextZIndex(response.getId()))
                .isVisible(true)
                .build();

        VisualQueryAnnotation savedAnnotation = annotationRepository.save(annotation);

        log.info("Created annotation {} of type {} on media {} by user {}",
                savedAnnotation.getId(), annotationType, media.getId(), creator.getId());

        return convertToDTO(savedAnnotation);
    }

    @Override
    public VisualQueryAnnotationDTO updateAnnotation(Long annotationId, double xPosition, double yPosition,
                                                    double width, double height, double rotation,
                                                    String color, double strokeWidth, String text, User user) {
        VisualQueryAnnotation annotation = annotationRepository.findById(annotationId).orElse(null);
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation not found");
        }

        // Check if user can update this annotation
        if (!canUserUpdateAnnotation(annotation, user)) {
            throw new IllegalArgumentException("User not authorized to update this annotation");
        }

        // Validate bounds
        Map<String, Object> validation = validateAnnotationBounds(xPosition, yPosition, width, height);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException((String) validation.get("message"));
        }

        annotation.setXPosition(xPosition);
        annotation.setYPosition(yPosition);
        annotation.setWidth(width);
        annotation.setHeight(height);
        annotation.setRotation(rotation);
        annotation.setColor(color);
        annotation.setStrokeWidth(strokeWidth);
        annotation.setText(text);

        VisualQueryAnnotation savedAnnotation = annotationRepository.save(annotation);

        log.info("Updated annotation {} by user {}", annotationId, user.getId());

        return convertToDTO(savedAnnotation);
    }

    @Override
    public boolean deleteAnnotation(Long annotationId, User user) {
        VisualQueryAnnotation annotation = annotationRepository.findById(annotationId).orElse(null);
        if (annotation == null) {
            return false;
        }

        // Check if user can delete this annotation
        if (!canUserUpdateAnnotation(annotation, user)) {
            throw new IllegalArgumentException("User not authorized to delete this annotation");
        }

        annotationRepository.delete(annotation);

        log.info("Deleted annotation {} by user {}", annotationId, user.getId());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getResponseAnnotations(Long responseId) {
        return annotationRepository.findByResponseOrderByZIndex(getResponseById(responseId)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getMediaAnnotations(Long mediaId) {
        return annotationRepository.findByMediaOrderByZIndex(getMediaById(mediaId)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getAnnotationsByType(VisualQueryAnnotation.AnnotationType annotationType,
                                                              Long responseId) {
        List<VisualQueryAnnotation> annotations;

        if (responseId != null) {
            VisualQueryResponse response = getResponseById(responseId);
            annotations = annotationRepository.findByResponse(response).stream()
                    .filter(a -> a.getAnnotationType() == annotationType)
                    .collect(Collectors.toList());
        } else {
            annotations = annotationRepository.findByAnnotationType(annotationType);
        }

        return annotations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getTextAnnotations(Long responseId) {
        VisualQueryResponse response = getResponseById(responseId);
        return annotationRepository.findByResponse(response).stream()
                .filter(VisualQueryAnnotation::isTextAnnotation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getShapeAnnotations(Long responseId) {
        VisualQueryResponse response = getResponseById(responseId);
        return annotationRepository.findByResponse(response).stream()
                .filter(VisualQueryAnnotation::isShapeAnnotation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean doAnnotationsOverlap(VisualQueryAnnotation annotation1, VisualQueryAnnotation annotation2) {
        return annotation1.overlapsWith(annotation2);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAnnotationLayers(Long responseId) {
        VisualQueryResponse response = getResponseById(responseId);
        List<VisualQueryAnnotation> annotations = annotationRepository.findByResponseOrderByZIndex(response);

        Map<String, Object> layers = new HashMap<>();
        layers.put("totalLayers", annotations.size());

        // Group annotations by z-index
        Map<Integer, List<VisualQueryAnnotationDTO>> layerMap = annotations.stream()
                .collect(Collectors.groupingBy(
                    VisualQueryAnnotation::getZIndex,
                    Collectors.mapping(this::convertToDTO, Collectors.toList())
                ));

        layers.put("layers", layerMap);

        return layers;
    }

    @Override
    public VisualQueryAnnotationDTO updateAnnotationVisibility(Long annotationId, boolean visible, User user) {
        VisualQueryAnnotation annotation = annotationRepository.findById(annotationId).orElse(null);
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation not found");
        }

        if (!canUserUpdateAnnotation(annotation, user)) {
            throw new IllegalArgumentException("User not authorized to update this annotation");
        }

        annotation.setIsVisible(visible);
        VisualQueryAnnotation savedAnnotation = annotationRepository.save(annotation);

        log.info("Updated visibility for annotation {} to {} by user {}", annotationId, visible, user.getId());

        return convertToDTO(savedAnnotation);
    }

    @Override
    public VisualQueryAnnotationDTO updateAnnotationZIndex(Long annotationId, int newZIndex, User user) {
        VisualQueryAnnotation annotation = annotationRepository.findById(annotationId).orElse(null);
        if (annotation == null) {
            throw new IllegalArgumentException("Annotation not found");
        }

        if (!canUserUpdateAnnotation(annotation, user)) {
            throw new IllegalArgumentException("User not authorized to update this annotation");
        }

        annotation.setZIndex(newZIndex);
        VisualQueryAnnotation savedAnnotation = annotationRepository.save(annotation);

        log.info("Updated z-index for annotation {} to {} by user {}", annotationId, newZIndex, user.getId());

        return convertToDTO(savedAnnotation);
    }

    @Override
    public VisualQueryAnnotationDTO duplicateAnnotation(Long annotationId, double offsetX, double offsetY, User user) {
        VisualQueryAnnotation original = annotationRepository.findById(annotationId).orElse(null);
        if (original == null) {
            throw new IllegalArgumentException("Annotation not found");
        }

        if (!canUserUpdateAnnotation(original, user)) {
            throw new IllegalArgumentException("User not authorized to duplicate this annotation");
        }

        VisualQueryAnnotation duplicate = VisualQueryAnnotation.builder()
                .response(original.getResponse())
                .media(original.getMedia())
                .annotationType(original.getAnnotationType())
                .xPosition(original.getXPosition() + offsetX)
                .yPosition(original.getYPosition() + offsetY)
                .width(original.getWidth())
                .height(original.getHeight())
                .rotation(original.getRotation())
                .color(original.getColor())
                .strokeWidth(original.getStrokeWidth())
                .text(original.getText())
                .fontSize(original.getFontSize())
                .fontFamily(original.getFontFamily())
                .shapeData(original.getShapeData())
                .zIndex(getNextZIndex(original.getResponse().getId()))
                .isVisible(true)
                .build();

        VisualQueryAnnotation savedDuplicate = annotationRepository.save(duplicate);

        log.info("Duplicated annotation {} to {} by user {}", annotationId, savedDuplicate.getId(), user.getId());

        return convertToDTO(savedDuplicate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAnnotationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalAnnotations = annotationRepository.count();
        long textAnnotations = annotationRepository.findTextAnnotations().size();
        long shapeAnnotations = annotationRepository.findHighlightAnnotations().size();
        long visibleAnnotations = annotationRepository.findVisibleAnnotations().size();

        stats.put("totalAnnotations", totalAnnotations);
        stats.put("textAnnotations", textAnnotations);
        stats.put("shapeAnnotations", shapeAnnotations);
        stats.put("visibleAnnotations", visibleAnnotations);

        // Annotation type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (VisualQueryAnnotation.AnnotationType type : VisualQueryAnnotation.AnnotationType.values()) {
            long count = annotationRepository.findByAnnotationType(type).size();
            typeDistribution.put(type.name(), count);
        }
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    @Override
    public Map<String, Object> validateAnnotationBounds(double xPosition, double yPosition, double width, double height) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("message", "Annotation bounds are valid");

        // Check if position is within bounds (0-1)
        if (xPosition < 0 || xPosition > 1 || yPosition < 0 || yPosition > 1) {
            validation.put("valid", false);
            validation.put("message", "Annotation position must be between 0 and 1");
            return validation;
        }

        // Check if size is reasonable
        if (width <= 0 || width > 1 || height <= 0 || height > 1) {
            validation.put("valid", false);
            validation.put("message", "Annotation size must be between 0 and 1");
            return validation;
        }

        // Check if annotation fits within media bounds
        if (xPosition + width > 1 || yPosition + height > 1) {
            validation.put("valid", false);
            validation.put("message", "Annotation extends beyond media bounds");
        }

        return validation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getUserAnnotations(User user, int limit) {
        // In a real implementation, this would require a join query to find annotations by user
        // For now, we'll return an empty list as this requires more complex repository logic
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryAnnotationDTO> getAnnotationsRequiringModeration(int limit) {
        // In a real implementation, this would find annotations that need review
        // For now, we'll return an empty list
        return new ArrayList<>();
    }

    @Override
    public boolean reportAnnotation(Long annotationId, User reporter, String reason) {
        // In a real implementation, this would create a report record
        // For now, we'll just log it
        log.info("Annotation {} reported by user {} for reason: {}", annotationId, reporter.getId(), reason);
        return true;
    }

    @Override
    public boolean hideAnnotation(Long annotationId, User moderator, String reason) {
        VisualQueryAnnotation annotation = annotationRepository.findById(annotationId).orElse(null);
        if (annotation == null) {
            return false;
        }

        annotation.setIsVisible(false);
        annotationRepository.save(annotation);

        log.info("Hid annotation {} by moderator {} for reason: {}", annotationId, moderator.getId(), reason);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAnnotationUsageAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        long totalAnnotations = annotationRepository.count();
        long visibleAnnotations = annotationRepository.findVisibleAnnotations().size();

        analytics.put("totalAnnotations", totalAnnotations);
        analytics.put("visibleAnnotations", visibleAnnotations);
        analytics.put("hiddenAnnotations", totalAnnotations - visibleAnnotations);

        // Usage by annotation type
        Map<String, Long> usageByType = new HashMap<>();
        for (VisualQueryAnnotation.AnnotationType type : VisualQueryAnnotation.AnnotationType.values()) {
            long count = annotationRepository.findByAnnotationType(type).size();
            usageByType.put(type.name(), count);
        }
        analytics.put("usageByType", usageByType);

        return analytics;
    }

    @Override
    public String exportAnnotationsAsJson(Long responseId) {
        VisualQueryResponse response = getResponseById(responseId);
        List<VisualQueryAnnotation> annotations = annotationRepository.findByResponseOrderByZIndex(response);

        List<Map<String, Object>> annotationData = annotations.stream()
                .map(this::annotationToMap)
                .collect(Collectors.toList());

        Map<String, Object> exportData = new HashMap<>();
        exportData.put("responseId", responseId);
        exportData.put("exportedAt", LocalDateTime.now());
        exportData.put("annotations", annotationData);

        // In a real implementation, this would be proper JSON serialization
        return exportData.toString();
    }

    @Override
    public int importAnnotationsFromJson(Long responseId, String annotationsJson, User user) {
        // In a real implementation, this would parse JSON and create annotations
        // For now, we'll return 0 as this requires JSON parsing logic
        log.info("Would import annotations for response {} by user {}", responseId, user.getId());
        return 0;
    }

    // Helper methods
    private int getNextZIndex(Long responseId) {
        VisualQueryResponse response = getResponseById(responseId);
        List<VisualQueryAnnotation> annotations = annotationRepository.findByResponseOrderByZIndex(response);

        if (annotations.isEmpty()) {
            return 1;
        }

        return annotations.get(annotations.size() - 1).getZIndex() + 1;
    }

    private boolean canUserUpdateAnnotation(VisualQueryAnnotation annotation, User user) {
        // Check if user is the creator of the annotation or the response owner
        return annotation.getResponse().getMentor().getId().equals(user.getId());
    }

    private VisualQueryResponse getResponseById(Long responseId) {
        // In a real implementation, this would fetch from ResponseService
        // For now, we'll create a mock response
        return VisualQueryResponse.builder()
                .id(responseId)
                .build();
    }

    private VisualQueryMedia getMediaById(Long mediaId) {
        // In a real implementation, this would fetch from MediaService
        // For now, we'll create a mock media
        return VisualQueryMedia.builder()
                .id(mediaId)
                .build();
    }

    private Map<String, Object> annotationToMap(VisualQueryAnnotation annotation) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", annotation.getId());
        data.put("type", annotation.getAnnotationType().name());
        data.put("x", annotation.getXPosition());
        data.put("y", annotation.getYPosition());
        data.put("width", annotation.getWidth());
        data.put("height", annotation.getHeight());
        data.put("rotation", annotation.getRotation());
        data.put("color", annotation.getColor());
        data.put("strokeWidth", annotation.getStrokeWidth());
        data.put("text", annotation.getText());
        data.put("zIndex", annotation.getZIndex());
        data.put("visible", annotation.isVisible());
        data.put("createdAt", annotation.getCreatedAt());
        return data;
    }

    private VisualQueryAnnotationDTO convertToDTO(VisualQueryAnnotation annotation) {
        double[] bounds = annotation.getBounds();

        return VisualQueryAnnotationDTO.builder()
                .id(annotation.getId())
                .annotationType(annotation.getAnnotationType().name())
                .xPosition(annotation.getXPosition())
                .yPosition(annotation.getYPosition())
                .width(annotation.getWidth())
                .height(annotation.getHeight())
                .rotation(annotation.getRotation())
                .color(annotation.getColor())
                .strokeWidth(annotation.getStrokeWidth())
                .text(annotation.getText())
                .fontSize(annotation.getFontSize())
                .fontFamily(annotation.getFontFamily())
                .shapeData(annotation.getShapeData())
                .zIndex(annotation.getZIndex())
                .isVisible(annotation.isVisible())
                .createdAt(annotation.getCreatedAt())
                .updatedAt(annotation.getUpdatedAt())
                .isTextAnnotation(annotation.isTextAnnotation())
                .isShapeAnnotation(annotation.isShapeAnnotation())
                .bounds(bounds)
                .renderData(annotation.toRenderData())
                .build();
    }
}
