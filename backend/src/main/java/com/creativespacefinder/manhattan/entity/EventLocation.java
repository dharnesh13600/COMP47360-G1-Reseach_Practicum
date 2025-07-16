//package com.creativespacefinder.manhattan.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "event_locations")
//public class EventLocation {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(name = "id", nullable = false)
//    private UUID id;
//
//    @Column(name = "location_name", nullable = false, columnDefinition = "TEXT")
//    private String locationName;
//
//    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
//    private BigDecimal latitude;
//
//    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
//    private BigDecimal longitude;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "nearest_taxi_zone_id")
//    private TaxiZone nearestTaxiZone;
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    // Constructors
//    public EventLocation() {}
//
//    public EventLocation(String locationName, BigDecimal latitude, BigDecimal longitude, TaxiZone nearestTaxiZone) {
//        this.locationName = locationName;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.nearestTaxiZone = nearestTaxiZone;
//    }
//
//    // Getters and Setters
//    public UUID getId() { return id; }
//    public void setId(UUID id) { this.id = id; }
//
//    public String getLocationName() { return locationName; }
//    public void setLocationName(String locationName) { this.locationName = locationName; }
//
//    public BigDecimal getLatitude() { return latitude; }
//    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
//
//    public BigDecimal getLongitude() { return longitude; }
//    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
//
//    public TaxiZone getNearestTaxiZone() { return nearestTaxiZone; }
//    public void setNearestTaxiZone(TaxiZone nearestTaxiZone) { this.nearestTaxiZone = nearestTaxiZone; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//}
//
// -------------------------------------------------------------

package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_locations")
public class EventLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "location_name", nullable = false, columnDefinition = "TEXT")
    private String locationName;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_taxi_zone_id")
    private TaxiZone nearestTaxiZone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public EventLocation() {}

    /**
     * Test‑helper ctor that your repository tests expect.
     */
    public EventLocation(String locationName, double latitude, double longitude) {  // ← ADDED
        this.locationName = locationName;                                          // ← ADDED
        this.latitude     = BigDecimal.valueOf(latitude);                          // ← ADDED
        this.longitude    = BigDecimal.valueOf(longitude);                         // ← ADDED
        this.nearestTaxiZone = null;                                               // ← ADDED
    }                                                                              // ← ADDED

    public EventLocation(String locationName, BigDecimal latitude, BigDecimal longitude, TaxiZone nearestTaxiZone) {
        this.locationName    = locationName;
        this.latitude        = latitude;
        this.longitude       = longitude;
        this.nearestTaxiZone = nearestTaxiZone;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public TaxiZone getNearestTaxiZone() { return nearestTaxiZone; }
    public void setNearestTaxiZone(TaxiZone nearestTaxiZone) { this.nearestTaxiZone = nearestTaxiZone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

