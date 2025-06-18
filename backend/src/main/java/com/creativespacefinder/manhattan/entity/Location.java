package com.creativespacefinder.manhattan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;


@Entity
@Table(name = "locations")
public class Location {
    // Initialising variables to represent each of the columns within the entity
    @Id
    @Column(name = "location_id", nullable = false)
    private UUID id;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // Hibernate kept trying to break down 'geom',
    // However this should not happen for my initial JSON showing
    // Hence I put in @Transient and @JSONIgnore
    @Transient
    @JsonIgnore
    @Column(name = "geom")
    private Point geom;

    // Creating the getters and setters for each column within the locations database
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Point getGeom() { return geom; }
    public void setGeom(Point geom) { this.geom = geom; }
}
