package com.samjhadoo.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request to approve or reject a topic
 */
@Data
public class TopicApprovalRequest {

    @NotBlank
    private String action; // APPROVE or REJECT

    private String reason; // Required for rejection

    @NotBlank
    private String adminUsername;
}
