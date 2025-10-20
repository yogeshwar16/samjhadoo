package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.Referral;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Referral.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    List<Referral> findByReferrer(User referrer);

    List<Referral> findByReferee(User referee);

    Optional<Referral> findByCode(String code);

    List<Referral> findByStatus(ReferralStatus status);

    @Query("SELECT r FROM Referral r WHERE r.referrer = :referrer ORDER BY r.createdAt DESC")
    List<Referral> findByReferrerOrderByCreatedAtDesc(@Param("referrer") User referrer);

    @Query("SELECT r FROM Referral r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Referral> findByStatusOrderByCreatedAtDesc(@Param("status") ReferralStatus status);

    @Query("SELECT r FROM Referral r WHERE r.expiresAt < :now AND r.status = 'PENDING'")
    List<Referral> findExpiredPendingReferrals(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer = :referrer AND r.status = 'COMPLETED'")
    long countCompletedReferralsByReferrer(@Param("referrer") User referrer);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer = :referrer")
    long countTotalReferralsByReferrer(@Param("referrer") User referrer);

    @Query("SELECT r FROM Referral r WHERE r.refereeEmail = :email AND r.status = 'PENDING'")
    List<Referral> findPendingReferralsByRefereeEmail(@Param("email") String email);

    @Query("SELECT r FROM Referral r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Referral> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByCode(String code);

    boolean existsByReferrerAndRefereeEmail(User referrer, String refereeEmail);
}
