package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.LocationRecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import com.creativespacefinder.manhattan.repository.MLPredictionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationRecommendationServiceSimpleTests {

    @Mock ActivityRepository activityRepository;
    @Mock LocationActivityScoreRepository locationScoreRepo;
    @Mock MLPredictionLogRepository mlLogRepo;
    @Mock AnalyticsService analyticsService;

    @InjectMocks
    private LocationRecommendationService svc;

    @Test
    void getAllActivities_delegatesToRepository() {
        Activity a1 = new Activity(); a1.setName("A");
        Activity a2 = new Activity(); a2.setName("B");
        when(activityRepository.findAll())
                .thenReturn(Arrays.asList(a1, a2));

        List<Activity> result = svc.getAllActivities();
        assertThat(result)
                .hasSize(2)
                .extracting(Activity::getName)
                .containsExactly("A", "B");
    }

    @Test
    void getAvailableDates_delegates() {
        String activity = "Something";
        List<LocalDate> dates = Arrays.asList(LocalDate.of(2025,1,1), LocalDate.of(2025,1,2));
        when(locationScoreRepo.findAvailableDatesByActivity(activity))
                .thenReturn(dates);

        List<LocalDate> result = svc.getAvailableDates(activity);
        assertThat(result).isEqualTo(dates);
    }

    @Test
    void getAvailableTimes_delegates() {
        String activity = "SomethingElse";
        LocalDate on = LocalDate.of(2025,2,2);
        List<LocalTime> times = Arrays.asList(LocalTime.of(9,0), LocalTime.of(15,30));
        when(locationScoreRepo.findAvailableTimesByActivityAndDate(activity, on))
                .thenReturn(times);

        List<LocalTime> result = svc.getAvailableTimes(activity, on);
        assertThat(result).isEqualTo(times);
    }

    @Test
    void getAvailableZones_returnsStaticKeys() {
        List<String> zones = svc.getAvailableZones();
        assertThat(zones)
                .contains("financial district", "midtown")
                .doesNotContain("NOT_A_ZONE");
    }
}
