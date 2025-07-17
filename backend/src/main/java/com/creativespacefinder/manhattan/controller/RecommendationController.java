package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/zones")
    public ResponseEntity<List<String>> getAvailableZones() {
        List<String> zones = locationRecommendationService.getAvailableZones();
        return ResponseEntity.ok(zones);
    }
}