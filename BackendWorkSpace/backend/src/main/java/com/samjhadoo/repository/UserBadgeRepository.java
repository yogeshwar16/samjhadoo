package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.UserBadge;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUser(User user);

    List<UserBadge> findByBadge(Badge badge);

    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user = :user AND ub.notified = false")
    List<UserBadge> findUnnotifiedByUser(@Param("user") User user);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.badge = :badge")
    long countUsersWithBadge(@Param("badge") Badge badge);

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user = :user ORDER BY ub.awardedAt DESC")
    List<UserBadge> findByUserOrderByAwardedAtDesc(@Param("user") User user);

    boolean existsByUserAndBadge(User user, Badge badge);
}
