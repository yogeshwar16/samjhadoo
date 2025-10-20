package com.samjhadoo.repository.gamification;

import com.samjhadoo.model.enums.gamification.BadgeType;
import com.samjhadoo.model.gamification.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByName(String name);

    List<Badge> findByType(BadgeType type);

    List<Badge> findByActiveTrue();

    @Query("SELECT b FROM Badge b WHERE b.active = true ORDER BY b.name")
    List<Badge> findAllActiveOrderByName();

    boolean existsByName(String name);
}
