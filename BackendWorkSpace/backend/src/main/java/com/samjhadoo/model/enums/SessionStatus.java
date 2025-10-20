package com.samjhadoo.model.enums;

public enum SessionStatus {
    SCHEDULED,      // Session is scheduled for future
    IN_PROGRESS,    // Session is currently ongoing
    COMPLETED,      // Session has been completed
    CANCELLED,      // Session was cancelled
    NO_SHOW,        // One party didn't show up
    REFUNDED        // Session was refunded
}
