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
public class VisualQueryResponseMediaDTO {
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
    private String description;
    private boolean isDemo;
    private boolean isSolutionMedia;
    private Integer stepNumber;
    private LocalDateTime createdAt;
    private String fileExtension;
    private boolean isImage;
    private boolean isVideo;
    private boolean isAudio;
    private boolean isDocument;
    private double aspectRatio;
    private boolean isPartOfStepByStep;
    private String displayDescription;
}
