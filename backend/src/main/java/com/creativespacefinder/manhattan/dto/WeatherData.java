package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WeatherData {
    
    @JsonProperty("dateTime")
    private LocalDateTime dateTime;
    
    @JsonProperty("temperature")
    private BigDecimal temperature;
    
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("formattedDateTime")
    private String formattedDateTime;

    // Constructors
    public WeatherData() {}
    
    public WeatherData(LocalDateTime dateTime, BigDecimal temperature, String condition, 
                      String description, String formattedDateTime) {
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.condition = condition;
        this.description = description;
        this.formattedDateTime = formattedDateTime;
    }
    
    // Getters and Setters
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFormattedDateTime() { return formattedDateTime; }
    public void setFormattedDateTime(String formattedDateTime) { this.formattedDateTime = formattedDateTime; }
}

