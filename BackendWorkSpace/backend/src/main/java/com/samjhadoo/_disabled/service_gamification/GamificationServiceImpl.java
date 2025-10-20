package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.UserProfileDTO;
import com.samjhadoo.dto.gamification.LeaderboardDTO;
import com.samjhadoo.dto.gamification.PointsTransactionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.gamification.AchievementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificationServiceImpl implements GamificationService {

    private final PointsService pointsService;
    private final StreakService streakService;
    private final BadgeService badgeService;
    private final AchievementService achievementService;
    private final ReferralService referralService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(User user) {
        return UserProfileDTO.builder()
                .userId(user.getId().toString())
                .userName(user.getFirstName() + " " + user.getLastName())
                .userEmail(user.getEmail())

                // Points
                .currentPoints(pointsService.getCurrentBalance(user))
                .lifetimeEarned(pointsService.getUserPointsAccount(user).getLifetimeEarned())
                .lifetimeSpent(pointsService.getUserPointsAccount(user).getLifetimeSpent())

                // Streaks
                .currentStreakDays(streakService.getUserStreak(user).getCurrentStreakDays())
                .maxStreakDays(streakService.getUserStreak(user).getMaxStreakDays())
                .totalLogins(streakService.getUserStreak(user).getTotalLogins())
                .streakActiveToday(streakService.hasActiveStreakToday(user))

                // Badges & Achievements
                .recentBadges(badgeService.getRecentUserBadges(user, 5))
                .inProgressAchievements(achievementService.getUserAchievementsInProgress(user))
                .completedAchievements(achievementService.getUserAchievementsCompleted(user))
                .totalBadgesEarned(badgeService.getUserBadges(user).size())
                .totalAchievementsCompleted(achievementService.getUserAchievementsCompleted(user).size())

                // Referrals
                .totalReferrals(referralService.getUserReferrals(user).size())
                .completedReferrals(referralService.getCompletedReferrals(user).size())
                .referralCode(generateReferralCodeForUser(user))
                .build();
    }

    @Override
    public BigDecimal awardPoints(User user, BigDecimal amount, com.samjhadoo.model.enums.gamification.PointsReason reason,
                                 String referenceId, String description) {
        return pointsService.awardPoints(user, amount, reason, referenceId, description);
    }

    @Override
    public BigDecimal deductPoints(User user, BigDecimal amount, com.samjhadoo.model.enums.gamification.PointsReason reason,
                                  String referenceId, String description) {
        return pointsService.deductPoints(user, amount, reason, referenceId, description);
    }

    @Override
    public boolean updateStreak(User user) {
        return streakService.updateStreak(user);
    }

    @Override
    public boolean processAchievement(User user, AchievementType achievementType, int increment) {
        return achievementService.processAchievementProgress(user, achievementType, increment);
    }

    @Override
    public boolean awardBadge(User user, Long badgeId, String reason) {
        return badgeService.awardBadgeToUser(user, badgeId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getPointsLeaderboard(int limit) {
        return pointsService.getTopPointsAccounts(limit).stream()
                .map(account -> LeaderboardDTO.builder()
                        .userId(account.getUser() != null ? account.getUser().getId() : null)
                        .userName(account.getUser() != null ? account.getUser().getFirstName() + " " + account.getUser().getLastName() : "Unknown User")
                        .userEmail(account.getUser() != null ? account.getUser().getEmail() : "unknown@example.com")
                        .rank(0) // Would need to be calculated
                        .points(account.getBalance())
                        .currentStreakDays(0) // Would need to be calculated
                        .totalBadges(0) // Would need to be calculated
                        .totalAchievements(0) // Would need to be calculated
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getStreakLeaderboard(int limit) {
        return streakService.getUsersWithLongestStreaks(limit).stream()
                .map(streak -> LeaderboardDTO.builder()
                        .userId(streak.getUser() != null ? streak.getUser().getId() : null)
                        .userName(streak.getUser() != null ? streak.getUser().getFirstName() + " " + streak.getUser().getLastName() : "Unknown User")
                        .userEmail(streak.getUser() != null ? streak.getUser().getEmail() : "unknown@example.com")
                        .rank(0) // Would need to be calculated
                        .points(java.math.BigDecimal.valueOf(streak.getCurrentStreakDays() * 10)) // Points based on streak
                        .currentStreakDays(streak.getCurrentStreakDays())
                        .totalBadges(0) // Would need to be calculated
                        .totalAchievements(0) // Would need to be calculated
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getBadgeLeaderboard(int limit) {
        // This is a simplified implementation
        // In a real scenario, you'd need a more complex query to aggregate badge counts per user
        return badgeService.getAllActiveBadges().stream()
                .limit(limit)
                .map(badge -> LeaderboardDTO.builder()
                        .userId(1L) // Would need user info
                        .userName("Badge Holder") // Would need user info
                        .userEmail("badgeholder@example.com") // Would need user info
                        .rank(0) // Would need to be calculated
                        .points(java.math.BigDecimal.valueOf(badge.getPointsValue() != null ? badge.getPointsValue() : 0))
                        .currentStreakDays(0) // Would need to be calculated
                        .totalBadges(1) // This badge
                        .totalAchievements(0) // Would need to be calculated
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean processReferral(User referrer, User referee) {
        // Generate referral code for referrer if they don't have one
        String referralCode = generateReferralCodeForUser(referrer);

        // Process the referral
        return referralService.processReferralSignup(referralCode, referee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsTransactionDTO> getPointsHistory(User user, int limit) {
        return pointsService.getUserTransactionHistory(user, limit);
    }

    private String generateReferralCodeForUser(User user) {
        // Generate a simple referral code based on user ID and username
        return "REF" + user.getId() + user.getEmail().substring(0, Math.min(3, user.getEmail().indexOf('@')));
    }
}
