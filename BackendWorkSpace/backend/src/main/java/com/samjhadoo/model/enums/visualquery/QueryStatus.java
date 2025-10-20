package com.samjhadoo.model.enums.visualquery;

public enum QueryStatus {
    DRAFT,          // Query being prepared
    SUBMITTED,      // Query submitted for review
    UNDER_REVIEW,   // Being reviewed by mentors
    IN_PROGRESS,    // Mentor is working on response
    RESPONDED,      // Response provided
    RESOLVED,       // Query resolved to user's satisfaction
    CLOSED,         // Query closed without resolution
    ESCALATED       // Query escalated to admin/expert
}
