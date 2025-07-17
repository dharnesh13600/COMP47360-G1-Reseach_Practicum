package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.DailyPrecomputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DailyPrecomputationService dailyPrecomputationService;

    /**
     * Manual trigger for daily cache warming
     * Useful for immediate cache warming after deployment
     */
    @PostMapping("/warm-cache")
    public ResponseEntity<String> warmCache() {
        try {
            dailyPrecomputationService.triggerDailyPrecomputation();
            return ResponseEntity.ok("Daily cache warming initiated successfully. This will take ~10 minutes to complete.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Cache warming failed: " + e.getMessage());
        }
    }

    /**
     * Get cache warming status
     */
    @GetMapping("/cache-status")
    public ResponseEntity<String> getCacheStatus() {
        return ResponseEntity.ok("Daily cache warming runs at 3 AM every day. Check logs for details.");
    }
}