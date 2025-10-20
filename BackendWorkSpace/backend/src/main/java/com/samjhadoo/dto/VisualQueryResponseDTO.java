package com.samjhadoo.dto.visualquery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryResponseDTO {
    private Long id;
    private String mentorName;
    private String responseType;
    private String content;
    private boolean isSolution;
    private Integer estimatedTimeMinutes;
    private String difficultyLevel;
    private String requiredMaterials;
    private BigDecimal costEstimate;
    private String stepByStepGuide;
    private String alternativeSolutions;
    private String safetyPrecautions;
    private String references;
    private List<VisualQueryAnnotationDTO> annotations;
    private List<VisualQueryResponseMediaDTO> responseMedia;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isHelpful;
    private int helpfulVotes;
    private int notHelpfulVotes;
    private int mentorRating;
    private String mentorFeedback;
    private int totalVotes;
    private double helpfulnessRatio;
    private boolean isHighlyHelpful;
    private boolean providesSolution;
    private int complexityScore;
}
