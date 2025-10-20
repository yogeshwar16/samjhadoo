package com.samjhadoo.model.enums;

public enum MemberStatus {
    PENDING,    // Waiting for approval (for private communities)
    ACTIVE,     // Active member
    SUSPENDED,  // Temporarily suspended
    BANNED,     // Permanently banned
    LEFT        // Left the community
}
