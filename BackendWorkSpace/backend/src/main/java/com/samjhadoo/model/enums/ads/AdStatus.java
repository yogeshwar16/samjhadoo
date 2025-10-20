package com.samjhadoo.model.enums.ads;

public enum AdStatus {
    DRAFT,          // Ad being created
    PENDING,        // Submitted for review
    APPROVED,       // Approved and active
    ACTIVE,         // Currently running
    PAUSED,         // Temporarily paused
    EXPIRED,        // Campaign ended
    REJECTED,       // Rejected during review
    SUSPENDED       // Suspended due to violations
}
