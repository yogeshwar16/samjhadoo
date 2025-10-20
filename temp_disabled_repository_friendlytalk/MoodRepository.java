package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.friendlytalk.Mood;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {

    Optional<Mood> findByUser(User user);

    List<Mood> findByMoodType(MoodType moodType);

    List<Mood> findByAnonymousTrue();

    List<Mood> findByLookingForTalkTrue();

    @Query("SELECT m FROM Mood m WHERE m.lookingForTalk = true AND m.expiresAt > :now ORDER BY m.createdAt ASC")
    List<Mood> findActiveLookingForTalk(@Param("now") LocalDateTime now);

    @Query("SELECT m FROM Mood m WHERE m.moodType = :moodType AND m.lookingForTalk = true AND m.expiresAt > :now ORDER BY m.intensity DESC, m.createdAt ASC")
    List<Mood> findByMoodTypeLookingForTalk(@Param("moodType") MoodType moodType, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Mood m WHERE m.intensity >= :minIntensity AND m.lookingForTalk = true AND m.expiresAt > :now ORDER BY m.intensity DESC, m.createdAt ASC")
    List<Mood> findHighIntensityLookingForTalk(@Param("minIntensity") int minIntensity, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Mood m WHERE m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<Mood> findRecentMoods(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(m) FROM Mood m WHERE m.lookingForTalk = true AND m.expiresAt > :now")
    long countActiveLookingForTalk(@Param("now") LocalDateTime now);

    @Query("SELECT m FROM Mood m WHERE m.user = :user AND m.expiresAt > :now")
    Optional<Mood> findActiveMoodByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Mood m WHERE m.expiresAt <= :now")
    List<Mood> findExpiredMoods(@Param("now") LocalDateTime now);
}
