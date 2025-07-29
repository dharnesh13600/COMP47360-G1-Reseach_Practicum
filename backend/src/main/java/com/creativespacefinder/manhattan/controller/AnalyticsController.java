package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.entity.RequestAnalytics;
import com.creativespacefinder.manhattan.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get popular activity and time combinations
     */
    @GetMapping("/popular-combinations")
    public ResponseEntity<List<Map<String, Object>>> getPopularCombinations() {
        List<RequestAnalytics> popular = analyticsService.getPopularCombinations();

        List<Map<String, Object>> result = popular.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", r.getActivityName());
            map.put("hour", r.getRequestedHour());
            map.put("dayOfWeek", r.getRequestedDayOfWeek());
            map.put("requestCount", r.getRequestCount());
            map.put("lastRequested", r.getLastRequested());
            map.put("avgResponseTime", r.getResponseTimeMs());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Get cache performance statistics
     */
    @GetMapping("/cache-performance")
    public ResponseEntity<List<Map<String, Object>>> getCachePerformance() {
        List<Object[]> stats = analyticsService.getCachePerformanceStats();

        List<Map<String, Object>> result = stats.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", row[0]);
            map.put("hour", row[1]);
            map.put("cacheHitRate", String.format("%.1f%%", ((Double) row[2]) * 100));
            map.put("totalRequests", row[3]);
            map.put("avgResponseTime", row[4] != null ? Math.round((Double) row[4]) : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Get activity popularity trends
     */
    @GetMapping("/activity-trends")
    public ResponseEntity<List<Map<String, Object>>> getActivityTrends() {
        List<Object[]> trends = analyticsService.getActivityTrends();

        List<Map<String, Object>> result = trends.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", row[0]);
            map.put("totalRequests", row[1]);
            map.put("avgResponseTime", row[2] != null ? Math.round((Double) row[2]) : 0);
            map.put("lastRequested", row[3]);
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Get hourly usage patterns
     */
    @GetMapping("/hourly-patterns")
    public ResponseEntity<List<Map<String, Object>>> getHourlyPatterns() {
        List<Object[]> patterns = analyticsService.getHourlyUsagePatterns();

        List<Map<String, Object>> result = patterns.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("hour", row[0]);
            map.put("totalRequests", row[1]);
            map.put("uniqueActivities", row[2]);
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Get recent activity for 7 days
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        List<RequestAnalytics> recent = analyticsService.getRecentActivity();

        List<Map<String, Object>> result = recent.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activity", r.getActivityName());
            map.put("hour", r.getRequestedHour());
            map.put("requestCount", r.getRequestCount());
            map.put("lastRequested", r.getLastRequested());
            map.put("cacheHit", r.getCacheHit());
            map.put("responseTime", r.getResponseTimeMs());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    /**
     * Analytics dashboard summary
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Popular combinations
        List<RequestAnalytics> popular = analyticsService.getPopularCombinations();
        dashboard.put("popularCombinations", popular.size());

        // Cache performance
        List<Object[]> cacheStats = analyticsService.getCachePerformanceStats();
        double avgCacheHitRate = cacheStats.stream()
                .mapToDouble(row -> (Double) row[2])
                .average()
                .orElse(0.0);
        dashboard.put("avgCacheHitRate", String.format("%.1f%%", avgCacheHitRate * 100));

        // Activity trends
        List<Object[]> activityStats = analyticsService.getActivityTrends();
        dashboard.put("totalActivities", activityStats.size());

        long totalRequests = activityStats.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
        dashboard.put("totalRequests", totalRequests);

        // Recent activity
        List<RequestAnalytics> recent = analyticsService.getRecentActivity();
        dashboard.put("recentActivityCount", recent.size());

        return ResponseEntity.ok(dashboard);
    }
}