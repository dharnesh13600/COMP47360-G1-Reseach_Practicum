package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private LocationRecommendationService locationRecommendationService;

    @PostMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {

        RecommendationResponse response = locationRecommendationService
                .getLocationRecommendations(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getAllActivities() {
        List<Activity> activities = locationRecommendationService.getAllActivities();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/{activityName}/dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable String activityName) {
        List<LocalDate> dates = locationRecommendationService.getAvailableDates(activityName);
        return ResponseEntity.ok(dates);
    }

    @GetMapping("/activities/{activityName}/dates/{date}/times")
    public ResponseEntity<List<LocalTime>> getAvailableTimes(
            @PathVariable String activityName,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> times = locationRecommendationService.getAvailableTimes(activityName, date);
        return ResponseEntity.ok(times);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Creative Space Finder API is running. Weather data is for visualization only.");
    }
}
