package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PredictionResponse {

    @JsonProperty("muse_score")
    private Float museScore;  // nullable

    @JsonProperty("estimated_crowd_number")
    private Integer estimatedCrowdNumber;

    @JsonProperty("crowd_score")
    private Float crowdScore;

    @JsonProperty("creative_activity_score")
    private Float creativeActivityScore;

    // --------------------------------------------------------------------
    // Constructors
    // --------------------------------------------------------------------
    public PredictionResponse() {}

    public PredictionResponse(Float museScore,
                              Integer estimatedCrowdNumber,
                              Float crowdScore,
                              Float creativeActivityScore) {
        this.museScore = museScore;
        this.estimatedCrowdNumber = estimatedCrowdNumber;
        this.crowdScore = crowdScore;
        this.creativeActivityScore = creativeActivityScore;
    }

    // --------------------------------------------------------------------
    // Getters & Setters
    // --------------------------------------------------------------------
    public Float getMuseScore() {
        return museScore;
    }

    public void setMuseScore(Float museScore) {
        this.museScore = museScore;
    }

    public Integer getEstimatedCrowdNumber() {
        return estimatedCrowdNumber;
    }

    public void setEstimatedCrowdNumber(Integer estimatedCrowdNumber) {
        this.estimatedCrowdNumber = estimatedCrowdNumber;
    }

    public Float getCrowdScore() {
        return crowdScore;
    }

    public void setCrowdScore(Float crowdScore) {
        this.crowdScore = crowdScore;
    }

    public Float getCreativeActivityScore() {
        return creativeActivityScore;
    }

    public void setCreativeActivityScore(Float creativeActivityScore) {
        this.creativeActivityScore = creativeActivityScore;
    }
}
