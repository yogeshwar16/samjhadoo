package com.samjhadoo.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStats {
    private long totalUsers;
    private long totalSessions;
    private long activeSessions;
    private BigDecimal totalRevenue;
}
