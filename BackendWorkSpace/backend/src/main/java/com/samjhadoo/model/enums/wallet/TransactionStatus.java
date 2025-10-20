package com.samjhadoo.model.enums.wallet;

public enum TransactionStatus {
    PENDING,        // Transaction initiated but not processed
    PROCESSING,     // Transaction being processed
    COMPLETED,      // Transaction successfully completed
    FAILED,         // Transaction failed
    CANCELLED,      // Transaction cancelled
    REFUNDED,       // Transaction refunded
    DISPUTED,       // Transaction disputed
    ON_HOLD         // Transaction on hold for review
}
