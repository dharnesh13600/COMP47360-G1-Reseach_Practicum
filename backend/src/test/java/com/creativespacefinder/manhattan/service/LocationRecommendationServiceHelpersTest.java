package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import com.creativespacefinder.manhattan.repository.MLPredictionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// Tests the helper methods in LocationRecommendadtionService
class LocationRecommendationServiceHelpersTest {

    //tells spring and mockito to inject a mock instance

  @Mock LocationActivityScoreRepository lasRepo;
  @Mock ActivityRepository activityRepo;
  @Mock MLPredictionLogRepository logRepo;
  @Mock AnalyticsService analyticsService;
  @Spy @InjectMocks LocationRecommendationService svc;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(svc, "mlPredictUrl", "http://dummy");
  }

  @Test
  void getAllActivities_delegatesToRepo() {
    Activity a1 = new Activity("A");
    Activity a2 = new Activity("B");

    when(activityRepo.findAll()).thenReturn(List.of(a1, a2));

    List<Activity> got = svc.getAllActivities();
    assertThat(got).containsExactly(a1, a2);
  }

  @Test
  void getAvailableDates_delegatesToRepo() {
    when(lasRepo.findAvailableDatesByActivity("X"))
            .thenReturn(List.of(LocalDate.of(2025, 1, 1)));

    List<LocalDate> dates = svc.getAvailableDates("X");
    assertThat(dates).containsExactly(LocalDate.of(2025, 1, 1));
  }

  @Test
  void getAvailableTimes_delegatesToRepo() {
    when(lasRepo.findAvailableTimesByActivityAndDate("X", LocalDate.of(2025, 1, 1)))
            .thenReturn(List.of(LocalTime.NOON));

    List<LocalTime> times = svc.getAvailableTimes("X", LocalDate.of(2025, 1, 1));
    assertThat(times).containsExactly(LocalTime.NOON);
  }

  @Test
  void getAvailableZones_returns_allKeysFromStaticMap() {
    List<String> zones = svc.getAvailableZones();
    assertThat(zones).contains("midtown", "central park", "soho hudson square");
    @SuppressWarnings("unchecked")
    var map = (java.util.Map<String, List<String>>) ReflectionTestUtils.getField(
            LocationRecommendationService.class, "MANHATTAN_ZONES");

    assertThat(zones.size()).isEqualTo(map.size());
  }

  @Test
  void isQuietActivity_detectsQuietKeywords() {
    // private method via reflection
    boolean b1 = (boolean) ReflectionTestUtils.invokeMethod(svc, "isQuietActivity", "reading");
    boolean b2 = (boolean) ReflectionTestUtils.invokeMethod(svc, "isQuietActivity", "hiking");

    assertThat(b1).isTrue();
    assertThat(b2).isFalse();
  }
}
