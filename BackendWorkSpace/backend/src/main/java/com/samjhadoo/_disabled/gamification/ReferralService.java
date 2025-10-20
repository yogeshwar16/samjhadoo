package com.samjhadoo.service.gamification;

import com.samjhadoo.dto.gamification.ReferralDTO;
import com.samjhadoo.model.User;

import java.util.List;

/**
 * Service for managing the referral program.
 */
public interface ReferralService {

    /**
     * Creates a referral for a user.
     * @param referrer The user making the referral
     * @param refereeEmail The email of the person being referred
     * @return The created referral code
     */
    String createReferral(User referrer, String refereeEmail);

    /**
     * Gets all referrals for a user.
     * @param user The user
     * @return List of user's referrals
     */
    List<ReferralDTO> getUserReferrals(User user);

    /**
     * Gets pending referrals for a user.
     * @param user The user
     * @return List of pending referrals
     */
    List<ReferralDTO> getPendingReferrals(User user);

    /**
     * Gets completed referrals for a user.
     * @param user The user
     * @return List of completed referrals
     */
    List<ReferralDTO> getCompletedReferrals(User user);

    /**
     * Processes a referral when the referee signs up.
     * @param referralCode The referral code used
     * @param referee The user who was referred
     * @return true if referral was processed successfully
     */
    boolean processReferralSignup(String referralCode, User referee);

    /**
     * Gets a referral by code.
     * @param code The referral code
     * @return The referral DTO or null if not found
     */
    ReferralDTO getReferralByCode(String code);

    /**
     * Checks if a referral code is valid and active.
     * @param code The referral code
     * @return true if code is valid and active
     */
    boolean isValidReferralCode(String code);

    /**
     * Gets the referrer for a referral code.
     * @param code The referral code
     * @return The referrer user or null if code is invalid
     */
    User getReferrerByCode(String code);

    /**
     * Awards referral rewards when conditions are met.
     * @param referrer The referrer
     * @param referee The referee
     * @return true if rewards were awarded
     */
    boolean awardReferralRewards(User referrer, User referee);

    /**
     * Gets referral statistics for a user.
     * @param user The user
     * @return Map of referral statistics
     */
    java.util.Map<String, Object> getReferralStatistics(User user);

    /**
     * Gets global referral statistics.
     * @return Map of global referral statistics
     */
    java.util.Map<String, Object> getGlobalReferralStatistics();

    /**
     * Expires old pending referrals.
     * @return Number of referrals expired
     */
    int expireOldReferrals();

    /**
     * Generates a unique referral code.
     * @return A unique referral code
     */
    String generateUniqueReferralCode();

    /**
     * Validates if an email can be referred by a user.
     * @param referrer The referrer
     * @param refereeEmail The email to refer
     * @return true if email can be referred
     */
    boolean canReferEmail(User referrer, String refereeEmail);

    /**
     * Gets top referrers.
     * @param limit Maximum number of referrers to return
     * @return List of top referrers with their referral counts
     */
    List<com.samjhadoo.dto.gamification.LeaderboardDTO> getTopReferrers(int limit);
}
