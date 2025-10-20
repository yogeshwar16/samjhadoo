package com.samjhadoo.dto;

import com.samjhadoo.model.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDTO {

    private Long id;
    private Long mentorId;
    private String mentorName;
    private Long menteeId;
    private String menteeName;
    private int rating;
    private String comment;
    private ReviewStatus status;
    private LocalDateTime createdAt;
}
