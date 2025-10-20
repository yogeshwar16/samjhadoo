package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.enums.gamification.AchievementType;
import com.samjhadoo.model.gamification.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByType(AchievementType type);

    List<Achievement> findByActiveTrue();

    List<Achievement> findByRepeatableTrue();

    List<Achievement> findByRepeatableFalse();

    @Query("SELECT a FROM Achievement a WHERE a.active = true ORDER BY a.threshold")
    List<Achievement> findAllActiveOrderByThreshold();

    @Query("SELECT a FROM Achievement a WHERE a.type = :type AND a.active = true ORDER BY a.threshold")
    List<Achievement> findByTypeAndActiveTrueOrderByThreshold(@Param("type") AchievementType type);

    boolean existsByName(String name);
}
