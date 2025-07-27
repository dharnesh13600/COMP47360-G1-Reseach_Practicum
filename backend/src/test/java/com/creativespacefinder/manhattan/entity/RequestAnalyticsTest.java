package com.creativespacefinder.manhattan.entity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestAnalyticsTest {

    @Test
    void defaultConstructor_initializesDefaults() {
        RequestAnalytics ra = new RequestAnalytics();

        assertNull(ra.getId());

        assertNull(ra.getActivityName());
        assertNull(ra.getRequestedHour());
        assertNull(ra.getRequestedDayOfWeek());

        assertEquals(1, ra.getRequestCount());

        assertNull(ra.getLastRequested());

        assertFalse(ra.getCacheHit());

        assertNull(ra.getResponseTimeMs());
        assertNull(ra.getUserAgent());
    }

    @Test
    void allArgsConstructor_andGetters() {
        String activity = "testActivity";
        Integer hour = 14;
        Integer dow = 3;
        Boolean cacheHit = true;
        Long respTime = 250L;
        String ua = "JUnit";

        RequestAnalytics ra = new RequestAnalytics(activity, hour, dow, cacheHit, respTime, ua);

        assertNull(ra.getId(), "ID should still be null before persistence");
        assertEquals(activity, ra.getActivityName());
        assertEquals(hour, ra.getRequestedHour());
        assertEquals(dow, ra.getRequestedDayOfWeek());

        assertNotNull(ra.getLastRequested());
        Duration age = Duration.between(ra.getLastRequested(), LocalDateTime.now());
        assertTrue(age.toMillis() < 1000, "lastRequested should be within the last second");

        assertEquals(cacheHit, ra.getCacheHit());
        assertEquals(respTime, ra.getResponseTimeMs());
        assertEquals(ua, ra.getUserAgent());
        assertEquals(1, ra.getRequestCount());
    }

    @Test
    void setters_andGetters_roundTrip() {
        RequestAnalytics ra = new RequestAnalytics();

        UUID id = UUID.randomUUID();
        ra.setId(id);
        assertEquals(id, ra.getId());

        ra.setActivityName("foo");
        assertEquals("foo", ra.getActivityName());

        ra.setRequestedHour(23);
        assertEquals(23, ra.getRequestedHour());

        ra.setRequestedDayOfWeek(7);
        assertEquals(7, ra.getRequestedDayOfWeek());

        ra.setRequestCount(42);
        assertEquals(42, ra.getRequestCount());

        LocalDateTime then = LocalDateTime.of(2000,1,1, 0, 0);
        ra.setLastRequested(then);
        assertEquals(then, ra.getLastRequested());

        ra.setCacheHit(true);
        assertTrue(ra.getCacheHit());

        ra.setResponseTimeMs(999L);
        assertEquals(999L, ra.getResponseTimeMs());

        ra.setUserAgent("agent");
        assertEquals("agent", ra.getUserAgent());
    }

    @Test
    void incrementRequestCount_incrementsAndUpdatesTimestamp() throws InterruptedException {
        RequestAnalytics ra = new RequestAnalytics();
        ra.setRequestCount(5);
        LocalDateTime before = LocalDateTime.now();
        ra.setLastRequested(before.minusMinutes(5));

        Thread.sleep(10);

        ra.incrementRequestCount();

        assertEquals(6, ra.getRequestCount(), "Request count should increment by 1");
        assertNotNull(ra.getLastRequested());
        assertTrue(ra.getLastRequested().isAfter(before),
                "lastRequested should be updated to now (after the original timestamp)");
    }
}
