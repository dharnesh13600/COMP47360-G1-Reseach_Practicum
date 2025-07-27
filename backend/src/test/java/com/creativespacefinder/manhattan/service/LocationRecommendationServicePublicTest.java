package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.LocationRecommendationResponse;
import com.creativespacefinder.manhattan.dto.PredictionResponse;
import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.EventLocation;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.entity.TaxiZone;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import com.creativespacefinder.manhattan.repository.MLPredictionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationRecommendationServicePublicTest {

    @Spy @InjectMocks
    private LocationRecommendationService service;

    @Mock private LocationActivityScoreRepository lasRepo;
    @Mock private ActivityRepository activityRepo;
    @Mock private MLPredictionLogRepository logRepo;
    @Mock private AnalyticsService analyticsService;

    private final LocalDateTime NOW = LocalDateTime.of(2025, 7, 17, 15, 0);

    @BeforeEach
    void setUp() {
    }

    private LocationActivityScore makeScore(UUID id, double lat, double lon, String zoneName) {
        EventLocation loc = new EventLocation();
        loc.setId(id);
        loc.setLocationName(zoneName);
        loc.setLatitude(BigDecimal.valueOf(lat));
        loc.setLongitude(BigDecimal.valueOf(lon));

        TaxiZone tz = new TaxiZone();
        tz.setZoneName(zoneName);

        LocationActivityScore s = new LocationActivityScore();
        s.setLocation(loc);
        s.setTaxiZone(tz);
        return s;
    }


    private void stubML(int count) {
        PredictionResponse[] prs = new PredictionResponse[count];
        for (int i = 0; i < count; i++) {
            prs[i] = new PredictionResponse(null, 1, 5f, 5f);
        }
        Mockito.doReturn(prs).when(service).callMLModelBatch(any());
    }

    @Test
    void whenActivityNotFound_thenThrowsRuntimeException() {
        // Arrange: make findByName return empty
        when(activityRepo.findByName("MissingActivity"))
                .thenReturn(Optional.empty());

        RecommendationRequest req = new RecommendationRequest("MissingActivity", NOW, null);

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.getLocationRecommendations(req)
        );
        assertThat(ex.getMessage()).isEqualTo("Activity not found: MissingActivity");
    }

    @Test
    void whenTwoLocationsTooClose_thenOnlyOneReturned() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(activityRepo.findByName("Test"))
                .thenReturn(Optional.of(new com.creativespacefinder.manhattan.entity.Activity()));
        when(lasRepo.findDistinctLocationIdsByActivityName("Test", 100))
                .thenReturn(List.of(id1.toString(), id2.toString()));

        var s1 = makeScore(id1, 40.0, -73.0, "Z");
        var s2 = makeScore(id2, 40.00045, -73.0, "Z");
        when(lasRepo.findByIdsWithEagerLoading(any()))
                .thenReturn(new ArrayList<>(List.of(s1, s2))); // mutable list

        stubML(2);
        RecommendationResponse resp = service.getLocationRecommendations(
                new RecommendationRequest("Test", NOW, null)
        );

        assertThat(resp.getTotalResults()).isEqualTo(1);
    }

    @Test
    void whenTwoLocationsAtThreshold_thenBothReturned() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(activityRepo.findByName("Test"))
                .thenReturn(Optional.of(new com.creativespacefinder.manhattan.entity.Activity()));
        when(lasRepo.findDistinctLocationIdsByActivityName("Test", 100))
                .thenReturn(List.of(id1.toString(), id2.toString()));

        var s1 = makeScore(id1, 40.0, -73.0, "Z");
        var s2 = makeScore(id2, 40.009, -73.0, "Z");
        when(lasRepo.findByIdsWithEagerLoading(any()))
                .thenReturn(new ArrayList<>(List.of(s1, s2))); // mutable list

        stubML(2);
        RecommendationResponse resp = service.getLocationRecommendations(
                new RecommendationRequest("Test", NOW, null)
        );

        assertThat(resp.getTotalResults()).isEqualTo(2);
    }

    @Test
    void whenThreeUniformScores_thenCrowdLevelsVary() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        when(activityRepo.findByName("Test"))
                .thenReturn(Optional.of(new com.creativespacefinder.manhattan.entity.Activity()));
        when(lasRepo.findDistinctLocationIdsByActivityName("Test", 100))
                .thenReturn(List.of(id1.toString(), id2.toString(), id3.toString()));

        var s1 = makeScore(id1, 40.0, -73.0, "Z");
        var s2 = makeScore(id2, 40.01, -73.0, "Z");
        var s3 = makeScore(id3, 40.02, -73.0, "Z");
        when(lasRepo.findByIdsWithEagerLoading(any()))
                .thenReturn(new ArrayList<>(List.of(s1, s2, s3)));

        stubML(3);
        RecommendationResponse resp = service.getLocationRecommendations(
                new RecommendationRequest("Test", NOW, null)
        );

        List<String> levels = resp.getLocations().stream()
                .map(LocationRecommendationResponse::getCrowdLevel)
                .toList();

        assertThat(levels).containsExactlyInAnyOrder("Quiet","Moderate","Busy");
    }

    @Test
    void getAvailableZones_includesAllDefinedZones() {
        List<String> zones = service.getAvailableZones();
        assertThat(zones).isNotEmpty().contains("midtown","harlem","inwood","central park");
    }
}
