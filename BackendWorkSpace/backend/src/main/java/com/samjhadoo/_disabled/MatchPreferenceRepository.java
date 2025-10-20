package com.samjhadoo.repository;

import com.samjhadoo.model.User;
import com.samjhadoo.model.ai.MatchPreference;
import com.samjhadoo.model.enums.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchPreferenceRepository extends JpaRepository<MatchPreference, String> {
    Optional<MatchPreference> findByUserId(Long userId);
    
    default MatchPreference getOrCreateDefault(User user) {
        return findByUserId(user.getId())
                .orElseGet(() -> MatchPreference.builder()
                        .user(user)
                        .preferredSessionType(SessionType.valueOf(user.getDefaultSessionType()))
                        .minMentorRating(4.0)
                        .onlyVerifiedMentors(true)
                        .build());
    }
}
