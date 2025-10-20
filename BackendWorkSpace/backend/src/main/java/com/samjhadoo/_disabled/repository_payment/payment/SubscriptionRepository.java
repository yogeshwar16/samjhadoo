package com.samjhadoo.repository.payment;

import com.samjhadoo.model.payment.Subscription;
import com.samjhadoo.model.payment.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    
    List<Subscription> findByUserId(Long userId);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    Optional<Subscription> findByPaymentGatewaySubscriptionId(String subscriptionId);
    
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate <= :date AND s.status IN ('ACTIVE', 'TRIALING')")
    List<Subscription> findSubscriptionsDueForBilling(@Param("date") LocalDate date);
    
    @Query("SELECT s FROM Subscription s WHERE s.endsAt IS NOT NULL AND s.endsAt <= :date AND s.status = 'ACTIVE'")
    List<Subscription> findExpiringSubscriptions(@Param("date") LocalDate date);
    
    @Modifying
    @Query("UPDATE Subscription s SET s.status = 'CANCELED', s.canceledAt = CURRENT_TIMESTAMP, s.endsAt = CURRENT_DATE WHERE s.id = :id")
    void cancelSubscription(@Param("id") String subscriptionId);
    
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.status IN ('ACTIVE', 'TRIALING')")
    boolean hasActiveSubscription(@Param("userId") String userId);
    
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status IN ('ACTIVE', 'TRIALING') ORDER BY s.createdAt DESC")
    List<Subscription> findActiveSubscriptionsByUser(@Param("userId") String userId);
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s WHERE s.status = 'ACTIVE'")
    BigDecimal getTotalMonthlyRecurringRevenue();
}
