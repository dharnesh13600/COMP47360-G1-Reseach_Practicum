package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.DailyPrecomputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DailyPrecomputationService dailyPrecomputationService;

    @Autowired
    private CacheManager cacheManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Admin credentials from environment variables
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password.hash}")
    private String adminPasswordHash;

    // ===============================
    // AUTHENTICATION ENDPOINTS
    // ===============================

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("Login attempt - Username: " + loginRequest.getUsername());

            // Validate credentials
            if (adminUsername.equals(loginRequest.getUsername()) &&
                    passwordEncoder.matches(loginRequest.getPassword(), adminPasswordHash)) {

                // Create session
                session.setAttribute("adminAuthenticated", true);
                session.setAttribute("adminUsername", adminUsername);
                session.setAttribute("loginTime", System.currentTimeMillis());

                response.put("success", true);
                response.put("message", "Login successful");
                response.put("sessionId", session.getId());

                System.out.println("Login successful for: " + adminUsername);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                System.out.println("Login failed - invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("Logout request received");
            session.invalidate();
            response.put("success", true);
            response.put("message", "Logout successful");
            System.out.println("Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Logout error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/validate-session")
    public ResponseEntity<Map<String, Object>> validateSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("🔍 Session validation request received");

        Boolean isAuthenticated = (Boolean) session.getAttribute("adminAuthenticated");
        Long loginTime = (Long) session.getAttribute("loginTime");

        if (isAuthenticated != null && isAuthenticated && loginTime != null) {
            // Check if session is still valid (24 hours)
            long currentTime = System.currentTimeMillis();
            long sessionDuration = currentTime - loginTime;
            long maxDuration = 24 * 60 * 60 * 1000; // 24 hours

            if (sessionDuration < maxDuration) {
                response.put("valid", true);
                response.put("username", session.getAttribute("adminUsername"));
                System.out.println("Session valid for user: " + session.getAttribute("adminUsername"));
                return ResponseEntity.ok(response);
            }
        }

        response.put("valid", false);
        System.out.println("Session invalid or expired");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ===============================
    // CACHE MANAGEMENT ENDPOINTS
    // ===============================

    /**
     * Manual trigger for daily cache warming - ASYNC VERSION (prevents 502 errors)
     * This method returns immediately while cache warming runs in background
     * Requires authentication
     */
    @PostMapping("/warm-cache")
    public ResponseEntity<String> warmCache(HttpSession session) {
        System.out.println("🔥 Async cache warming request received");

        // Check authentication
        if (!isAuthenticated(session)) {
            System.out.println("Cache warming rejected - not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            System.out.println("🚀 Starting ASYNC cache warming process...");

            // Start cache warming in background - this returns immediately!
            dailyPrecomputationService.triggerAsyncDailyPrecomputation();

            String responseMessage = "Cache warming started successfully in background!\n\n" +
                    "Process Duration: ~10-15 minutes\n" +
                    "Runs in background - you can continue using the app\n" +
                    "Cache will be populated automatically when complete";

            System.out.println("✅ Async cache warming initiated - returning immediate response");
            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            String errorMessage = "❌ Failed to start cache warming: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorMessage);
        }
    }

    /**
     * Get cache warming status
     * Requires authentication
     */
    @GetMapping("/cache-status")
    public ResponseEntity<String> getCacheStatus(HttpSession session) {
        System.out.println("Cache status request received");

        // Check authentication
        if (!isAuthenticated(session)) {
            System.out.println("Cache status rejected - not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        System.out.println("Cache status request authenticated");
        return ResponseEntity.ok("Daily cache warming runs at 3 AM every day. Check logs for details.");
    }

    /**
     * Debug cache contents and statistics
     */
    @GetMapping("/cache-debug")
    public ResponseEntity<Map<String, Object>> debugCache(HttpSession session) {
        System.out.println("Cache debug request received");

        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Map<String, Object> debug = new HashMap<>();

        try {
            var cache = cacheManager.getCache("recommendations");
            if (cache != null) {
                debug.put("cacheExists", true);

                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache) {
                    var caffeineCache = ((org.springframework.cache.caffeine.CaffeineCache) cache).getNativeCache();
                    var stats = caffeineCache.stats();

                    Map<String, Object> cacheStats = new HashMap<>();
                    cacheStats.put("estimatedSize", caffeineCache.estimatedSize());
                    cacheStats.put("hitCount", stats.hitCount());
                    cacheStats.put("missCount", stats.missCount());
                    cacheStats.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
                    cacheStats.put("missRate", String.format("%.2f%%", stats.missRate() * 100));
                    cacheStats.put("requestCount", stats.requestCount());
                    cacheStats.put("averageLoadTime", String.format("%.2fms", stats.averageLoadPenalty() / 1_000_000.0));
                    cacheStats.put("evictionCount", stats.evictionCount());

                    debug.put("statistics", cacheStats);

                    // Get some sample cache keys (first 10)
                    Set<Object> keys = caffeineCache.asMap().keySet();
                    List<String> sampleKeys = keys.stream()
                            .limit(10)
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    debug.put("sampleKeys", sampleKeys);
                    debug.put("totalKeys", keys.size());

                } else {
                    debug.put("cacheType", cache.getClass().getSimpleName());
                    debug.put("note", "Not a Caffeine cache - limited debug info available");
                }
            } else {
                debug.put("cacheExists", false);
                debug.put("error", "Cache 'recommendations' not found");
            }

        } catch (Exception e) {
            debug.put("error", "Error accessing cache: " + e.getMessage());
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * Clear the cache for testing
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache(HttpSession session) {
        System.out.println("Clear cache request received");

        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            var cache = cacheManager.getCache("recommendations");
            if (cache != null) {
                cache.clear();
                System.out.println("✅ Cache cleared successfully");
                return ResponseEntity.ok("Cache cleared successfully. Next requests will be cache misses.");
            } else {
                return ResponseEntity.status(404).body("Cache 'recommendations' not found");
            }
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
            return ResponseEntity.status(500).body("Error clearing cache: " + e.getMessage());
        }
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    /**
     * Helper method to check if user is authenticated
     * @param session HTTP session
     * @return true if authenticated and session is valid
     */
    private boolean isAuthenticated(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("adminAuthenticated");
        Long loginTime = (Long) session.getAttribute("loginTime");

        System.out.println("Checking authentication - authenticated: " + isAuthenticated + ", loginTime: " + loginTime);

        if (isAuthenticated != null && isAuthenticated && loginTime != null) {
            // Check if session is still valid (24 hours)
            long currentTime = System.currentTimeMillis();
            long sessionDuration = currentTime - loginTime;
            long maxDuration = 24 * 60 * 60 * 1000; // 24 hours

            boolean valid = sessionDuration < maxDuration;
            System.out.println("Session valid: " + valid + " (duration: " + (sessionDuration / 1000 / 60) + " minutes)");
            return valid;
        }

        System.out.println("Authentication failed - missing session data");
        return false;
    }

    // ===============================
    // DTO CLASSES
    // ===============================

    /**
     * DTO for login requests
     */
    public static class LoginRequest {
        private String username;
        private String password;

        // Default constructor
        public LoginRequest() {}

        // Parameterized constructor
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "LoginRequest{username='" + username + "', password='[HIDDEN]'}";
        }
    }
}
