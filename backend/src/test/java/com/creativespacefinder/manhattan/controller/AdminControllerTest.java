package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.DailyPrecomputationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import jakarta.servlet.http.HttpSession;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BCryptPasswordEncoder.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DailyPrecomputationService dailyPrecomputationService;

    // these values get injected into @Value fields in AdminController
    private static final String ADMIN_USER  = "adminUser";
    private static final String ADMIN_PASS  = "secretPass";

    @DynamicPropertySource
    static void setAdminProps(DynamicPropertyRegistry reg) {
        String hashed = new BCryptPasswordEncoder().encode(ADMIN_PASS);
        reg.add("admin.username",       () -> ADMIN_USER);
        reg.add("admin.password.hash",  () -> hashed);
    }

    private String loginJson(String user, String pass) {
        return String.format("{\"username\":\"%s\",\"password\":\"%s\"}", user, pass);
    }

    @Test
    @DisplayName("POST /api/admin/login → 200 + session attributes + body")
    void login_success() throws Exception {
        mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_USER, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.sessionId").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/admin/login wrong password → 401 Unauthorized")
    void login_badCredentials() throws Exception {
        mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_USER, "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/admin/logout → 200 + success body")
    void logout_alwaysSucceeds() throws Exception {
        mvc.perform(post("/api/admin/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    @DisplayName("GET /api/admin/validate-session without login → 401")
    void validateSession_withoutLogin() throws Exception {
        mvc.perform(get("/api/admin/validate-session"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("GET /api/admin/validate-session after login → 200 + valid=true")
    void validateSession_afterLogin() throws Exception {
        // first perform login to set session attributes
        MockHttpServletRequestBuilder login = post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(ADMIN_USER, ADMIN_PASS));
        var mvcResult = mvc.perform(login)
                .andReturn();

        HttpSession session = mvcResult.getRequest().getSession(false);

        mvc.perform(get("/api/admin/validate-session").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value(ADMIN_USER));
    }

    @Test
    @DisplayName("POST /api/admin/warm-cache without auth → 401")
    void warmCache_unauthenticated() throws Exception {
        mvc.perform(post("/api/admin/warm-cache"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication required"));
        verifyNoInteractions(dailyPrecomputationService);
    }

    @Test
    @DisplayName("POST /api/admin/warm-cache with auth → 200 + service called")
    void warmCache_authenticated() throws Exception {
        // login first
        var loginResult = mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_USER, ADMIN_PASS)))
                .andReturn();
        var session = loginResult.getRequest().getSession(false);

        mvc.perform(post("/api/admin/warm-cache").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string("Daily cache warming initiated successfully. This will take ~10 minutes to complete."));
        verify(dailyPrecomputationService, times(1)).triggerDailyPrecomputation();
    }

    @Test
    @DisplayName("GET /api/admin/cache-status without auth → 401")
    void cacheStatus_unauthenticated() throws Exception {
        mvc.perform(get("/api/admin/cache-status"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication required"));
    }

    @Test
    @DisplayName("GET /api/admin/cache-status with auth → 200 + info text")
    void cacheStatus_authenticated() throws Exception {
        // login first
        var loginResult = mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN_USER, ADMIN_PASS)))
                .andReturn();
        var session = loginResult.getRequest().getSession(false);

        mvc.perform(get("/api/admin/cache-status").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string("Daily cache warming runs at 3 AM every day. Check logs for details."));
    }
}
