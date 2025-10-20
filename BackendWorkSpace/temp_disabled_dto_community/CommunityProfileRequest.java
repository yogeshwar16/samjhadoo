package com.samjhadoo.dto.community;

import com.samjhadoo.model.enums.CommunityTag;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to update user's community tag
 */
@Data
public class CommunityProfileRequest {

    @NotNull
    private CommunityTag communityTag;

    private String documentUrl; // For verification (optional)
}
