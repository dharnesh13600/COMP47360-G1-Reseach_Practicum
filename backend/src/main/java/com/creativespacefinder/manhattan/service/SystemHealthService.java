package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.entity.RequestAnalytics;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import com.creativespacefinder.manhattan.repository.MLPredictionLogRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//added by dharnesh for ml model
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



@Service
public class SystemHealthService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;

    @Autowired
    private MLPredictionLogRepository mlPredictionLogRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private WeatherForecastService weatherForecastService;
    
    //// added by dharnesh for injecting ML predict URL instead of hard-coding
    @Value("${ml.predict.url}")
    private String mlPredictUrl;
    // ----------------
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final long startTime = System.currentTimeMillis();

    public Map<String, Object> getComprehensiveHealthStatus() {
        Map<String, Object> health = new LinkedHashMap<>();

        // System Overview
        health.put("system", getSystemOverview());

        // Database Health
        health.put("database", getDatabaseHealth());

        // Cache Performance
        health.put("cache", getCacheHealth());

        // ML Model Connectivity
        health.put("mlModel", getMLModelHealth());

        // Weather API Status
        health.put("weatherApi", getWeatherApiHealth());

        // Performance Metrics
        health.put("performance", getPerformanceMetrics());

        // Analytics Insights
        health.put("analytics", getAnalyticsInsights());

        // Resource Utilization
        health.put("resources", getResourceUtilization());

        // API Endpoints Status
        health.put("endpoints", getEndpointsStatus());

        return health;
    }

    private Map<String, Object> getSystemOverview() {
        Map<String, Object> system = new LinkedHashMap<>();

        long uptimeMs = System.currentTimeMillis() - startTime;
        long uptimeMinutes = uptimeMs / (1000 * 60);
        long uptimeHours = uptimeMinutes / 60;

        system.put("status", "HEALTHY");
        system.put("version", "3.0");
        system.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        system.put("uptimeMs", uptimeMs);
        system.put("uptimeHours", uptimeHours);
        system.put("uptimeMinutes", uptimeMinutes % 60);
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("springProfile", System.getProperty("spring.profiles.active", "default"));

        return system;
    }

    private Map<String, Object> getDatabaseHealth() {
        Map<String, Object> db = new LinkedHashMap<>();

        try {
            // Connection pool status
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                var poolBean = hikariDS.getHikariPoolMXBean();

                Map<String, Object> connectionPool = new LinkedHashMap<>();
                connectionPool.put("activeConnections", poolBean.getActiveConnections());
                connectionPool.put("idleConnections", poolBean.getIdleConnections());
                connectionPool.put("totalConnections", poolBean.getTotalConnections());
                connectionPool.put("maxPoolSize", hikariDS.getMaximumPoolSize());
                connectionPool.put("connectionTimeoutMs", hikariDS.getConnectionTimeout());

                db.put("connectionPool", connectionPool);
            }

            // Data statistics
            Map<String, Object> dataStats = new LinkedHashMap<>();
            dataStats.put("totalActivities", activityRepository.count());
            dataStats.put("totalLocationScores", locationActivityScoreRepository.count());
            dataStats.put("recordsWithMLPredictions", locationActivityScoreRepository.countRecordsWithMLPredictions());
            dataStats.put("recordsWithHistoricalData", locationActivityScoreRepository.countRecordsWithHistoricalData());
            dataStats.put("mlPredictionLogs", mlPredictionLogRepository.count());

            // Calculate data coverage percentage
            long totalRecords = locationActivityScoreRepository.count();
            long mlRecords = locationActivityScoreRepository.countRecordsWithMLPredictions();
            double coveragePercentage = totalRecords > 0 ? (double) mlRecords / totalRecords * 100 : 0;
            dataStats.put("mlCoveragePercentage", coveragePercentage);

            db.put("dataStatistics", dataStats);
            db.put("status", "CONNECTED");

        } catch (Exception e) {
            db.put("status", "ERROR");
            db.put("error", e.getMessage());
        }

        return db;
    }

    private Map<String, Object> getCacheHealth() {
        Map<String, Object> cache = new LinkedHashMap<>();

        try {
            var recommendationsCache = cacheManager.getCache("recommendations");
            if (recommendationsCache instanceof CaffeineCache) {
                var caffeineCache = ((CaffeineCache) recommendationsCache).getNativeCache();
                var stats = caffeineCache.stats();

                Map<String, Object> cacheStats = new LinkedHashMap<>();
                cacheStats.put("hitCount", stats.hitCount());
                cacheStats.put("missCount", stats.missCount());
                cacheStats.put("hitRate", stats.hitRate());
                cacheStats.put("missRate", stats.missRate());
                cacheStats.put("requestCount", stats.requestCount());
                cacheStats.put("estimatedSize", caffeineCache.estimatedSize());
                cacheStats.put("averageLoadTimeNanos", stats.averageLoadPenalty());
                cacheStats.put("evictionCount", stats.evictionCount());

                cache.put("statistics", cacheStats);
                cache.put("status", "ACTIVE");
                cache.put("maxSize", 1000);
                cache.put("expirationHours", 24);

            }
        } catch (Exception e) {
            cache.put("status", "ERROR");
            cache.put("error", e.getMessage());
        }

        return cache;
    }

    private Map<String, Object> getMLModelHealth() {
        Map<String, Object> ml = new LinkedHashMap<>();

        try {
            // Test ML model connectivity
            List<Map<String, Object>> testPayload = Arrays.asList(
                    Map.of(
                            "latitude", 40.7589,
                            "longitude", -73.9851,
                            "hour", 15,
                            "month", 7,
                            "day", 18,
                            "cultural_activity_prefered", "Portrait photography"
                    )
            );

            long startTime = System.currentTimeMillis();
            // ← CHANGED: use injected URL instead of hard-coded IP (by dharnesh)
            var response = restTemplate.postForObject(
                     mlPredictUrl, 
                     //-----------
                    testPayload,
                    Object[].class
            );
            long responseTime = System.currentTimeMillis() - startTime;

            ml.put("status", "CONNECTED");
            ml.put("responseTimeMs", responseTime);
            ml.put("testPayloadSize", testPayload.size());
            ml.put("lastTestedTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        } catch (Exception e) {
            ml.put("status", "DISCONNECTED");
            ml.put("error", e.getMessage());
        }

        return ml;
    }

    private Map<String, Object> getWeatherApiHealth() {
        Map<String, Object> weather = new LinkedHashMap<>();

        try {
            // Test weather API connectivity
            long startTime = System.currentTimeMillis();
            var forecast = weatherForecastService.get96HourForecast();
            long responseTime = System.currentTimeMillis() - startTime;

            weather.put("status", "CONNECTED");
            weather.put("responseTimeMs", responseTime);
            weather.put("forecastHours", forecast.getHourly() != null ? forecast.getHourly().size() : 0);
            weather.put("lastTestedTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        } catch (Exception e) {
            weather.put("status", "ERROR");
            weather.put("error", e.getMessage());
        }

        return weather;
    }

    private Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> performance = new LinkedHashMap<>();

        try {
            // Get cache performance stats from analytics
            List<Object[]> cacheStats = analyticsService.getCachePerformanceStats();

            if (!cacheStats.isEmpty()) {
                double avgCacheHitRate = cacheStats.stream()
                        .mapToDouble(row -> (Double) row[2])
                        .average()
                        .orElse(0.0);

                double avgResponseTime = cacheStats.stream()
                        .mapToDouble(row -> row[4] != null ? (Double) row[4] : 0.0)
                        .average()
                        .orElse(0.0);

                performance.put("averageCacheHitRate", avgCacheHitRate);
                performance.put("averageResponseTimeMs", avgResponseTime);
            }

            // Recent activity performance
            List<RequestAnalytics> recentActivity = analyticsService.getRecentActivity();
            performance.put("requestsLast7Days", recentActivity.size());

            if (!recentActivity.isEmpty()) {
                double recentAvgResponseTime = recentActivity.stream()
                        .mapToLong(RequestAnalytics::getResponseTimeMs)
                        .average()
                        .orElse(0.0);

                performance.put("recentAverageResponseTimeMs", recentAvgResponseTime);
            }

            performance.put("status", "MONITORED");

        } catch (Exception e) {
            performance.put("status", "ERROR");
            performance.put("error", e.getMessage());
        }

        return performance;
    }

    private Map<String, Object> getAnalyticsInsights() {
        Map<String, Object> analytics = new LinkedHashMap<>();

        try {
            // Popular combinations
            List<RequestAnalytics> popular = analyticsService.getPopularCombinations();

            Map<String, Object> insights = new LinkedHashMap<>();
            insights.put("totalPopularCombinations", popular.size());

            if (!popular.isEmpty()) {
                RequestAnalytics mostPopular = popular.get(0);
                Map<String, Object> topCombination = new LinkedHashMap<>();
                topCombination.put("activity", mostPopular.getActivityName());
                topCombination.put("hour", mostPopular.getRequestedHour());
                topCombination.put("dayOfWeek", mostPopular.getRequestedDayOfWeek());
                topCombination.put("requestCount", mostPopular.getRequestCount());

                insights.put("mostPopularCombination", topCombination);
            }

            // Activity trends
            List<Object[]> activityTrends = analyticsService.getActivityTrends();
            insights.put("totalActivitiesTracked", activityTrends.size());

            long totalRequests = activityTrends.stream()
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();
            insights.put("totalRequestsAllTime", totalRequests);

            // Hourly patterns
            List<Object[]> hourlyPatterns = analyticsService.getHourlyUsagePatterns();
            if (!hourlyPatterns.isEmpty()) {
                Object[] peakHour = hourlyPatterns.stream()
                        .max(Comparator.comparing(row -> ((Number) row[1]).longValue()))
                        .orElse(null);

                if (peakHour != null) {
                    insights.put("peakUsageHour", peakHour[0]);
                    insights.put("peakHourRequests", peakHour[1]);
                }
            }

            analytics.put("insights", insights);
            analytics.put("status", "ACTIVE");

        } catch (Exception e) {
            analytics.put("status", "ERROR");
            analytics.put("error", e.getMessage());
        }

        return analytics;
    }

    private Map<String, Object> getResourceUtilization() {
        Map<String, Object> resources = new LinkedHashMap<>();

        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            var heapMemory = memoryBean.getHeapMemoryUsage();
            var nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            Map<String, Object> memory = new LinkedHashMap<>();
            memory.put("heapUsedBytes", heapMemory.getUsed());
            memory.put("heapMaxBytes", heapMemory.getMax());
            memory.put("heapUtilizationPercent", (double) heapMemory.getUsed() / heapMemory.getMax() * 100);
            memory.put("nonHeapUsedBytes", nonHeapMemory.getUsed());
            memory.put("nonHeapMaxBytes", nonHeapMemory.getMax());

            Map<String, Object> system = new LinkedHashMap<>();
            system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            system.put("totalMemoryBytes", Runtime.getRuntime().totalMemory());
            system.put("freeMemoryBytes", Runtime.getRuntime().freeMemory());
            system.put("maxMemoryBytes", Runtime.getRuntime().maxMemory());

            resources.put("memory", memory);
            resources.put("system", system);
            resources.put("status", "MONITORED");

        } catch (Exception e) {
            resources.put("status", "ERROR");
            resources.put("error", e.getMessage());
        }

        return resources;
    }

    private Map<String, Object> getEndpointsStatus() {
        Map<String, Object> endpoints = new LinkedHashMap<>();

        endpoints.put("totalEndpoints", 12);
        endpoints.put("activeEndpoints", 12);
        endpoints.put("inactiveEndpoints", 0);

        return endpoints;
    }
}