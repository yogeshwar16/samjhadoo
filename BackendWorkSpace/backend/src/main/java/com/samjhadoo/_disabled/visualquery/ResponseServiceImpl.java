package com.samjhadoo.service.visualquery;

import com.samjhadoo.dto.visualquery.VisualQueryResponseDTO;
import com.samjhadoo.dto.visualquery.VisualQueryResponseMediaDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.model.visualquery.VisualQueryResponseMedia;
import com.samjhadoo.model.enums.visualquery.MediaType;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.visualquery.VisualQueryResponse.ResponseType;
import com.samjhadoo.repository.visualquery.VisualQueryResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResponseServiceImpl implements ResponseService {

    private final VisualQueryResponseRepository responseRepository;
    private final MediaService mediaService;

    @Override
    public VisualQueryResponseDTO createResponse(VisualQuery query, User mentor, ResponseType responseType,
                                                String content, boolean isSolution, Integer estimatedTimeMinutes,
                                                String difficultyLevel, String requiredMaterials,
                                                BigDecimal costEstimate, String stepByStepGuide,
                                                String alternativeSolutions, String safetyPrecautions,
                                                String references) {
        VisualQueryResponse response = VisualQueryResponse.builder()
                .visualQuery(query)
                .mentor(mentor)
                .responseType(responseType)
                .content(content)
                .isSolution(isSolution)
                .estimatedTimeMinutes(estimatedTimeMinutes)
                .difficultyLevel(difficultyLevel)
                .requiredMaterials(requiredMaterials)
                .costEstimate(costEstimate)
                .stepByStepGuide(stepByStepGuide)
                .alternativeSolutions(alternativeSolutions)
                .safetyPrecautions(safetyPrecautions)
                .references(references)
                .build();

        VisualQueryResponse savedResponse = responseRepository.save(response);

        log.info("Created response {} for query {} by mentor {}", savedResponse.getId(), query.getQueryId(), mentor.getId());

        return convertToDTO(savedResponse);
    }

    @Override
    public VisualQueryResponseDTO updateResponse(Long responseId, User mentor, String content,
                                               boolean isSolution, Integer estimatedTimeMinutes,
                                               String difficultyLevel, String requiredMaterials,
                                               BigDecimal costEstimate, String stepByStepGuide,
                                               String alternativeSolutions, String safetyPrecautions,
                                               String references) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            throw new IllegalArgumentException("Response not found");
        }

        if (!response.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Only the original mentor can update this response");
        }

        response.setContent(content);
        response.setIsSolution(isSolution);
        response.setEstimatedTimeMinutes(estimatedTimeMinutes);
        response.setDifficultyLevel(difficultyLevel);
        response.setRequiredMaterials(requiredMaterials);
        response.setCostEstimate(costEstimate);
        response.setStepByStepGuide(stepByStepGuide);
        response.setAlternativeSolutions(alternativeSolutions);
        response.setSafetyPrecautions(safetyPrecautions);
        response.setReferences(references);

        VisualQueryResponse savedResponse = responseRepository.save(response);

        log.info("Updated response {} by mentor {}", responseId, mentor.getId());

        return convertToDTO(savedResponse);
    }

    @Override
    public boolean deleteResponse(Long responseId, User mentor) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            return false;
        }

        if (!response.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Only the original mentor can delete this response");
        }

        responseRepository.delete(response);

        log.info("Deleted response {} by mentor {}", responseId, mentor.getId());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryResponseDTO> getQueryResponses(String queryId) {
        VisualQuery query = new VisualQuery(); // Would fetch from repository
        query.setQueryId(queryId);

        return responseRepository.findByVisualQuery(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VisualQueryResponseDTO getResponseById(Long responseId) {
        return responseRepository.findById(responseId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryResponseDTO> getMentorResponses(User mentor, int limit) {
        return responseRepository.findByMentorOrderByCreated(mentor).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryResponseDTO> getHelpfulResponses(double minHelpfulnessRatio, int minVotes, int limit) {
        return responseRepository.findHighlyHelpfulResponses(minHelpfulnessRatio, minVotes).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VisualQueryResponseMediaDTO addResponseMedia(Long responseId, MultipartFile file, MediaType mediaType,
                                                       String description, boolean isDemo, boolean isSolutionMedia,
                                                       Integer stepNumber, User mentor) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            throw new IllegalArgumentException("Response not found");
        }

        if (!response.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Only the response mentor can add media");
        }

        // Create media entity for response
        VisualQueryResponseMedia responseMedia = VisualQueryResponseMedia.builder()
                .response(response)
                .mediaType(mediaType)
                .fileName(generateMediaFileName(file.getOriginalFilename(), mediaType))
                .originalFileName(file.getOriginalFilename())
                .filePath("responses/" + response.getId() + "/" + generateMediaFileName(file.getOriginalFilename(), mediaType))
                .fileSizeBytes(file.getSize())
                .mimeType(file.getContentType())
                .description(description)
                .isDemo(isDemo)
                .isSolutionMedia(isSolutionMedia)
                .stepNumber(stepNumber)
                .build();

        // In a real implementation, this would save to storage and process the media
        // For now, we'll just create the entity structure

        log.info("Added media to response {} by mentor {}", responseId, mentor.getId());

        return convertResponseMediaToDTO(responseMedia);
    }

    @Override
    public boolean removeResponseMedia(Long mediaId, User mentor) {
        // In a real implementation, this would find and delete the response media
        // For now, we'll return true as this requires more complex logic
        log.info("Would remove response media {} by mentor {}", mediaId, mentor.getId());
        return true;
    }

    @Override
    public boolean addHelpfulVote(Long responseId, User user) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            return false;
        }

        response.addHelpfulVote();
        responseRepository.save(response);

        log.info("Added helpful vote to response {} by user {}", responseId, user.getId());

        return true;
    }

    @Override
    public boolean addNotHelpfulVote(Long responseId, User user) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            return false;
        }

        response.addNotHelpfulVote();
        responseRepository.save(response);

        log.info("Added not helpful vote to response {} by user {}", responseId, user.getId());

        return true;
    }

    @Override
    public VisualQueryResponseDTO rateResponse(Long responseId, User user, int rating, String feedback) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            throw new IllegalArgumentException("Response not found");
        }

        response.setMentorRating(rating, feedback);
        VisualQueryResponse savedResponse = responseRepository.save(response);

        log.info("Rated response {} with {} stars by user {}: {}", responseId, rating, user.getId(), feedback);

        return convertToDTO(savedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getResponseStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalResponses = responseRepository.count();
        long solutionResponses = responseRepository.findSolutionResponses().size();
        long helpfulResponses = responseRepository.findHelpfulResponses().size();

        stats.put("totalResponses", totalResponses);
        stats.put("solutionResponses", solutionResponses);
        stats.put("helpfulResponses", helpfulResponses);

        // Response type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (ResponseType type : ResponseType.values()) {
            long count = responseRepository.findByResponseType(type).size();
            typeDistribution.put(type.name(), count);
        }
        stats.put("typeDistribution", typeDistribution);

        // Average ratings
        Double avgMentorRating = responseRepository.getAverageMentorRating();
        stats.put("averageMentorRating", avgMentorRating != null ? avgMentorRating : 0);

        Double avgHelpfulness = responseRepository.getAverageHelpfulnessRatio();
        stats.put("averageHelpfulnessRatio", avgHelpfulness != null ? avgHelpfulness : 0);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryResponseDTO> getResponsesRequiringModeration(int limit) {
        // In a real implementation, this would find responses that need review
        // For now, we'll return an empty list
        return new ArrayList<>();
    }

    @Override
    public boolean reportResponse(Long responseId, User reporter, String reason) {
        log.info("Response {} reported by user {} for reason: {}", responseId, reporter.getId(), reason);
        return true;
    }

    @Override
    public boolean hideResponse(Long responseId, User moderator, String reason) {
        // In a real implementation, this would mark the response as hidden
        log.info("Hid response {} by moderator {} for reason: {}", responseId, moderator.getId(), reason);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMentorPerformanceMetrics(User mentor) {
        Map<String, Object> metrics = new HashMap<>();

        List<VisualQueryResponse> responses = responseRepository.findByMentorOrderByCreated(mentor);

        long totalResponses = responses.size();
        long solutionResponses = responses.stream().filter(VisualQueryResponse::providesSolution).count();
        long helpfulResponses = responses.stream().filter(r -> r.getHelpfulnessRatio() >= 80).count();

        metrics.put("totalResponses", totalResponses);
        metrics.put("solutionResponses", solutionResponses);
        metrics.put("helpfulResponses", helpfulResponses);

        if (totalResponses > 0) {
            metrics.put("solutionRate", (double) solutionResponses / totalResponses * 100);
            metrics.put("helpfulnessRate", (double) helpfulResponses / totalResponses * 100);
        }

        // Average ratings
        double avgRating = responses.stream()
                .filter(r -> r.getMentorRating() > 0)
                .mapToInt(VisualQueryResponse::getMentorRating)
                .average()
                .orElse(0);

        metrics.put("averageRating", avgRating);

        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getResponseAnalyticsByCategory() {
        Map<String, Object> analytics = new HashMap<>();

        // Mock analytics by category
        Map<String, Integer> responseCountByCategory = new HashMap<>();
        Map<String, Double> avgRatingByCategory = new HashMap<>();

        for (QueryCategory category : QueryCategory.values()) {
            responseCountByCategory.put(category.name(), (int) (Math.random() * 50));
            avgRatingByCategory.put(category.name(), 3.5 + Math.random() * 1.5);
        }

        analytics.put("responseCountByCategory", responseCountByCategory);
        analytics.put("averageRatingByCategory", avgRatingByCategory);

        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAverageResponseTimeByCategory() {
        Map<String, Object> responseTimes = new HashMap<>();

        // Mock response times by category (in hours)
        Map<String, Double> avgResponseTime = new HashMap<>();
        for (QueryCategory category : QueryCategory.values()) {
            avgResponseTime.put(category.name(), 2.0 + Math.random() * 4.0); // 2-6 hours
        }

        responseTimes.put("averageResponseTimeByCategory", avgResponseTime);
        responseTimes.put("overallAverageResponseTime", 3.2);

        return responseTimes;
    }

    @Override
    public Map<String, Object> exportResponsesForAnalysis(LocalDateTime since, int limit) {
        Map<String, Object> export = new HashMap<>();

        export.put("exportedAt", LocalDateTime.now());
        export.put("since", since);
        export.put("limit", limit);
        export.put("responseCount", 450); // Mock count
        export.put("categories", Arrays.asList("AGRICULTURE", "HANDICRAFTS", "HOME_IMPROVEMENT"));

        log.info("Exported responses for analysis since {} (limit: {})", since, limit);

        return export;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrendingSolutionApproaches(QueryCategory category, int limit) {
        List<Map<String, Object>> approaches = new ArrayList<>();

        // Mock trending approaches
        Map<String, Object> approach1 = new HashMap<>();
        approach1.put("approach", "Organic Pest Control");
        approach1.put("category", "AGRICULTURE");
        approach1.put("usageCount", 25);
        approach1.put("effectiveness", 0.85);

        Map<String, Object> approach2 = new HashMap<>();
        approach2.put("approach", "Traditional Weaving Techniques");
        approach2.put("category", "HANDICRAFTS");
        approach2.put("usageCount", 18);
        approach2.put("effectiveness", 0.92);

        approaches.add(approach1);
        approaches.add(approach2);

        return approaches.stream()
                .filter(a -> category == null || category.name().equals(a.get("category")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getResponseQualityScore(Long responseId) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            return new HashMap<>();
        }

        Map<String, Object> quality = new HashMap<>();

        // Calculate quality score based on multiple factors
        int score = 0;

        // Content quality (length, completeness)
        if (response.getContent() != null && response.getContent().length() > 100) {
            score += 25;
        }

        // Solution completeness
        if (response.providesSolution()) {
            score += 30;
        }

        // Helpfulness based on votes
        if (response.getHelpfulnessRatio() >= 80) {
            score += 20;
        }

        // Mentor rating
        if (response.getMentorRating() >= 4) {
            score += 15;
        }

        // Media richness
        if (response.getResponseMedia() != null && !response.getResponseMedia().isEmpty()) {
            score += 10;
        }

        quality.put("score", Math.min(100, score));
        quality.put("grade", score >= 80 ? "A" : score >= 60 ? "B" : "C");
        quality.put("factors", Arrays.asList("content", "solution", "helpfulness", "rating", "media"));

        return quality;
    }

    @Override
    public VisualQueryResponseDTO updateResponseFormatting(Long responseId, User mentor, String formattedContent) {
        VisualQueryResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) {
            throw new IllegalArgumentException("Response not found");
        }

        if (!response.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Only the original mentor can update formatting");
        }

        response.setContent(formattedContent);
        VisualQueryResponse savedResponse = responseRepository.save(response);

        log.info("Updated formatting for response {} by mentor {}", responseId, mentor.getId());

        return convertToDTO(savedResponse);
    }

    @Override
    public VisualQueryResponseDTO duplicateResponse(Long sourceResponseId, String targetQueryId, User mentor) {
        VisualQueryResponse sourceResponse = responseRepository.findById(sourceResponseId).orElse(null);
        if (sourceResponse == null) {
            throw new IllegalArgumentException("Source response not found");
        }

        // In a real implementation, this would create a new response based on the source
        // For now, we'll create a simplified version
        VisualQueryResponse duplicatedResponse = VisualQueryResponse.builder()
                .visualQuery(sourceResponse.getVisualQuery())
                .mentor(mentor)
                .responseType(sourceResponse.getResponseType())
                .content(sourceResponse.getContent() + " (Duplicated)")
                .isSolution(sourceResponse.isSolution())
                .estimatedTimeMinutes(sourceResponse.getEstimatedTimeMinutes())
                .difficultyLevel(sourceResponse.getDifficultyLevel())
                .requiredMaterials(sourceResponse.getRequiredMaterials())
                .costEstimate(sourceResponse.getCostEstimate())
                .stepByStepGuide(sourceResponse.getStepByStepGuide())
                .alternativeSolutions(sourceResponse.getAlternativeSolutions())
                .safetyPrecautions(sourceResponse.getSafetyPrecautions())
                .references(sourceResponse.getReferences())
                .build();

        VisualQueryResponse savedResponse = responseRepository.save(duplicatedResponse);

        log.info("Duplicated response {} to query {} by mentor {}", sourceResponseId, targetQueryId, mentor.getId());

        return convertToDTO(savedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisualQueryResponseDTO> getResponseTemplates(User mentor, QueryCategory category) {
        // In a real implementation, this would fetch saved templates
        // For now, we'll return recent responses as templates
        return responseRepository.findByMentorOrderByCreated(mentor).stream()
                .filter(r -> category == null || r.getVisualQuery().getCategory() == category)
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveResponseAsTemplate(Long responseId, User mentor, String templateName, QueryCategory category) {
        log.info("Saved response {} as template '{}' in category {} by mentor {}",
                responseId, templateName, category, mentor.getId());

        // In a real implementation, this would save to a templates table
        return true;
    }

    // Helper methods
    private String generateMediaFileName(String originalName, MediaType mediaType) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        return mediaType.name().toLowerCase() + "_" + System.currentTimeMillis() + extension;
    }

    private VisualQueryResponseDTO convertToDTO(VisualQueryResponse response) {
        List<VisualQueryResponseMediaDTO> mediaDTOs = response.getResponseMedia() != null ?
                response.getResponseMedia().stream()
                        .map(this::convertResponseMediaToDTO)
                        .collect(Collectors.toList()) : new ArrayList<>();

        return VisualQueryResponseDTO.builder()
                .id(response.getId())
                .mentorName(response.getMentor().getFirstName() + " " + response.getMentor().getLastName())
                .responseType(response.getResponseType().name())
                .content(response.getContent())
                .isSolution(response.isSolution())
                .estimatedTimeMinutes(response.getEstimatedTimeMinutes())
                .difficultyLevel(response.getDifficultyLevel())
                .requiredMaterials(response.getRequiredMaterials())
                .costEstimate(response.getCostEstimate())
                .stepByStepGuide(response.getStepByStepGuide())
                .alternativeSolutions(response.getAlternativeSolutions())
                .safetyPrecautions(response.getSafetyPrecautions())
                .references(response.getReferences())
                .annotations(new ArrayList<>()) // Would need AnnotationService
                .responseMedia(mediaDTOs)
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .isHelpful(response.isHelpful())
                .helpfulVotes(response.getHelpfulVotes())
                .notHelpfulVotes(response.getNotHelpfulVotes())
                .mentorRating(response.getMentorRating())
                .mentorFeedback(response.getMentorFeedback())
                .totalVotes(response.getTotalVotes())
                .helpfulnessRatio(response.getHelpfulnessRatio())
                .isHighlyHelpful(response.isHighlyHelpful())
                .providesSolution(response.providesSolution())
                .complexityScore(response.getComplexityScore())
                .build();
    }

    private VisualQueryResponseMediaDTO convertResponseMediaToDTO(VisualQueryResponseMedia media) {
        return VisualQueryResponseMediaDTO.builder()
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
                .description(media.getDescription())
                .isDemo(media.isDemo())
                .isSolutionMedia(media.isSolutionMedia())
                .stepNumber(media.getStepNumber())
                .createdAt(media.getCreatedAt())
                .fileExtension(media.getFileExtension())
                .isImage(media.isImage())
                .isVideo(media.isVideo())
                .isAudio(media.isAudio())
                .isDocument(media.isDocument())
                .aspectRatio(media.getAspectRatio())
                .isPartOfStepByStep(media.isPartOfStepByStep())
                .displayDescription(media.getDisplayDescription())
                .build();
    }
}
