package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.ForecastResponse;
import com.creativespacefinder.manhattan.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherForecastService {

    // Pulls the open weather api key from the .yaml which gets it from the .env file
    @Value("${openweather.api-key}")
    private String apiKey;

    private final String BASE_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";;

    // Manhattan coordinates
    private final double LAT = 40.7831;
    private final double LON = -73.9662;

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
}
