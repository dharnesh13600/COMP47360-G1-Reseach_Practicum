package com.creativespacefinder.manhattan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Associated request_analytics table of user request patterns and statistics
*/
@Entity
@Table(name = "request_analytics")
public class RequestAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "requested_hour", nullable = false)
    private Integer requestedHour;

    @Column(name = "requested_day_of_week", nullable = false)
    private Integer requestedDayOfWeek; // 1=Monday, 7=Sunday

    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 1;

    @Column(name = "last_requested", nullable = false)
    private LocalDateTime lastRequested;

    @Column(name = "cache_hit", nullable = false)
    private Boolean cacheHit = false;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "user_agent")
    private String userAgent; // To understand mobile vs desktop usage

    // Constructors
    public RequestAnalytics() {}

    public RequestAnalytics(String activityName, Integer requestedHour, Integer requestedDayOfWeek,
                            Boolean cacheHit, Long responseTimeMs, String userAgent) {
        this.activityName = activityName;
        this.requestedHour = requestedHour;
        this.requestedDayOfWeek = requestedDayOfWeek;
        this.lastRequested = LocalDateTime.now();
        this.cacheHit = cacheHit;
        this.responseTimeMs = responseTimeMs;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public Integer getRequestedHour() { return requestedHour; }
    public void setRequestedHour(Integer requestedHour) { this.requestedHour = requestedHour; }

    public Integer getRequestedDayOfWeek() { return requestedDayOfWeek; }
    public void setRequestedDayOfWeek(Integer requestedDayOfWeek) { this.requestedDayOfWeek = requestedDayOfWeek; }

    public Integer getRequestCount() { return requestCount; }
    public void setRequestCount(Integer requestCount) { this.requestCount = requestCount; }

    public LocalDateTime getLastRequested() { return lastRequested; }
    public void setLastRequested(LocalDateTime lastRequested) { this.lastRequested = lastRequested; }

    public Boolean getCacheHit() { return cacheHit; }
    public void setCacheHit(Boolean cacheHit) { this.cacheHit = cacheHit; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public void incrementRequestCount() {
        this.requestCount++;
        this.lastRequested = LocalDateTime.now();
    }
}