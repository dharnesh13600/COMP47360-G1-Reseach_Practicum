package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
* Associated ml_prediction__logs table in database recording the history of ML predictions for testing
*/
@Entity
@Table(name = "ml_prediction_logs")
public class MLPredictionLog {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Version of the model that generated these predictions.
     * Nullable to allow default or legacy entries.
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "prediction_type", nullable = false, length = 50)
    private String predictionType;

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed;

    @Column(name = "records_updated", nullable = false)
    private Integer recordsUpdated;

    /**
     * Timestamp when these predictions were logged.
     */
    @Column(name = "prediction_date", nullable = false)
    private OffsetDateTime predictionDate;

    @Column(name = "model_accuracy", precision = 5, scale = 4)
    private BigDecimal modelAccuracy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public MLPredictionLog() {
    }

    public MLPredictionLog(UUID id,
                           String modelVersion,
                           String predictionType,
                           Integer recordsProcessed,
                           Integer recordsUpdated,
                           OffsetDateTime predictionDate) {
        this.id = id;
        this.modelVersion = modelVersion;
        this.predictionType = predictionType;
        this.recordsProcessed = recordsProcessed;
        this.recordsUpdated = recordsUpdated;
        this.predictionDate = predictionDate;
    }

    // Getters & Setters

    public UUID getId() {
        return id;
    }

    /** We assign this manually in the service with UUID.randomUUID() */
    public void setId(UUID id) {
        this.id = id;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getRecordsUpdated() {
        return recordsUpdated;
    }

    public void setRecordsUpdated(Integer recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public OffsetDateTime getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(OffsetDateTime predictionDate) {
        this.predictionDate = predictionDate;
    }

    public BigDecimal getModelAccuracy() {
        return modelAccuracy;
    }

    public void setModelAccuracy(BigDecimal modelAccuracy) {
        this.modelAccuracy = modelAccuracy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
