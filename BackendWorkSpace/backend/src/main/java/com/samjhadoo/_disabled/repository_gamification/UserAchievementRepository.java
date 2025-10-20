package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.gamification.UserAchievement;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUser(User user);

    List<UserAchievement> findByAchievement(Achievement achievement);

    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.completed = false ORDER BY ua.progress DESC")
    List<UserAchievement> findInProgressByUser(@Param("user") User user);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.completed = true ORDER BY ua.completedAt DESC")
    List<UserAchievement> findCompletedByUser(@Param("user") User user);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.achievement = :achievement AND ua.completed = true")
    long countUsersWhoCompletedAchievement(@Param("achievement") Achievement achievement);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.progress >= ua.achievement.threshold AND ua.completed = false")
    List<UserAchievement> findReadyForCompletion();
}
