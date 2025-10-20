package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.PointsAccount;
import com.samjhadoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointsAccountRepository extends JpaRepository<PointsAccount, Long> {

    Optional<PointsAccount> findByUser(User user);

    @Query("SELECT pa FROM PointsAccount pa WHERE pa.balance > 0 ORDER BY pa.balance DESC")
    List<PointsAccount> findAllWithPositiveBalance();

    @Query("SELECT pa FROM PointsAccount pa WHERE pa.lifetimeEarned >= :minEarned ORDER BY pa.lifetimeEarned DESC")
    List<PointsAccount> findByLifetimeEarnedGreaterThanEqual(@Param("minEarned") BigDecimal minEarned);

    @Query("SELECT pa FROM PointsAccount pa WHERE pa.balance >= :minBalance ORDER BY pa.balance DESC")
    List<PointsAccount> findByBalanceGreaterThanEqual(@Param("minBalance") BigDecimal minBalance);

    @Query("SELECT pa FROM PointsAccount pa WHERE pa.lastActivity >= :sinceDate ORDER BY pa.lastActivity DESC")
    List<PointsAccount> findActiveSince(@Param("sinceDate") java.time.LocalDateTime sinceDate);

    @Query("SELECT COUNT(pa) FROM PointsAccount pa WHERE pa.balance > 0")
    long countAccountsWithPositiveBalance();

    @Query("SELECT SUM(pa.balance) FROM PointsAccount pa WHERE pa.balance > 0")
    BigDecimal sumTotalPointsBalance();
}
