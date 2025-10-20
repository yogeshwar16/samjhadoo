package com.samjhadoo.dto.community;

import com.samjhadoo.model.enums.CommunityType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CreateCommunityRequest {
    @NotBlank(message = "Community name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;
    
    @NotNull(message = "Community type is required")
    private CommunityType type;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private boolean isPrivate = false;
    private boolean requiresApproval = true;
}
