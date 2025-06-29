package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, UUID> {
    
    /**
     * Find cached weather data for a specific datetime
     */
    @Query("""
        SELECT wc FROM WeatherCache wc
        WHERE wc.forecastDateTime = :forecastDateTime
        AND wc.expiresAt > :currentTime
        """)
    Optional<WeatherCache> findValidCacheByDateTime(
        @Param("forecastDateTime") LocalDateTime forecastDateTime,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    /**
     * Delete expired cache entries
     */
    @Modifying
    @Query("DELETE FROM WeatherCache wc WHERE wc.expiresAt <= :currentTime")
    int deleteExpiredEntries(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find weather data within a time range
     */
    @Query("""
        SELECT wc FROM WeatherCache wc
        WHERE wc.forecastDateTime BETWEEN :startTime AND :endTime
        AND wc.expiresAt > :currentTime
        ORDER BY wc.forecastDateTime
        """)
    Optional<WeatherCache> findCacheInTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("currentTime") LocalDateTime currentTime
    );
}

