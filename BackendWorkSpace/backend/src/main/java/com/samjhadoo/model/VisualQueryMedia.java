package com.samjhadoo.model.visualquery;

import com.samjhadoo.model.enums.visualquery.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents media attachments for visual queries (photos, videos, documents).
 */
@Entity
@Table(name = "visual_query_media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id", nullable = false)
    private VisualQuery visualQuery;

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

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary; // Is this the primary media for the query

    @Lob
    @Column
    private String description; // Optional description/caption

    @Column(name = "upload_status", nullable = false)
    private String uploadStatus; // PENDING, UPLOADING, COMPLETED, FAILED

    @Column(name = "compression_applied", nullable = false)
    private boolean compressionApplied;

    @Column(name = "original_size_bytes")
    private Long originalSizeBytes; // Size before compression

    @Column(name = "compressed_size_bytes")
    private Long compressedSizeBytes; // Size after compression

    @Lob
    @Column(name = "ai_analysis_result")
    private String aiAnalysisResult; // JSON result from AI analysis

    @Column(name = "ai_category_suggestion")
    private String aiCategorySuggestion; // AI-suggested category

    @Column(name = "ai_tags")
    private String aiTags; // AI-extracted tags

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (uploadStatus == null) {
            uploadStatus = "PENDING";
        }
        if (!compressionApplied) {
            compressionApplied = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Mark as processed when AI analysis is complete
        if (aiAnalysisResult != null && processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the upload as completed.
     */
    public void markUploadCompleted() {
        this.uploadStatus = "COMPLETED";
    }

    /**
     * Marks the upload as failed.
     */
    public void markUploadFailed() {
        this.uploadStatus = "FAILED";
    }

    /**
     * Marks the upload as in progress.
     */
    public void markUploadInProgress() {
        this.uploadStatus = "UPLOADING";
    }

    /**
     * Records compression details.
     * @param originalSize Original file size
     * @param compressedSize Compressed file size
     */
    public void recordCompression(Long originalSize, Long compressedSize) {
        this.originalSizeBytes = originalSize;
        this.compressedSizeBytes = compressedSize;
        this.compressionApplied = true;
    }

    /**
     * Records AI analysis results.
     * @param analysisResult JSON analysis result
     * @param categorySuggestion AI category suggestion
     * @param tags AI-extracted tags
     */
    public void recordAIAnalysis(String analysisResult, String categorySuggestion, String tags) {
        this.aiAnalysisResult = analysisResult;
        this.aiCategorySuggestion = categorySuggestion;
        this.aiTags = tags;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Gets the compression ratio as a percentage.
     * @return Compression ratio (0-100), or 0 if not compressed
     */
    public double getCompressionRatio() {
        if (originalSizeBytes != null && compressedSizeBytes != null && originalSizeBytes > 0) {
            return ((double) (originalSizeBytes - compressedSizeBytes) / originalSizeBytes) * 100;
        }
        return 0;
    }

    /**
     * Checks if the media has been processed by AI.
     * @return true if AI analysis is complete
     */
    public boolean isProcessed() {
        return aiAnalysisResult != null && processedAt != null;
    }

    /**
     * Checks if the upload is complete.
     * @return true if upload is completed
     */
    public boolean isUploadComplete() {
        return "COMPLETED".equals(uploadStatus);
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
     * Sets whether this media is primary for the query.
     * @param isPrimary true if this should be the primary media
     */
    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    /**
     * Gets the aspect ratio of the media (width/height).
     * @return aspect ratio as double, or 0 if dimensions are not available
     */
    public double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return 0.0;
    }
}
