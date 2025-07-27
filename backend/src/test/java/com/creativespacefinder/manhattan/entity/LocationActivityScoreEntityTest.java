package com.creativespacefinder.manhattan.entity;

import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.entity.EventLocation;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.entity.TaxiZone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class LocationActivityScoreEntityTest {

    @Test
    void testFullConstructorAndGettersSetters() {
        UUID id = UUID.randomUUID();
        int eid = 42;
        EventLocation loc   = new EventLocation("MyPlace", 1.23, 4.56);
        Activity act   = new Activity("Climbing");
        TaxiZone tz    = new TaxiZone("MyZone", BigDecimal.valueOf(7.8), BigDecimal.valueOf(9.0));
        LocalDate      date  = LocalDate.of(2025, 8, 15);
        LocalTime      time  = LocalTime.of(18, 30);

        BigDecimal histCrowd = BigDecimal.valueOf(0.33);
        BigDecimal histAct   = BigDecimal.valueOf(0.44);
        LocationActivityScore score = new LocationActivityScore(
                eid, loc, act, tz, date, time,
                histCrowd, histAct
        );

        score.setId(id);
        score.setCreatedAt(LocalDateTime.now().minusDays(1));
        score.setUpdatedAt(LocalDateTime.now());
        score.setMlPredictionDate(LocalDateTime.now());

        BigDecimal cult = BigDecimal.valueOf(1.1);
        BigDecimal crowd = BigDecimal.valueOf(2.2);
        BigDecimal muse = BigDecimal.valueOf(3.3);
        score.setCulturalActivityScore(cult);
        score.setCrowdScore(crowd);
        score.setMuseScore(muse);

        assertThat(score.getId()).isEqualTo(id);
        assertThat(score.getEventId()).isEqualTo(eid);
        assertThat(score.getLocation()).isSameAs(loc);
        assertThat(score.getActivity()).isSameAs(act);
        assertThat(score.getTaxiZone()).isSameAs(tz);
        assertThat(score.getEventDate()).isEqualTo(date);
        assertThat(score.getEventTime()).isEqualTo(time);

        assertThat(score.getHistoricalTaxiZoneCrowdScore())
                .isEqualByComparingTo(histCrowd);
        assertThat(score.getHistoricalActivityScore())
                .isEqualByComparingTo(histAct);

        assertThat(score.getCulturalActivityScore())
                .isEqualByComparingTo(cult);
        assertThat(score.getCrowdScore())
                .isEqualByComparingTo(crowd);
        assertThat(score.getMuseScore())
                .isEqualByComparingTo(muse);

        assertThat(score.getCreatedAt()).isNotNull();
        assertThat(score.getUpdatedAt()).isNotNull();
        assertThat(score.getMlPredictionDate()).isNotNull();
    }

    @Test
    void testHelperMethodsAndRecommendationStatus() {
        LocationActivityScore score = new LocationActivityScore();

        assertThat(score.hasMLPredictions()).isFalse();
        assertThat(score.hasHistoricalData()).isFalse();
        assertThat(score.getRecommendationStatus()).isEqualTo("no_data");

        score.setHistoricalActivityScore(BigDecimal.ONE);
        assertThat(score.hasHistoricalData()).isTrue();
        assertThat(score.hasMLPredictions()).isFalse();
        assertThat(score.getRecommendationStatus())
                .isEqualTo("historical_fallback");

        score.setCulturalActivityScore(BigDecimal.ONE);
        score.setCrowdScore(BigDecimal.ONE);
        score.setMuseScore(BigDecimal.ONE);

        assertThat(score.hasMLPredictions()).isTrue();
        assertThat(score.getRecommendationStatus()).isEqualTo("ml_ready");
    }
}
