package com.samjhadoo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.samjhadoo.model.enums.SessionStatus;
import com.samjhadoo.model.enums.SessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    
    private String id;
    private String title;
    private String description;
    private SessionType sessionType;
    private SessionStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
    
    private Integer durationMinutes;
    private String meetingUrl;
    private String meetingId;
    private Double price;
    private String currency;
    private String receiptUrl;
    private UserProfileResponse mentor;
    private UserProfileResponse mentee;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;
}
