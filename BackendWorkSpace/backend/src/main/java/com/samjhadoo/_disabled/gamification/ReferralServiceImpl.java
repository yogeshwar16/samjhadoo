package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.ReferralDTO;
import com.samjhadoo.dto.gamification.LeaderboardDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Referral;
import com.samjhadoo.model.enums.gamification.PointsReason;
import com.samjhadoo.repository.gamification.ReferralRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReferralServiceImpl implements ReferralService {

    private final ReferralRepository referralRepository;
    private final PointsService pointsService;

    @Override
    public String createReferral(User referrer, String refereeEmail) {
        // Check if email can be referred
        if (!canReferEmail(referrer, refereeEmail)) {
            throw new IllegalArgumentException("Cannot refer this email");
        }

        // Generate unique code
        String code = generateUniqueReferralCode();

        // Create referral
        Referral referral = Referral.builder()
                .referrer(referrer)
                .refereeEmail(refereeEmail)
                .code(code)
                .status(Referral.ReferralStatus.PENDING)
                .build();

        referralRepository.save(referral);

        log.info("Created referral {} for user {} referring {}",
                code, referrer.getId(), refereeEmail);

        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> getUserReferrals(User user) {
        return referralRepository.findByReferrerOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> getPendingReferrals(User user) {
        return referralRepository.findByReferrerOrderByCreatedAtDesc(user).stream()
                .filter(r -> r.getStatus() == Referral.ReferralStatus.PENDING)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> getCompletedReferrals(User user) {
        return referralRepository.findByReferrerOrderByCreatedAtDesc(user).stream()
                .filter(r -> r.getStatus() == Referral.ReferralStatus.COMPLETED)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean processReferralSignup(String referralCode, User referee) {
        Referral referral = referralRepository.findByCode(referralCode).orElse(null);
        if (referral == null || referral.getStatus() != Referral.ReferralStatus.PENDING) {
            return false;
        }

        // Check if referral is still active
        if (!referral.isActive()) {
            referral.setStatus(Referral.ReferralStatus.EXPIRED);
            referralRepository.save(referral);
            return false;
        }

        // Complete the referral
        boolean completed = referral.completeReferral(referee);
        if (completed) {
            referralRepository.save(referral);

            // Award rewards
            awardReferralRewards(referral.getReferrer(), referee);

            log.info("Processed referral signup for code {}: referrer {}, referee {}",
                    referralCode, referral.getReferrer().getId(), referee.getId());
        }

        return completed;
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralDTO getReferralByCode(String code) {
        return referralRepository.findByCode(code)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidReferralCode(String code) {
        return referralRepository.findByCode(code)
                .map(Referral::isActive)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public User getReferrerByCode(String code) {
        return referralRepository.findByCode(code)
                .map(Referral::getReferrer)
                .orElse(null);
    }

    @Override
    public boolean awardReferralRewards(User referrer, User referee) {
        // Award points to referrer
        pointsService.awardPoints(referrer, java.math.BigDecimal.valueOf(100),
                PointsReason.REFERRAL_SIGNUP, "REFERRAL_" + referee.getId(),
                "Referral signup reward");

        // Award points to referee (signup bonus)
        pointsService.awardPoints(referee, java.math.BigDecimal.valueOf(50),
                PointsReason.REFERRAL_SIGNUP, "WELCOME_BONUS",
                "Welcome bonus for using referral");

        log.info("Awarded referral rewards: referrer {}, referee {}", referrer.getId(), referee.getId());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReferralStatistics(User user) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReferrals", referralRepository.countTotalReferralsByReferrer(user));
        stats.put("completedReferrals", referralRepository.countCompletedReferralsByReferrer(user));
        stats.put("pendingReferrals", referralRepository.findByReferrerOrderByCreatedAtDesc(user).stream()
                .mapToLong(r -> r.getStatus() == Referral.ReferralStatus.PENDING ? 1 : 0)
                .sum());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalReferralStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalReferrals = referralRepository.count();
        long completedReferrals = referralRepository.findByStatus(Referral.ReferralStatus.COMPLETED).size();
        long pendingReferrals = referralRepository.findByStatus(Referral.ReferralStatus.PENDING).size();

        stats.put("totalReferrals", totalReferrals);
        stats.put("completedReferrals", completedReferrals);
        stats.put("pendingReferrals", pendingReferrals);
        stats.put("expiredReferrals", referralRepository.findByStatus(Referral.ReferralStatus.EXPIRED).size());
        stats.put("conversionRate", totalReferrals > 0 ? (double) completedReferrals / totalReferrals * 100 : 0);

        return stats;
    }

    @Override
    public int expireOldReferrals() {
        List<Referral> expiredReferrals = referralRepository.findExpiredPendingReferrals(LocalDateTime.now());
        expiredReferrals.forEach(r -> {
            r.setStatus(Referral.ReferralStatus.EXPIRED);
            r.setUpdatedAt(LocalDateTime.now());
        });

        if (!expiredReferrals.isEmpty()) {
            referralRepository.saveAll(expiredReferrals);
            log.info("Expired {} old referrals", expiredReferrals.size());
        }

        return expiredReferrals.size();
    }

    @Override
    public String generateUniqueReferralCode() {
        String code;
        int attempts = 0;
        do {
            code = "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique referral code");
            }
        } while (referralRepository.existsByCode(code));

        return code;
    }

    @Override
    public boolean canReferEmail(User referrer, String refereeEmail) {
        // Check if referrer has already referred this email
        if (referralRepository.existsByReferrerAndRefereeEmail(referrer, refereeEmail)) {
            return false;
        }

        // Check if email is already a registered user (basic check)
        // In a real implementation, you'd check against the User repository
        // For now, we'll allow it but this could be enhanced

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getTopReferrers(int limit) {
        // This is a simplified implementation
        // In a real scenario, you'd have a more complex query to get referrers ordered by successful referrals
        return referralRepository.findByReferrerOrderByCreatedAtDesc(null).stream()
                .collect(Collectors.groupingBy(Referral::getReferrer))
                .entrySet().stream()
                .map(entry -> {
                    User referrer = entry.getKey();
                    long completedCount = entry.getValue().stream()
                            .mapToLong(r -> r.getStatus() == Referral.ReferralStatus.COMPLETED ? 1 : 0)
                            .sum();

                    return LeaderboardDTO.builder()
                            .userId(referrer.getId())
                            .userName(referrer.getFirstName() + " " + referrer.getLastName())
                            .userEmail(referrer.getEmail())
                            .rank(0) // Would need to be calculated
                            .points(java.math.BigDecimal.valueOf(completedCount * 100)) // Points based on referrals
                            .totalBadges(0) // Would need to be calculated
                            .totalAchievements(0) // Would need to be calculated
                            .build();
                })
                .sorted((a, b) -> b.getPoints().compareTo(a.getPoints()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private ReferralDTO convertToDTO(Referral referral) {
        return ReferralDTO.builder()
                .id(referral.getId())
                .referrerName(referral.getReferrer().getFirstName() + " " + referral.getReferrer().getLastName())
                .refereeName(referral.getReferee() != null ?
                        referral.getReferee().getFirstName() + " " + referral.getReferee().getLastName() : null)
                .refereeEmail(referral.getRefereeEmail())
                .code(referral.getCode())
                .status(referral.getStatus().name())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .completedAt(referral.getCompletedAt())
                .expiresAt(referral.getExpiresAt())
                .rewardAwarded(referral.isRewardAwarded())
                .rewardDescription(referral.getRewardDescription())
                .active(referral.isActive())
                .build();
    }
}
