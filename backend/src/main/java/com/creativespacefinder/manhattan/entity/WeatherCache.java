package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weather_cache")
public class WeatherCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "forecast_datetime", nullable = false)
    private LocalDateTime forecastDateTime;

    @Column(name = "temperature", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "weather_condition", nullable = false, length = 50)
    private String weatherCondition;

    @Column(name = "weather_description", nullable = false, length = 100)
    private String weatherDescription;

    @CreationTimestamp
    @Column(name = "cached_at", nullable = false, updatable = false)
    private LocalDateTime cachedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Constructors
    public WeatherCache() {}

    public WeatherCache(LocalDateTime forecastDateTime, BigDecimal temperature, String weatherCondition,
                       String weatherDescription, LocalDateTime expiresAt) {
        this.forecastDateTime = forecastDateTime;
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.weatherDescription = weatherDescription;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getForecastDateTime() { return forecastDateTime; }
    public void setForecastDateTime(LocalDateTime forecastDateTime) { this.forecastDateTime = forecastDateTime; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }

    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }

    public LocalDateTime getCachedAt() { return cachedAt; }
    public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}

