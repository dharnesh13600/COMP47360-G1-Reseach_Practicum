package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ml_prediction_logs")
public class MLPredictionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "prediction_type", nullable = false, length = 50)
    private String predictionType; // 'cultural_activity', 'crowd', 'muse_score'

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed;

    @Column(name = "records_updated", nullable = false)
    private Integer recordsUpdated;

    @CreationTimestamp
    @Column(name = "prediction_date", nullable = false)
    private LocalDateTime predictionDate;

    @Column(name = "model_accuracy", precision = 5, scale = 4)
    private BigDecimal modelAccuracy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public MLPredictionLog() {}

    public MLPredictionLog(String modelVersion, String predictionType, 
                          Integer recordsProcessed, Integer recordsUpdated) {
        this.modelVersion = modelVersion;
        this.predictionType = predictionType;
        this.recordsProcessed = recordsProcessed;
        this.recordsUpdated = recordsUpdated;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getPredictionType() { return predictionType; }
    public void setPredictionType(String predictionType) { this.predictionType = predictionType; }

    public Integer getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(Integer recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public Integer getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(Integer recordsUpdated) { this.recordsUpdated = recordsUpdated; }

    public LocalDateTime getPredictionDate() { return predictionDate; }
    public void setPredictionDate(LocalDateTime predictionDate) { this.predictionDate = predictionDate; }

    public BigDecimal getModelAccuracy() { return modelAccuracy; }
    public void setModelAccuracy(BigDecimal modelAccuracy) { this.modelAccuracy = modelAccuracy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

