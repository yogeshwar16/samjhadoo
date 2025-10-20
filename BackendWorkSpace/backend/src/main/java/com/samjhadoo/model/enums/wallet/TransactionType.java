package com.samjhadoo.model.enums.wallet;

public enum TransactionType {
    // Credit additions
    TOP_UP,              // Manual wallet top-up
    REFUND,              // Refund from cancelled transaction
    REWARD,              // Reward from ads, referrals, etc.
    CASHBACK,            // Cashback from purchases
    BONUS,               // Promotional bonus
    INTEREST,            // Interest earned

    // Debit transactions
    PAYMENT,             // Payment for services
    WITHDRAWAL,          // Money withdrawal
    FEE,                 // Platform fees
    CHARGEBACK,          // Payment reversal
    ADJUSTMENT,          // Manual adjustment

    // Transfer transactions
    TRANSFER_SENT,       // Money sent to another user
    TRANSFER_RECEIVED,   // Money received from another user
    ESCROW_HOLD,         // Money held in escrow
    ESCROW_RELEASE       // Money released from escrow
}
