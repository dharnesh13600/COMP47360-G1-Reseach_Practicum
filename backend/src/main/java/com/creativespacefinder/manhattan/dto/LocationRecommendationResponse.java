package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for individual location recommendations
 * Scoring is based ONLY on MuseScore, activity score, and crowd score
 * NO weather influence on recommendations
 */
public class LocationRecommendationResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("zoneName")
    private String zoneName;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("activityScore")
    private BigDecimal activityScore;

    @JsonProperty("museScore")
    private BigDecimal museScore;

    @JsonProperty("crowdScore")
    private BigDecimal crowdScore;

    @JsonProperty("estimatedCrowdNumber")
    private Integer estimatedCrowdNumber;

    @JsonProperty("scoreBreakdown")
    private ScoreBreakdown scoreBreakdown;

    // Constructors
    public LocationRecommendationResponse() {}

    public LocationRecommendationResponse(UUID id, String zoneName, BigDecimal latitude, BigDecimal longitude,
                                          BigDecimal activityScore, BigDecimal museScore,
                                          BigDecimal crowdScore, Integer estimatedCrowdNumber) {
        this.id = id;
        this.zoneName = zoneName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.activityScore = activityScore;
        this.museScore = museScore;
        this.crowdScore = crowdScore;
        this.estimatedCrowdNumber = estimatedCrowdNumber;
        this.scoreBreakdown = new ScoreBreakdown(activityScore, museScore, crowdScore);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getActivityScore() { return activityScore; }
    public void setActivityScore(BigDecimal activityScore) { this.activityScore = activityScore; }

    public BigDecimal getMuseScore() { return museScore; }
    public void setMuseScore(BigDecimal museScore) { this.museScore = museScore; }

    public BigDecimal getCrowdScore() { return crowdScore; }
    public void setCrowdScore(BigDecimal crowdScore) { this.crowdScore = crowdScore; }

    public Integer getEstimatedCrowdNumber() { return estimatedCrowdNumber; }
    public void setEstimatedCrowdNumber(Integer estimatedCrowdNumber) { this.estimatedCrowdNumber = estimatedCrowdNumber; }

    public ScoreBreakdown getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(ScoreBreakdown scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    /**
     * Inner class for score breakdown explanation
     */
    public static class ScoreBreakdown {
        @JsonProperty("activityScore")
        private BigDecimal activityScore;

        @JsonProperty("museScore")
        private BigDecimal museScore;

        @JsonProperty("crowdScore")
        private BigDecimal crowdScore;

        @JsonProperty("explanation")
        private String explanation;

        //added by dharnesh
        public ScoreBreakdown() {}

        public ScoreBreakdown(BigDecimal activityScore, BigDecimal museScore, BigDecimal crowdScore) {
            this.activityScore = activityScore;
            this.museScore = museScore;
            this.crowdScore = crowdScore;

            if (museScore != null) {
                this.explanation = "Calculated from ML .pkl file";
            } else {
                this.explanation = "Calculation already logged in database";
            }
        }

        // Getters and Setters
        public BigDecimal getActivityScore() { return activityScore; }
        public void setActivityScore(BigDecimal activityScore) { this.activityScore = activityScore; }

        public BigDecimal getMuseScore() { return museScore; }
        public void setMuseScore(BigDecimal museScore) { this.museScore = museScore; }

        public BigDecimal getCrowdScore() { return crowdScore; }
        public void setCrowdScore(BigDecimal crowdScore) { this.crowdScore = crowdScore; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}
