package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "location_activity_scores")
public class LocationActivityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private EventLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_zone_id", nullable = false)
    private TaxiZone taxiZone;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time", nullable = false)
    private LocalTime eventTime;

    // Historical data from original dataset
    @Column(name = "historical_taxi_zone_crowd_score", precision = 5, scale = 3)
    private BigDecimal historicalTaxiZoneCrowdScore;

    @Column(name = "historical_activity_score", precision = 5, scale = 2)
    private BigDecimal historicalActivityScore;

    // ML Predicted scores (nullable - to be populated by ML team)
    @Column(name = "cultural_activity_score", precision = 5, scale = 2)
    private BigDecimal culturalActivityScore;

    @Column(name = "crowd_score", precision = 5, scale = 2)
    private BigDecimal crowdScore;

    @Column(name = "muse_score", precision = 5, scale = 2)
    private BigDecimal museScore;

    @Column(name = "estimated_crowd_number")
    private Integer estimatedCrowdNumber;

    public Integer getEstimatedCrowdNumber() {
        return estimatedCrowdNumber;
    }

    public void setEstimatedCrowdNumber(Integer estimatedCrowdNumber) {
        this.estimatedCrowdNumber = estimatedCrowdNumber;
    }

    // Metadata - for the teams help
    @Column(name = "ml_prediction_date")
    private LocalDateTime mlPredictionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public LocationActivityScore() {}

    public LocationActivityScore(Integer eventId, EventLocation location, Activity activity,
                               TaxiZone taxiZone, LocalDate eventDate, LocalTime eventTime,
                               BigDecimal historicalTaxiZoneCrowdScore, BigDecimal historicalActivityScore) {
        this.eventId = eventId;
        this.location = location;
        this.activity = activity;
        this.taxiZone = taxiZone;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.historicalTaxiZoneCrowdScore = historicalTaxiZoneCrowdScore;
        this.historicalActivityScore = historicalActivityScore;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public EventLocation getLocation() { return location; }
    public void setLocation(EventLocation location) { this.location = location; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public TaxiZone getTaxiZone() { return taxiZone; }
    public void setTaxiZone(TaxiZone taxiZone) { this.taxiZone = taxiZone; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public BigDecimal getHistoricalTaxiZoneCrowdScore() { return historicalTaxiZoneCrowdScore; }
    public void setHistoricalTaxiZoneCrowdScore(BigDecimal historicalTaxiZoneCrowdScore) {
        this.historicalTaxiZoneCrowdScore = historicalTaxiZoneCrowdScore;
    }

    public BigDecimal getHistoricalActivityScore() { return historicalActivityScore; }
    public void setHistoricalActivityScore(BigDecimal historicalActivityScore) {
        this.historicalActivityScore = historicalActivityScore;
    }

    public BigDecimal getCulturalActivityScore() { return culturalActivityScore; }
    public void setCulturalActivityScore(BigDecimal culturalActivityScore) {
        this.culturalActivityScore = culturalActivityScore;
    }

    public BigDecimal getCrowdScore() { return crowdScore; }
    public void setCrowdScore(BigDecimal crowdScore) { this.crowdScore = crowdScore; }

    public BigDecimal getMuseScore() { return museScore; }
    public void setMuseScore(BigDecimal museScore) { this.museScore = museScore; }

    public LocalDateTime getMlPredictionDate() { return mlPredictionDate; }
    public void setMlPredictionDate(LocalDateTime mlPredictionDate) {
        this.mlPredictionDate = mlPredictionDate;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean hasMLPredictions() {
        return culturalActivityScore != null && crowdScore != null && museScore != null;
    }

    public boolean hasHistoricalData() {
        return historicalActivityScore != null;
    }

    public String getRecommendationStatus() {
        if (hasMLPredictions()) {
            return "ml_ready";
        } else if (hasHistoricalData()) {
            return "historical_fallback";
        } else {
            return "no_data";
        }
    }
}


