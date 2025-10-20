package com.samjhadoo.service;

import com.samjhadoo.dto.analytics.DashboardStats;
import com.samjhadoo.dto.analytics.RevenueStats;
import com.samjhadoo.dto.analytics.UserGrowthStats;
import com.samjhadoo.model.enums.SessionStatus;
import com.samjhadoo.repository.SessionRepository;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardStats", unless = "#result == null")
    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalSessions = sessionRepository.count();
        long activeSessions = sessionRepository.countByStatus(SessionStatus.IN_PROGRESS);
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();

        return DashboardStats.builder()
                .totalUsers(totalUsers)
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserGrowthStats getUserGrowthStats(LocalDateTime startDate, LocalDateTime endDate) {
        long newUsers = userRepository.countByCreatedAtBetween(startDate, endDate);
        long totalUsers = userRepository.count();

        return UserGrowthStats.builder()
                .newUsers(newUsers)
                .totalUsers(totalUsers)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueStats getRevenueStats(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = paymentRepository.getRevenueBetween(startDate, endDate);
        long transactionCount = paymentRepository.countByCreatedAtBetween(startDate, endDate);

        return RevenueStats.builder()
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .transactionCount(transactionCount)
                .averageTransactionValue(transactionCount > 0 ? 
                    revenue.divide(BigDecimal.valueOf(transactionCount), 2, BigDecimal.ROUND_HALF_UP) : 
                    BigDecimal.ZERO)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSessionStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("scheduled", sessionRepository.countByStatus(SessionStatus.SCHEDULED));
        stats.put("inProgress", sessionRepository.countByStatus(SessionStatus.IN_PROGRESS));
        stats.put("completed", sessionRepository.countByStatus(SessionStatus.COMPLETED));
        stats.put("cancelled", sessionRepository.countByStatus(SessionStatus.CANCELLED));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getCommunityStats() {
        Map<String, Long> stats = new HashMap<>();
        // TODO: Add community-specific stats once the repository methods are available
        stats.put("totalCommunities", 0L);
        stats.put("activeCommunities", 0L);
        return stats;
    }
}
