package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.*;
import com.creativespacefinder.manhattan.service.LocationRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean LocationRecommendationService svc;

    @Test
    void postValid_returns200() throws Exception {
        RecommendationRequest req = new RecommendationRequest("Photography", LocalDateTime.now());
        RecommendationResponse resp = new RecommendationResponse(
                List.of(), "Photography", req.getDateTime().toString());
        when(svc.getLocationRecommendations(any())).thenReturn(resp);

        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity").value("Photography"));
    }

    @Test
    void missingField_returns400ValidationError() throws Exception {
        String invalidJson = """
            { "dateTime":"2025-01-01T10:00" }
            """;

        mvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
