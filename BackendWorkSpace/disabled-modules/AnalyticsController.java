package com.samjhadoo.controller.api;

import com.samjhadoo.dto.analytics.*;
import com.samjhadoo.service.AnalyticsService;
import com.samjhadoo.service.impl.AnalyticsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsServiceImpl analyticsServiceImpl;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    @GetMapping("/users/growth")
    public ResponseEntity<UserGrowthStats> getUserGrowthStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getUserGrowthStats(startDate, endDate));
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueStats> getRevenueStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getRevenueStats(startDate, endDate));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Long>> getSessionStats() {
        return ResponseEntity.ok(analyticsService.getSessionStats());
    }

    @GetMapping("/communities")
    public ResponseEntity<Map<String, Long>> getCommunityStats() {
        return ResponseEntity.ok(analyticsService.getCommunityStats());
    }

    // New user-specific analytics endpoints
    @GetMapping("/user/{userId}/dashboard")
    public ResponseEntity<DashboardDataDTO> getUserDashboard(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsServiceImpl.getUserDashboard(userId));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionAnalyticsDTO> getSessionAnalytics(@PathVariable String sessionId) {
        return ResponseEntity.ok(analyticsServiceImpl.getSessionAnalytics(sessionId));
    }

    @GetMapping("/user/{userId}/engagement")
    public ResponseEntity<Map<String, Object>> getEngagementMetrics(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsServiceImpl.getEngagementMetrics(userId));
    }
}
