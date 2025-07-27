package com.creativespacefinder.manhattan.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaxiZoneTest {

    @Test
    void noArgsConstructor_and_settersGetters_work() {
        TaxiZone tz = new TaxiZone();

        UUID id = UUID.randomUUID();
        String name = "Midtown";
        BigDecimal lat = BigDecimal.valueOf(40.754932);
        BigDecimal lon = BigDecimal.valueOf(-73.984016);
        LocalDateTime created = LocalDateTime.of(2025,7,27,10,30);
        LocalDateTime updated = LocalDateTime.of(2025,7,27,11,45);

        tz.setId(id);
        tz.setZoneName(name);
        tz.setLatitude(lat);
        tz.setLongitude(lon);
        tz.setCreatedAt(created);
        tz.setUpdatedAt(updated);

        assertEquals(id, tz.getId(),       "id should round‑trip");
        assertEquals(name, tz.getZoneName(), "zoneName should round‑trip");
        assertEquals(lat, tz.getLatitude(),  "latitude should round‑trip");
        assertEquals(lon, tz.getLongitude(), "longitude should round‑trip");
        assertEquals(created, tz.getCreatedAt(), "createdAt should round‑trip");
        assertEquals(updated, tz.getUpdatedAt(), "updatedAt should round‑trip");
    }

    @Test
    void allArgsConstructor_initializes_fields_correctly() {
        String name = "Downtown";
        BigDecimal lat = BigDecimal.valueOf(40.712776);
        BigDecimal lon = BigDecimal.valueOf(-74.005974);

        TaxiZone tz = new TaxiZone(name, lat, lon);

        assertNull(tz.getId(),"id should be null before persistence");
        assertEquals(name,tz.getZoneName(),"constructor should set zoneName");
        assertEquals(lat,tz.getLatitude(),"constructor should set latitude");
        assertEquals(lon,tz.getLongitude(),"constructor should set longitude");

        assertNull(tz.getCreatedAt(),"createdAt remains null until Hibernate populates it");
        assertNull(tz.getUpdatedAt(),"updatedAt remains null until Hibernate populates it");
    }

    @Test
    void toString_contains_zoneName_and_coordinates() {
        TaxiZone tz = new TaxiZone("Uptown", BigDecimal.ZERO, BigDecimal.ONE);
        String s = tz.toString();
        assertNotNull(s);
        assertTrue(s.contains("TaxiZone"), "toString() should include the class name");
    }
}
