package com.samjhadoo.model.visualquery;

import com.samjhadoo.model.enums.visualquery.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents media attachments in visual query responses (diagrams, videos, etc.).
 */
@Entity
@Table(name = "visual_query_response_media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryResponseMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private VisualQueryResponse response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String fileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath; // Path in storage system

    @Column(name = "file_url")
    private String fileUrl; // Public URL for access

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "width")
    private Integer width; // For images/videos

    @Column(name = "height")
    private Integer height; // For images/videos

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // For videos/audio

    @Column(name = "thumbnail_path")
    private String thumbnailPath; // Path to thumbnail image

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // Public URL for thumbnail

    @Lob
    @Column
    private String description; // Description of what this media shows

    @Column(name = "is_demo", nullable = false)
    private boolean isDemo; // Whether this is a demonstration/example

    @Column(name = "is_solution_media", nullable = false)
    private boolean isSolutionMedia; // Whether this media shows the solution

    @Column(name = "step_number")
    private Integer stepNumber; // For step-by-step guides

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (!isDemo) {
            isDemo = false; // Default to not demo
        }
        if (!isSolutionMedia) {
            isSolutionMedia = false; // Default to not solution media
        }
    }

    /**
     * Gets the file extension from the filename.
     * @return File extension (without dot)
     */
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Checks if this is an image file.
     * @return true if media type is IMAGE
     */
    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    /**
     * Checks if this is a video file.
     * @return true if media type is VIDEO
     */
    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    /**
     * Checks if this is an audio file.
     * @return true if media type is AUDIO
     */
    public boolean isAudio() {
        return mediaType == MediaType.AUDIO;
    }

    /**
     * Checks if this is a document file.
     * @return true if media type is DOCUMENT
     */
    public boolean isDocument() {
        return mediaType == MediaType.DOCUMENT;
    }

    /**
     * Gets the aspect ratio of the media (for images/videos).
     * @return Aspect ratio (width/height), or 0 if dimensions not available
     */
    public double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return 0;
    }

    /**
     * Checks if this media is part of a step-by-step solution.
     * @return true if step number is set
     */
    public boolean isPartOfStepByStep() {
        return stepNumber != null && stepNumber > 0;
    }

    /**
     * Gets a display-friendly description of the media.
     * @return Description or default based on type
     */
    public String getDisplayDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }

        if (isSolutionMedia) {
            return "Solution demonstration";
        }

        if (isDemo) {
            return "Example demonstration";
        }

        return switch (mediaType) {
            case IMAGE -> "Reference image";
            case VIDEO -> "Instructional video";
            case AUDIO -> "Audio explanation";
            case DOCUMENT -> "Supporting document";
        };
    }
}
