package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryMediaDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.enums.visualquery.MediaType;
import com.samjhadoo.repository.visualquery.VisualQueryMediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final VisualQueryMediaRepository mediaRepository;
    private final CategorizationService categorizationService;

    @Override
    public VisualQueryMediaDTO uploadMedia(VisualQuery query, MultipartFile file, MediaType mediaType,
                                          boolean isPrimary, String description, User uploader) {
        try {
            // Validate file
            Map<String, Object> validation = validateFile(file, mediaType);
            if (!(Boolean) validation.get("valid")) {
                throw new IllegalArgumentException((String) validation.get("message"));
            }

            // Generate unique file name and path
            String fileName = generateUniqueFileName(file.getOriginalFilename(), mediaType);
            String filePath = generateFilePath(query.getQueryId(), fileName);

            // Create media entity
            VisualQueryMedia media = VisualQueryMedia.builder()
                    .visualQuery(query)
                    .mediaType(mediaType)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSizeBytes(file.getSize())
                    .mimeType(file.getContentType())
                    .isPrimary(isPrimary)
                    .description(description)
                    .uploadStatus("PENDING")
                    .build();

            // Set primary media logic
            if (isPrimary) {
                // Unset other primary media for this query
                query.getMediaAttachments().forEach(m -> {
                    if (m.isPrimary()) {
                        m.setIsPrimary(false);
                        mediaRepository.save(m);
                    }
                });
            }

            VisualQueryMedia savedMedia = mediaRepository.save(media);

            // TODO: Actually save file to storage system
            // For now, we'll simulate the upload
            simulateFileUpload(savedMedia, file);

            log.info("Uploaded media {} for query {} by user {}", savedMedia.getId(), query.getQueryId(), uploader.getId());

            return convertToDTO(savedMedia);

        } catch (Exception e) {
            log.error("Error uploading media for query {}: {}", query.getQueryId(), e.getMessage());
            throw new RuntimeException("Failed to upload media", e);
        }
    }

    @Override
    public VisualQueryMediaDTO processMedia(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            throw new IllegalArgumentException("Media not found");
        }

        try {
            // Compress media if needed
            Map<String, Object> compressionResult = compressMedia(mediaId);
            boolean wasCompressed = (Boolean) compressionResult.get("compressed");

            // Generate thumbnail for images/videos
            String thumbnailUrl = generateThumbnail(mediaId);

            // Perform AI analysis
            Map<String, Object> analysisResult = analyzeMedia(mediaId);
            String suggestedCategory = (String) analysisResult.get("category");
            List<String> tags = (List<String>) analysisResult.get("tags");

            // Update media with processing results
            if (wasCompressed) {
                Long originalSize = (Long) compressionResult.get("originalSize");
                Long compressedSize = (Long) compressionResult.get("compressedSize");
                media.recordCompression(originalSize, compressedSize);
            }

            if (suggestedCategory != null) {
                media.recordAIAnalysis(
                    analysisResult.toString(),
                    suggestedCategory,
                    tags != null ? String.join(",", tags) : null
                );
            }

            media.markUploadCompleted();
            VisualQueryMedia savedMedia = mediaRepository.save(media);

            log.info("Processed media {}: compressed={}, category={}", mediaId, wasCompressed, suggestedCategory);

            return convertToDTO(savedMedia);

        } catch (Exception e) {
            log.error("Error processing media {}: {}", mediaId, e.getMessage());
            media.markUploadFailed();
            mediaRepository.save(media);
            throw new RuntimeException("Failed to process media", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryMediaDTO> getQueryMedia(String queryId) {
        VisualQuery query = new VisualQuery(); // Would need to fetch from repository
        query.setQueryId(queryId);

        return mediaRepository.findByVisualQuery(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VisualQueryMediaDTO getMediaById(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public boolean deleteMedia(Long mediaId, User user) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return false;
        }

        // Check if user owns the query
        if (!media.getVisualQuery().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User not authorized to delete this media");
        }

        // TODO: Delete file from storage
        simulateFileDeletion(media);

        mediaRepository.delete(media);

        log.info("Deleted media {} by user {}", mediaId, user.getId());

        return true;
    }

    @Override
    public Map<String, Object> compressMedia(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            throw new IllegalArgumentException("Media not found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("compressed", false);

        // Simple compression logic based on file type and size
        if (media.getFileSizeBytes() > 1024 * 1024) { // Files larger than 1MB
            long originalSize = media.getFileSizeBytes();
            long compressedSize = (long) (originalSize * 0.8); // Simulate 20% compression

            media.recordCompression(originalSize, compressedSize);
            mediaRepository.save(media);

            result.put("compressed", true);
            result.put("originalSize", originalSize);
            result.put("compressedSize", compressedSize);
            result.put("compressionRatio", media.getCompressionRatio());

            log.info("Compressed media {} from {} to {} bytes", mediaId, originalSize, compressedSize);
        }

        return result;
    }

    @Override
    public String generateThumbnail(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            throw new IllegalArgumentException("Media not found");
        }

        if (media.getMediaType() == MediaType.IMAGE || media.getMediaType() == MediaType.VIDEO) {
            String thumbnailPath = generateThumbnailPath(media.getFilePath());
            String thumbnailUrl = generateThumbnailUrl(media.getFileUrl());

            media.setThumbnailPath(thumbnailPath);
            media.setThumbnailUrl(thumbnailUrl);
            mediaRepository.save(media);

            log.info("Generated thumbnail for media {}: {}", mediaId, thumbnailUrl);

            return thumbnailUrl;
        }

        return null;
    }

    @Override
    public Map<String, Object> analyzeMedia(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            throw new IllegalArgumentException("Media not found");
        }

        Map<String, Object> analysis = new HashMap<>();

        try {
            // Use categorization service for AI analysis
            if (media.getMediaType() == MediaType.IMAGE) {
                Map<String, Object> imageAnalysis = categorizationService.analyzeImageContent(mediaId);
                analysis.putAll(imageAnalysis);
            } else if (media.getMediaType() == MediaType.VIDEO) {
                Map<String, Object> videoAnalysis = categorizationService.analyzeVideoContent(mediaId);
                analysis.putAll(videoAnalysis);
            } else if (media.getMediaType() == MediaType.DOCUMENT) {
                Map<String, Object> docAnalysis = categorizationService.analyzeDocumentContent(mediaId);
                analysis.putAll(docAnalysis);
            }

            // Get safety analysis
            Map<String, Object> safetyAnalysis = categorizationService.analyzeContentSafety(mediaId);
            analysis.put("safety", safetyAnalysis);

            log.info("Analyzed media {} with AI", mediaId);

        } catch (Exception e) {
            log.error("Error analyzing media {}: {}", mediaId, e.getMessage());
            analysis.put("error", e.getMessage());
        }

        return analysis;
    }

    @Override
    public com.samjhadoo.model.enums.visualquery.QueryCategory suggestCategory(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return null;
        }

        Map<String, Object> analysis = categorizationService.suggestCategoryFromMedia(media);
        String suggestedCategory = (String) analysis.get("category");

        if (suggestedCategory != null) {
            try {
                return com.samjhadoo.model.enums.visualquery.QueryCategory.valueOf(suggestedCategory);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid suggested category {} for media {}", suggestedCategory, mediaId);
            }
        }

        return null;
    }

    @Override
    public List<String> extractTags(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return new ArrayList<>();
        }

        return categorizationService.extractTagsFromMedia(media);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMediaProcessingStatus(Long mediaId) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return new HashMap<>();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("uploadStatus", media.getUploadStatus());
        status.put("compressionApplied", media.isCompressionApplied());
        status.put("compressionRatio", media.getCompressionRatio());
        status.put("isProcessed", media.isProcessed());
        status.put("aiAnalysisResult", media.getAiAnalysisResult());
        status.put("categorySuggestion", media.getAiCategorySuggestion());
        status.put("tags", media.getAiTags());

        return status;
    }

    @Override
    public Map<String, Object> validateFile(MultipartFile file, MediaType mediaType) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("message", "File is valid");

        // Check file size (max 50MB)
        long maxSize = 50 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            validation.put("valid", false);
            validation.put("message", "File size exceeds maximum allowed size of 50MB");
            return validation;
        }

        // Check file type based on media type
        String contentType = file.getContentType();
        if (contentType == null) {
            validation.put("valid", false);
            validation.put("message", "Cannot determine file type");
            return validation;
        }

        boolean validType = switch (mediaType) {
            case IMAGE -> contentType.startsWith("image/");
            case VIDEO -> contentType.startsWith("video/");
            case AUDIO -> contentType.startsWith("audio/");
            case DOCUMENT -> contentType.equals("application/pdf") ||
                           contentType.startsWith("application/msword") ||
                           contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                           contentType.startsWith("text/");
        };

        if (!validType) {
            validation.put("valid", false);
            validation.put("message", "File type does not match expected media type");
        }

        return validation;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMediaStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalMedia = mediaRepository.count();
        long uploadedMedia = mediaRepository.findUploadedMedia().size();
        long compressedMedia = mediaRepository.findCompressedMedia().size();
        long analyzedMedia = mediaRepository.findAnalyzedMedia().size();

        stats.put("totalMedia", totalMedia);
        stats.put("uploadedMedia", uploadedMedia);
        stats.put("compressedMedia", compressedMedia);
        stats.put("analyzedMedia", analyzedMedia);

        // Media type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (MediaType type : MediaType.values()) {
            long count = mediaRepository.findByMediaType(type).size();
            typeDistribution.put(type.name(), count);
        }
        stats.put("typeDistribution", typeDistribution);

        // Average compression ratio
        Double avgCompression = mediaRepository.getAverageCompressionRatio();
        stats.put("averageCompressionRatio", avgCompression != null ? avgCompression : 0);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryMediaDTO> getMediaByProcessingStatus(String status, int limit) {
        return mediaRepository.findAll().stream()
                .filter(m -> status.equals(m.getUploadStatus()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int reprocessFailedMedia() {
        List<VisualQueryMedia> failedMedia = mediaRepository.findFailedUploads();
        int reprocessed = 0;

        for (VisualQueryMedia media : failedMedia) {
            try {
                processMedia(media.getId());
                reprocessed++;
            } catch (Exception e) {
                log.error("Failed to reprocess media {}: {}", media.getId(), e.getMessage());
            }
        }

        if (reprocessed > 0) {
            log.info("Reprocessed {} failed media uploads", reprocessed);
        }

        return reprocessed;
    }

    @Override
    public int cleanupOrphanedMedia() {
        // In a real implementation, this would check for media not associated with any query
        // For now, we'll return 0 as this requires more complex logic
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Calculate total storage used
        Long totalSize = mediaRepository.findUploadedMedia().stream()
                .mapToLong(VisualQueryMedia::getFileSizeBytes)
                .sum();

        stats.put("totalStorageBytes", totalSize);
        stats.put("totalStorageMB", totalSize / (1024.0 * 1024.0));

        // Storage by media type
        Map<String, Long> storageByType = new HashMap<>();
        for (MediaType type : MediaType.values()) {
            long typeSize = mediaRepository.findByMediaType(type).stream()
                    .mapToLong(m -> m.getFileSizeBytes() != null ? m.getFileSizeBytes() : 0)
                    .sum();
            storageByType.put(type.name(), typeSize);
        }
        stats.put("storageByType", storageByType);

        return stats;
    }

    @Override
    public VisualQueryMediaDTO updateMediaMetadata(Long mediaId, String description, User user) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            throw new IllegalArgumentException("Media not found");
        }

        // Check if user owns the query
        if (!media.getVisualQuery().getUser().getId().equals(user.getId()) &&
            (media.getVisualQuery().getAssignedMentor() == null ||
             !media.getVisualQuery().getAssignedMentor().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("User not authorized to update this media");
        }

        media.setDescription(description);
        VisualQueryMedia savedMedia = mediaRepository.save(media);

        log.info("Updated metadata for media {} by user {}", mediaId, user.getId());

        return convertToDTO(savedMedia);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userOwnsMedia(Long mediaId, User user) {
        VisualQueryMedia media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            return false;
        }

        return media.getVisualQuery().getUser().getId().equals(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryMediaDTO> getMediaRequiringProcessing(int limit) {
        return mediaRepository.findAll().stream()
                .filter(m -> "COMPLETED".equals(m.getUploadStatus()) && !m.isProcessed())
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private String generateUniqueFileName(String originalName, MediaType mediaType) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        return mediaType.name().toLowerCase() + "_" + timestamp + extension;
    }

    private String generateFilePath(String queryId, String fileName) {
        return "queries/" + queryId + "/" + fileName;
    }

    private String generateThumbnailPath(String filePath) {
        return filePath.replace(".", "_thumb.");
    }

    private String generateThumbnailUrl(String fileUrl) {
        if (fileUrl != null) {
            return fileUrl.replace(".", "_thumb.");
        }
        return null;
    }

    private void simulateFileUpload(VisualQueryMedia media, MultipartFile file) throws IOException {
        // In a real implementation, this would save the file to cloud storage (S3, etc.)
        // For now, we'll just mark as completed
        media.markUploadCompleted();

        // Simulate processing delay
        try {
            Thread.sleep(100); // 100ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateFileDeletion(VisualQueryMedia media) {
        // In a real implementation, this would delete from cloud storage
        log.info("Would delete file from storage: {}", media.getFilePath());
    }

    private VisualQueryMediaDTO convertToDTO(VisualQueryMedia media) {
        return VisualQueryMediaDTO.builder()
                .id(media.getId())
                .mediaType(media.getMediaType())
                .fileName(media.getFileName())
                .originalFileName(media.getOriginalFileName())
                .filePath(media.getFilePath())
                .fileUrl(media.getFileUrl())
                .fileSizeBytes(media.getFileSizeBytes())
                .mimeType(media.getMimeType())
                .width(media.getWidth())
                .height(media.getHeight())
                .durationSeconds(media.getDurationSeconds())
                .thumbnailPath(media.getThumbnailPath())
                .thumbnailUrl(media.getThumbnailUrl())
                .isPrimary(media.isPrimary())
                .description(media.getDescription())
                .uploadStatus(media.getUploadStatus())
                .compressionApplied(media.isCompressionApplied())
                .originalSizeBytes(media.getOriginalSizeBytes())
                .compressedSizeBytes(media.getCompressedSizeBytes())
                .aiAnalysisResult(media.getAiAnalysisResult())
                .aiCategorySuggestion(media.getAiCategorySuggestion())
                .aiTags(media.getAiTags())
                .createdAt(media.getCreatedAt())
                .processedAt(media.getProcessedAt())
                .compressionRatio(media.getCompressionRatio())
                .isProcessed(media.isProcessed())
                .isUploadComplete(media.isUploadComplete())
                .fileExtension(media.getFileExtension())
                .aspectRatio(media.getAspectRatio())
                .build();
    }
}
