package com.samjhadoo.model.gamification;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Tracks user login streaks and engagement patterns.
 */
@Entity
@Table(name = "user_streaks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "current_streak_days", nullable = false)
    private int currentStreakDays;
    
    @Column(name = "max_streak_days", nullable = false)
    private int maxStreakDays;
    
    @Column(name = "last_activity_date", nullable = false)
    private LocalDate lastActivityDate;
    
    @Column(name = "total_logins", nullable = false)
    private int totalLogins;
    
    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;
    
    /**
     * Updates the streak based on a new login date.
     * @param loginDate The date of the new login
     * @return true if the streak was updated, false if it was already updated today
     */
    public boolean updateStreak(LocalDate loginDate) {
        // Don't update if already logged in today
        if (loginDate.equals(lastLoginDate)) {
            return false;
        }
        
        // Update login tracking
        totalLogins++;
        lastLoginDate = loginDate;
        
        // If last activity was yesterday, increment streak
        if (loginDate.minusDays(1).equals(lastActivityDate)) {
            currentStreakDays++;
            // Update max streak if needed
            if (currentStreakDays > maxStreakDays) {
                maxStreakDays = currentStreakDays;
            }
        } 
        // If last activity was today, do nothing (already handled by first check)
        // If there was a gap of more than one day, reset streak
        else if (!loginDate.equals(lastActivityDate)) {
            currentStreakDays = 1;
        }
        
        lastActivityDate = loginDate;
        return true;
    }
    
    @PrePersist
    protected void onCreate() {
        if (lastActivityDate == null) {
            lastActivityDate = LocalDate.now();
        }
        if (currentStreakDays == 0) {
            currentStreakDays = 1;
            maxStreakDays = 1;
        }
        if (totalLogins == 0) {
            totalLogins = 1;
        }
    }
}
