package com.samjhadoo.repository.payment;

import com.samjhadoo.model.payment.Payout;
import com.samjhadoo.model.payment.Payout.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, String> {
    
    List<Payout> findByMentorId(String mentorId);
    
    List<Payout> findByStatus(PayoutStatus status);
    
    Page<Payout> findByMentorId(String mentorId, Pageable pageable);
    
    @Query("SELECT p FROM Payout p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findPayoutsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.mentor.id = :mentorId AND p.status = 'COMPLETED'")
    BigDecimal getTotalEarningsByMentor(@Param("mentorId") String mentorId);
    
    @Query("SELECT p FROM Payout p WHERE p.scheduledDate <= :currentDate AND p.status = 'PENDING'")
    List<Payout> findPendingPayoutsDue(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT p FROM Payout p WHERE p.processedDate BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    List<Payout> findCompletedPayoutsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT p.mentor.id, SUM(p.amount) as totalAmount FROM Payout p " +
           "WHERE p.status = 'COMPLETED' AND p.processedDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.mentor.id ORDER BY totalAmount DESC")
    List<Object[]> findTopEarningMentors(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Payout p WHERE p.transactionId = :transactionId")
    Optional<Payout> findByTransactionId(@Param("transactionId") String transactionId);
}
