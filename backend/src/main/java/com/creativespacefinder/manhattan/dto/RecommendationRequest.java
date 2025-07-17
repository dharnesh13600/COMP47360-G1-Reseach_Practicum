package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RecommendationRequest {

    @NotBlank(message = "Activity is required")
    @JsonProperty("activity")
    private String activity;

    @NotNull(message = "DateTime is required")
    @JsonProperty("dateTime")
    private LocalDateTime dateTime;

    @JsonProperty("selectedZone")
    private String selectedZone; // Optional zone filter

    // Constructors
    public RecommendationRequest() {}

    public RecommendationRequest(String activity, LocalDateTime dateTime) {
        this.activity = activity;
        this.dateTime = dateTime;
    }

    public RecommendationRequest(String activity, LocalDateTime dateTime, String selectedZone) {
        this.activity = activity;
        this.dateTime = dateTime;
        this.selectedZone = selectedZone;
    }

    // Getters and Setters
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getSelectedZone() { return selectedZone; }
    public void setSelectedZone(String selectedZone) { this.selectedZone = selectedZone; }
}