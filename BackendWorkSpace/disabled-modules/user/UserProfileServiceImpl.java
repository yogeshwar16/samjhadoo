package com.samjhadoo.service.user;

import com.samjhadoo.dto.user.*;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.mapper.UserProfileMapper;
import com.samjhadoo.model.user.*;
import com.samjhadoo.repository.UserProfileRepository;
import com.samjhadoo.service.FileStorageService;
import com.samjhadoo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserProfileMapper profileMapper;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileDTO getProfile(String userId) {
        log.debug("Fetching profile for user: {}", userId);
        UserProfile profile = findProfileOrThrow(userId);
        return profileMapper.toDTO(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public UserProfileDTO updateProfile(String userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);
        UserProfile profile = findProfileOrThrow(userId);
        
        // Update basic profile information
        profile.setDisplayName(request.getDisplayName());
        profile.setHeadline(request.getHeadline());
        profile.setBio(request.getBio());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        
        // Handle skills update if present
        if (request.getSkills() != null) {
            updateSkills(profile, request.getSkills());
        }
        
        // Publish profile updated event
        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, "basic_info"));
        
        return profileMapper.toDTO(profileRepository.save(profile));
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void updateProfileImage(String userId, MultipartFile imageFile) {
        log.info("Updating profile image for user: {}", userId);
        UserProfile profile = findProfileOrThrow(userId);
        
        // Upload new image
        String newImageUrl = fileStorageService.storeFile("profiles/" + userId, imageFile);
        
        // Delete old image if exists
        if (profile.getProfileImageUrl() != null) {
            fileStorageService.deleteFile(profile.getProfileImageUrl());
        }
        
        // Update profile
        profile.setProfileImageUrl(newImageUrl);
        profileRepository.save(profile);
        
        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, "profile_image"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> searchProfiles(ProfileSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching profiles with criteria: {}", criteria);
        return profileRepository.searchProfiles(criteria, pageable)
                .map(profileMapper::toDTO);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> processProfileVerification(String userId, MultipartFile idDocument) {
        log.info("Processing verification for user: {}", userId);
        UserProfile profile = findProfileOrThrow(userId);
        
        // Upload document
        String documentUrl = fileStorageService.storeFile("verification/" + userId, idDocument);
        
        // Create or update verification details
        VerificationDetails verification = profile.getVerificationDetails();
        if (verification == null) {
            verification = new VerificationDetails();
            verification.setUserProfile(profile);
            profile.setVerificationDetails(verification);
        }
        
        verification.setStatus(VerificationStatus.PENDING);
        verification.setIdDocumentUrl(documentUrl);
        verification.setVerificationNotes("Verification submitted");
        
        profileRepository.save(profile);
        
        // Notify admin about new verification request
        notificationService.sendVerificationRequestNotification(userId);
        
        return CompletableFuture.completedFuture(null);
    }

    // Helper methods
    private UserProfile findProfileOrThrow(String userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
    }
    
    private void updateSkills(UserProfile profile, Set<UserSkillDTO> skillDTOs) {
        // Clear existing skills
        profile.getSkills().clear();
        
        // Add new skills
        skillDTOs.forEach(skillDTO -> {
            UserSkill skill = new UserSkill();
            skill.setName(skillDTO.getName());
            skill.setLevel(skillDTO.getLevel());
            skill.setYearsOfExperience(skillDTO.getYearsOfExperience());
            skill.setPrimary(skillDTO.isPrimary());
            profile.addSkill(skill);
        });
    }
}
