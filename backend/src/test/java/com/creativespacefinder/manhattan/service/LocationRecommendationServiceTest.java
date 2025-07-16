package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.*;
import com.creativespacefinder.manhattan.entity.*;
import com.creativespacefinder.manhattan.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationRecommendationServiceTests {

    @InjectMocks LocationRecommendationService service;

    @Mock LocationActivityScoreRepository scoreRepo;
    @Mock ActivityRepository             activityRepo;
    @Mock MLPredictionLogRepository      logRepo;

    Activity photo;

    @BeforeEach
    void setUp() {
        photo = new Activity();
        photo.setId(UUID.randomUUID());
        photo.setName("Photography");
    }

    /* SVC‑001 */
    @Test
    void unknownActivity_throws() {
        when(activityRepo.findByName("Surfing")).thenReturn(Optional.empty());

        RecommendationRequest req = new RecommendationRequest("Surfing", LocalDateTime.now());
        assertThatThrownBy(() -> service.getLocationRecommendations(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Activity not found");
    }

    /* SVC‑002 */
    @Test
    void noScores_returnsEmptyArray() {
        when(activityRepo.findByName("Photography")).thenReturn(Optional.of(photo));
        when(scoreRepo.findTopByActivityNameIgnoreDateTime(eq("Photography"), any(PageRequest.class)))
                .thenReturn(List.of());

        RecommendationResponse resp =
                service.getLocationRecommendations(new RecommendationRequest("Photography", LocalDateTime.now()));

        assertThat(resp.getLocations()).isEmpty();
        verifyNoInteractions(logRepo);
    }

    /* SVC‑003 */
    @Test
    void happyPath_returnsTop10_andPersists() {
        when(activityRepo.findByName("Photography")).thenReturn(Optional.of(photo));

        List<LocationActivityScore> cands = buildDummyScores(25);
        when(scoreRepo.findTopByActivityNameIgnoreDateTime(eq("Photography"), any(PageRequest.class)))
                .thenReturn(cands);

        // Spy to stub private ML batch
        LocationRecommendationService spy = Mockito.spy(service);
        doReturn(buildPredictions(cands.size())).when(spy).callMLModelBatch(any());

        RecommendationResponse resp =
                spy.getLocationRecommendations(new RecommendationRequest("Photography", LocalDateTime.now()));

        assertThat(resp.getLocations()).hasSize(10);
        verify(scoreRepo).saveAll(cands);
        verify(logRepo).save(any(MLPredictionLog.class));
    }

    /* SVC‑004 */
    @Test
    void getAllActivities_passThru() {
        service.getAllActivities();
        verify(activityRepo).findAll();
    }

    /* SVC‑005 */
    @Test
    void fewerPredictions_extraCandidatesIgnored() {
        when(activityRepo.findByName("Photography")).thenReturn(Optional.of(photo));

        List<LocationActivityScore> cands = buildDummyScores(5);
        when(scoreRepo.findTopByActivityNameIgnoreDateTime(eq("Photography"), any(PageRequest.class)))
                .thenReturn(cands);

        LocationRecommendationService spy = Mockito.spy(service);
        // Return only 3 predictions for 5 candidates
        doReturn(buildPredictions(3)).when(spy).callMLModelBatch(any());

        RecommendationResponse resp =
                spy.getLocationRecommendations(new RecommendationRequest("Photography", LocalDateTime.now()));

        // should still not blow up, just zero MuseScore for extra
        assertThat(resp.getLocations()).hasSizeLessThanOrEqualTo(5);
    }

    /* helpers */
    private List<LocationActivityScore> buildDummyScores(int n) {
        List<LocationActivityScore> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            EventLocation loc = new EventLocation("Loc-"+i,
                    BigDecimal.valueOf(40.7+i*0.01),
                    BigDecimal.valueOf(-73.9-i*0.01), null);
            loc.setId(UUID.randomUUID());

            LocationActivityScore las = new LocationActivityScore();
            las.setId(UUID.randomUUID());
            las.setActivity(photo);
            las.setLocation(loc);
            las.setEventDate(LocalDate.now());
            las.setEventTime(LocalTime.NOON);
            las.setMuseScore(BigDecimal.valueOf(i));
            list.add(las);
        }
        return list;
    }
    private PredictionResponse[] buildPredictions(int n) {
        PredictionResponse[] arr = new PredictionResponse[n];
        Arrays.fill(arr, new PredictionResponse(0f, 100, 0f, 0f));
        return arr;
    }
}
