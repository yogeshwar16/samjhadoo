package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.gamification.*;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Achievement;
import com.samjhadoo.model.gamification.Badge;
import com.samjhadoo.service.gamification.AchievementService;
import com.samjhadoo.service.gamification.BadgeService;
import com.samjhadoo.service.gamification.GamificationService;
import com.samjhadoo.service.gamification.PointsService;
import com.samjhadoo.service.gamification.ReferralService;
import com.samjhadoo.service.gamification.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin API controller for gamification management.
 * Provides endpoints for administrators to manage gamification features.
 */
@RestController
@RequestMapping("/api/admin/gamification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Gamification", description = "Admin gamification management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGamificationController {

    private final GamificationService gamificationService;
    private final BadgeService badgeService;
    private final AchievementService achievementService;
    private final PointsService pointsService;
    private final StreakService streakService;
    private final ReferralService referralService;

    // Badge Management
    @PostMapping("/badges")
    @Operation(summary = "Create new badge",
               description = "Creates a new badge that users can earn")
    public ResponseEntity<BadgeDTO> createBadge(@RequestBody BadgeDTO badgeDTO) {
        try {
            Badge badge = badgeService.createBadge(badgeDTO);
            BadgeDTO response = badgeService.getBadgeById(badge.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating badge: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/badges/{badgeId}")
    @Operation(summary = "Update badge",
               description = "Updates an existing badge")
    public ResponseEntity<BadgeDTO> updateBadge(
            @PathVariable Long badgeId,
            @RequestBody BadgeDTO badgeDTO) {
        try {
            Badge badge = badgeService.updateBadge(badgeId, badgeDTO);
            if (badge == null) {
                return ResponseEntity.notFound().build();
            }
            BadgeDTO response = badgeService.getBadgeById(badge.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating badge {}: {}", badgeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/badges/{badgeId}")
    @Operation(summary = "Deactivate badge",
               description = "Deactivates a badge so it can no longer be earned")
    public ResponseEntity<Void> deactivateBadge(@PathVariable Long badgeId) {
        try {
            boolean deactivated = badgeService.deactivateBadge(badgeId);
            if (!deactivated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deactivating badge {}: {}", badgeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/badges/{badgeId}/award/{userId}")
    @Operation(summary = "Award badge to user",
               description = "Manually awards a badge to a specific user")
    public ResponseEntity<Void> awardBadgeToUser(
            @PathVariable Long badgeId,
            @PathVariable Long userId,
            @RequestParam String reason) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            boolean awarded = badgeService.awardBadgeToUser(null, badgeId, reason);
            if (!awarded) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error awarding badge {} to user {}: {}", badgeId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Achievement Management
    @PostMapping("/achievements")
    @Operation(summary = "Create new achievement",
               description = "Creates a new achievement that users can complete")
    public ResponseEntity<AchievementDTO> createAchievement(@RequestBody AchievementDTO achievementDTO) {
        try {
            Achievement achievement = achievementService.createAchievement(achievementDTO);
            AchievementDTO response = achievementService.getAchievementById(achievement.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating achievement: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/achievements/{achievementId}")
    @Operation(summary = "Update achievement",
               description = "Updates an existing achievement")
    public ResponseEntity<AchievementDTO> updateAchievement(
            @PathVariable Long achievementId,
            @RequestBody AchievementDTO achievementDTO) {
        try {
            Achievement achievement = achievementService.updateAchievement(achievementId, achievementDTO);
            if (achievement == null) {
                return ResponseEntity.notFound().build();
            }
            AchievementDTO response = achievementService.getAchievementById(achievement.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating achievement {}: {}", achievementId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/achievements/{achievementId}")
    @Operation(summary = "Deactivate achievement",
               description = "Deactivates an achievement so it can no longer be completed")
    public ResponseEntity<Void> deactivateAchievement(@PathVariable Long achievementId) {
        try {
            boolean deactivated = achievementService.deactivateAchievement(achievementId);
            if (!deactivated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deactivating achievement {}: {}", achievementId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/achievements/{achievementId}/award/{userId}")
    @Operation(summary = "Award achievement to user",
               description = "Manually awards an achievement to a specific user")
    public ResponseEntity<Void> awardAchievementToUser(
            @PathVariable Long achievementId,
            @PathVariable Long userId) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            boolean awarded = achievementService.awardAchievementToUser(null, achievementId);
            if (!awarded) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error awarding achievement {} to user {}: {}", achievementId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Points Management
    @PostMapping("/points/award/{userId}")
    @Operation(summary = "Award points to user",
               description = "Manually awards points to a specific user")
    public ResponseEntity<BigDecimal> awardPointsToUser(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String description) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            BigDecimal newBalance = pointsService.awardPoints(null,
                    amount,
                    com.samjhadoo.model.enums.gamification.PointsReason.valueOf(reason.toUpperCase()),
                    referenceId,
                    description);
            return ResponseEntity.ok(newBalance);
        } catch (Exception e) {
            log.error("Error awarding points to user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/points/deduct/{userId}")
    @Operation(summary = "Deduct points from user",
               description = "Manually deducts points from a specific user")
    public ResponseEntity<BigDecimal> deductPointsFromUser(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String description) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            BigDecimal newBalance = pointsService.deductPoints(null,
                    amount,
                    com.samjhadoo.model.enums.gamification.PointsReason.valueOf(reason.toUpperCase()),
                    referenceId,
                    description);
            return ResponseEntity.ok(newBalance);
        } catch (Exception e) {
            log.error("Error deducting points from user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/points/reverse/{transactionId}")
    @Operation(summary = "Reverse points transaction",
               description = "Reverses a points transaction")
    public ResponseEntity<Boolean> reverseTransaction(
            @PathVariable Long transactionId,
            @RequestParam String reason) {
        try {
            boolean reversed = pointsService.reverseTransaction(transactionId, reason);
            return ResponseEntity.ok(reversed);
        } catch (Exception e) {
            log.error("Error reversing transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/points/process-expired")
    @Operation(summary = "Process expired transactions",
               description = "Processes expired points transactions")
    public ResponseEntity<Integer> processExpiredTransactions() {
        try {
            int processed = pointsService.processExpiredTransactions();
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            log.error("Error processing expired transactions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Streak Management
    @PostMapping("/streaks/reset/{userId}")
    @Operation(summary = "Reset user streak",
               description = "Resets a user's streak")
    public ResponseEntity<Boolean> resetUserStreak(
            @PathVariable Long userId,
            @RequestParam String reason) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            boolean reset = streakService.resetUserStreak(null, reason);
            return ResponseEntity.ok(reset);
        } catch (Exception e) {
            log.error("Error resetting streak for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/streaks/statistics")
    @Operation(summary = "Get streak statistics",
               description = "Retrieves statistics about user streaks")
    public ResponseEntity<Map<String, Object>> getStreakStatistics() {
        try {
            Map<String, Object> statistics = streakService.getStreakStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting streak statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Referral Management
    @PostMapping("/referrals/expire")
    @Operation(summary = "Expire old referrals",
               description = "Expires old pending referrals")
    public ResponseEntity<Integer> expireOldReferrals() {
        try {
            int expired = referralService.expireOldReferrals();
            return ResponseEntity.ok(expired);
        } catch (Exception e) {
            log.error("Error expiring old referrals: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referrals/statistics")
    @Operation(summary = "Get referral statistics",
               description = "Retrieves global referral statistics")
    public ResponseEntity<Map<String, Object>> getReferralStatistics() {
        try {
            Map<String, Object> statistics = referralService.getGlobalReferralStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting referral statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referrals/top-referrers")
    @Operation(summary = "Get top referrers",
               description = "Retrieves users with the most successful referrals")
    public ResponseEntity<List<LeaderboardDTO>> getTopReferrers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<LeaderboardDTO> topReferrers = referralService.getTopReferrers(limit);
            return ResponseEntity.ok(topReferrers);
        } catch (Exception e) {
            log.error("Error getting top referrers: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // General Statistics
    @GetMapping("/statistics")
    @Operation(summary = "Get gamification statistics",
               description = "Retrieves overall gamification statistics")
    public ResponseEntity<Map<String, Object>> getGamificationStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // Points statistics
            statistics.put("totalPointsBalance", pointsService.getTotalPointsBalance());
            statistics.put("pointsAccountsCount", pointsService.getTopPointsAccounts(1).size());

            // Streak statistics
            statistics.putAll(streakService.getStreakStatistics());

            // Referral statistics
            statistics.putAll(referralService.getGlobalReferralStatistics());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting gamification statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/leaderboards")
    @Operation(summary = "Get all leaderboards",
               description = "Retrieves all leaderboard data")
    public ResponseEntity<Map<String, List<LeaderboardDTO>>> getAllLeaderboards(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, List<LeaderboardDTO>> leaderboards = new java.util.HashMap<>();

            leaderboards.put("points", gamificationService.getPointsLeaderboard(limit));
            leaderboards.put("streaks", gamificationService.getStreakLeaderboard(limit));
            leaderboards.put("badges", gamificationService.getBadgeLeaderboard(limit));

            return ResponseEntity.ok(leaderboards);
        } catch (Exception e) {
            log.error("Error getting all leaderboards: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/achievements/{achievementId}/reset/{userId}")
    @Operation(summary = "Reset user achievement progress",
               description = "Resets progress for a repeatable achievement")
    public ResponseEntity<Boolean> resetUserAchievementProgress(
            @PathVariable Long achievementId,
            @PathVariable Long userId) {
        try {
            // In a real implementation, you'd fetch the user by ID
            // For now, we'll assume the user exists and proceed
            boolean reset = achievementService.resetAchievementProgress(null, achievementId);
            return ResponseEntity.ok(reset);
        } catch (Exception e) {
            log.error("Error resetting achievement progress for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/badges/{badgeId}/users")
    @Operation(summary = "Get users with badge",
               description = "Retrieves users who have earned a specific badge")
    public ResponseEntity<Long> getBadgeUserCount(@PathVariable Long badgeId) {
        try {
            long count = badgeService.getBadgeUserCount(badgeId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting badge user count for badge {}: {}", badgeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievements/{achievementId}/users")
    @Operation(summary = "Get users with achievement",
               description = "Retrieves users who have completed a specific achievement")
    public ResponseEntity<Long> getAchievementUserCount(@PathVariable Long achievementId) {
        try {
            long count = achievementService.getAchievementUserCount(achievementId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting achievement user count for achievement {}: {}", achievementId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
