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

    @Autowired
    private AnalyticsService analyticsService;

    @Cacheable(cacheNames = "recommendations", key = "#request.activity + '_' + #request.dateTime.toString()")
    @Transactional
    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false; // Will be true if this method doesn't execute (cache hit)

        System.out.println("CACHE MISS - Processing recommendation request for activity: " + request.getActivity() + ", dateTime: " + request.getDateTime());

        LocalDateTime requestDateTime = request.getDateTime();
        String activityName = request.getActivity();

        try {
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
            // Increased sample size to give distance filter more options
            List<LocationActivityScore> sample = universe.stream()
                    .limit(50)
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
                BigDecimal baseScore = cult.multiply(BigDecimal.valueOf(0.6))
                        .add(crowd.multiply(BigDecimal.valueOf(0.4)));
                BigDecimal muse = baseScore.multiply(BigDecimal.valueOf(0.15))
                        .add(BigDecimal.valueOf(8.0));
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

            // Apply geographic filtering with 1000m minimum distance
            List<LocationRecommendationResponse> top10 = filterByDistance(mapped, 1000.0);

            RecommendationResponse response = new RecommendationResponse(top10, activityName, requestDateTime.toString());

            // Track analytics
            long responseTime = System.currentTimeMillis() - startTime;
            analyticsService.trackRequest(activityName, requestDateTime, cacheHit, responseTime);

            return response;

        } catch (Exception e) {
            // Track failed request
            long responseTime = System.currentTimeMillis() - startTime;
            analyticsService.trackRequest(activityName, requestDateTime, cacheHit, responseTime);
            throw e;
        }
    }

    /**
     * Filter locations to ensure minimum distance between recommendations
     */
    private List<LocationRecommendationResponse> filterByDistance(
            List<LocationRecommendationResponse> locations,
            double minDistanceMeters) {

        List<LocationRecommendationResponse> filtered = new ArrayList<>();

        for (LocationRecommendationResponse candidate : locations) {
            boolean tooClose = false;

            // Check distance against all already selected locations
            for (LocationRecommendationResponse selected : filtered) {
                double distance = calculateDistance(
                        candidate.getLatitude().doubleValue(),
                        candidate.getLongitude().doubleValue(),
                        selected.getLatitude().doubleValue(),
                        selected.getLongitude().doubleValue()
                );

                if (distance < minDistanceMeters) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                filtered.add(candidate);

                // Stop when we have 10 locations
                if (filtered.size() >= 10) {
                    break;
                }
            }
        }

        return filtered;
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    protected PredictionResponse[] callMLModelBatch(List<Map<String,Object>> bodies) {
        System.out.println("Calling ML service for batch prediction with " + bodies.size() + " locations");
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