package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.AchievementDTO;
import com.samjhadoo.dto.gamification.UserAchievementDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Achievement;
import com.samjhadoo.model.gamification.UserAchievement;
import com.samjhadoo.model.enums.gamification.AchievementType;
import com.samjhadoo.repository.gamification.AchievementRepository;
import com.samjhadoo.repository.gamification.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PointsService pointsService;

    @Override
    @Transactional(readOnly = true)
    public List<AchievementDTO> getAllActiveAchievements() {
        return achievementRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementDTO> getAchievementsByType(AchievementType type) {
        return achievementRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AchievementDTO getAchievementById(Long achievementId) {
        return achievementRepository.findById(achievementId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievements(User user) {
        return userAchievementRepository.findByUser(user).stream()
                .map(this::convertToUserAchievementDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievementsInProgress(User user) {
        return userAchievementRepository.findInProgressByUser(user).stream()
                .map(this::convertToUserAchievementDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievementsCompleted(User user) {
        return userAchievementRepository.findCompletedByUser(user).stream()
                .map(this::convertToUserAchievementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean processAchievementProgress(User user, AchievementType achievementType, int increment) {
        // Find all achievements of this type that are active
        List<Achievement> achievements = achievementRepository.findByTypeAndActiveTrueOrderByThreshold(achievementType);

        boolean anyCompleted = false;

        for (Achievement achievement : achievements) {
            // Get or create user achievement
            UserAchievement userAchievement = userAchievementRepository
                    .findByUserAndAchievement(user, achievement)
                    .orElse(UserAchievement.builder()
                            .user(user)
                            .achievement(achievement)
                            .progress(0)
                            .completed(false)
                            .lastUpdated(LocalDateTime.now())
                            .build());

            // Only process if not already completed
            if (!userAchievement.isCompleted()) {
                userAchievement.setProgress(userAchievement.getProgress() + increment);
                userAchievementRepository.save(userAchievement);

                // Check if achievement is now completed
                if (userAchievement.getProgress() >= achievement.getThreshold()) {
                    completeAchievement(userAchievement);
                    anyCompleted = true;
                }
            }
        }

        return anyCompleted;
    }

    @Override
    public boolean awardAchievementToUser(User user, Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null || !achievement.isActive()) {
            return false;
        }

        UserAchievement userAchievement = userAchievementRepository
                .findByUserAndAchievement(user, achievement)
                .orElse(UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .progress(achievement.getThreshold())
                        .completed(true)
                        .completedAt(LocalDateTime.now())
                        .lastUpdated(LocalDateTime.now())
                        .build());

        if (!userAchievement.isCompleted()) {
            userAchievement.setProgress(achievement.getThreshold());
            userAchievement.setCompleted(true);
            userAchievement.setCompletedAt(LocalDateTime.now());
            userAchievementRepository.save(userAchievement);

            // Award points for completing the achievement
            if (achievement.getPointsReward() > 0) {
                pointsService.awardPoints(user, java.math.BigDecimal.valueOf(achievement.getPointsReward()),
                        com.samjhadoo.model.enums.gamification.PointsReason.ACHIEVEMENT_UNLOCKED,
                        "ACHIEVEMENT_" + achievement.getId(), "Completed achievement: " + achievement.getName());
            }

            log.info("Awarded achievement {} to user {}", achievementId, user.getId());
            return true;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getAchievementsReadyForCompletion() {
        return userAchievementRepository.findReadyForCompletion().stream()
                .map(this::convertToUserAchievementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Achievement createAchievement(AchievementDTO achievementDTO) {
        Achievement achievement = Achievement.builder()
                .type(achievementDTO.getType())
                .name(achievementDTO.getName())
                .description(achievementDTO.getDescription())
                .threshold(achievementDTO.getThreshold())
                .pointsReward(achievementDTO.getPointsReward())
                .repeatable(achievementDTO.isRepeatable())
                .active(achievementDTO.isActive())
                .build();

        return achievementRepository.save(achievement);
    }

    @Override
    public Achievement updateAchievement(Long achievementId, AchievementDTO achievementDTO) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null) {
            return null;
        }

        achievement.setType(achievementDTO.getType());
        achievement.setName(achievementDTO.getName());
        achievement.setDescription(achievementDTO.getDescription());
        achievement.setThreshold(achievementDTO.getThreshold());
        achievement.setPointsReward(achievementDTO.getPointsReward());
        achievement.setRepeatable(achievementDTO.isRepeatable());
        achievement.setActive(achievementDTO.isActive());

        return achievementRepository.save(achievement);
    }

    @Override
    public boolean deactivateAchievement(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null) {
            return false;
        }

        achievement.setActive(false);
        achievementRepository.save(achievement);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long getAchievementUserCount(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null) {
            return 0;
        }
        return userAchievementRepository.countUsersWhoCompletedAchievement(achievement);
    }

    @Override
    public boolean resetAchievementProgress(User user, Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId).orElse(null);
        if (achievement == null || !achievement.isRepeatable()) {
            return false;
        }

        UserAchievement userAchievement = userAchievementRepository
                .findByUserAndAchievement(user, achievement)
                .orElse(null);

        if (userAchievement != null && userAchievement.isCompleted()) {
            userAchievement.setProgress(0);
            userAchievement.setCompleted(false);
            userAchievement.setCompletedAt(null);
            userAchievement.setLastUpdated(LocalDateTime.now());
            userAchievementRepository.save(userAchievement);
            return true;
        }

        return false;
    }

    private void completeAchievement(UserAchievement userAchievement) {
        userAchievement.setCompleted(true);
        userAchievement.setCompletedAt(LocalDateTime.now());
        userAchievementRepository.save(userAchievement);

        // Award points for completing the achievement
        Achievement achievement = userAchievement.getAchievement();
        if (achievement.getPointsReward() > 0) {
            pointsService.awardPoints(userAchievement.getUser(),
                    java.math.BigDecimal.valueOf(achievement.getPointsReward()),
                    com.samjhadoo.model.enums.gamification.PointsReason.ACHIEVEMENT_UNLOCKED,
                    "ACHIEVEMENT_" + achievement.getId(),
                    "Completed achievement: " + achievement.getName());
        }

        log.info("User {} completed achievement {} ({})",
                userAchievement.getUser().getId(),
                achievement.getId(),
                achievement.getName());
    }

    private AchievementDTO convertToDTO(Achievement achievement) {
        return AchievementDTO.builder()
                .id(achievement.getId())
                .type(achievement.getType())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .threshold(achievement.getThreshold())
                .pointsReward(achievement.getPointsReward())
                .repeatable(achievement.isRepeatable())
                .active(achievement.isActive())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }

    private UserAchievementDTO convertToUserAchievementDTO(UserAchievement userAchievement) {
        Achievement achievement = userAchievement.getAchievement();
        double progressPercentage = achievement.getThreshold() > 0 ?
                (double) userAchievement.getProgress() / achievement.getThreshold() * 100 : 0;

        return UserAchievementDTO.builder()
                .id(userAchievement.getId())
                .achievementName(achievement.getName())
                .achievementType(achievement.getType())
                .achievementDescription(achievement.getDescription())
                .threshold(achievement.getThreshold())
                .pointsReward(achievement.getPointsReward())
                .progress(userAchievement.getProgress())
                .completed(userAchievement.isCompleted())
                .completedAt(userAchievement.getCompletedAt())
                .lastUpdated(userAchievement.getLastUpdated())
                .progressPercentage(progressPercentage)
                .build();
    }
}
