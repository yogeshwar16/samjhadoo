package com.samjhadoo.model.visualquery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents annotations on visual query media (drawings, highlights, comments).
 */
@Entity
@Table(name = "visual_query_annotations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryAnnotation {

    public enum AnnotationType {
        DRAWING,        // Freehand drawing
        ARROW,          // Arrow pointing to area
        CIRCLE,         // Circle highlighting area
        RECTANGLE,      // Rectangle highlighting area
        TEXT,           // Text annotation
        HIGHLIGHT,      // Highlighted area
        MEASUREMENT,    // Measurement annotation
        LABEL           // Label with text
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private VisualQueryResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private VisualQueryMedia media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnotationType annotationType;

    @Column(name = "x_position", nullable = false)
    private double xPosition; // X coordinate (0-1, percentage of image width)

    @Column(name = "y_position", nullable = false)
    private double yPosition; // Y coordinate (0-1, percentage of image height)

    @Column(name = "width")
    private double width; // Width of annotation (0-1, percentage)

    @Column(name = "height")
    private double height; // Height of annotation (0-1, percentage)

    @Column(name = "rotation")
    private double rotation; // Rotation angle in degrees

    @Column(name = "color")
    private String color; // Hex color code

    @Column(name = "stroke_width")
    private double strokeWidth; // Line thickness

    @Lob
    @Column
    private String text; // Text content for text annotations

    @Column(name = "font_size")
    private double fontSize; // Font size for text

    @Column(name = "font_family")
    private String fontFamily; // Font family for text

    @Lob
    @Column(name = "shape_data")
    private String shapeData; // JSON data for complex shapes

    @Column(name = "z_index", nullable = false)
    private int zIndex; // Layer order for overlapping annotations

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (zIndex == 0) {
            zIndex = 1; // Default z-index
        }
        if (isVisible) {
            isVisible = true; // Default to visible
        }
        if (strokeWidth == 0) {
            strokeWidth = 2.0; // Default stroke width
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the annotation is a text annotation.
     * @return true if annotation type is TEXT or LABEL
     */
    public boolean isTextAnnotation() {
        return annotationType == AnnotationType.TEXT || annotationType == AnnotationType.LABEL;
    }

    /**
     * Checks if the annotation is a shape annotation.
     * @return true if annotation type is DRAWING, ARROW, CIRCLE, RECTANGLE, HIGHLIGHT, MEASUREMENT
     */
    public boolean isShapeAnnotation() {
        return annotationType == AnnotationType.DRAWING ||
               annotationType == AnnotationType.ARROW ||
               annotationType == AnnotationType.CIRCLE ||
               annotationType == AnnotationType.RECTANGLE ||
               annotationType == AnnotationType.HIGHLIGHT ||
               annotationType == AnnotationType.MEASUREMENT;
    }

    /**
     * Gets the annotation bounds as a rectangle.
     * @return Array of [x, y, width, height] in percentage coordinates
     */
    public double[] getBounds() {
        return new double[]{xPosition, yPosition, width, height};
    }

    /**
     * Sets the annotation position and size.
     * @param x X position (0-1)
     * @param y Y position (0-1)
     * @param w Width (0-1)
     * @param h Height (0-1)
     */
    public void setBounds(double x, double y, double w, double h) {
        this.xPosition = x;
        this.yPosition = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Checks if this annotation overlaps with another annotation.
     * @param other The other annotation
     * @return true if they overlap
     */
    public boolean overlapsWith(VisualQueryAnnotation other) {
        // Simple rectangle overlap detection
        return !(xPosition + width < other.xPosition ||
                 other.xPosition + other.width < xPosition ||
                 yPosition + height < other.yPosition ||
                 other.yPosition + other.height < yPosition);
    }

    /**
     * Gets the annotation as a simple data structure for frontend rendering.
     * @return Map representation of the annotation
     */
    public java.util.Map<String, Object> toRenderData() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", id);
        data.put("type", annotationType.name());
        data.put("x", xPosition);
        data.put("y", yPosition);
        data.put("width", width);
        data.put("height", height);
        data.put("rotation", rotation);
        data.put("color", color);
        data.put("strokeWidth", strokeWidth);
        data.put("text", text);
        data.put("fontSize", fontSize);
        data.put("fontFamily", fontFamily);
        data.put("shapeData", shapeData);
        data.put("zIndex", zIndex);
        data.put("visible", isVisible);
        return data;
    }
}
