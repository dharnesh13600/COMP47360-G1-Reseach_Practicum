package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DailyPrecomputationService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LocationRecommendationService locationRecommendationService;

    @Autowired
    private DataSource dataSource;

    /**
     * Pre-compute all combinations once daily at 3 AM
     * With aggressive connection management for Supabase
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void dailyPrecomputation() {
        System.out.println("Starting daily pre-computation at 3 AM with connection monitoring...");

        // Monitor connections before starting
        logConnectionStats("BEFORE pre-computation");

        List<Activity> activities = activityRepository.findAll();
        List<LocalTime> reasonableTimes = List.of(
                LocalTime.of(17, 0),   // 5 PM
                LocalTime.of(13, 0),  // 1 PM
                LocalTime.of(12, 0),  // 12 PM
                LocalTime.of(14, 0),  // 2 PM
                LocalTime.of(16, 0),  // 4 PM
                LocalTime.of(18, 0),  // 6 PM
                LocalTime.of(15, 0)   // 3 PM
        );

        int totalProcessed = 0;
        int batchCount = 0;

        try {
            // Process all 4 days
            for (int dayOffset = 0; dayOffset < 4; dayOffset++) {
                LocalDateTime baseDate = LocalDateTime.now().plusDays(dayOffset);

                for (Activity activity : activities) {
                    for (LocalTime time : reasonableTimes) {
                        try {
                            LocalDateTime targetDateTime = baseDate.toLocalDate().atTime(time);

                            // Only pre-compute future times
                            if (targetDateTime.isAfter(LocalDateTime.now())) {
                                RecommendationRequest request = new RecommendationRequest(
                                        activity.getName(),
                                        targetDateTime
                                );

                                // Cache the result
                                locationRecommendationService.getLocationRecommendations(request);
                                totalProcessed++;

                                // AGGRESSIVE connection management
                                forceConnectionCleanup();
                                Thread.sleep(2000); // 2 second break after each request

                                // Every 3 requests, take a longer break and force cleanup
                                if (totalProcessed % 3 == 0) {
                                    batchCount++;
                                    System.out.println("Completed batch " + batchCount + " (processed " + totalProcessed + " combinations)");

                                    // Force cleanup and longer break
                                    forceConnectionCleanup();
                                    Thread.sleep(5000); // 5 second break every 3 requests

                                    // Monitor connection status
                                    logConnectionStats("After batch " + batchCount);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error pre-computing for " + activity.getName() +
                                    " at " + time + ": " + e.getMessage());

                            // Force cleanup on error
                            forceConnectionCleanup();
                            try {
                                Thread.sleep(3000); // Extra break on error
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }
        } finally {
            // CRITICAL: Force cleanup after completion
            forceConnectionCleanup();
            logConnectionStats("AFTER pre-computation");
        }

        System.out.println("Daily pre-computation completed at " + LocalDateTime.now());
        System.out.println("Total processed: " + totalProcessed + " combinations");
        System.out.println("Next pre-computation scheduled for 3 AM tomorrow");
    }

    /**
     * Async cache warming that prevents 502 errors
     * This method starts the cache warming in background and returns immediately
     */
    @Async("cacheWarmingExecutor")
    public CompletableFuture<String> triggerAsyncDailyPrecomputation() {
        try {
            System.out.println("🚀 ASYNC Cache Warming Started in Background Thread: " + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();

            // Call your existing synchronous cache warming method
            dailyPrecomputation();

            long durationMs = System.currentTimeMillis() - startTime;
            long durationMinutes = durationMs / (1000 * 60);

            String successMessage = String.format("✅ ASYNC Cache warming completed successfully! Duration: %d minutes (%d ms)",
                    durationMinutes, durationMs);
            System.out.println(successMessage);

            return CompletableFuture.completedFuture(successMessage);

        } catch (Exception e) {
            String errorMessage = "❌ ASYNC Cache warming failed: " + e.getMessage();
            System.err.println(errorMessage);

            // Don't throw exception - just return the error in the CompletableFuture
            return CompletableFuture.completedFuture(errorMessage);
        }
    }

    /**
     * Force connection cleanup
     */
    private void forceConnectionCleanup() {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                hikariDS.getHikariPoolMXBean().softEvictConnections();
            }
        } catch (Exception e) {
            System.err.println("Error during connection cleanup: " + e.getMessage());
        }
    }

    /**
     * Log connection statistics
     */
    private void logConnectionStats(String phase) {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                var poolBean = hikariDS.getHikariPoolMXBean();

                System.out.println("=== CONNECTIONS " + phase + " ===");
                System.out.println("Active: " + poolBean.getActiveConnections());
                System.out.println("Idle: " + poolBean.getIdleConnections());
                System.out.println("Total: " + poolBean.getTotalConnections());
                System.out.println("==============================");
            }
        } catch (Exception e) {
            System.err.println("Error logging connection stats: " + e.getMessage());
        }
    }

    /**
     * Manual trigger for testing
     */
    public void triggerDailyPrecomputation() {
        dailyPrecomputation();
    }
}
