package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

//This class will map the JSON structure from OpenWeather for a 96hr forecast
public class ForecastResponse {

    // "hourly" contains the list of all our location hourly forecasts
    @JsonProperty("list")
    private List<HourlyForecast> hourly;

    // A getter for the hourly forecast list in JSON
    public List<HourlyForecast> getHourly() {
        return hourly;
    }

    // A setter for the hourly forecast list in JSON
    public void setHourly(List<HourlyForecast> hourly) {
        this.hourly = hourly;
    }

    // The class was created to hold the data for each of the forecasts
    public static class HourlyForecast {
        private long dt; // make sure that this is human-readable!

        // This was done to get rid of the 'main:temp' in the OpenWeather API response
        @JsonProperty("main")
        private TempInfo tempInfo;

        private List<Weather> weather;

        // Setters and Getters for the JSON data returned

        public long getDt() {
            return dt;
        }

        public void setDt(long dt) {
            this.dt = dt;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<Weather> weather) {
            this.weather = weather;
        }

        // Returns the human-readable time, for New York
        public String getReadableTime() {
            return Instant.ofEpochSecond(dt)
                    .atZone(ZoneId.of("America/New_York"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        // Temperature getter based on nested 'main.temp' but don't show both
        public double getTemp() {
            return tempInfo != null ? tempInfo.temp : 0.0;
        }

        // High level weather condition (rain, cloud, sun, snow etc.)
        public String getCondition() {
            return weather != null && !weather.isEmpty() ? weather.get(0).getMain() : "Unknown";
        }

        // Nested class to represent the 'main' JSON object
        public static class TempInfo {
            public double temp;
        }
    }

    // The Weather class will handle the main condition and a description if needed for testing
    public static class Weather {
        private String main;
        private String description;

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
