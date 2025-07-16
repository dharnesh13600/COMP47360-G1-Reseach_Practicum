package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForecastResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Mock JSON string representing a typical OpenWeather 96hr forecast response
        String json = """
            {
                "cod": "200",
                "message": 0,
                "cnt": 1,
                "list": [
                    {
                        "dt": 1720818000,
                        "main": {
                            "temp": 298.15,
                            "feels_like": 298.6,
                            "temp_min": 297.0,
                            "temp_max": 299.0,
                            "pressure": 1012,
                            "humidity": 60,
                            "temp_kf": 0.0
                        },
                        "weather": [
                            {
                                "id": 800,
                                "main": "Clear",
                                "description": "clear sky",
                                "icon": "01n"
                            }
                        ],
                        "clouds": { "all": 0 },
                        "wind": { "speed": 3.0, "deg": 180, "gust": 4.0 },
                        "visibility": 10000,
                        "pop": 0,
                        "sys": { "pod": "n" },
                        "dt_txt": "2024-07-12 21:00:00"
                    }
                ],
                "city": {
                    "id": 5128581,
                    "name": "New York",
                    "coord": { "lat": 40.7143, "lon": -74.006 },
                    "country": "US",
                    "timezone": -14400,
                    "sunrise": 1720762800,
                    "sunset": 1720815600
                }
            }
            """;

        // Deserialize the JSON string into ForecastResponse object
        ForecastResponse forecastResponse = objectMapper.readValue(json, ForecastResponse.class);

        // Assertions to check if the deserialization was successful and data is mapped correctly
        assertNotNull(forecastResponse);
        assertNotNull(forecastResponse.getHourly());
        assertFalse(forecastResponse.getHourly().isEmpty());

        ForecastResponse.HourlyForecast firstForecast = forecastResponse.getHourly().get(0);
        assertNotNull(firstForecast);

        // Test dt mapping
        assertEquals(1720818000L, firstForecast.getDt());

        // Test tempInfo and getTemp() mapping
        // We cannot assert on tempInfo directly because it's private.
        // We only assert on the public getter method.
        assertEquals(298.15, firstForecast.getTemp());

        // Test weather list and getCondition() mapping
        assertNotNull(firstForecast.getWeather());
        assertFalse(firstForecast.getWeather().isEmpty());
        assertEquals("Clear", firstForecast.getCondition());
        assertEquals("clear sky", firstForecast.getWeather().get(0).getDescription());

        // Test getReadableTime() with the expected format and timezone
        String expectedReadableTime = Instant.ofEpochSecond(1720818000L)
                                            .atZone(ZoneId.of("America/New_York"))
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        assertEquals(expectedReadableTime, firstForecast.getReadableTime());
    }

    @Test
    void testGetTemp_NoTempInfo() throws Exception {
        // Create an HourlyForecast JSON where 'main' (TempInfo) is null
        String jsonWithNullMain = """
            {
                "dt": 123456789,
                "main": null,
                "weather": []
            }
            """;
        ForecastResponse.HourlyForecast forecast = objectMapper.readValue(jsonWithNullMain, ForecastResponse.HourlyForecast.class);

        // Should return 0.0 if tempInfo is null
        assertEquals(0.0, forecast.getTemp());
    }

    @Test
    void testGetCondition_NoWeatherInfo() throws Exception {
        // Create an HourlyForecast JSON where 'weather' is null
        String jsonWithNullWeather = """
            {
                "dt": 123456789,
                "main": {"temp": 25.0},
                "weather": null
            }
            """;
        ForecastResponse.HourlyForecast forecastNullWeather = objectMapper.readValue(jsonWithNullWeather, ForecastResponse.HourlyForecast.class);
        assertEquals("Unknown", forecastNullWeather.getCondition());

        // Create an HourlyForecast JSON where 'weather' is an empty list
        String jsonWithEmptyWeather = """
            {
                "dt": 123456789,
                "main": {"temp": 25.0},
                "weather": []
            }
            """;
        ForecastResponse.HourlyForecast forecastEmptyWeather = objectMapper.readValue(jsonWithEmptyWeather, ForecastResponse.HourlyForecast.class);
        assertEquals("Unknown", forecastEmptyWeather.getCondition());
    }

    @Test
    void testSerializationAndDeserializationConsistency() throws Exception {
        // Create an instance of ForecastResponse programmatically by building a JSON string
        // This is necessary because we cannot directly set private fields like tempInfo
        String innerHourlyJson = """
            {
                "dt": 1678886400,
                "main": {"temp": 285.5},
                "weather": [{"main": "Clouds", "description": "scattered clouds"}]
            }
            """;

        String fullResponseJson = "{ \"list\": [" + innerHourlyJson + "] }";

        // Deserialize to object
        ForecastResponse deserializedResponse = objectMapper.readValue(fullResponseJson, ForecastResponse.class);

        // Assert equality after round-trip (effectively through JSON conversion)
        assertNotNull(deserializedResponse);
        assertEquals(1, deserializedResponse.getHourly().size());
        ForecastResponse.HourlyForecast retrievedForecast = deserializedResponse.getHourly().get(0);
        
        assertEquals(1678886400L, retrievedForecast.getDt());
        assertEquals(285.5, retrievedForecast.getTemp());
        assertEquals("Clouds", retrievedForecast.getCondition());
    }
}