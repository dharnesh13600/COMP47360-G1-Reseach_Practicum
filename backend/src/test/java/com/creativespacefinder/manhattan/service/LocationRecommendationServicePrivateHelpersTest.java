package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.LocationRecommendationResponse;
import com.creativespacefinder.manhattan.entity.EventLocation;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.entity.TaxiZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class LocationRecommendationServicePrivateHelpersTest {

    private LocationRecommendationService service;

    @BeforeEach void init() {
        service = new LocationRecommendationService();
    }

    private LocationRecommendationResponse makeResponse(double lat, double lon, double crowdScore) {
        return new LocationRecommendationResponse(
                UUID.randomUUID(),
                "Zone",
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lon),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(crowdScore),
                0
        );
    }

    private LocationActivityScore makeScore(String zoneName) {
        var las = new LocationActivityScore();
        var loc = new EventLocation();
        var tz = new TaxiZone();
        tz.setZoneName(zoneName);
        las.setLocation(loc);
        las.setTaxiZone(tz);
        return las;
    }

    @Test
    void assignCrowdLevels_variedScores_assignsQuietModerateBusy() {
        var l1 = makeResponse(0,0,1.0);
        var l2 = makeResponse(0,0,5.0);
        var l3 = makeResponse(0,0,9.0);
        var list = new ArrayList<>(List.of(l1,l2,l3));

        ReflectionTestUtils.invokeMethod(service, "assignCrowdLevels", list);

        assertThat(list).extracting(LocationRecommendationResponse::getCrowdLevel)
                .containsExactlyInAnyOrder("Quiet","Moderate","Busy");
    }

    @Test
    void assignCrowdLevels_sameScores_andThreeLocations_forcesVariation() {
        var l1 = makeResponse(0,0,5.0);
        var l2 = makeResponse(0,0,5.0);
        var l3 = makeResponse(0,0,5.0);
        var list = new ArrayList<>(List.of(l1,l2,l3));

        ReflectionTestUtils.invokeMethod(service, "assignCrowdLevels", list);

        assertThat(list.get(0).getCrowdLevel()).isEqualTo("Busy");
        assertThat(list.get(1).getCrowdLevel()).isEqualTo("Moderate");
        assertThat(list.get(2).getCrowdLevel()).isEqualTo("Quiet");
    }

    @Test
    void filterByDistance_respectsMinDistance() {
        var r1 = makeResponse(40, -73, 0);
        var r2 = makeResponse(40.01, -73, 0);
        var r3 = makeResponse(40.02, -73, 0);
        var in = new ArrayList<>(List.of(r1,r2,r3));

        @SuppressWarnings("unchecked")
        var out = (List<LocationRecommendationResponse>) ReflectionTestUtils.invokeMethod(
                service, "filterByDistance", in, 1000.0, 10
        );
        assertThat(out).hasSize(3);
    }

    @Test
    void filterByZone_selectsOnlyMatching() {
        var s1 = makeScore("Midtown South");
        var s2 = makeScore("Central Harlem");
        var in = new ArrayList<>(List.of(s1,s2));

        @SuppressWarnings("unchecked")
        var out = (List<LocationActivityScore>) ReflectionTestUtils.invokeMethod(
                service, "filterByZone", in, "midtown"
        );
        assertThat(out).contains(s1).doesNotContain(s2);
    }


    @Test
    void calculateDistance_sameCoordinates_returnsZero() {
        double d = ReflectionTestUtils.invokeMethod(
                service, "calculateDistance", 40.0, -73.0, 40.0, -73.0
        );
        assertThat(d).isEqualTo(0.0);
    }

    @Test
    void calculateDistance_knownPoints_returnsExpectedApprox() {
        double d = ReflectionTestUtils.invokeMethod(
                service, "calculateDistance", 40.0, -73.0, 41.0, -73.0
        );
        assertThat(d)
                .isCloseTo(111_000.0, within(1_000.0));
    }
}

