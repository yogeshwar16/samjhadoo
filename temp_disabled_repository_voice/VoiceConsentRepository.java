package com.samjhadoo.repository.voice;

import com.samjhadoo.model.User;
import com.samjhadoo.model.voice.VoiceConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceConsentRepository extends JpaRepository<VoiceConsent, Long> {
    
    Optional<VoiceConsent> findByUserAndActiveTrue(User user);
    
    List<VoiceConsent> findByUser(User user);
    
    @Query("SELECT vc FROM VoiceConsent vc WHERE vc.user = :user AND vc.active = true AND vc.consentGiven = true")
    Optional<VoiceConsent> findActiveConsentByUser(@Param("user") User user);
    
    boolean existsByUserAndActiveTrueAndConsentGivenTrue(User user);
    
    @Modifying
    @Query("UPDATE VoiceConsent vc SET vc.active = false WHERE vc.user = :user")
    int deactivateAllConsents(@Param("user") User user);
    
    @Query("SELECT COUNT(vc) FROM VoiceConsent vc WHERE vc.active = true AND vc.consentGiven = true")
    long countActiveConsents();
}
