package com.samjhadoo.controller.api.gamification;

import com.samjhadoo.dto.gamification.*;
import com.samjhadoo.model.User;
import com.samjhadoo.service.gamification.GamificationService;
import com.samjhadoo.service.gamification.BadgeService;
import com.samjhadoo.service.gamification.AchievementService;
import com.samjhadoo.service.gamification.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API controller for gamification features.
 * Provides endpoints for users to access their gamification data.
 */
@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gamification", description = "User gamification features")
public class GamificationController {

    private final GamificationService gamificationService;
    private final BadgeService badgeService;
    private final AchievementService achievementService;
    private final ReferralService referralService;

    @GetMapping("/profile")
    @Operation(summary = "Get user gamification profile",
               description = "Retrieves the complete gamification profile for the authenticated user")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            UserProfileDTO profile = gamificationService.getUserProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting user profile for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/badges")
    @Operation(summary = "Get user badges",
               description = "Retrieves all badges earned by the authenticated user")
    public ResponseEntity<List<UserBadgeDTO>> getUserBadges(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<UserBadgeDTO> badges = badgeService.getUserBadges(user);
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            log.error("Error getting user badges for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/badges/recent")
    @Operation(summary = "Get recent user badges",
               description = "Retrieves the most recently earned badges for the authenticated user")
    public ResponseEntity<List<UserBadgeDTO>> getRecentUserBadges(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<UserBadgeDTO> badges = badgeService.getRecentUserBadges(user, limit);
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            log.error("Error getting recent user badges for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievements")
    @Operation(summary = "Get user achievements",
               description = "Retrieves all achievements (completed and in progress) for the authenticated user")
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievements(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<UserAchievementDTO> achievements = achievementService.getUserAchievements(user);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            log.error("Error getting user achievements for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievements/in-progress")
    @Operation(summary = "Get in-progress achievements",
               description = "Retrieves achievements that are currently in progress for the authenticated user")
    public ResponseEntity<List<UserAchievementDTO>> getInProgressAchievements(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<UserAchievementDTO> achievements = achievementService.getUserAchievementsInProgress(user);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            log.error("Error getting in-progress achievements for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievements/completed")
    @Operation(summary = "Get completed achievements",
               description = "Retrieves achievements that have been completed by the authenticated user")
    public ResponseEntity<List<UserAchievementDTO>> getCompletedAchievements(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<UserAchievementDTO> achievements = achievementService.getUserAchievementsCompleted(user);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            log.error("Error getting completed achievements for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard",
               description = "Retrieves leaderboard data for different categories")
    public ResponseEntity<List<LeaderboardDTO>> getLeaderboard(
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<LeaderboardDTO> leaderboard;

            switch (type.toLowerCase()) {
                case "streaks":
                case "streak":
                    leaderboard = gamificationService.getStreakLeaderboard(limit);
                    break;
                case "badges":
                case "badge":
                    leaderboard = gamificationService.getBadgeLeaderboard(limit);
                    break;
                case "points":
                default:
                    leaderboard = gamificationService.getPointsLeaderboard(limit);
                    break;
            }

            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            log.error("Error getting leaderboard for type {}: {}", type, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/streak/update")
    @Operation(summary = "Update user streak",
               description = "Updates the user's login streak for today")
    public ResponseEntity<Boolean> updateStreak(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            boolean updated = gamificationService.updateStreak(user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating streak for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/referral")
    @Operation(summary = "Create referral",
               description = "Creates a referral code for the authenticated user to share")
    public ResponseEntity<String> createReferral(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam String email) {
        try {
            String referralCode = referralService.createReferral(user, email);
            return ResponseEntity.ok(referralCode);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid referral request for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating referral for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referral/{code}")
    @Operation(summary = "Get referral info",
               description = "Retrieves information about a referral code")
    public ResponseEntity<ReferralDTO> getReferralInfo(@PathVariable String code) {
        try {
            ReferralDTO referral = referralService.getReferralByCode(code);
            if (referral == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(referral);
        } catch (Exception e) {
            log.error("Error getting referral info for code {}: {}", code, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referrals")
    @Operation(summary = "Get user referrals",
               description = "Retrieves all referrals for the authenticated user")
    public ResponseEntity<List<ReferralDTO>> getUserReferrals(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<ReferralDTO> referrals = referralService.getUserReferrals(user);
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting user referrals for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referrals/pending")
    @Operation(summary = "Get pending referrals",
               description = "Retrieves pending referrals for the authenticated user")
    public ResponseEntity<List<ReferralDTO>> getPendingReferrals(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<ReferralDTO> referrals = referralService.getPendingReferrals(user);
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting pending referrals for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/referrals/completed")
    @Operation(summary = "Get completed referrals",
               description = "Retrieves completed referrals for the authenticated user")
    public ResponseEntity<List<ReferralDTO>> getCompletedReferrals(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        try {
            List<ReferralDTO> referrals = referralService.getCompletedReferrals(user);
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting completed referrals for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/points/history")
    @Operation(summary = "Get points transaction history",
               description = "Retrieves the points transaction history for the authenticated user")
    public ResponseEntity<List<PointsTransactionDTO>> getPointsHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<PointsTransactionDTO> transactions = gamificationService.getPointsHistory(user, limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting points history for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/badges/all")
    @Operation(summary = "Get all available badges",
               description = "Retrieves all active badges available in the system")
    public ResponseEntity<List<BadgeDTO>> getAllBadges() {
        try {
            List<BadgeDTO> badges = badgeService.getAllActiveBadges();
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            log.error("Error getting all badges: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievements/all")
    @Operation(summary = "Get all available achievements",
               description = "Retrieves all active achievements available in the system")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        try {
            List<AchievementDTO> achievements = achievementService.getAllActiveAchievements();
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            log.error("Error getting all achievements: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
