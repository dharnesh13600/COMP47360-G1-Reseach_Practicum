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
     * Pre-compute all the user potential combinations daily for 3am
     * Also, the connection management for the Database
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
            // Process the 96hrs
            for (int dayOffset = 0; dayOffset < 4; dayOffset++) {
                LocalDateTime baseDate = LocalDateTime.now().plusDays(dayOffset);

                for (Activity activity : activities) {
                    for (LocalTime time : reasonableTimes) {
                        try {
                            LocalDateTime targetDateTime = baseDate.toLocalDate().atTime(time);

                            // Precompute future times
                            if (targetDateTime.isAfter(LocalDateTime.now())) {
                                RecommendationRequest request = new RecommendationRequest(
                                        activity.getName(),
                                        targetDateTime
                                );

                                // Cache the result
                                locationRecommendationService.getLocationRecommendations(request);
                                totalProcessed++;

                                // Connection management due to my supabase crashing due to free tier
                                forceConnectionCleanup();
                                Thread.sleep(2000); // 2 seconds after each request

                                // Every 3 requests - take a longer break and force a connection cleanup
                                if (totalProcessed % 3 == 0) {
                                    batchCount++;
                                    System.out.println("Completed batch " + batchCount + " (processed " + totalProcessed + " combinations)");

                                    // Force the cleanup and a longer break
                                    forceConnectionCleanup();
                                    Thread.sleep(5000); // 5 second break every 3 requests

                                    // Monitor connection status
                                    logConnectionStats("After batch " + batchCount);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error pre-computing for " + activity.getName() +
                                    " at " + time + ": " + e.getMessage());

                            // Force a cleanup on the pre comp error
                            forceConnectionCleanup();
                            try {
                                Thread.sleep(3000); // Another break on error
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }
        } finally {
            // After completion, force a cleanup of connection
            forceConnectionCleanup();
            logConnectionStats("AFTER pre-computation");
        }

        System.out.println("Daily pre-computation completed at " + LocalDateTime.now());
        System.out.println("Total processed: " + totalProcessed + " combinations");
        System.out.println("Next pre-computation scheduled for 3 AM tomorrow");
    }

    /**
     * Async cache warming to prevent 502 errors
     * This method starts the cache warming in the background and returns it immediately
     */
    @Async("cacheWarmingExecutor")
    public CompletableFuture<String> triggerAsyncDailyPrecomputation() {
        try {
            System.out.println("ASYNC Cache Warming Started in Background Thread: " + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();

            // Call the existing synchronous cache warming method
            dailyPrecomputation();

            long durationMs = System.currentTimeMillis() - startTime;
            long durationMinutes = durationMs / (1000 * 60);

            String successMessage = String.format("ASYNC Cache warming completed successfully! Duration: %d minutes (%d ms)",
                    durationMinutes, durationMs);
            System.out.println(successMessage);

            return CompletableFuture.completedFuture(successMessage);

        } catch (Exception e) {
            String errorMessage = "ASYNC Cache warming failed: " + e.getMessage();
            System.err.println(errorMessage);

            // Return an error if the cache warming failed
            return CompletableFuture.completedFuture(errorMessage);
        }
    }

    /**
     * Force a connection cleanup
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
     * Log any connection statistics
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
     * This is a manual trigger for testing
     */
    public void triggerDailyPrecomputation() {
        dailyPrecomputation();
    }
}
