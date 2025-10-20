package com.samjhadoo.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserGrowthStats {
    private long newUsers;
    private long totalUsers;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
