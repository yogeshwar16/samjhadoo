package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.PointsTransaction;
import com.samjhadoo.model.gamification.PointsAccount;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.gamification.PointsReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    List<PointsTransaction> findByAccount(PointsAccount account);

    List<PointsTransaction> findByUser(User user);

    List<PointsTransaction> findByReason(PointsReason reason);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.account = :account ORDER BY pt.transactionDate DESC")
    List<PointsTransaction> findByAccountOrderByTransactionDateDesc(@Param("account") PointsAccount account);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.user = :user ORDER BY pt.transactionDate DESC")
    List<PointsTransaction> findByUserOrderByTransactionDateDesc(@Param("user") User user);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.transactionDate BETWEEN :startDate AND :endDate ORDER BY pt.transactionDate DESC")
    List<PointsTransaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.account = :account AND pt.transactionDate BETWEEN :startDate AND :endDate ORDER BY pt.transactionDate DESC")
    List<PointsTransaction> findByAccountAndTransactionDateBetween(@Param("account") PointsAccount account, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.referenceId = :referenceId")
    List<PointsTransaction> findByReferenceId(@Param("referenceId") String referenceId);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.reversed = false AND pt.expiresAt IS NOT NULL AND pt.expiresAt < :now")
    List<PointsTransaction> findExpiredUnreversedTransactions(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(pt.delta) FROM PointsTransaction pt WHERE pt.account = :account AND pt.reversed = false")
    BigDecimal sumPointsByAccount(@Param("account") PointsAccount account);

    @Query("SELECT SUM(pt.delta) FROM PointsTransaction pt WHERE pt.user = :user AND pt.reversed = false")
    BigDecimal sumPointsByUser(@Param("user") User user);
}
