package com.samjhadoo.dto.friendlytalk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDTO {

    private Long id;
    private UserInfo user;
    private UserInfo favoriteUser;
    private String tag;
    private String notes;
    private Boolean notifyWhenOnline;
    private Boolean isMutual;
    private Boolean isOnline;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String profilePictureUrl;
        private Boolean isOnline;
        private String status; // Available, Busy, Offline
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long favoriteUserId;
        private String tag;
        private String notes;
        private Boolean notifyWhenOnline;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String tag;
        private String notes;
        private Boolean notifyWhenOnline;
    }
}
