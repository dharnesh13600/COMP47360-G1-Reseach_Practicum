package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.service.SystemHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class HealthControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SystemHealthService systemHealthService;

    @BeforeEach
    void setUp() {
        //make the health service always up
        when(systemHealthService.getComprehensiveHealthStatus())
                .thenReturn(Map.of("status", "UP"));
    }

    @Test
    @DisplayName("GET /api/health → 200 + UP status")
    // calling the health endpoint and checks for 200 response
    void health_returnsUp() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/health/nonexistent → 500 Internal Server Error")
    void missingEndpoint_returns500() throws Exception {
        // calls an invalid path and returns a 500 error
        mvc.perform(get("/api/health/badpath"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/health with unsupported Accept → 406 Not Acceptable")
    // Requesting plain text instead of JSON which returns 406
    void wrongAcceptHeader_returns406() throws Exception {
        mvc.perform(get("/api/health")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("OPTIONS /api/health → 200 OK")
    //OPTIONS should be allowed always
    void optionsOnHealth_returns200() throws Exception {
        mvc.perform(options("/api/health"))
                .andExpect(status().isOk());
    }
}
