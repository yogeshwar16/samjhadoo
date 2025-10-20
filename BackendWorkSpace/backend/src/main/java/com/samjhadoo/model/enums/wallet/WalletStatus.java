package com.samjhadoo.model.enums.wallet;

public enum WalletStatus {
    ACTIVE,         // Normal active wallet
    SUSPENDED,      // Temporarily suspended
    FROZEN,         // Frozen due to violations
    CLOSED,         // Permanently closed
    PENDING         // Pending verification/activation
}
