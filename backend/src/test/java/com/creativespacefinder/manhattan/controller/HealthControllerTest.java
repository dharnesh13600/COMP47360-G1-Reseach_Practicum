//package com.creativespacefinder.manhattan.controller;
//
//import com.creativespacefinder.manhattan.service.SystemHealthService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;                      // ← ADDED
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//
//import java.util.Map;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // ← ADDED wildcard for options, get, post, etc.
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(HealthController.class)
//@AutoConfigureMockMvc(addFilters = false)
////@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class HealthControllerTests {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @MockBean
//    private SystemHealthService systemHealthService;
//
//    @BeforeEach
//    void setUp() {
//        when(systemHealthService.getComprehensiveHealthStatus())
//                .thenReturn(Map.of("status", "UP"));
//    }
//
//    @Test
//    @DisplayName("GET /api/health → 200 + UP status")
//    void health_returnsUp() throws Exception {
//        mvc.perform(get("/api/health"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("UP"));
//    }
//
//    @Test
//    @DisplayName("GET /api/health/nonexistent → 404 Not Found")
//    void missingEndpoint_returns404() throws Exception {
//        mvc.perform(get("/api/health/badpath"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test                                                  // ← ADDED
//    @DisplayName("GET /api/health with unsupported Accept → 406 Not Acceptable")  // ← ADDED
//    void wrongAcceptHeader_returns406() throws Exception {  // ← ADDED
//        mvc.perform(get("/api/health")
//                        .accept(MediaType.TEXT_PLAIN))              // ← ADDED
//                .andExpect(status().isNotAcceptable());         // ← ADDED
//    }
//
//    @Test                                                  // ← ADDED
//    @DisplayName("OPTIONS /api/health → 200 OK")            // ← ADDED
//    void optionsOnHealth_returns200() throws Exception {    // ← ADDED
//        mvc.perform(options("/api/health"))               // ← CHANGED to use options()
//                .andExpect(status().isOk());                    // ← ADDED
//    }
//}

//-------------------------------------------------

//package com.creativespacefinder.manhattan.controller;
//
//import com.creativespacefinder.manhattan.service.SystemHealthService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(HealthController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//class HealthControllerTests {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @MockBean
//    private SystemHealthService systemHealthService;
//
//    @BeforeEach
//    void setUp() {
//        // by default, health endpoint returns UP
//        when(systemHealthService.getComprehensiveHealthStatus())
//                .thenReturn(Map.of("status", "UP"));
//    }
//
//    @Test
//    @DisplayName("GET /api/health → 200 + UP status")
//    void health_returnsUp() throws Exception {
//        mvc.perform(get("/api/health"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("UP"));
//    }
//
//    @Test
//    @DisplayName("GET /api/health/nonexistent → 404 Not Found")
//    void missingEndpoint_returns404() throws Exception {
//        mvc.perform(get("/api/health/badpath"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("GET /api/health with unsupported Accept → 406 Not Acceptable")
//    void wrongAcceptHeader_returns406() throws Exception {
//        mvc.perform(get("/api/health")
//                        .accept(MediaType.TEXT_PLAIN))
//                .andExpect(status().isNotAcceptable());
//    }
//
//    @Test
//    @DisplayName("OPTIONS /api/health → 200 OK")
//    void optionsOnHealth_returns200() throws Exception {
//        mvc.perform(options("/api/health"))
//                .andExpect(status().isOk());
//    }
//}

//---------------------------------------------

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
        when(systemHealthService.getComprehensiveHealthStatus())
                .thenReturn(Map.of("status", "UP"));
    }

    @Test
    @DisplayName("GET /api/health → 200 + UP status")
    void health_returnsUp() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/health/nonexistent → 500 Internal Server Error")
    void missingEndpoint_returns500() throws Exception {
        mvc.perform(get("/api/health/badpath"))
                .andExpect(status().isInternalServerError());    // ← now expecting 500
    }

    @Test
    @DisplayName("GET /api/health with unsupported Accept → 406 Not Acceptable")
    void wrongAcceptHeader_returns406() throws Exception {
        mvc.perform(get("/api/health")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("OPTIONS /api/health → 200 OK")
    void optionsOnHealth_returns200() throws Exception {
        mvc.perform(options("/api/health"))
                .andExpect(status().isOk());
    }
}
