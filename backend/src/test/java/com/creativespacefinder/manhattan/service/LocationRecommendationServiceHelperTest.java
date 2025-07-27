package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationRecommendationServiceHelperTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private LocationActivityScoreRepository locationActivityScoreRepository;
    @InjectMocks private LocationRecommendationService service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllActivities_returnsFromRepo() {
        Activity a1 = new Activity("Yoga");
        Activity a2 = new Activity("Dance");
        when(activityRepository.findAll()).thenReturn(List.of(a1,a2));

        var result = service.getAllActivities();
        assertThat(result).containsExactly(a1,a2);
    }

    @Test
    void getAvailableDates_returnsFromRepo() {
        LocalDate d1 = LocalDate.of(2025,7,20);
        LocalDate d2 = LocalDate.of(2025,7,21);
        when(locationActivityScoreRepository.findAvailableDatesByActivity("A"))
                .thenReturn(List.of(d1,d2));

        var result = service.getAvailableDates("A");
        assertThat(result).containsExactly(d1,d2);
    }

    @Test
    void getAvailableTimes_returnsFromRepo() {
        var date = LocalDate.of(2025,7,20);
        LocalTime t1 = LocalTime.of(9,0), t2 = LocalTime.of(17,0);
        when(locationActivityScoreRepository.findAvailableTimesByActivityAndDate("A", date))
                .thenReturn(List.of(t1,t2));

        var result = service.getAvailableTimes("A", date);
        assertThat(result).containsExactly(t1,t2);
    }

    @Test
    void getAvailableZones_containsKnownZones() {
        var zones = service.getAvailableZones();
        assertThat(zones).contains("midtown","harlem","inwood","central park");
    }
}
