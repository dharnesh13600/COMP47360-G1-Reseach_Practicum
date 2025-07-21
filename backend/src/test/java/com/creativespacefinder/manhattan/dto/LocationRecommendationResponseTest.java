package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationRecommendationResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testConstructorAndGetters() {
        UUID testId = UUID.randomUUID();
        String testZoneName = "Central Park";
        BigDecimal testLatitude = new BigDecimal("40.785091");
        BigDecimal testLongitude = new BigDecimal("-73.968285");
        BigDecimal testActivityScore = new BigDecimal("0.90");
        BigDecimal testMuseScore = new BigDecimal("0.85");
        BigDecimal testCrowdScore = new BigDecimal("0.70");
        Integer testEstimatedCrowdNumber = 500;

        LocationRecommendationResponse response = new LocationRecommendationResponse(
                testId,
                testZoneName,
                testLatitude,
                testLongitude,
                testActivityScore,
                testMuseScore,
                testCrowdScore,
                testEstimatedCrowdNumber
        );

        assertNotNull(response);
        assertEquals(testId, response.getId());
        assertEquals(testZoneName, response.getZoneName());
        assertEquals(testLatitude, response.getLatitude());
        assertEquals(testLongitude, response.getLongitude());
        assertEquals(testActivityScore, response.getActivityScore());
        assertEquals(testMuseScore, response.getMuseScore());
        assertEquals(testCrowdScore, response.getCrowdScore());
        assertEquals(testEstimatedCrowdNumber, response.getEstimatedCrowdNumber());

        // Test the automatically created ScoreBreakdown
        assertNotNull(response.getScoreBreakdown());
        assertEquals(testActivityScore, response.getScoreBreakdown().getActivityScore());
        assertEquals(testMuseScore, response.getScoreBreakdown().getMuseScore());
        assertEquals(testCrowdScore, response.getScoreBreakdown().getCrowdScore());
        assertEquals("Calculated from ML .pkl file", response.getScoreBreakdown().getExplanation());
    }

    @Test
    void testSetters() {
        LocationRecommendationResponse response = new LocationRecommendationResponse();
        UUID newId = UUID.randomUUID();
        String newZoneName = "Times Square";
        BigDecimal newLatitude = new BigDecimal("40.7580");
        BigDecimal newLongitude = new BigDecimal("-73.9855");
        BigDecimal newActivityScore = new BigDecimal("0.75");
        BigDecimal newMuseScore = new BigDecimal("0.95");
        BigDecimal newCrowdScore = new BigDecimal("0.50");
        Integer newEstimatedCrowdNumber = 1000;
        LocationRecommendationResponse.ScoreBreakdown newScoreBreakdown =
                new LocationRecommendationResponse.ScoreBreakdown(
                        new BigDecimal("0.1"), new BigDecimal("0.2"), new BigDecimal("0.3")
                );


        response.setId(newId);
        response.setZoneName(newZoneName);
        response.setLatitude(newLatitude);
        response.setLongitude(newLongitude);
        response.setActivityScore(newActivityScore);
        response.setMuseScore(newMuseScore);
        response.setCrowdScore(newCrowdScore);
        response.setEstimatedCrowdNumber(newEstimatedCrowdNumber);
        response.setScoreBreakdown(newScoreBreakdown);

        assertEquals(newId, response.getId());
        assertEquals(newZoneName, response.getZoneName());
        assertEquals(newLatitude, response.getLatitude());
        assertEquals(newLongitude, response.getLongitude());
        assertEquals(newActivityScore, response.getActivityScore());
        assertEquals(newMuseScore, response.getMuseScore());
        assertEquals(newCrowdScore, response.getCrowdScore());
        assertEquals(newEstimatedCrowdNumber, response.getEstimatedCrowdNumber());
        assertEquals(newScoreBreakdown, response.getScoreBreakdown());
    }

    @Test
    void testScoreBreakdownExplanation_CalculatedFromML() {
        // When museScore is NOT null, explanation should be "Calculated from ML .pkl file"
        LocationRecommendationResponse.ScoreBreakdown breakdown =
                new LocationRecommendationResponse.ScoreBreakdown(
                        new BigDecimal("0.5"), new BigDecimal("0.6"), new BigDecimal("0.7")
                );
        assertEquals("Calculated from ML .pkl file", breakdown.getExplanation());
    }

    @Test
    void testScoreBreakdownExplanation_LoggedToDatabase() {
        // When museScore IS null, explanation should be "Calculation already logged in database"
        LocationRecommendationResponse.ScoreBreakdown breakdown =
                new LocationRecommendationResponse.ScoreBreakdown(
                        new BigDecimal("0.5"), null, new BigDecimal("0.7") // museScore is null
                );
        assertEquals("Calculation already logged in database", breakdown.getExplanation());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        UUID testId = UUID.randomUUID();
        String testZoneName = "Brooklyn Bridge Park";
        BigDecimal testLatitude = new BigDecimal("40.7011");
        BigDecimal testLongitude = new BigDecimal("-73.9934");
        BigDecimal testActivityScore = new BigDecimal("0.88");
        BigDecimal testMuseScore = new BigDecimal("0.90");
        BigDecimal testCrowdScore = new BigDecimal("0.65");
        Integer testEstimatedCrowdNumber = 300;

        LocationRecommendationResponse originalResponse = new LocationRecommendationResponse(
                testId,
                testZoneName,
                testLatitude,
                testLongitude,
                testActivityScore,
                testMuseScore,
                testCrowdScore,
                testEstimatedCrowdNumber
        );

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(originalResponse);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"" + testId + "\""));
        assertTrue(json.contains("\"zoneName\":\"Brooklyn Bridge Park\""));
        assertTrue(json.contains("\"latitude\":40.7011"));
        assertTrue(json.contains("\"activityScore\":0.88"));
        assertTrue(json.contains("\"museScore\":0.90"));
        assertTrue(json.contains("\"crowdScore\":0.65"));
        assertTrue(json.contains("\"estimatedCrowdNumber\":300"));
        assertTrue(json.contains("\"explanation\":\"Calculated from ML .pkl file\"")); // Check nested object

        // Deserialize back from JSON
        LocationRecommendationResponse deserializedResponse = objectMapper.readValue(json, LocationRecommendationResponse.class);

        // Assert that the deserialized object matches the original
        assertEquals(originalResponse.getId(), deserializedResponse.getId());
        assertEquals(originalResponse.getZoneName(), deserializedResponse.getZoneName());
        assertEquals(originalResponse.getLatitude(), deserializedResponse.getLatitude());
        assertEquals(originalResponse.getLongitude(), deserializedResponse.getLongitude());
        assertEquals(originalResponse.getActivityScore(), deserializedResponse.getActivityScore());
        assertEquals(originalResponse.getMuseScore(), deserializedResponse.getMuseScore());
        assertEquals(originalResponse.getCrowdScore(), deserializedResponse.getCrowdScore());
        assertEquals(originalResponse.getEstimatedCrowdNumber(), deserializedResponse.getEstimatedCrowdNumber());

        // Assert nested ScoreBreakdown
        assertNotNull(deserializedResponse.getScoreBreakdown());
        assertEquals(originalResponse.getScoreBreakdown().getActivityScore(), deserializedResponse.getScoreBreakdown().getActivityScore());
        assertEquals(originalResponse.getScoreBreakdown().getMuseScore(), deserializedResponse.getScoreBreakdown().getMuseScore());
        assertEquals(originalResponse.getScoreBreakdown().getCrowdScore(), deserializedResponse.getScoreBreakdown().getCrowdScore());
        assertEquals(originalResponse.getScoreBreakdown().getExplanation(), deserializedResponse.getScoreBreakdown().getExplanation());
    }

    @Test
    void testJsonDeserializationWithNullMuseScore() throws Exception {
        UUID testId = UUID.randomUUID();
        String json = String.format("""
            {
                "id": "%s",
                "zoneName": "Test Zone",
                "latitude": 10.0,
                "longitude": 20.0,
                "activityScore": 0.5,
                "museScore": null,
                "crowdScore": 0.3,
                "estimatedCrowdNumber": 100,
                "scoreBreakdown": {
                    "activityScore": 0.5,
                    "museScore": null,
                    "crowdScore": 0.3,
                    "explanation": "Calculation already logged in database"
                }
            }
            """, testId);

        LocationRecommendationResponse deserializedResponse = objectMapper.readValue(json, LocationRecommendationResponse.class);

        assertNotNull(deserializedResponse);
        assertEquals(testId, deserializedResponse.getId());
        assertNull(deserializedResponse.getMuseScore()); // Direct museScore in main DTO should be null

        assertNotNull(deserializedResponse.getScoreBreakdown());
        assertNull(deserializedResponse.getScoreBreakdown().getMuseScore()); // Nested museScore should be null
        assertEquals("Calculation already logged in database", deserializedResponse.getScoreBreakdown().getExplanation());
    }
}