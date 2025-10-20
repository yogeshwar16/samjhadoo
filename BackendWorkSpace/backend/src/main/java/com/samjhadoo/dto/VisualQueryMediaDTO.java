package com.samjhadoo.dto.visualquery;

import com.samjhadoo.model.enums.visualquery.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryMediaDTO {
    private Long id;
    private MediaType mediaType;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String fileUrl;
    private Long fileSizeBytes;
    private String mimeType;
    private Integer width;
    private Integer height;
    private Integer durationSeconds;
    private String thumbnailPath;
    private String thumbnailUrl;
    private boolean isPrimary;
    private String description;
    private String uploadStatus;
    private boolean compressionApplied;
    private Long originalSizeBytes;
    private Long compressedSizeBytes;
    private String aiAnalysisResult;
    private String aiCategorySuggestion;
    private String aiTags;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private double compressionRatio;
    private boolean isProcessed;
    private boolean isUploadComplete;
    private String fileExtension;
    private double aspectRatio;
}
