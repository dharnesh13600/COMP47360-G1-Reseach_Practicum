package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.RequestAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestAnalyticsRepository extends JpaRepository<RequestAnalytics, UUID> {

    /**
     * Find existing analytics record for activity + hour + day combination
     */
    Optional<RequestAnalytics> findByActivityNameAndRequestedHourAndRequestedDayOfWeek(
            String activityName, Integer requestedHour, Integer requestedDayOfWeek);

    /**
     * Get top popular combinations by request count
     */
    @Query("""
        SELECT r FROM RequestAnalytics r 
        WHERE r.requestCount >= :minRequests 
        ORDER BY r.requestCount DESC, r.lastRequested DESC
        """)
    List<RequestAnalytics> findPopularCombinations(@Param("minRequests") Integer minRequests);

    /**
     * Get most popular combinations for specific activity
     */
    @Query("""
        SELECT r FROM RequestAnalytics r 
        WHERE r.activityName = :activityName 
        ORDER BY r.requestCount DESC, r.lastRequested DESC
        """)
    List<RequestAnalytics> findPopularCombinationsForActivity(@Param("activityName") String activityName);

    /**
     * Get cache hit rate statistics
     */
    @Query("""
        SELECT 
            r.activityName,
            r.requestedHour,
            AVG(CASE WHEN r.cacheHit = true THEN 1.0 ELSE 0.0 END) as cacheHitRate,
            SUM(r.requestCount) as totalRequests,
            AVG(r.responseTimeMs) as avgResponseTime
        FROM RequestAnalytics r 
        GROUP BY r.activityName, r.requestedHour
        ORDER BY totalRequests DESC
        """)
    List<Object[]> getCacheHitRateStats();

    /**
     * Get recent activity (last 7 days)
     */
    @Query("""
        SELECT r FROM RequestAnalytics r 
        WHERE r.lastRequested >= :since 
        ORDER BY r.lastRequested DESC
        """)
    List<RequestAnalytics> findRecentActivity(@Param("since") LocalDateTime since);

    /**
     * Get activity popularity trends
     */
    @Query("""
        SELECT 
            r.activityName,
            SUM(r.requestCount) as totalRequests,
            AVG(r.responseTimeMs) as avgResponseTime,
            MAX(r.lastRequested) as lastRequested
        FROM RequestAnalytics r 
        GROUP BY r.activityName
        ORDER BY totalRequests DESC
        """)
    List<Object[]> getActivityPopularityStats();

    /**
     * Get hourly usage patterns
     */
    @Query("""
        SELECT 
            r.requestedHour,
            SUM(r.requestCount) as totalRequests,
            COUNT(DISTINCT r.activityName) as uniqueActivities
        FROM RequestAnalytics r 
        GROUP BY r.requestedHour
        ORDER BY r.requestedHour
        """)
    List<Object[]> getHourlyUsageStats();
}