package com.samjhadoo.dto.response;

import com.samjhadoo.model.enums.CommunityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String bio;
    private String location;
    private CommunityType communityType;
    private Double rating;
    private Integer totalSessions;
}
