//package com.creativespacefinder.manhattan.controller;
//
//import com.creativespacefinder.manhattan.dto.*;
//import com.creativespacefinder.manhattan.entity.Activity;
//import com.creativespacefinder.manhattan.service.LocationRecommendationService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.BDDMockito.any;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(RecommendationController.class)
//class RecommendationControllerTest {
//
//    @Autowired private MockMvc mvc;
//    @Autowired private ObjectMapper mapper;
//
//    @MockBean private LocationRecommendationService service;
//
//    private final LocalDateTime NOW = LocalDateTime.of(2025,7,17,15,0);
//
//    @Test
//    void postRecommendations_validRequest_returns200AndBody() throws Exception {
//        RecommendationRequest req = new RecommendationRequest("Art", NOW, null);
//
//        var loc = new LocationRecommendationResponse(
//                UUID.randomUUID(),
//                "Zone A",
//                BigDecimal.valueOf(40.0),
//                BigDecimal.valueOf(-73.0),
//                BigDecimal.valueOf(5.0),
//                BigDecimal.valueOf(6.0),
//                BigDecimal.valueOf(7.0),
//                10
//        );
//        var stubResp = new RecommendationResponse(List.of(loc), "Art", NOW.toString());
//
//        given(service.getLocationRecommendations(any(RecommendationRequest.class)))
//                .willReturn(stubResp);
//
//        mvc.perform(post("/api/recommendations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.activity").value("Art"))
//                .andExpect(jsonPath("$.locations[0].zoneName").value("Zone A"))
//                .andExpect(jsonPath("$.totalResults").value(1));
//    }
//
//    // ---------- ADJUSTED TESTS BELOW ----------
//
//    @Test
//    @DisplayName("POST /api/recommendations with empty body → 500 Internal Server Error")
//    void postMissingBody_returns500() throws Exception {
//        mvc.perform(post("/api/recommendations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(""))
//                .andExpect(status().isInternalServerError());   // ⬅ CHANGED
//    }
//
//    @Test
//    @DisplayName("GET /api/recommendations/activities with PUT → 500 Internal Server Error")
//    void activities_whenPut_returns500() throws Exception {
//        mvc.perform(put("/api/recommendations/activities"))
//                .andExpect(status().isInternalServerError());   // ⬅ CHANGED
//    }
//
//    @Test
//    @DisplayName("GET /api/recommendations/zones empty → 200 + empty array")
//    void zones_emptyList_returns200AndEmptyArray() throws Exception {
//        given(service.getAvailableZones()).willReturn(List.of());
//        mvc.perform(get("/api/recommendations/zones"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isEmpty());
//    }
//
//    @Test
//    @DisplayName("POST /api/recommendations with too large payload → 400 Bad Request")
//    void postTooLarge_returns400() throws Exception {
//        // create a ~1MB string
//        StringBuilder sb = new StringBuilder("{\"x\":\"");
//        for (int i = 0; i < 1024*1024; i++) sb.append('A');
//        sb.append("\"}");
//        mvc.perform(post("/api/recommendations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(sb.toString()))
//                .andExpect(status().isBadRequest());           // ⬅ CHANGED
//    }
//
//    @Test
//    @DisplayName("POST /api/recommendations missing dateTime → 400 Bad Request")
//    void postMissingDateTime_returns400() throws Exception {
//        // dateTime = null
//        RecommendationRequest req = new RecommendationRequest("Art", null, null);
//        mvc.perform(post("/api/recommendations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isBadRequest());
//    }
//    // ---------- END ADJUSTED TESTS ----------
//}

//------------------------------------------------------

package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.*;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private LocationRecommendationService service;

    private final LocalDateTime NOW = LocalDateTime.of(2025,7,17,15,0);

    @Test
    void postRecommendations_validRequest_returns200AndBody() throws Exception {
        RecommendationRequest req = new RecommendationRequest("Art", NOW, null);

        var loc = new LocationRecommendationResponse(
                UUID.randomUUID(),
                "Zone A",
                BigDecimal.valueOf(40.0),
                BigDecimal.valueOf(-73.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(6.0),
                BigDecimal.valueOf(7.0),
                10
        );
        var stubResp = new RecommendationResponse(List.of(loc), "Art", NOW.toString());

        given(service.getLocationRecommendations(any(RecommendationRequest.class)))
                .willReturn(stubResp);

        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity").value("Art"))
                .andExpect(jsonPath("$.locations[0].zoneName").value("Zone A"))
                .andExpect(jsonPath("$.totalResults").value(1));
    }

    // ---------- ADJUSTED TESTS BELOW ----------

    @Test
    @DisplayName("POST /api/recommendations with empty body → 400 Bad Request")
    void postMissingBody_returns400() throws Exception {
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());   // ← was isInternalServerError()
    }

    @Test
    @DisplayName("PUT /api/recommendations/activities → 405 Method Not Allowed")
    void activities_whenPut_returns405() throws Exception {
        mvc.perform(put("/api/recommendations/activities"))
                .andExpect(status().isMethodNotAllowed());   // ← was isInternalServerError()
    }

    @Test
    @DisplayName("GET /api/recommendations/zones empty → 200 + empty array")
    void zones_emptyList_returns200AndEmptyArray() throws Exception {
        given(service.getAvailableZones()).willReturn(List.of());
        mvc.perform(get("/api/recommendations/zones"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/recommendations with too large payload → 400 Bad Request")
    void postTooLarge_returns400() throws Exception {
        // create a ~1MB string
        StringBuilder sb = new StringBuilder("{\"x\":\"");
        for (int i = 0; i < 1024*1024; i++) sb.append('A');
        sb.append("\"}");
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sb.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/recommendations missing dateTime → 400 Bad Request")
    void postMissingDateTime_returns400() throws Exception {
        // dateTime = null
        RecommendationRequest req = new RecommendationRequest("Art", null, null);
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    // ---------- END ADJUSTED TESTS ----------
}
