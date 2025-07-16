package com.creativespacefinder.manhattan.controller;

import com.creativespacefinder.manhattan.dto.ForecastResponse;
import com.creativespacefinder.manhattan.dto.WeatherData;
import com.creativespacefinder.manhattan.service.WeatherForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/forecast")
public class WeatherForecastController {

    private final WeatherForecastService weatherForecastService;

    @Autowired
    public WeatherForecastController(WeatherForecastService weatherForecastService) {
        this.weatherForecastService = weatherForecastService;
    }

    /**
     * Returns the full 96-hour forecast (raw weather data)
     * GET /api/forecast
     */
    @GetMapping
    public ForecastResponse getForecast() {
        return weatherForecastService.get96HourForecast();
    }

    /**
     * Returns the list of available forecast datetimes
     * GET /api/forecast/available-datetimes
     */
    @GetMapping("/available-datetimes")
    public ResponseEntity<List<LocalDateTime>> getAvailableForecastDateTimes() {
        List<LocalDateTime> forecastTimes = weatherForecastService.getAvailableForecastDateTimes();
        return ResponseEntity.ok(forecastTimes);
    }

    /**
     * Returns weather data for a specific datetime
     * GET /api/forecast?datetime=2025-07-12T08:00
     */
    @GetMapping(params = "datetime")
    public ResponseEntity<WeatherData> getForecastForDateTime(@RequestParam String datetime) {
        LocalDateTime parsedDateTime = LocalDateTime.parse(datetime);
        WeatherData weather = weatherForecastService.getWeatherForDateTime(parsedDateTime);
        return ResponseEntity.ok(weather);
    }
}
