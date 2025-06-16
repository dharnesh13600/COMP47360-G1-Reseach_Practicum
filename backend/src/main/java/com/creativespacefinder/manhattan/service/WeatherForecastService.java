package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.model.ForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherForecastService {

    // Pulls the open weather api key from the .yaml which gets it from the .env file
    @Value("${openweather.api-key}")
    private String apiKey;

    private final String BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";

    // Manhattan coordinates
    private final double LAT = 40.7831;
    private final double LON = -73.9662;

    // Call the free OpenWeather api key using the given URL to get the 48hr forecast
    public ForecastResponse get48HourForecast() {
        String url = String.format("%s?lat=%f&lon=%f&exclude=current,minutely,daily,alerts&appid=%s&units=metric",
                BASE_URL, LAT, LON, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, ForecastResponse.class);
    }
}
