package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.model.ForecastResponse;
import com.creativespacefinder.manhattan.service.WeatherForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class WeatherForecastController {

    private final WeatherForecastService forecastService;

    @Autowired
    public WeatherForecastController(WeatherForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    public ForecastResponse getForecast() {
        return forecastService.get48HourForecast();
    }
}
