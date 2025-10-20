package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.BadgeDTO;
import com.samjhadoo.dto.gamification.UserBadgeDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.gamification.Badge;
import com.samjhadoo.model.gamification.UserBadge;
import com.samjhadoo.model.enums.gamification.BadgeType;
import com.samjhadoo.repository.gamification.BadgeRepository;
import com.samjhadoo.repository.gamification.UserBadgeRepository;
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
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getAllActiveBadges() {
        return badgeRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getBadgesByType(BadgeType type) {
        return badgeRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeDTO getBadgeById(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadgeDTO> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByAwardedAtDesc(user).stream()
                .map(this::convertToUserBadgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadgeDTO> getRecentUserBadges(User user, int limit) {
        return userBadgeRepository.findByUserOrderByAwardedAtDesc(user).stream()
                .limit(limit)
                .map(this::convertToUserBadgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean awardBadgeToUser(User user, Long badgeId, String reason) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        if (badge == null || !badge.isActive()) {
            return false;
        }

        // Check if user already has this badge
        if (userBadgeRepository.existsByUserAndBadge(user, badge)) {
            log.info("User {} already has badge {}", user.getId(), badgeId);
            return false;
        }

        // Award the badge
        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .awardedAt(LocalDateTime.now())
                .awardedFor(reason)
                .notified(false)
                .build();

        userBadgeRepository.save(userBadge);

        log.info("Awarded badge {} to user {} for reason: {}", badgeId, user.getId(), reason);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasBadge(User user, Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        if (badge == null) {
            return false;
        }
        return userBadgeRepository.existsByUserAndBadge(user, badge);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBadgeUserCount(Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        if (badge == null) {
            return 0;
        }
        return userBadgeRepository.countUsersWithBadge(badge);
    }

    @Override
    public Badge createBadge(BadgeDTO badgeDTO) {
        Badge badge = Badge.builder()
                .name(badgeDTO.getName())
                .type(badgeDTO.getType())
                .description(badgeDTO.getDescription())
                .iconUrl(badgeDTO.getIconUrl())
                .pointsValue(badgeDTO.getPointsValue())
                .active(badgeDTO.isActive())
                .build();

        return badgeRepository.save(badge);
    }

    @Override
    public Badge updateBadge(Long badgeId, BadgeDTO badgeDTO) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        if (badge == null) {
            return null;
        }

        badge.setName(badgeDTO.getName());
        badge.setType(badgeDTO.getType());
        badge.setDescription(badgeDTO.getDescription());
        badge.setIconUrl(badgeDTO.getIconUrl());
        badge.setPointsValue(badgeDTO.getPointsValue());
        badge.setActive(badgeDTO.isActive());

        return badgeRepository.save(badge);
    }

    @Override
    public boolean deactivateBadge(Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        if (badge == null) {
            return false;
        }

        badge.setActive(false);
        badgeRepository.save(badge);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadgeDTO> getUnnotifiedUserBadges(User user) {
        return userBadgeRepository.findUnnotifiedByUser(user).stream()
                .map(this::convertToUserBadgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markBadgesAsNotified(User user, List<Long> badgeIds) {
        List<UserBadge> userBadges = userBadgeRepository.findAllById(badgeIds);
        userBadges.forEach(badge -> {
            if (badge.getUser().getId().equals(user.getId())) {
                badge.setNotified(true);
            }
        });
        userBadgeRepository.saveAll(userBadges);
    }

    private BadgeDTO convertToDTO(Badge badge) {
        return BadgeDTO.builder()
                .id(badge.getId())
                .name(badge.getName())
                .type(badge.getType())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .pointsValue(badge.getPointsValue())
                .active(badge.isActive())
                .createdAt(badge.getCreatedAt())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }

    private UserBadgeDTO convertToUserBadgeDTO(UserBadge userBadge) {
        return UserBadgeDTO.builder()
                .id(userBadge.getId())
                .badgeName(userBadge.getBadge().getName())
                .badgeType(userBadge.getBadge().getType())
                .badgeDescription(userBadge.getBadge().getDescription())
                .badgeIconUrl(userBadge.getBadge().getIconUrl())
                .badgePointsValue(userBadge.getBadge().getPointsValue())
                .awardedAt(userBadge.getAwardedAt())
                .awardedFor(userBadge.getAwardedFor())
                .notified(userBadge.isNotified())
                .build();
    }
}
