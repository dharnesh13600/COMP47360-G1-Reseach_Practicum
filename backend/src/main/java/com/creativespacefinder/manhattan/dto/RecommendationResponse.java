package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for location recommendations
 * Weather is now decoupled and handled separately via WeatherForecastController
 */
public class RecommendationResponse {

    @JsonProperty("locations")
    private List<LocationRecommendationResponse> locations;

    @JsonProperty("activity")
    private String activity;

    @JsonProperty("requestedDateTime")
    private String requestedDateTime;

    @JsonProperty("totalResults")
    private int totalResults;

    // Constructors
    public RecommendationResponse() {}

    public RecommendationResponse(List<LocationRecommendationResponse> locations,
                                  String activity,
                                  String requestedDateTime) {
        this.locations = locations;
        this.activity = activity;
        this.requestedDateTime = requestedDateTime;
        this.totalResults = locations != null ? locations.size() : 0;
    }

    // Getters and Setters
    public List<LocationRecommendationResponse> getLocations() { return locations; }
    public void setLocations(List<LocationRecommendationResponse> locations) {
        this.locations = locations;
        this.totalResults = locations != null ? locations.size() : 0;
    }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getRequestedDateTime() { return requestedDateTime; }
    public void setRequestedDateTime(String requestedDateTime) { this.requestedDateTime = requestedDateTime; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
}
