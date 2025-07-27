package com.creativespacefinder.manhattan.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class EventLocationTest {

    @Test
    void testHelperConstructor_setsNameAndCoordsAndNullZone() {
        EventLocation loc = new EventLocation("Central Park", 40.785091, -73.968285);

        assertThat(loc.getId()).isNull();
        assertThat(loc.getLocationName()).isEqualTo("Central Park");
        assertThat(loc.getLatitude())
            .isEqualByComparingTo(BigDecimal.valueOf(40.785091));
        assertThat(loc.getLongitude())
            .isEqualByComparingTo(BigDecimal.valueOf(-73.968285));
        assertThat(loc.getNearestTaxiZone()).isNull();
    }

    @Test
    void plainSettersAndGetters_workAsExpected() {
        EventLocation loc = new EventLocation();
        loc.setLocationName("Times Square");
        loc.setLatitude(BigDecimal.valueOf(40.7580));
        loc.setLongitude(BigDecimal.valueOf(-73.9855));

        TaxiZone z = new TaxiZone();
        loc.setNearestTaxiZone(z);

        assertThat(loc.getLocationName()).isEqualTo("Times Square");
        assertThat(loc.getLatitude()).isEqualByComparingTo("40.7580");
        assertThat(loc.getLongitude()).isEqualByComparingTo("-73.9855");
        assertThat(loc.getNearestTaxiZone()).isSameAs(z);
    }
}
