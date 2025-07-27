package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PredictionResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testNoArgsConstructor() {
        PredictionResponse response = new PredictionResponse();
        assertNotNull(response);
        assertNull(response.getMuseScore());
        assertNull(response.getEstimatedCrowdNumber());
        assertNull(response.getCrowdScore());
        assertNull(response.getCreativeActivityScore());
    }

    @Test
    void testAllArgsConstructor() {
        Float museScore = 0.8f;
        Integer estimatedCrowdNumber = 150;
        Float crowdScore = 0.7f;
        Float creativeActivityScore = 0.9f;

        PredictionResponse response = new PredictionResponse(
                museScore, estimatedCrowdNumber, crowdScore, creativeActivityScore
        );

        assertNotNull(response);
        assertEquals(museScore, response.getMuseScore());
        assertEquals(estimatedCrowdNumber, response.getEstimatedCrowdNumber());
        assertEquals(crowdScore, response.getCrowdScore());
        assertEquals(creativeActivityScore, response.getCreativeActivityScore());
    }

    @Test
    void testGettersAndSetters() {
        PredictionResponse response = new PredictionResponse();

        Float museScore = 0.6f;
        Integer estimatedCrowdNumber = 200;
        Float crowdScore = 0.5f;
        Float creativeActivityScore = 0.8f;

        response.setMuseScore(museScore);
        response.setEstimatedCrowdNumber(estimatedCrowdNumber);
        response.setCrowdScore(crowdScore);
        response.setCreativeActivityScore(creativeActivityScore);

        assertEquals(museScore, response.getMuseScore());
        assertEquals(estimatedCrowdNumber, response.getEstimatedCrowdNumber());
        assertEquals(crowdScore, response.getCrowdScore());
        assertEquals(creativeActivityScore, response.getCreativeActivityScore());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        Float museScore = 0.75f;
        Integer estimatedCrowdNumber = 100;
        Float crowdScore = 0.65f;
        Float creativeActivityScore = 0.95f;

        PredictionResponse originalResponse = new PredictionResponse(
                museScore, estimatedCrowdNumber, crowdScore, creativeActivityScore
        );

        String json = objectMapper.writeValueAsString(originalResponse);
        assertNotNull(json);
        assertTrue(json.contains("\"muse_score\":0.75"));
        assertTrue(json.contains("\"estimated_crowd_number\":100"));
        assertTrue(json.contains("\"crowd_score\":0.65"));
        assertTrue(json.contains("\"creative_activity_score\":0.95"));

        PredictionResponse deserializedResponse = objectMapper.readValue(json, PredictionResponse.class);

        assertEquals(originalResponse.getMuseScore(), deserializedResponse.getMuseScore());
        assertEquals(originalResponse.getEstimatedCrowdNumber(), deserializedResponse.getEstimatedCrowdNumber());
        assertEquals(originalResponse.getCrowdScore(), deserializedResponse.getCrowdScore());
        assertEquals(originalResponse.getCreativeActivityScore(), deserializedResponse.getCreativeActivityScore());
    }

    @Test
    void testJsonSerializationAndDeserializationWithNullMuseScore() throws Exception {
        Integer estimatedCrowdNumber = 50;
        Float crowdScore = 0.4f;
        Float creativeActivityScore = 0.7f;

        PredictionResponse originalResponse = new PredictionResponse(
                null, estimatedCrowdNumber, crowdScore, creativeActivityScore
        );

        String json = objectMapper.writeValueAsString(originalResponse);
        assertNotNull(json);
        assertTrue(json.contains("\"muse_score\":null"));
        assertTrue(json.contains("\"estimated_crowd_number\":50"));
        assertTrue(json.contains("\"crowd_score\":0.4"));
        assertTrue(json.contains("\"creative_activity_score\":0.7"));

        PredictionResponse deserializedResponse = objectMapper.readValue(json, PredictionResponse.class);

        assertNull(deserializedResponse.getMuseScore());
        assertEquals(originalResponse.getEstimatedCrowdNumber(), deserializedResponse.getEstimatedCrowdNumber());
        assertEquals(originalResponse.getCrowdScore(), deserializedResponse.getCrowdScore());
        assertEquals(originalResponse.getCreativeActivityScore(), deserializedResponse.getCreativeActivityScore());
    }
}