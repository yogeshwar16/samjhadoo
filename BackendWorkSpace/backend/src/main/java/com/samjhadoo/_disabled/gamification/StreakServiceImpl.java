package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.UserStreakDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.UserStreak;
import com.samjhadoo.model.enums.gamification.PointsReason;
import com.samjhadoo.repository.gamification.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StreakServiceImpl implements StreakService {

    private final UserStreakRepository userStreakRepository;
    private final PointsService pointsService;

    @Override
    public boolean updateStreak(User user) {
        UserStreak userStreak = userStreakRepository.findByUser(user)
                .orElseGet(() -> createUserStreak(user));

        LocalDate today = LocalDate.now();
        boolean updated = userStreak.updateStreak(today);

        if (updated) {
            userStreakRepository.save(userStreak);

            // Award points for daily login
            pointsService.awardPoints(user, java.math.BigDecimal.valueOf(5),
                    PointsReason.DAILY_LOGIN, "STREAK_UPDATE", "Daily login streak");

            // Check for streak milestones
            int milestonePoints = awardStreakMilestonePoints(user, userStreak.getCurrentStreakDays());
            if (milestonePoints > 0) {
                pointsService.awardPoints(user, java.math.BigDecimal.valueOf(milestonePoints),
                        PointsReason.WEEKLY_STREAK, "STREAK_MILESTONE_" + userStreak.getCurrentStreakDays(),
                        "Streak milestone: " + userStreak.getCurrentStreakDays() + " days");
            }

            log.info("Updated streak for user {}: {} days (max: {})",
                    user.getId(), userStreak.getCurrentStreakDays(), userStreak.getMaxStreakDays());
        }

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStreakDTO getUserStreak(User user) {
        return userStreakRepository.findByUser(user)
                .map(this::convertToDTO)
                .orElse(createDefaultStreakDTO());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStreakDTO> getUsersWithActiveStreaks(int limit) {
        return userStreakRepository.findAllWithActiveStreaks().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStreakDTO> getUsersWithLongestStreaks(int limit) {
        return userStreakRepository.findByMaxStreakDaysGreaterThanEqual(1).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStreakDTO> getUsersWithMostLogins(int limit) {
        return userStreakRepository.findByTotalLoginsGreaterThanEqual(1).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveStreakToday(User user) {
        return userStreakRepository.findByUser(user)
                .map(streak -> streak.getLastLoginDate() != null && streak.getLastLoginDate().equals(LocalDate.now()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDate getLastLoginDate(User user) {
        return userStreakRepository.findByUser(user)
                .map(UserStreak::getLastLoginDate)
                .orElse(null);
    }

    @Override
    public boolean resetUserStreak(User user, String reason) {
        UserStreak userStreak = userStreakRepository.findByUser(user).orElse(null);
        if (userStreak == null) {
            return false;
        }

        userStreak.setCurrentStreakDays(0);
        userStreak.setLastActivityDate(LocalDate.now());
        userStreakRepository.save(userStreak);

        log.info("Reset streak for user {}: {}", user.getId(), reason);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStreakStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsersWithStreaks", userStreakRepository.countUsersWithActiveStreaks());
        stats.put("averageCurrentStreak", calculateAverageCurrentStreak());
        stats.put("averageMaxStreak", calculateAverageMaxStreak());
        stats.put("totalLogins", calculateTotalLogins());
        return stats;
    }

    @Override
    public int awardStreakMilestonePoints(User user, int streakDays) {
        int points = 0;

        // Weekly streak (7 days)
        if (streakDays == 7) {
            points = 25;
        }
        // Monthly streak (30 days)
        else if (streakDays == 30) {
            points = 100;
        }
        // 100 days milestone
        else if (streakDays == 100) {
            points = 500;
        }
        // 365 days milestone (1 year)
        else if (streakDays == 365) {
            points = 1000;
        }

        return points;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStreakDTO> getUsersForStreakMilestoneRewards() {
        return userStreakRepository.findAll().stream()
                .filter(streak -> {
                    int currentDays = streak.getCurrentStreakDays();
                    return currentDays == 7 || currentDays == 30 || currentDays == 100 || currentDays == 365;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserStreak createUserStreak(User user) {
        UserStreak streak = UserStreak.builder()
                .user(user)
                .currentStreakDays(0)
                .maxStreakDays(0)
                .totalLogins(0)
                .build();

        return userStreakRepository.save(streak);
    }

    private UserStreakDTO createDefaultStreakDTO() {
        return UserStreakDTO.builder()
                .currentStreakDays(0)
                .maxStreakDays(0)
                .totalLogins(0)
                .streakActiveToday(false)
                .build();
    }

    private UserStreakDTO convertToDTO(UserStreak userStreak) {
        return UserStreakDTO.builder()
                .id(userStreak.getId())
                .currentStreakDays(userStreak.getCurrentStreakDays())
                .maxStreakDays(userStreak.getMaxStreakDays())
                .lastActivityDate(userStreak.getLastActivityDate())
                .totalLogins(userStreak.getTotalLogins())
                .lastLoginDate(userStreak.getLastLoginDate())
                .streakActiveToday(userStreak.getLastLoginDate() != null &&
                                   userStreak.getLastLoginDate().equals(LocalDate.now()))
                .build();
    }

    private double calculateAverageCurrentStreak() {
        List<UserStreak> streaks = userStreakRepository.findAllWithActiveStreaks();
        if (streaks.isEmpty()) {
            return 0.0;
        }
        return streaks.stream()
                .mapToInt(UserStreak::getCurrentStreakDays)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageMaxStreak() {
        List<UserStreak> streaks = userStreakRepository.findAll();
        if (streaks.isEmpty()) {
            return 0.0;
        }
        return streaks.stream()
                .mapToInt(UserStreak::getMaxStreakDays)
                .average()
                .orElse(0.0);
    }

    private long calculateTotalLogins() {
        return userStreakRepository.findAll().stream()
                .mapToLong(UserStreak::getTotalLogins)
                .sum();
    }
}
