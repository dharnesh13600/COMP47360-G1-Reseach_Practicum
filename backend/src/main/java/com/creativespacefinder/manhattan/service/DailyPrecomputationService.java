package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DailyPrecomputationService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LocationRecommendationService locationRecommendationService;

    /**
     * Pre-compute all combinations once daily at 3 AM
     * Optimized for Supabase Free plan (20 connection limit)
     */
    @Scheduled(cron = "0 0 3 * * *") // 3 AM every day
    public void dailyPrecomputation() {
        System.out.println("Starting daily pre-computation at 3 AM...");

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

        // Process all 4 days (96 hours to match weather API)
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

                            // Conservative delay to avoid overwhelming connections
                            Thread.sleep(1000); // 1 second between requests

                            // Every 5 requests, take a longer break
                            if (totalProcessed % 5 == 0) {
                                batchCount++;
                                System.out.println("Completed batch " + batchCount + " (processed " + totalProcessed + " combinations so far...)");
                                Thread.sleep(3000); // 3 second break every 5 requests
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error pre-computing for " + activity.getName() +
                                " at " + time + ": " + e.getMessage());
                        // Continue processing even if one fails
                    }
                }
            }
        }

        System.out.println("Daily pre-computation completed at " + LocalDateTime.now());
        System.out.println("Total processed: " + totalProcessed + " combinations");
        System.out.println("Next pre-computation scheduled for 3 AM tomorrow");
    }

    /**
     * Manual trigger for daily pre-computation (for testing)
     */
    public void triggerDailyPrecomputation() {
        dailyPrecomputation();
    }
}