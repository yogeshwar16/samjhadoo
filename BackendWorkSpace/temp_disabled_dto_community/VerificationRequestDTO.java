package com.samjhadoo.dto.community;

import com.samjhadoo.model.community.CommunityVerification;
import com.samjhadoo.model.enums.CommunityTag;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for Community Verification
 */
@Data
@Builder
public class VerificationRequestDTO {

    private Long id;
    private Long userId;
    private String userName;
    private CommunityTag communityTag;
    private CommunityVerification.VerificationStatus status;
    private String documentUrl;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
}
