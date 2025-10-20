package com.samjhadoo.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakDTO {
    private Long id;
    private int currentStreakDays;
    private int maxStreakDays;
    private LocalDate lastActivityDate;
    private int totalLogins;
    private LocalDate lastLoginDate;
    private boolean streakActiveToday;
}
