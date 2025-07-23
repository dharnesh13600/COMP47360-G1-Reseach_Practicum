package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.SystemHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
public class HealthControllerBadRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemHealthService systemHealthService;

    @BeforeEach
    void setUp() {
        reset(systemHealthService);
    }

    @Test
    void invalidEndpoint_returns404() throws Exception {
        mockMvc.perform(get("/api/health/badpath"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthServiceThrows_returns500() throws Exception {
        doThrow(new RuntimeException("fail"))
                .when(systemHealthService).getComprehensiveHealthStatus();

        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("fail"));
    }
}
