package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.ForecastResponse;
import com.creativespacefinder.manhattan.dto.WeatherData;
import com.creativespacefinder.manhattan.entity.WeatherCache;
import com.creativespacefinder.manhattan.exception.ApiException;
import com.creativespacefinder.manhattan.repository.WeatherCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class WeatherForecastService {

    // Pulls the open weather api key from the .yaml which gets it from the .env file
    @Value("${openweather.api-key}")
    private String apiKey;

    private final String BASE_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";

    // Manhattan coordinates
    private final double LAT = 40.7831;
    private final double LON = -73.9662;

    @Autowired
    private WeatherCacheRepository weatherCacheRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Call the free OpenWeather api key using the given URL to get the 96hr forecast
    public ForecastResponse get96HourForecast() {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=imperial",
                BASE_URL, LAT, LON, apiKey);

        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject(url, ForecastResponse.class);
        } catch (HttpClientErrorException e) {
            // Wrap the API failure into the exception
            throw new ApiException("OpenWeather API call failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // Catch other unexpected issues maybe forgotten
            throw new ApiException("Unexpected error while calling OpenWeather API: " + e.getMessage());
        }
    }

    /**
     * Get weather forecast for a specific datetime with caching
     */
    public WeatherData getWeatherForDateTime(LocalDateTime dateTime) {
        // Check cache first
        Optional<WeatherCache> cachedWeather = weatherCacheRepository
                .findValidCacheByDateTime(dateTime, LocalDateTime.now());

        if (cachedWeather.isPresent()) {
            return convertCacheToWeatherData(cachedWeather.get());
        }

        // Fetch from OpenWeather API
        try {
            ForecastResponse forecast = get96HourForecast();
            WeatherData weatherData = findWeatherForDateTime(forecast, dateTime);

            // Cache the result
            cacheWeatherData(weatherData);

            return weatherData;

        } catch (Exception e) {
            // Return default weather data if API fails
            return createDefaultWeatherData(dateTime);
        }
    }

    /**
     * Find weather data for specific datetime from forecast response
     */
    private WeatherData findWeatherForDateTime(ForecastResponse forecast, LocalDateTime targetDateTime) {
        // This would need to be implemented based on the actual ForecastResponse structure
        // For now, return default data
        return createDefaultWeatherData(targetDateTime);
    }

    /**
     * Cache weather data
     */
    private void cacheWeatherData(WeatherData weatherData) {
        WeatherCache cache = new WeatherCache(
                weatherData.getDateTime(),
                weatherData.getTemperature(),
                weatherData.getCondition(),
                weatherData.getDescription(),
                LocalDateTime.now().plusHours(1) // Cache for 1 hour
        );

        weatherCacheRepository.save(cache);
    }

    /**
     * Convert cached weather to WeatherData
     */
    private WeatherData convertCacheToWeatherData(WeatherCache cache) {
        return new WeatherData(
                cache.getForecastDateTime(),
                cache.getTemperature(),
                cache.getWeatherCondition(),
                cache.getWeatherDescription(),
                cache.getForecastDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }

    /**
     * Create default weather data when API is unavailable
     */
    private WeatherData createDefaultWeatherData(LocalDateTime dateTime) {
        return new WeatherData(
                dateTime,
                new BigDecimal("70.0"), // Default temperature
                "Clear",
                "clear sky",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }

    /**
     * Clean up expired cache entries
     */
    public void cleanupExpiredCache() {
        weatherCacheRepository.deleteExpiredEntries(LocalDateTime.now());
    }
}

