package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.PointsAccountDTO;
import com.samjhadoo.dto.gamification.PointsTransactionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.PointsAccount;
import com.samjhadoo.model.gamification.PointsTransaction;
import com.samjhadoo.model.enums.gamification.PointsReason;
import com.samjhadoo.repository.gamification.PointsAccountRepository;
import com.samjhadoo.repository.gamification.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PointsServiceImpl implements PointsService {

    private final PointsAccountRepository pointsAccountRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public PointsAccountDTO getUserPointsAccount(User user) {
        return pointsAccountRepository.findByUser(user)
                .map(this::convertToDTO)
                .orElse(createDefaultAccount(user));
    }

    @Override
    public BigDecimal awardPoints(User user, BigDecimal amount, PointsReason reason, String referenceId, String description) {
        PointsAccount account = getOrCreateAccount(user);

        // Create transaction record
        PointsTransaction transaction = PointsTransaction.builder()
                .account(account)
                .user(user)
                .delta(amount)
                .reason(reason)
                .referenceId(referenceId)
                .description(description)
                .transactionDate(LocalDateTime.now())
                .build();

        pointsTransactionRepository.save(transaction);

        // Update account balance
        BigDecimal newBalance = account.addPoints(amount);
        pointsAccountRepository.save(account);

        log.info("Awarded {} points to user {} for reason {} (new balance: {})",
                amount, user.getId(), reason, newBalance);

        return newBalance;
    }

    @Override
    public BigDecimal deductPoints(User user, BigDecimal amount, PointsReason reason, String referenceId, String description) {
        PointsAccount account = getOrCreateAccount(user);

        // Create transaction record
        PointsTransaction transaction = PointsTransaction.builder()
                .account(account)
                .user(user)
                .delta(amount.negate()) // Negative for deduction
                .reason(reason)
                .referenceId(referenceId)
                .description(description)
                .transactionDate(LocalDateTime.now())
                .build();

        pointsTransactionRepository.save(transaction);

        // Update account balance
        BigDecimal newBalance = account.spendPoints(amount);
        pointsAccountRepository.save(account);

        log.info("Deducted {} points from user {} for reason {} (new balance: {})",
                amount, user.getId(), reason, newBalance);

        return newBalance;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(User user) {
        return pointsAccountRepository.findByUser(user)
                .map(PointsAccount::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsTransactionDTO> getUserTransactionHistory(User user, int limit) {
        return pointsTransactionRepository.findByUserOrderByTransactionDateDesc(user).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsTransactionDTO> getUserTransactionHistory(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return pointsTransactionRepository.findByUserAndTransactionDateBetween(user, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsAccountDTO> getTopPointsAccounts(int limit) {
        return pointsAccountRepository.findAllWithPositiveBalance().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPointsBalance() {
        return pointsAccountRepository.sumTotalPointsBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPointsEarnedInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return pointsTransactionRepository.findByUserAndTransactionDateBetween(user, startDate, endDate).stream()
                .filter(t -> t.getDelta().compareTo(BigDecimal.ZERO) > 0 && !t.isReversed())
                .map(PointsTransaction::getDelta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPointsSpentInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return pointsTransactionRepository.findByUserAndTransactionDateBetween(user, startDate, endDate).stream()
                .filter(t -> t.getDelta().compareTo(BigDecimal.ZERO) < 0 && !t.isReversed())
                .map(t -> t.getDelta().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean reverseTransaction(Long transactionId, String reason) {
        PointsTransaction transaction = pointsTransactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || transaction.isReversed()) {
            return false;
        }

        PointsTransaction reversal = transaction.createReversal(reason);
        pointsTransactionRepository.save(reversal);

        // Update account balance
        PointsAccount account = transaction.getAccount();
        if (transaction.getDelta().compareTo(BigDecimal.ZERO) > 0) {
            // Original was a credit, reversal is a debit
            account.spendPoints(transaction.getDelta().abs());
        } else {
            // Original was a debit, reversal is a credit
            account.addPoints(transaction.getDelta().abs());
        }
        pointsAccountRepository.save(account);

        log.info("Reversed transaction {} for reason: {}", transactionId, reason);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsTransactionDTO> getExpiredTransactions() {
        return pointsTransactionRepository.findExpiredUnreversedTransactions(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int processExpiredTransactions() {
        List<PointsTransaction> expiredTransactions = pointsTransactionRepository.findExpiredUnreversedTransactions(LocalDateTime.now());
        int processed = 0;

        for (PointsTransaction transaction : expiredTransactions) {
            if (reverseTransaction(transaction.getId(), "Expired transaction")) {
                processed++;
            }
        }

        log.info("Processed {} expired transactions", processed);
        return processed;
    }

    private PointsAccount getOrCreateAccount(User user) {
        return pointsAccountRepository.findByUser(user)
                .orElseGet(() -> createAccount(user));
    }

    private PointsAccount createAccount(User user) {
        PointsAccount account = PointsAccount.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .lifetimeEarned(BigDecimal.ZERO)
                .lifetimeSpent(BigDecimal.ZERO)
                .build();

        return pointsAccountRepository.save(account);
    }

    private PointsAccountDTO createDefaultAccount(User user) {
        return PointsAccountDTO.builder()
                .balance(BigDecimal.ZERO)
                .lifetimeEarned(BigDecimal.ZERO)
                .lifetimeSpent(BigDecimal.ZERO)
                .build();
    }

    private PointsAccountDTO convertToDTO(PointsAccount account) {
        return PointsAccountDTO.builder()
                .id(account.getId())
                .balance(account.getBalance())
                .lifetimeEarned(account.getLifetimeEarned())
                .lifetimeSpent(account.getLifetimeSpent())
                .lastActivity(account.getLastActivity())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private PointsTransactionDTO convertToDTO(PointsTransaction transaction) {
        return PointsTransactionDTO.builder()
                .id(transaction.getId())
                .delta(transaction.getDelta())
                .reason(transaction.getReason())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .expiresAt(transaction.getExpiresAt())
                .reversed(transaction.isReversed())
                .reversalReason(transaction.getReversalReason())
                .reversedAt(transaction.getReversedAt())
                .build();
    }
}
