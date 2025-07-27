package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.SystemHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// we are telling spring to crate MockMvc without security filters

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(HealthController.class)
public class HealthControllerBadRequestTest {

    @Autowired
    private MockMvc mockMvc; //sends fake HTTP requests

    @MockBean
    private SystemHealthService systemHealthService; // fake health service

    @BeforeEach
    void setUp() {
        reset(systemHealthService);
    } // clears interactions before running tests

    @Test
    void invalidEndpoint_returns500() throws Exception {
        // calls a bad path which should give HTTP 500
        mockMvc.perform(get("/api/health/badpath"))
                .andExpect(status().isInternalServerError());    // ← now expecting 500
    }

    @Test
    void healthServiceThrows_returns500() throws Exception {
        doThrow(new RuntimeException("fail"))
                .when(systemHealthService).getComprehensiveHealthStatus();

        // calls the correct endpoint, expecting a JSON error response
        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("fail"));
    }
}
