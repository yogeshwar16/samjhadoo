package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.MoodDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.Mood;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import com.samjhadoo.repository.friendlytalk.MoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MoodServiceImpl implements MoodService {

    private final MoodRepository moodRepository;

    @Override
    public MoodDTO setUserMood(User user, MoodType moodType, int intensity, String description,
                              boolean anonymous, boolean lookingForTalk) {
        // Validate intensity
        if (intensity < 1 || intensity > 10) {
            throw new IllegalArgumentException("Intensity must be between 1 and 10");
        }

        // Find existing mood or create new one
        Mood mood = moodRepository.findByUser(user)
                .orElse(Mood.builder()
                        .user(user)
                        .build());

        // Update mood
        mood.updateMood(moodType, intensity, description, anonymous);
        mood.setLookingForTalk(lookingForTalk);

        Mood savedMood = moodRepository.save(mood);

        log.info("Updated mood for user {}: {} (intensity: {}, looking for talk: {})",
                user.getId(), moodType, intensity, lookingForTalk);

        return convertToDTO(savedMood);
    }

    @Override
    @Transactional(readOnly = true)
    public MoodDTO getUserMood(User user) {
        return moodRepository.findActiveMoodByUser(user, LocalDateTime.now())
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public boolean clearUserMood(User user) {
        Mood mood = moodRepository.findByUser(user).orElse(null);
        if (mood != null) {
            mood.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expire immediately
            moodRepository.save(mood);
            log.info("Cleared mood for user {}", user.getId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodDTO> findCompatibleMoods(User user, int limit) {
        Mood userMood = moodRepository.findActiveMoodByUser(user, LocalDateTime.now()).orElse(null);
        if (userMood == null) {
            return List.of();
        }

        return moodRepository.findActiveLookingForTalk(LocalDateTime.now()).stream()
                .filter(m -> !m.getUser().getId().equals(user.getId()))
                .filter(m -> areMoodsCompatible(userMood, m))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodDTO> findHighIntensityMoods(int minIntensity, int limit) {
        return moodRepository.findHighIntensityLookingForTalk(minIntensity, LocalDateTime.now()).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodDTO> getActiveMoodsLookingForTalk(int limit) {
        return moodRepository.findActiveLookingForTalk(LocalDateTime.now()).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodDTO> getMoodsByType(MoodType moodType, int limit) {
        return moodRepository.findByMoodTypeLookingForTalk(moodType, LocalDateTime.now()).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int expireOldMoods() {
        List<Mood> expiredMoods = moodRepository.findExpiredMoods(LocalDateTime.now());
        int count = expiredMoods.size();

        if (!expiredMoods.isEmpty()) {
            moodRepository.deleteAll(expiredMoods);
            log.info("Expired {} old moods", count);
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMoodStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalActive = moodRepository.countActiveLookingForTalk(LocalDateTime.now());
        stats.put("totalActiveMoods", totalActive);

        // Mood type distribution
        Map<String, Long> moodDistribution = new HashMap<>();
        for (MoodType moodType : MoodType.values()) {
            long count = moodRepository.findByMoodTypeLookingForTalk(moodType, LocalDateTime.now()).size();
            moodDistribution.put(moodType.name(), count);
        }
        stats.put("moodDistribution", moodDistribution);

        // Intensity distribution
        Map<String, Long> intensityDistribution = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            long count = moodRepository.findHighIntensityLookingForTalk(i, LocalDateTime.now()).size();
            intensityDistribution.put(String.valueOf(i), count);
        }
        stats.put("intensityDistribution", intensityDistribution);

        return stats;
    }

    @Override
    public boolean areMoodsCompatible(Mood mood1, Mood mood2) {
        // Same mood types are generally compatible
        if (mood1.getMoodType() == mood2.getMoodType()) {
            return true;
        }

        // Calculate compatibility score
        int score = calculateMoodCompatibility(mood1, mood2);

        // Consider moods compatible if score is above threshold (70)
        return score >= 70;
    }

    @Override
    public int calculateMoodCompatibility(Mood mood1, Mood mood2) {
        int score = 0;

        // Base compatibility by mood type similarity
        if (mood1.getMoodType() == mood2.getMoodType()) {
            score += 50;
        }

        // Intensity similarity (closer intensities are more compatible)
        int intensityDiff = Math.abs(mood1.getIntensity() - mood2.getIntensity());
        score += Math.max(0, 30 - intensityDiff * 3); // Max 30 points for intensity similarity

        // Both users looking for talk is good
        if (mood1.isLookingForTalk() && mood2.isLookingForTalk()) {
            score += 20;
        }

        return Math.min(100, score);
    }

    private MoodDTO convertToDTO(Mood mood) {
        return MoodDTO.builder()
                .id(mood.getId())
                .userId(mood.getUser().getId().toString())
                .userName(mood.isAnonymous() ? "Anonymous" :
                         mood.getUser().getFirstName() + " " + mood.getUser().getLastName())
                .moodType(mood.getMoodType())
                .intensity(mood.getIntensity())
                .description(mood.getDescription())
                .anonymous(mood.isAnonymous())
                .lookingForTalk(mood.isLookingForTalk())
                .createdAt(mood.getCreatedAt())
                .expiresAt(mood.getExpiresAt())
                .lastUpdated(mood.getLastUpdated())
                .active(mood.isActive())
                .build();
    }
}
