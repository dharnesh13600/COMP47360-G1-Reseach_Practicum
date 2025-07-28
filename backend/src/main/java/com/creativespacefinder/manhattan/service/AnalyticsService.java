package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.entity.RequestAnalytics;
import com.creativespacefinder.manhattan.repository.RequestAnalyticsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyticsService {

    @Autowired
    private RequestAnalyticsRepository analyticsRepository;

    /**
     * Track any recommendation request with an automatic cache hit detection
     */
    public void trackRequest(String activityName, LocalDateTime requestedDateTime,
                             boolean cacheHit, long responseTimeMs) {
        try {
            Integer hour = requestedDateTime.getHour();
            Integer dayOfWeek = requestedDateTime.getDayOfWeek().getValue(); // 1 is Monday and 7 is Sunday
            String userAgent = getUserAgent();

            Optional<RequestAnalytics> existing = analyticsRepository
                    .findByActivityNameAndRequestedHourAndRequestedDayOfWeek(activityName, hour, dayOfWeek);

            if (existing.isPresent()) {
                // Update any existing records
                RequestAnalytics analytics = existing.get();
                analytics.incrementRequestCount();
                analytics.setCacheHit(cacheHit);
                analytics.setResponseTimeMs(responseTimeMs);
                if (userAgent != null) {
                    analytics.setUserAgent(userAgent);
                }
                analyticsRepository.save(analytics);
            } else {
                // Create a  new record
                RequestAnalytics analytics = new RequestAnalytics(
                        activityName, hour, dayOfWeek, cacheHit, responseTimeMs, userAgent);
                analyticsRepository.save(analytics);
            }
        } catch (Exception e) {
            // I prevent the tracking breaking the flow
            System.err.println("Error tracking request analytics: " + e.getMessage());
        }
    }

    /**
     * Get all the popular combinations for optimisation of the app ML
     */
    public List<RequestAnalytics> getPopularCombinations() {
        return analyticsRepository.findPopularCombinations(3);
    }

    /**
     * Get popular combinations for a specific activity
     */
    public List<RequestAnalytics> getPopularCombinationsForActivity(String activityName) {
        return analyticsRepository.findPopularCombinationsForActivity(activityName);
    }

    /**
     * Get cache performance statistics of the app
     */
    public List<Object[]> getCachePerformanceStats() {
        return analyticsRepository.getCacheHitRateStats();
    }

    /**
     * Get recent activity over the last 7 days
     */
    public List<RequestAnalytics> getRecentActivity() {
        return analyticsRepository.findRecentActivity(LocalDateTime.now().minusDays(7));
    }

    /**
     * Get activity popularity trends of the app
     */
    public List<Object[]> getActivityTrends() {
        return analyticsRepository.getActivityPopularityStats();
    }

    /**
     * Get hourly usage patterns of app
     */
    public List<Object[]> getHourlyUsagePatterns() {
        return analyticsRepository.getHourlyUsageStats();
    }

    /**
     * Extract the user agent from the current request (idk i saw this online!)
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}