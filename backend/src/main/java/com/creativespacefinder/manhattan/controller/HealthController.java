package com.creativespacefinder.manhattan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


// Controller for health check endpoints
@RestController
@RequestMapping("/api/health")
public class HealthController {

    // Health check endpoint
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", ZonedDateTime.now());
        response.put("service", "manhattan-muse");

        return ResponseEntity.ok(response);
    }
}
