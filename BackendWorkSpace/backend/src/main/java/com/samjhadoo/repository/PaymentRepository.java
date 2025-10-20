package com.samjhadoo.repository.payment;

import com.samjhadoo.model.payment.Payment;
import com.samjhadoo.model.payment.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    Optional<Payment> findByPaymentGatewayId(String paymentGatewayId);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsBetweenDates(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.user.id = :userId")
    BigDecimal getTotalAmountPaidByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED' AND p.createdAt >= :date")
    boolean hasSuccessfulPaymentAfter(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    @Query("SELECT p FROM Payment p WHERE p.isRefunded = false AND p.status = 'COMPLETED' AND p.amount > COALESCE(p.refundAmount, 0)")
    List<Payment> findRefundablePayments();
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
