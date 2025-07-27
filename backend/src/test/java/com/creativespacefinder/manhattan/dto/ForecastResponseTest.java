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

        ForecastResponse forecastResponse = objectMapper.readValue(json, ForecastResponse.class);

        assertNotNull(forecastResponse);
        assertNotNull(forecastResponse.getHourly());
        assertFalse(forecastResponse.getHourly().isEmpty());

        ForecastResponse.HourlyForecast firstForecast = forecastResponse.getHourly().get(0);
        assertNotNull(firstForecast);

        assertEquals(1720818000L, firstForecast.getDt());
        assertEquals(298.15, firstForecast.getTemp());

        assertNotNull(firstForecast.getWeather());
        assertFalse(firstForecast.getWeather().isEmpty());
        assertEquals("Clear", firstForecast.getCondition());
        assertEquals("clear sky", firstForecast.getWeather().get(0).getDescription());

        String expectedReadableTime = Instant.ofEpochSecond(1720818000L)
                                            .atZone(ZoneId.of("America/New_York"))
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        assertEquals(expectedReadableTime, firstForecast.getReadableTime());
    }

    @Test
    void testGetTemp_NoTempInfo() throws Exception {
        String jsonWithNullMain = """
            {
                "dt": 123456789,
                "main": null,
                "weather": []
            }
            """;
        ForecastResponse.HourlyForecast forecast = objectMapper.readValue(jsonWithNullMain, ForecastResponse.HourlyForecast.class);
        assertEquals(0.0, forecast.getTemp());
    }

    @Test
    void testGetCondition_NoWeatherInfo() throws Exception {
        String jsonWithNullWeather = """
            {
                "dt": 123456789,
                "main": {"temp": 25.0},
                "weather": null
            }
            """;
        ForecastResponse.HourlyForecast forecastNullWeather = objectMapper.readValue(jsonWithNullWeather, ForecastResponse.HourlyForecast.class);
        assertEquals("Unknown", forecastNullWeather.getCondition());

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
        String innerHourlyJson = """
            {
                "dt": 1678886400,
                "main": {"temp": 285.5},
                "weather": [{"main": "Clouds", "description": "scattered clouds"}]
            }
            """;

        String fullResponseJson = "{ \"list\": [" + innerHourlyJson + "] }";
        ForecastResponse deserializedResponse = objectMapper.readValue(fullResponseJson, ForecastResponse.class);

        assertNotNull(deserializedResponse);
        assertEquals(1, deserializedResponse.getHourly().size());
        ForecastResponse.HourlyForecast retrievedForecast = deserializedResponse.getHourly().get(0);
        
        assertEquals(1678886400L, retrievedForecast.getDt());
        assertEquals(285.5, retrievedForecast.getTemp());
        assertEquals("Clouds", retrievedForecast.getCondition());
    }
}