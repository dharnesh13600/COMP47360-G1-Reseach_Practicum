package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.DailyPrecomputationService;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminControllerTest.TestConfig.class)


// This class runs tests for AdminController endpoints

class AdminControllerTest {

    //  lets us to send fake HTTP requests
    @Autowired MockMvc mvc;

    // pretends to be cache warming service
    @MockBean DailyPrecomputationService dailyPrecomputationService;

    // pretends to be cache manager
    @MockBean CacheManager cacheManager;

    static final String ADMIN_USER = "adminUser";
    static final String ADMIN_PASS = "secretPass";

    @DynamicPropertySource
    static void adminProps(DynamicPropertyRegistry reg) {
        reg.add("admin.username",      () -> ADMIN_USER);
        reg.add("admin.password.hash", () -> new BCryptPasswordEncoder().encode(ADMIN_PASS));
    }

    // this is a helper method which logs in with correct creds and returns a session cookie for the tests

    private MockHttpSession login() throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", ADMIN_USER, ADMIN_PASS);
        var result = mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())   // we expect a 200 response
                .andReturn();
        // takes the session that was created
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Nested @DisplayName("Authentication Endpoints")
    class AuthTests {
        @Test @DisplayName("login success")
        void login_success() throws Exception {

            // send the crt creds and expecting success in JSON

            mvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\""+ADMIN_USER+"\",\"password\":\""+ADMIN_PASS+"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").isNotEmpty())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test @DisplayName("login bad credentials → 401")
        void login_bad() throws Exception {

            // Send a bad password and expect a error code 401

            mvc.perform(post("/api/admin/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\""+ADMIN_USER+"\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized())   // HTTP 401
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        //Checking the Logout Endpoint

        @Test @DisplayName("logout always → 200")
        void logout() throws Exception {
            mvc.perform(post("/api/admin/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }
            // calls validate-session whithout giving 401

        @Test @DisplayName("validate-session before login → 401")
        void validate_before() throws Exception {
            mvc.perform(get("/api/admin/validate-session"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.valid").value(false));
        }


                // Login and call the validate session with the session

        @Test @DisplayName("validate-session after login → 200")
        void validate_after() throws Exception {
            MockHttpSession sess = login();
            mvc.perform(get("/api/admin/validate-session").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.username").value(ADMIN_USER));
        }
    }

    @Nested @DisplayName(" Cache Warming Endpoints")
    class WarmTests {
        @BeforeEach
        void clearWarmInvocations() {
            clearInvocations(dailyPrecomputationService);
        }

        private void stubWarmService() {
            doReturn(null)
                    .when(dailyPrecomputationService)
                    .triggerAsyncDailyPrecomputation();
        }

        @Test @DisplayName("POST /warm-cache no auth → 401")
        void warm_noAuth() throws Exception {
            stubWarmService();
            MockHttpSession empty = new MockHttpSession();
            mvc.perform(post("/api/admin/warm-cache").session(empty))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Authentication required"));
            verify(dailyPrecomputationService, never()).triggerAsyncDailyPrecomputation();
        }

        @Test @DisplayName("POST /warm-cache with auth → 200")
        void warm_withAuth() throws Exception {
            stubWarmService();
            MockHttpSession sess = login();
            mvc.perform(post("/api/admin/warm-cache").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Cache warming started successfully")));
            verify(dailyPrecomputationService, times(1)).triggerAsyncDailyPrecomputation();
        }

        @Test @DisplayName("GET /cache-status no auth → 401")
        void status_noAuth() throws Exception {
            stubWarmService();
            MockHttpSession empty = new MockHttpSession();
            mvc.perform(get("/api/admin/cache-status").session(empty))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Authentication required"));
        }

        @Test @DisplayName("GET /cache-status with auth → 200")
        void status_withAuth() throws Exception {
            stubWarmService();
            MockHttpSession sess = login();
            mvc.perform(get("/api/admin/cache-status").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Daily cache warming runs at 3 AM")));
        }
    }

    @Nested @DisplayName(" Debug & Clear Cache Endpoints")
    class CacheTests {
        @Test @DisplayName("GET /cache-debug no auth → 401")
        void debug_noAuth() throws Exception {
            MockHttpSession empty = new MockHttpSession();
            mvc.perform(get("/api/admin/cache-debug").session(empty))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Authentication required"));
        }

        @Test @DisplayName("GET /cache-debug missing → info")
        void debug_missing() throws Exception {
            when(cacheManager.getCache("recommendations")).thenReturn(null);
            MockHttpSession sess = login();
            mvc.perform(get("/api/admin/cache-debug").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cacheExists").value(false))
                    .andExpect(jsonPath("$.error").value("Cache 'recommendations' not found"));
        }

        @Test @DisplayName("GET /cache-debug non‑Caffeine → limited")
        void debug_nonCaffeine() throws Exception {
            Cache fake = mock(Cache.class);
            when(cacheManager.getCache("recommendations")).thenReturn(fake);
            MockHttpSession sess = login();
            mvc.perform(get("/api/admin/cache-debug").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cacheType").exists())
                    .andExpect(jsonPath("$.note").value("Not a Caffeine cache - limited debug info available"));
        }

        @Test @DisplayName("GET /cache-debug with Caffeine stats")
        void debug_caffeine() throws Exception {
            com.github.benmanes.caffeine.cache.Cache<Object,Object> nativeCache =
                    Caffeine.newBuilder()
                            .recordStats()
                            .expireAfterWrite(1, TimeUnit.MINUTES)
                            .build();
            nativeCache.put("k1","v1");
            nativeCache.put("k2","v2");
            nativeCache.getIfPresent("k1");

            CaffeineCache wrapper = new CaffeineCache("recommendations", nativeCache, false);
            when(cacheManager.getCache("recommendations")).thenReturn(wrapper);

            MockHttpSession sess = login();
            mvc.perform(get("/api/admin/cache-debug").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statistics.estimatedSize").isNumber())
                    .andExpect(jsonPath("$.sampleKeys", hasSize(lessThanOrEqualTo(10))))
                    .andExpect(jsonPath("$.totalKeys").value(2));
        }

        @Test @DisplayName("POST /clear-cache no auth → 401")
        void clear_noAuth() throws Exception {
            MockHttpSession empty = new MockHttpSession();
            mvc.perform(post("/api/admin/clear-cache").session(empty))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Authentication required"));
        }

        @Test @DisplayName("POST /clear-cache missing → 404")
        void clear_missing() throws Exception {
            when(cacheManager.getCache("recommendations")).thenReturn(null);
            MockHttpSession sess = login();
            mvc.perform(post("/api/admin/clear-cache").session(sess))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Cache 'recommendations' not found"));
        }

        @Test @DisplayName("POST /clear-cache success → 200")
        void clear_success() throws Exception {
            com.github.benmanes.caffeine.cache.Cache<Object,Object> nativeCache =
                    Caffeine.newBuilder().build();
            CaffeineCache wrapper =
                    new CaffeineCache("recommendations", nativeCache, false);
            when(cacheManager.getCache("recommendations")).thenReturn(wrapper);

            MockHttpSession sess = login();
            mvc.perform(post("/api/admin/clear-cache").session(sess))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Cache cleared successfully")));
            Assertions.assertTrue(nativeCache.asMap().isEmpty());
        }
    }

    // provides the password encoder bean to tests
    @TestConfiguration
    static class TestConfig {
        @Bean BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}





