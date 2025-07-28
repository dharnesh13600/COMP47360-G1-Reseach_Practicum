package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.ForecastResponse;
import com.creativespacefinder.manhattan.dto.WeatherData;
import com.creativespacefinder.manhattan.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class WeatherForecastService {

    @Value("${openweather.api-key}")
    private String apiKey;

    private static final String BASE_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private static final double LAT = 40.7831;
    private static final double LON = -73.9662;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    /* ------------------------------------------------------------------ */
    /*                        96-hour forecast                            */
    /* ------------------------------------------------------------------ */
    public ForecastResponse get96HourForecast() {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=imperial", BASE_URL, LAT, LON, apiKey);
        try {
            return rest.getForObject(url, ForecastResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ApiException("OpenWeather API call failed: " + e.getStatusCode()
                    + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ApiException("Unexpected error while calling OpenWeather API: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*              Available Forecast with DateTimes                     */
    /* ------------------------------------------------------------------ */
    public List<LocalDateTime> getAvailableForecastDateTimes() {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=imperial", BASE_URL, LAT, LON, apiKey);
        try {
            String raw = rest.getForObject(url, String.class);
            JsonNode root = mapper.readTree(raw);
            JsonNode listNode = root.get("list");
            if (listNode == null || !listNode.isArray()) {
                throw new ApiException("Invalid response format from OpenWeather API");
            }
            return StreamSupport.stream(listNode.spliterator(), false)
                    .map(node -> node.get("dt").asLong())
                    .map(epoch -> Instant.ofEpochSecond(epoch)
                            .atZone(ZoneId.of("America/New_York"))
                            .toLocalDateTime())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to extract forecast datetimes: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*               Weather For Exact DateTime                           */
    /* ------------------------------------------------------------------ */
    public WeatherData getWeatherForDateTime(LocalDateTime target) {
        try {
            ForecastResponse forecast = get96HourForecast();
            return findWeatherForDateTime(forecast, target);
        } catch (Exception e) {
            return createDefaultWeatherData(target);   // graceful fallback
        }
    }

    /* ------------------------------------------------------------------ */
    /*              Find Match Forecast for DateTime                     */
    /* ------------------------------------------------------------------ */
    private WeatherData findWeatherForDateTime(ForecastResponse forecast, LocalDateTime target) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return forecast.getHourly().stream()
                .filter(h -> target.format(fmt).equals(h.getReadableTime()))
                .findFirst()
                .map(h -> new WeatherData(
                        target,
                        BigDecimal.valueOf(h.getTemp()),
                        h.getCondition(),
                        h.getWeather().isEmpty() ? "" : h.getWeather().get(0).getDescription(),
                        target.format(fmt)
                ))
                .orElseGet(() -> createDefaultWeatherData(target));
    }

    /* ------------------------------------------------------------------ */
    /*                      Default Fallback                              */
    /* ------------------------------------------------------------------ */
    private WeatherData createDefaultWeatherData(LocalDateTime dateTime) {
        return new WeatherData(
                dateTime,
                new BigDecimal("70.0"),
                "Clear",
                "clear sky",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}
