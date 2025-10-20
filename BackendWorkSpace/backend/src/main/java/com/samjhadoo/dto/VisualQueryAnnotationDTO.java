package com.samjhadoo.dto.visualquery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryAnnotationDTO {
    private Long id;
    private String annotationType;
    private double xPosition;
    private double yPosition;
    private double width;
    private double height;
    private double rotation;
    private String color;
    private double strokeWidth;
    private String text;
    private double fontSize;
    private String fontFamily;
    private String shapeData;
    private int zIndex;
    private boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isTextAnnotation;
    private boolean isShapeAnnotation;
    private double[] bounds;
    private Map<String, Object> renderData;
}
