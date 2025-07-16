package com.creativespacefinder.manhattan.repository;

import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.entity.EventLocation;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.entity.TaxiZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest

class LocationActivityScoreRepositoryTest {

    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Activity hikingActivity;
    private Activity cyclingActivity;
    private TaxiZone centralParkZone;
    private EventLocation bethesdaTerrace;
    private LocalDate testDate;
    private LocalTime testTime;

    @BeforeEach
    void setUp() {
        entityManager.getEntityManager().createQuery("DELETE FROM LocationActivityScore").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM EventLocation").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM TaxiZone").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Activity").executeUpdate();


        hikingActivity = entityManager.persistAndFlush(new Activity("Hiking"));
        cyclingActivity = entityManager.persistAndFlush(new Activity("Cycling"));
        centralParkZone = entityManager.persistAndFlush(new TaxiZone("Central Park", BigDecimal.valueOf(40.78), BigDecimal.valueOf(-73.96)));
        bethesdaTerrace = entityManager.persistAndFlush(new EventLocation("Bethesda Terrace", BigDecimal.valueOf(40.779), BigDecimal.valueOf(-73.97), centralParkZone));

        testDate = LocalDate.of(2025, 7, 10);
        testTime = LocalTime.of(15, 0);

        // ML data
        createAndPersistScore(1, bethesdaTerrace, hikingActivity, centralParkZone, testDate, testTime,
                BigDecimal.valueOf(0.9), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.85), null);
        createAndPersistScore(2, bethesdaTerrace, hikingActivity, centralParkZone, testDate, testTime,
                BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.70), null);
        createAndPersistScore(3, bethesdaTerrace, hikingActivity, centralParkZone, LocalDate.of(2025, 7, 11), LocalTime.of(10, 0),
                BigDecimal.valueOf(0.6), BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.60), null);

        // Historical data (no ML scores)
        createAndPersistScore(4, bethesdaTerrace, hikingActivity, centralParkZone, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0),
                null, null, null, BigDecimal.valueOf(0.75));
        createAndPersistScore(5, bethesdaTerrace, hikingActivity, centralParkZone, LocalDate.of(2024, 1, 2), LocalTime.of(12, 0),
                null, null, null, BigDecimal.valueOf(0.80));

        // Other activity data
        createAndPersistScore(6, bethesdaTerrace, cyclingActivity, centralParkZone, testDate, testTime,
                BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5), null);
    }

    private void createAndPersistScore(int eventId, EventLocation location, Activity activity, TaxiZone taxiZone,
                                        LocalDate date, LocalTime time, BigDecimal culturalScore, BigDecimal crowdScore,
                                        BigDecimal museScore, BigDecimal historicalScore) {
        LocationActivityScore score = new LocationActivityScore();
        score.setEventId(eventId);
        score.setLocation(location);
        score.setActivity(activity);
        score.setTaxiZone(taxiZone);
        score.setEventDate(date);
        score.setEventTime(time);
        score.setCulturalActivityScore(culturalScore);
        score.setCrowdScore(crowdScore);
        score.setMuseScore(museScore);
        score.setHistoricalActivityScore(historicalScore);
        entityManager.persistAndFlush(score);
    }

    @Test
    void findTop10ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc() { // CHANGED METHOD NAME HERE
        List<LocationActivityScore> results = locationActivityScoreRepository
                .findTop10ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc( // CHANGED METHOD CALL HERE
                        hikingActivity.getName(), testDate, testTime
                );

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(BigDecimal.valueOf(0.85), results.get(0).getMuseScore());
        assertEquals(BigDecimal.valueOf(0.70), results.get(1).getMuseScore());
    }

    @Test
    void findTopByActivityNameIgnoreDateTime() {
        Pageable pageable = PageRequest.of(0, 5);
        List<LocationActivityScore> results = locationActivityScoreRepository
                .findTopByActivityNameIgnoreDateTime(hikingActivity.getName(), pageable);

        assertNotNull(results);
        // This assertion will fail until you fix the repository query
        assertEquals(2, results.size());
        assertEquals(BigDecimal.valueOf(0.80), results.get(0).getHistoricalActivityScore());
        assertEquals(BigDecimal.valueOf(0.75), results.get(1).getHistoricalActivityScore());
    }

    @Test
    void findAvailableDatesByActivity() {
        List<LocalDate> dates = locationActivityScoreRepository.findAvailableDatesByActivity(hikingActivity.getName());
        assertNotNull(dates);
        // Corrected assertion
        assertEquals(4, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 7, 10)));
        assertTrue(dates.contains(LocalDate.of(2025, 7, 11)));
    }

    @Test
    void findAvailableTimesByActivityAndDate() {
        List<LocalTime> times = locationActivityScoreRepository.findAvailableTimesByActivityAndDate(hikingActivity.getName(), testDate);
        assertNotNull(times);
        assertEquals(1, times.size());
        assertTrue(times.contains(LocalTime.of(15, 0)));
    }

    @Test
    void countRecordsWithMLPredictions() {
        Long count = locationActivityScoreRepository.countRecordsWithMLPredictions();
        // Corrected assertion
        assertEquals(4L, count);
    }

    @Test
    void countRecordsWithHistoricalData() {
        Long count = locationActivityScoreRepository.countRecordsWithHistoricalData();
        assertEquals(2L, count);
    }

    @Test
    void findTop10ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc_NoData() { // CHANGED METHOD NAME HERE
        List<LocationActivityScore> results = locationActivityScoreRepository
                .findTop10ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc( // CHANGED METHOD CALL HERE
                        "NonExistentActivity", testDate, testTime
                );
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}


