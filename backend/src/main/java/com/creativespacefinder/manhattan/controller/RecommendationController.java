package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import com.creativespacefinder.manhattan.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private LocationRecommendationService locationRecommendationService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CacheManager cacheManager;

    @PostMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {

        long startTime = System.currentTimeMillis();

        // Generate the same cache key that the @Cacheable annotation uses
        String cacheKey = request.getActivity() + "_" + request.getDateTime().toString() + "_" +
                (request.getSelectedZone() != null ? request.getSelectedZone() : "all");

        // Check if cache contains this key before calling the service
        boolean cacheHit = false;
        var cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            var cachedValue = cache.get(cacheKey);
            cacheHit = (cachedValue != null);
        }

        // Log cache hit/miss for debugging
        if (cacheHit) {
            System.out.println("CACHE HIT for: " + request.getActivity() + " at " + request.getDateTime());
        } else {
            System.out.println("CACHE MISS for: " + request.getActivity() + " at " + request.getDateTime());
        }

        // Call the service (will use cache if available)
        RecommendationResponse response = locationRecommendationService
                .getLocationRecommendations(request);

        // Calculate response time
        long responseTime = System.currentTimeMillis() - startTime;

        // Track analytics with correct cache hit detection
        try {
            analyticsService.trackRequest(
                    request.getActivity(),
                    request.getDateTime(),
                    cacheHit,
                    responseTime
            );
        } catch (Exception e) {
            System.err.println("Analytics tracking failed: " + e.getMessage());
            // Don't fail the request if analytics fail
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getAllActivities() {
        List<Activity> activities = locationRecommendationService.getAllActivities();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/zones")
    public ResponseEntity<List<String>> getAvailableZones() {
        List<String> zones = locationRecommendationService.getAvailableZones();
        return ResponseEntity.ok(zones);
    }
}
