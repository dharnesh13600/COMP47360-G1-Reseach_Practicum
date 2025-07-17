package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.SystemHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private SystemHealthService systemHealthService;

    /**
     * Comprehensive system health dashboard
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(systemHealthService.getComprehensiveHealthStatus());
    }
}