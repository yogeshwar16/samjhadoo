package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.UserStreak;
import com.samjhadoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUser(User user);

    @Query("SELECT us FROM UserStreak us WHERE us.lastActivityDate >= :sinceDate ORDER BY us.currentStreakDays DESC")
    List<UserStreak> findActiveStreaksSince(@Param("sinceDate") LocalDate sinceDate);

    @Query("SELECT us FROM UserStreak us WHERE us.currentStreakDays > 0 ORDER BY us.currentStreakDays DESC")
    List<UserStreak> findAllWithActiveStreaks();

    @Query("SELECT us FROM UserStreak us WHERE us.maxStreakDays >= :minStreak ORDER BY us.maxStreakDays DESC")
    List<UserStreak> findByMaxStreakDaysGreaterThanEqual(@Param("minStreak") int minStreak);

    @Query("SELECT us FROM UserStreak us WHERE us.totalLogins >= :minLogins ORDER BY us.totalLogins DESC")
    List<UserStreak> findByTotalLoginsGreaterThanEqual(@Param("minLogins") int minLogins);

    @Query("SELECT us FROM UserStreak us WHERE us.lastLoginDate = :date")
    List<UserStreak> findByLastLoginDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(us) FROM UserStreak us WHERE us.currentStreakDays > 0")
    long countUsersWithActiveStreaks();
}
