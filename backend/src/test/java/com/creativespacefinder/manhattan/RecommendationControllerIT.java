package com.creativespacefinder.manhattan;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RecommendationControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        setupTestData();
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        resetStubs();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    void setupTestData() {
        jdbcTemplate.execute("DELETE FROM request_analytics");
        jdbcTemplate.execute("DELETE FROM location_activity_scores");
        jdbcTemplate.execute("DELETE FROM activities");

        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Street photography')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Filmmaking')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Portrait painting')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Art Sale')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Landscape painting')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Busking')");
        jdbcTemplate.execute("INSERT INTO activities (id, name) VALUES (RANDOM_UUID(), 'Portrait photography')");
    }

    void resetStubs() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
    }

    @Test
    void testGetActivities_shouldReturnAllActivities() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/recommendations/activities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[?(@.name=='Street photography')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Portrait painting')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Filmmaking')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Busking')]").exists());
    }

    @Test
    void testGetZones_shouldReturnZones() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/recommendations/zones"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testHealthEndpoint_shouldReturnHealthy() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.system.status").value("HEALTHY"))
                .andExpect(jsonPath("$.system.version").exists())
                .andExpect(jsonPath("$.database").exists());
    }

    @Test
    void testInvalidActivity_shouldReturn500() throws Exception {
        String requestJson = """
          {
            "activity": "Nonexistent activity",
            "dateTime": "2025-07-16T10:00:00"
          }
          """;

        mvc.perform(MockMvcRequestBuilders.post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Activity not found: Nonexistent activity"));
    }

    @Test
    void testMalformedRequest_shouldReturn400() throws Exception {
        String malformedJson = """
          {
            "activity": "Street photography"
            // Missing dateTime and malformed JSON
          }
          """;

        mvc.perform(MockMvcRequestBuilders.post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testEmptyActivityRequest_shouldReturn400() throws Exception {
        String requestJson = """
          {
            "activity": "",
            "dateTime": "2025-07-16T10:00:00"
          }
          """;

        mvc.perform(MockMvcRequestBuilders.post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Activity is required"));
    }

    @Test
    void testBasicValidationWorks() throws Exception {
        String requestJson = """
          {
            "activity": "Street photography",
            "dateTime": "2025-07-16T10:00:00"
          }
          """;

        mvc.perform(MockMvcRequestBuilders.post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}