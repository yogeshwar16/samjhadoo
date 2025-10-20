package com.samjhadoo.dto.friendlytalk;

import com.samjhadoo.model.friendlytalk.LiveSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionDTO {

    private Long id;
    private String title;
    private String description;
    private MentorInfo mentor;
    private String status;
    private String type;
    private Set<String> tags;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime scheduledStartTime;
    private Integer scheduledDurationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String meetingUrl;
    private String thumbnailUrl;
    private Boolean isRecorded;
    private String recordingUrl;
    private Boolean isFeatured;
    private Boolean isLive;
    private Boolean isFull;
    private Boolean canJoin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorInfo {
        private Long id;
        private String name;
        private String email;
        private String profilePictureUrl;
        private String expertise;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String title;
        private String description;
        private LiveSession.SessionType type;
        private Set<String> tags;
        private LocalDateTime scheduledStartTime;
        private Integer scheduledDurationMinutes;
        private Integer maxParticipants;
        private String thumbnailUrl;
        private Boolean isRecorded;
        private Boolean isFeatured;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private Set<String> tags;
        private LocalDateTime scheduledStartTime;
        private Integer scheduledDurationMinutes;
        private Integer maxParticipants;
        private String thumbnailUrl;
        private Boolean isFeatured;
    }
}
