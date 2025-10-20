package com.samjhadoo.service;

import com.samjhadoo.dto.analytics.DashboardStats;
import com.samjhadoo.dto.analytics.RevenueStats;
import com.samjhadoo.dto.analytics.UserGrowthStats;

import java.time.LocalDateTime;
import java.util.Map;

public interface AnalyticsService {

    DashboardStats getDashboardStats();

    UserGrowthStats getUserGrowthStats(LocalDateTime startDate, LocalDateTime endDate);

    RevenueStats getRevenueStats(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getSessionStats();

    Map<String, Long> getCommunityStats();
}
