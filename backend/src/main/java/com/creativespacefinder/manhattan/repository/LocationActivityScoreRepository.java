package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationActivityScoreRepository extends JpaRepository<LocationActivityScore, UUID> {

    // Main query: Top 5 results by activity, date, and time (ordered by MuseScore)
    @Query("""
        SELECT l FROM LocationActivityScore l
        WHERE l.activity.name = :activityName
          AND l.eventDate = :eventDate
          AND l.eventTime = :eventTime
        ORDER BY l.museScore DESC NULLS LAST
        """)
    List<LocationActivityScore> findTop5ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc(
            @Param("activityName") String activityName,
            @Param("eventDate") LocalDate eventDate,
            @Param("eventTime") LocalTime eventTime
    );

    // Fallback: Top N results by activity only (ignoring date/time) to fill gaps
    @Query("""
        SELECT l FROM LocationActivityScore l
        WHERE l.activity.name = :activityName
        ORDER BY l.historicalActivityScore DESC NULLS LAST
        """)
    List<LocationActivityScore> findTopByActivityNameIgnoreDateTime(
            @Param("activityName") String activityName,
            Pageable pageable
    );

    // Available dates for the activity
    @Query("SELECT DISTINCT l.eventDate FROM LocationActivityScore l WHERE l.activity.name = :activityName")
    List<LocalDate> findAvailableDatesByActivity(@Param("activityName") String activityName);

    // Available times for the activity on a given date
    @Query("SELECT DISTINCT l.eventTime FROM LocationActivityScore l WHERE l.activity.name = :activityName AND l.eventDate = :eventDate")
    List<LocalTime> findAvailableTimesByActivityAndDate(
            @Param("activityName") String activityName,
            @Param("eventDate") LocalDate eventDate
    );

    // Count records with ML predictions
    @Query("SELECT COUNT(l) FROM LocationActivityScore l WHERE l.museScore IS NOT NULL")
    Long countRecordsWithMLPredictions();

    // Count records with historical data
    @Query("SELECT COUNT(l) FROM LocationActivityScore l WHERE l.historicalActivityScore IS NOT NULL")
    Long countRecordsWithHistoricalData();
}
