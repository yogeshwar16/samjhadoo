package com.samjhadoo.model.enums;

/**
 * Topic approval status
 */
public enum TopicStatus {
    PENDING,      // AI-suggested, awaiting admin review
    APPROVED,     // Admin approved, live
    REJECTED,     // Admin rejected
    ARCHIVED      // No longer active
}
