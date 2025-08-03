package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.dto.LocationRecommendationResponse;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import com.creativespacefinder.manhattan.service.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RecommendationController.class)            // scans the controller and MVC infra
class RecommendationControllerTest {

    @Autowired
    private MockMvc mvc;        //fake http request

    @Autowired
    private ObjectMapper mapper;        //json deserialisation

    @MockBean
    private LocationRecommendationService service;

    @MockBean
    private AnalyticsService analyticsService;

    private final LocalDateTime NOW = LocalDateTime.of(2025,7,17,15,0);

         // valid json request returns 200 and correct body   
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

  // Missing body should return 400 Bad request

    @Test
    @DisplayName("POST /api/recommendations with empty body - 400 Bad Request")
    void postMissingBody_returns400() throws Exception {
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    // Wrong http methods results in 405 method not allowed

    @Test
    @DisplayName("PUT /api/recommendations/activities - 405 Method Not Allowed")
    void activities_whenPut_returns405() throws Exception {
        mvc.perform(put("/api/recommendations/activities"))
                .andExpect(status().isMethodNotAllowed());
    }

    
    // GET available zones returns 200 and an empty array when nothing exists
    @Test
    @DisplayName("GET /api/recommendations/zones empty - 200 + empty array")
    void zones_emptyList_returns200AndEmptyArray() throws Exception {
        given(service.getAvailableZones()).willReturn(List.of());
        mvc.perform(get("/api/recommendations/zones"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
    }

    // POST with too large payload returns 400 Bad Request
    @Test
    @DisplayName("POST /api/recommendations with too large payload - 400 Bad Request")
    void postTooLarge_returns400() throws Exception {
       
        StringBuilder sb = new StringBuilder("{\"x\":\"");
        for (int i = 0; i < 1024*1024; i++) sb.append('A');
        sb.append("\"}");
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sb.toString()))
                .andExpect(status().isBadRequest());
    }

    // missing datatime field gives 400 Bad request
    @Test
    @DisplayName("POST /api/recommendations missing dateTime - 400 Bad Request")
    void postMissingDateTime_returns400() throws Exception {
        
        RecommendationRequest req = new RecommendationRequest("Art", null, null);
        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
