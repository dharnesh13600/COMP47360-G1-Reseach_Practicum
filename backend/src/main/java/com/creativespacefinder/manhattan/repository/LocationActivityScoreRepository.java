// src/main/java/com/creativespacefinder/manhattan/repository/LocationActivityScoreRepository.java
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


     //(added by Dharnesh for unit testing) Top 10 scores for an activity at a specific date/time, sorted by museScore desc
        List<LocationActivityScore> findTop10ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc(
                String activityName,
                LocalDate eventDate,
                LocalTime eventTime
        );
        //(added by Dharnesh for unit testing) Top scores for an activity (ignoring date/time) using the given Pageable
        List<LocationActivityScore> findByActivityNameAndHistoricalActivityScoreNotNullOrderByHistoricalActivityScoreDesc(
                String activityName,
                Pageable pageable
        );


    // ——————————————————————————————————————————
    // PERFORMANCE FIX: Two-step query to avoid DISTINCT + FETCH issues
    // ——————————————————————————————————————————
    @Query(value = """
        SELECT las.id FROM location_activity_scores las
        WHERE las.id IN (
            SELECT DISTINCT ON (las2.location_id) las2.id
            FROM location_activity_scores las2
            JOIN activities a ON las2.activity_id = a.id
            WHERE LOWER(a.name) = LOWER(:activityName)
            ORDER BY las2.location_id, las2.historical_activity_score DESC NULLS LAST
        )
        ORDER BY las.historical_activity_score DESC NULLS LAST
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findDistinctLocationIdsByActivityName(@Param("activityName") String activityName, @Param("limit") int limit);

    @Query("""
        SELECT las FROM LocationActivityScore las
        JOIN FETCH las.location
        JOIN FETCH las.taxiZone
        WHERE las.id IN :ids
        """)
    List<LocationActivityScore> findByIdsWithEagerLoading(@Param("ids") List<UUID> ids);

    // ——————————————————————————————————————————
    // Keep original method as backup (you can remove this later)
    // ——————————————————————————————————————————
    @Query(value = """
    SELECT * FROM location_activity_scores las
    WHERE las.id IN (
        SELECT DISTINCT ON (las2.location_id) las2.id
        FROM location_activity_scores las2
        JOIN activities a ON las2.activity_id = a.id
        WHERE LOWER(a.name) = LOWER(:activityName)
        ORDER BY las2.location_id, las2.historical_activity_score DESC NULLS LAST
    )
    ORDER BY las.historical_activity_score DESC NULLS LAST
    """, nativeQuery = true)
    List<LocationActivityScore> findDistinctLocationsByActivityName(@Param("activityName") String activityName, Pageable pageable);

    // ——————————————————————————————————————————
    @Query("""
        SELECT DISTINCT l.eventDate
          FROM LocationActivityScore l
         WHERE l.activity.name = :activityName
    """)
    List<LocalDate> findAvailableDatesByActivity(
            @Param("activityName") String activityName
    );

    @Query("""
        SELECT DISTINCT l.eventTime
          FROM LocationActivityScore l
         WHERE l.activity.name = :activityName
           AND l.eventDate = :eventDate
    """)
    List<LocalTime> findAvailableTimesByActivityAndDate(
            @Param("activityName") String activityName,
            @Param("eventDate") LocalDate eventDate
    );

    @Query("""
        SELECT COUNT(l)
          FROM LocationActivityScore l
         WHERE l.museScore IS NOT NULL
    """)
    Long countRecordsWithMLPredictions();

    @Query("""
        SELECT COUNT(l)
          FROM LocationActivityScore l
         WHERE l.historicalActivityScore IS NOT NULL
    """)
    Long countRecordsWithHistoricalData();

    List<LocationActivityScore> findByActivityIdAndEventDateAndEventTime(
            UUID activityId,
            LocalDate eventDate,
            LocalTime eventTime
    );
}
