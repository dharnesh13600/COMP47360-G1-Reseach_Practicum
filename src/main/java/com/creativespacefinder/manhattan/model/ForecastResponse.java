package com.creativespacefinder.manhattan.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

//This class will map the JSON structure from OpenWeather for a 48hr forecast
public class ForecastResponse {

    // "hourly" contains the list of all our location hourly forecasts
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
        private long dt;            // make sure that this is human readable!
        private double temp;        // temperature in Celsius not Fahrenheit
        private List<Weather> weather;

        // Returns the human readable time, for New York
        public String getReadableTime() {
            return Instant.ofEpochSecond(dt)
                    .atZone(ZoneId.of("America/New_York"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        // Setters and Getters for the JSON data returned

        public long getDt() { return dt; }
        public void setDt(long dt) { this.dt = dt; }

        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }

        public List<Weather> getWeather() { return weather; }
        public void setWeather(List<Weather> weather) { this.weather = weather; }

        // My cheeky way of getting a readable one word condtion (this must be tested!)
        public String getCondition() {
            return weather != null && !weather.isEmpty() ? weather.get(0).getMain() : "Unknown";
        }
    }

    // The Weather class will handle the main condition and a description if needed for testing
    public static class Weather {
        private String main;
        private String description;

        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
