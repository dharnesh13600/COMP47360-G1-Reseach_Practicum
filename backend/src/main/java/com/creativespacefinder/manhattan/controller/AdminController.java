package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.DailyPrecomputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DailyPrecomputationService dailyPrecomputationService;

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
     * Manual trigger for daily cache warming
     * Useful for immediate cache warming after deployment
     * Requires authentication
     */
    @PostMapping("/warm-cache")
    public ResponseEntity<String> warmCache(HttpSession session) {
        System.out.println("Cache warming request received");

        // Check authentication
        if (!isAuthenticated(session)) {
            System.out.println("Cache warming rejected - not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        try {
            System.out.println("Starting cache warming process...");
            dailyPrecomputationService.triggerDailyPrecomputation();
            System.out.println("Cache warming initiated successfully");
            return ResponseEntity.ok("Daily cache warming initiated successfully. This will take ~10 minutes to complete.");
        } catch (Exception e) {
            System.err.println("Cache warming failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Cache warming failed: " + e.getMessage());
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