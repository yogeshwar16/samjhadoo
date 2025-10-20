package com.samjhadoo.service.ai;

import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.MatchPreference;
import com.samjhadoo.repository.MatchPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchPreferenceService {

    private final MatchPreferenceRepository matchPreferenceRepository;
    private final AIMatchingService aiMatchingService;

    @Transactional(readOnly = true)
    public MatchPreference getUserPreferences(User user) {
        return matchPreferenceRepository.getOrCreateDefault(user);
    }

    @Transactional
    public MatchPreference updatePreferences(User user, Map<String, Object> updates) {
        MatchPreference preferences = matchPreferenceRepository.getOrCreateDefault(user);
        
        // Update fields based on the provided updates map
        if (updates.containsKey("preferredSkills")) {
            //noinspection unchecked
            preferences.setPreferredSkills(Set.copyOf((Iterable<String>) updates.get("preferredSkills")));
        }
        if (updates.containsKey("minMentorExperience")) {
            preferences.setMinMentorExperience((Integer) updates.get("minMentorExperience"));
        }
        if (updates.containsKey("maxHourlyRate")) {
            preferences.setMaxHourlyRate(((Number) updates.get("maxHourlyRate")).doubleValue());
        }
        if (updates.containsKey("onlyVerifiedMentors")) {
            preferences.setOnlyVerifiedMentors((Boolean) updates.get("onlyVerifiedMentors"));
        }
        if (updates.containsKey("minMentorRating")) {
            preferences.setMinMentorRating(((Number) updates.get("minMentorRating")).doubleValue());
        }
        if (updates.containsKey("preferredLanguages")) {
            //noinspection unchecked
            preferences.setPreferredLanguages(Set.copyOf((Iterable<String>) updates.get("preferredLanguages")));
        }
        if (updates.containsKey("interests")) {
            //noinspection unchecked
            preferences.setInterests(Set.copyOf((Iterable<String>) updates.get("interests")));
        }
        
        return matchPreferenceRepository.save(preferences);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMatchInsights(User user) {
        // This could be enhanced with more sophisticated analytics
        MatchPreference preferences = getUserPreferences(user);
        
        return Map.of(
            "preferences", preferences,
            "potentialMatchesCount", aiMatchingService.countPotentialMatches(user),
            "recommendedSkills", aiMatchingService.getRecommendedSkills(user),
            "popularMentors", aiMatchingService.getPopularMentors(user, 3)
        );
    }
}
