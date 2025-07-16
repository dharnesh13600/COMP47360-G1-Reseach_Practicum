package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.LocationRecommendationResponse;
import com.creativespacefinder.manhattan.dto.PredictionResponse;
import com.creativespacefinder.manhattan.dto.RecommendationRequest;
import com.creativespacefinder.manhattan.dto.RecommendationResponse;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.entity.MLPredictionLog;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import com.creativespacefinder.manhattan.repository.MLPredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationRecommendationService {

    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MLPredictionLogRepository mlPredictionLogRepository;

    @Transactional
    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        LocalDateTime requestDateTime = request.getDateTime();
        String activityName = request.getActivity();

        Activity activity = activityRepository.findByName(activityName)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + activityName));

        List<LocationActivityScore> universe =
                locationActivityScoreRepository
                        .findDistinctLocationsByActivityName(
                                activityName,
                                PageRequest.of(0, 500)
                        );

        if (universe.isEmpty()) {
            return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
        }

        Collections.shuffle(universe);
        List<LocationActivityScore> sample = universe.stream()
                .limit(20)
                .collect(Collectors.toList());

        List<Map<String,Object>> mlPayload = sample.stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("latitude",  s.getLocation().getLatitude().doubleValue());
            m.put("longitude", s.getLocation().getLongitude().doubleValue());
            m.put("hour",      requestDateTime.getHour());
            m.put("month",     requestDateTime.getMonthValue());
            m.put("day",       requestDateTime.getDayOfMonth());
            m.put("cultural_activity_prefered", activityName);
            return m;
        }).collect(Collectors.toList());

        PredictionResponse[] predictions = callMLModelBatch(mlPayload);

        int limit = Math.min(predictions.length, sample.size());
        Map<UUID, BigDecimal> mlScores = new HashMap<>();
        for (int i = 0; i < limit; i++) {
            LocationActivityScore las = sample.get(i);
            PredictionResponse p    = predictions[i];
            BigDecimal cult = BigDecimal.valueOf(p.getCreativeActivityScore());
            BigDecimal crowd = BigDecimal.valueOf(p.getCrowdScore());
            BigDecimal muse = cult.multiply(BigDecimal.valueOf(0.6))
                    .add(crowd.multiply(BigDecimal.valueOf(0.4)));
            las.setCulturalActivityScore(cult);
            las.setCrowdScore(crowd);
            las.setEstimatedCrowdNumber(p.getEstimatedCrowdNumber());
            las.setMuseScore(muse);
            mlScores.put(las.getLocation().getId(), cult);
        }
        List<LocationActivityScore> processed = sample.subList(0, limit);
        locationActivityScoreRepository.saveAll(processed);

        MLPredictionLog log = new MLPredictionLog();
        log.setId(UUID.randomUUID());
        log.setModelVersion("1.0");
        log.setPredictionType("location_recommendation");
        log.setRecordsProcessed(processed.size());
        log.setRecordsUpdated(processed.size());
        log.setPredictionDate(OffsetDateTime.now());
        mlPredictionLogRepository.save(log);

        List<LocationRecommendationResponse> mapped = processed.stream()
                .sorted(Comparator.comparing(LocationActivityScore::getMuseScore).reversed())
                .map(las -> new LocationRecommendationResponse(
                        las.getLocation().getId(),
                        las.getLocation().getLocationName(),
                        las.getLocation().getLatitude(),
                        las.getLocation().getLongitude(),
                        mlScores.get(las.getLocation().getId()),
                        las.getMuseScore(),
                        las.getCrowdScore(),
                        las.getEstimatedCrowdNumber()
                ))
                .collect(Collectors.toList());

        System.out.println("Mapped entries: " + mapped.size());
        for (LocationRecommendationResponse lr : mapped) {
            System.out.println(lr.getLatitude() + ", " + lr.getLongitude());
        }

        Map<String,LocationRecommendationResponse> best = new HashMap<>();
        for (LocationRecommendationResponse lr : mapped) {
            String key = lr.getLatitude().toPlainString() + ":" + lr.getLongitude().toPlainString();
            best.merge(key, lr, (oldV, newV) ->
                    newV.getMuseScore().compareTo(oldV.getMuseScore()) > 0 ? newV : oldV
            );
        }

        List<LocationRecommendationResponse> top10 = best.values().stream()
                .sorted(Comparator.comparing(LocationRecommendationResponse::getMuseScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return new RecommendationResponse(top10, activityName, requestDateTime.toString());
    }

    @Cacheable(cacheNames = "mlPredictions", key = "#bodies.hashCode()")
    protected PredictionResponse[] callMLModelBatch(List<Map<String,Object>> bodies) {
        RestTemplate r = new RestTemplate();
        return r.postForObject("http://localhost:8000/predict_batch", bodies, PredictionResponse[].class);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public List<LocalDate> getAvailableDates(String activityName) {
        return locationActivityScoreRepository.findAvailableDatesByActivity(activityName);
    }

    public List<LocalTime> getAvailableTimes(String activityName, LocalDate date) {
        return locationActivityScoreRepository.findAvailableTimesByActivityAndDate(activityName, date);
    }
}
