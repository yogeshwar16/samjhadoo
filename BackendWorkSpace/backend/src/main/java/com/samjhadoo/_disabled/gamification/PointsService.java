package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.PointsAccountDTO;
import com.samjhadoo.dto.gamification.PointsTransactionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.gamification.PointsReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing points accounts and transactions.
 */
public interface PointsService {

    /**
     * Gets the points account for a user.
     * @param user The user
     * @return The user's points account
     */
    PointsAccountDTO getUserPointsAccount(User user);

    /**
     * Awards points to a user.
     * @param user The user to award points to
     * @param amount The amount of points to award
     * @param reason The reason for awarding points
     * @param referenceId Optional reference ID
     * @param description Optional description
     * @return The updated points balance
     */
    BigDecimal awardPoints(User user, BigDecimal amount, PointsReason reason, String referenceId, String description);

    /**
     * Deducts points from a user.
     * @param user The user to deduct points from
     * @param amount The amount of points to deduct
     * @param reason The reason for deducting points
     * @param referenceId Optional reference ID
     * @param description Optional description
     * @return The updated points balance
     */
    BigDecimal deductPoints(User user, BigDecimal amount, PointsReason reason, String referenceId, String description);

    /**
     * Gets the current points balance for a user.
     * @param user The user
     * @return The current points balance
     */
    BigDecimal getCurrentBalance(User user);

    /**
     * Gets transaction history for a user.
     * @param user The user
     * @param limit Maximum number of transactions to return
     * @return List of recent transactions
     */
    List<PointsTransactionDTO> getUserTransactionHistory(User user, int limit);

    /**
     * Gets transaction history for a user within a date range.
     * @param user The user
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions within the date range
     */
    List<PointsTransactionDTO> getUserTransactionHistory(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets all points accounts with positive balance.
     * @param limit Maximum number of accounts to return
     * @return List of points accounts ordered by balance
     */
    List<PointsAccountDTO> getTopPointsAccounts(int limit);

    /**
     * Gets total points balance across all users.
     * @return Sum of all positive points balances
     */
    BigDecimal getTotalPointsBalance();

    /**
     * Gets points earned by a user within a date range.
     * @param user The user
     * @param startDate Start date
     * @param endDate End date
     * @return Total points earned in the period
     */
    BigDecimal getPointsEarnedInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets points spent by a user within a date range.
     * @param user The user
     * @param startDate Start date
     * @param endDate End date
     * @return Total points spent in the period
     */
    BigDecimal getPointsSpentInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Reverses a points transaction.
     * @param transactionId The transaction ID to reverse
     * @param reason Reason for the reversal
     * @return true if reversal was successful
     */
    boolean reverseTransaction(Long transactionId, String reason);

    /**
     * Gets expired transactions that need to be reversed.
     * @return List of expired unreversed transactions
     */
    List<PointsTransactionDTO> getExpiredTransactions();

    /**
     * Processes expired transactions (reverses points if needed).
     * @return Number of transactions processed
     */
    int processExpiredTransactions();
}
