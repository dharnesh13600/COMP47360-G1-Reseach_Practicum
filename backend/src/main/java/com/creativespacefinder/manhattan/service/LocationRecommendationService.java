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
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.cache.annotation.Cacheable;
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

    @Value("${ml.predict.url}")               // <<< ADDED
    private String mlPredictUrl;              // <<< ADDED

    private static final Map<String, List<String>> MANHATTAN_ZONES = new HashMap<>();
    static {
        MANHATTAN_ZONES.put("financial district", Arrays.asList("Financial District North", "Financial District South", "World Trade Center", "Battery Park", "Battery Park City", "Seaport", "TriBeCa/Civic Center"));
        MANHATTAN_ZONES.put("soho hudson square", Arrays.asList("SoHo", "Hudson Sq", "Little Italy/NoLiTa"));
        MANHATTAN_ZONES.put("lower east side", Arrays.asList("Lower East Side", "Chinatown", "Two Bridges/Seward Park"));
        MANHATTAN_ZONES.put("east village", Arrays.asList("East Village", "Alphabet City"));
        MANHATTAN_ZONES.put("west village", Arrays.asList("West Village", "Greenwich Village North", "Greenwich Village South", "Meatpacking/West Village West"));
        MANHATTAN_ZONES.put("chelsea", Arrays.asList("West Chelsea/Hudson Yards", "East Chelsea", "Flatiron"));
        MANHATTAN_ZONES.put("midtown", Arrays.asList("Midtown South", "Midtown Center", "Midtown North"));
        MANHATTAN_ZONES.put("times square theater district", Arrays.asList("Times Sq/Theatre District", "Garment District"));
        MANHATTAN_ZONES.put("murray hill area", Arrays.asList("Murray Hill", "Kips Bay", "Gramercy"));
        MANHATTAN_ZONES.put("union square area", Arrays.asList("Union Sq", "Stuy Town/Peter Cooper Village", "UN/Turtle Bay South", "Sutton Place/Turtle Bay North"));
        MANHATTAN_ZONES.put("clinton hells kitchen", Arrays.asList("Clinton East", "Clinton West"));
        MANHATTAN_ZONES.put("upper west side", Arrays.asList("Upper West Side South", "Upper West Side North", "Lincoln Square East", "Lincoln Square West", "Manhattan Valley", "Bloomingdale"));
        MANHATTAN_ZONES.put("upper east side", Arrays.asList("Upper East Side South", "Upper East Side North", "Yorkville East", "Lenox Hill East", "Lenox Hill West"));
        MANHATTAN_ZONES.put("central park", Arrays.asList("Central Park"));
        MANHATTAN_ZONES.put("harlem", Arrays.asList("Central Harlem", "Central Harlem North", "Morningside Heights", "Manhattanville"));
        MANHATTAN_ZONES.put("hamilton heights", Arrays.asList("Hamilton Heights"));
        MANHATTAN_ZONES.put("east harlem", Arrays.asList("East Harlem South", "East Harlem North"));
        MANHATTAN_ZONES.put("washington heights", Arrays.asList("Washington Heights South", "Washington Heights North"));
        MANHATTAN_ZONES.put("inwood", Arrays.asList("Inwood", "Inwood Hill Park"));
        MANHATTAN_ZONES.put("special areas", Arrays.asList("Roosevelt Island", "Randalls Island", "Marble Hill", "Highbridge Park", "Governor's Island/Ellis Island/Liberty Island"));
    }

    @Cacheable(cacheNames = "recommendations", key = "#request.activity + '_' + #request.dateTime.toString() + '_' + (#request.selectedZone != null ? #request.selectedZone : 'all')")
    @Transactional
    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;

        String zoneInfo = (request.getSelectedZone() != null) ? request.getSelectedZone() : "ALL_MANHATTAN";
        System.out.println("CACHE MISS - Processing recommendation request for activity: " + request.getActivity() +
                ", dateTime: " + request.getDateTime() +
                ", zone: " + zoneInfo);

        LocalDateTime requestDateTime = request.getDateTime();
        String activityName = request.getActivity();
        String selectedZone = request.getSelectedZone();

        try {
            long dbStartTime = System.currentTimeMillis();
            Activity activity = activityRepository.findByName(activityName)
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + activityName));
            System.out.println("Activity lookup took: " + (System.currentTimeMillis() - dbStartTime) + "ms");

            dbStartTime = System.currentTimeMillis();
            // Two-step query for better performance
            List<String> locationIds = locationActivityScoreRepository
                    .findDistinctLocationIdsByActivityName(activityName, 100);
            System.out.println("Location ID query took: " + (System.currentTimeMillis() - dbStartTime) + "ms, found: " + locationIds.size() + " location IDs");

            if (locationIds.isEmpty()) {
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            dbStartTime = System.currentTimeMillis();
            List<UUID> uuids = locationIds.stream().map(UUID::fromString).collect(Collectors.toList());
            List<LocationActivityScore> universe = locationActivityScoreRepository.findByIdsWithEagerLoading(uuids);
            System.out.println("Eager loading query took: " + (System.currentTimeMillis() - dbStartTime) + "ms, loaded: " + universe.size() + " locations");

            if (universe.isEmpty()) {
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            if (selectedZone != null && !selectedZone.trim().isEmpty()) {
                long filterStartTime = System.currentTimeMillis();
                universe = filterByZone(universe, selectedZone);
                System.out.println("Zone filtering took: " + (System.currentTimeMillis() - filterStartTime) + "ms, filtered to: " + universe.size() + " locations");
            }

            if (universe.isEmpty()) {
                System.out.println("No locations found in selected zone: " + selectedZone);
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            Collections.shuffle(universe);
            List<LocationActivityScore> sample = new ArrayList<>(universe);

            long mlStartTime = System.currentTimeMillis();
            List<Map<String,Object>> mlPayload = new ArrayList<>(sample.size());

            for (LocationActivityScore s : sample) {
                Map<String,Object> m = new HashMap<>();
                m.put("latitude", s.getLocation().getLatitude().doubleValue());
                m.put("longitude", s.getLocation().getLongitude().doubleValue());
                m.put("hour", requestDateTime.getHour());
                m.put("month", requestDateTime.getMonthValue());
                m.put("day", requestDateTime.getDayOfMonth());
                m.put("cultural_activity_prefered", activityName);
                mlPayload.add(m);
            }
            System.out.println("ML payload creation took: " + (System.currentTimeMillis() - mlStartTime) + "ms");

            mlStartTime = System.currentTimeMillis();
            PredictionResponse[] predictions = callMLModelBatch(mlPayload);
            System.out.println("ML API call took: " + (System.currentTimeMillis() - mlStartTime) + "ms");

            long scoreStartTime = System.currentTimeMillis();
            int limit = Math.min(predictions.length, sample.size());
            Map<UUID, BigDecimal> mlScores = new HashMap<>();

            // Pre-calculate if this is a quiet activity (outside the loop)
            boolean isQuiet = isQuietActivity(activityName);

            for (int i = 0; i < limit; i++) {
                LocationActivityScore las = sample.get(i);
                PredictionResponse p = predictions[i];

                // Use primitive doubles for fast arithmetic
                double cultScore = p.getCreativeActivityScore();
                double crowdScore = p.getCrowdScore();
                int originalCrowdNumber = p.getEstimatedCrowdNumber();

                // Determine adjusted crowd score and display values based on activity preference
                double adjustedCrowdScore;
                double displayCrowdScore;
                int displayCrowdNumber;

                if (isQuiet) {
                    // For quiet activities: invert the crowd score for display and muse calculation
                    // Low actual crowd score (good for quiet) becomes high display score
                    adjustedCrowdScore = Math.max(0.0, Math.min(10.0, 10.0 - crowdScore));
                    displayCrowdScore = adjustedCrowdScore;
                    displayCrowdNumber = originalCrowdNumber; // Keep original ML prediction
                } else {
                    // For busy activities: use crowd score as-is
                    // High actual crowd score (good for busy) stays high display score
                    adjustedCrowdScore = Math.max(0.0, Math.min(10.0, crowdScore));
                    displayCrowdScore = crowdScore;
                    displayCrowdNumber = originalCrowdNumber; // Keep original ML prediction
                }

                // Updated weighting: 70% crowd preference + 30% cultural activity
                double museValue = (adjustedCrowdScore * 0.7) + (cultScore * 0.3);

                // Ensure muse score is between 1.0 and 10.0
                museValue = Math.max(1.0, Math.min(10.0, museValue));

                // Convert to BigDecimal
                BigDecimal cult = BigDecimal.valueOf(cultScore);
                BigDecimal crowd = BigDecimal.valueOf(displayCrowdScore); // Use display score
                BigDecimal muse = new BigDecimal(String.format("%.1f", museValue));

                las.setCulturalActivityScore(cult);
                las.setCrowdScore(crowd); // Store the adjusted crowd score
                las.setEstimatedCrowdNumber(displayCrowdNumber); // Store the adjusted crowd number
                las.setMuseScore(muse);
                mlScores.put(las.getLocation().getId(), cult);
            }
            System.out.println("Score calculation took: " + (System.currentTimeMillis() - scoreStartTime) + "ms");

            long saveStartTime = System.currentTimeMillis();
            List<LocationActivityScore> processed = sample.subList(0, limit);
            locationActivityScoreRepository.saveAll(processed);
            System.out.println("Database save took: " + (System.currentTimeMillis() - saveStartTime) + "ms");

            MLPredictionLog log = new MLPredictionLog();
            log.setId(UUID.randomUUID());
            log.setModelVersion("3.0");
            log.setPredictionType("location_recommendation");
            log.setRecordsProcessed(processed.size());
            log.setRecordsUpdated(processed.size());
            log.setPredictionDate(OffsetDateTime.now());
            mlPredictionLogRepository.save(log);

            long mapStartTime = System.currentTimeMillis();
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
            System.out.println("Response mapping took: " + (System.currentTimeMillis() - mapStartTime) + "ms");

            // POST-PROCESSING: Boost scores for busy activities to make them look appropriately high
            if (!isQuiet) {
                for (LocationRecommendationResponse location : mapped) {
                    // Double the crowd score and cap at 10
                    BigDecimal currentCrowdScore = location.getCrowdScore();
                    if (currentCrowdScore != null) {
                        double boostedCrowdScore = Math.min(10.0, currentCrowdScore.doubleValue() * 2.0);
                        location.setCrowdScore(BigDecimal.valueOf(boostedCrowdScore));
                    }

                    // Double the muse score and cap at 10
                    BigDecimal currentMuseScore = location.getMuseScore();
                    if (currentMuseScore != null) {
                        double boostedMuseScore = Math.min(10.0, currentMuseScore.doubleValue() * 2.0);
                        location.setMuseScore(BigDecimal.valueOf(boostedMuseScore));
                    }
                }
            }

            long filterStartTime = System.currentTimeMillis();
            // Use different distance thresholds based on search type
            double minDistance = (selectedZone != null && !selectedZone.trim().isEmpty()) ? 100.0 : 1000.0;
            List<LocationRecommendationResponse> top10 = filterByDistance(mapped, minDistance, 10);
            System.out.println("Distance filtering took: " + (System.currentTimeMillis() - filterStartTime) + "ms (min distance: " + minDistance + "m)");

            // Calculate crowd levels for the final result set
            long crowdStartTime = System.currentTimeMillis();
            assignCrowdLevels(top10);
            System.out.println("Crowd level assignment took: " + (System.currentTimeMillis() - crowdStartTime) + "ms");

            RecommendationResponse response = new RecommendationResponse(top10, activityName, requestDateTime.toString());

            long responseTime = System.currentTimeMillis() - startTime;
            System.out.println("TOTAL REQUEST TIME: " + responseTime + "ms");
            analyticsService.trackRequest(activityName, requestDateTime, cacheHit, responseTime);

            return response;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            analyticsService.trackRequest(activityName, requestDateTime, cacheHit, responseTime);
            throw e;
        }
    }

    /**
     * Determines if an activity prefers quiet crowds (true) or busy crowds (false)
     */
    private boolean isQuietActivity(String activityName) {
        // Activities that prefer quiet crowds
        String activity = activityName.toLowerCase();
        return activity.contains("portrait photography") ||
                activity.contains("filmmaking") ||
                activity.contains("landscape painting") ||
                activity.contains("portrait painting") ||
                activity.contains("reading") ||
                activity.contains("meditation") ||
                activity.contains("writing");
    }

    /**
     * Assigns crowd levels ("Busy", "Moderate", "Quiet") to locations based on
     * the estimated crowd numbers (not crowd scores) within the result set.
     */
    private void assignCrowdLevels(List<LocationRecommendationResponse> locations) {
        if (locations.isEmpty()) {
            return;
        }

        // Extract estimated crowd numbers, filtering out nulls
        List<Integer> crowdNumbers = locations.stream()
                .map(LocationRecommendationResponse::getEstimatedCrowdNumber)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (crowdNumbers.isEmpty()) {
            // If no crowd numbers available, assign default levels
            for (LocationRecommendationResponse location : locations) {
                location.setCrowdLevel("Moderate");
            }
            return;
        }

        // Calculate thresholds based on crowd number distribution within this result set
        int size = crowdNumbers.size();
        int lowerThreshold, upperThreshold;

        if (size == 1) {
            // Only one location, assign moderate
            lowerThreshold = crowdNumbers.get(0);
            upperThreshold = crowdNumbers.get(0);
        } else if (size == 2) {
            // Two locations: one quiet, one busy
            lowerThreshold = crowdNumbers.get(0);
            upperThreshold = crowdNumbers.get(1);
        } else {
            // Three or more: use 33rd and 67th percentiles for better distribution
            int lowerIndex = (int) Math.floor(size * 0.33);
            int upperIndex = (int) Math.floor(size * 0.67);

            lowerThreshold = crowdNumbers.get(Math.max(0, lowerIndex));
            upperThreshold = crowdNumbers.get(Math.min(size - 1, upperIndex));
        }

        // Assign crowd levels based on estimated crowd number thresholds
        for (LocationRecommendationResponse location : locations) {
            Integer crowdNumber = location.getEstimatedCrowdNumber();

            if (crowdNumber == null) {
                location.setCrowdLevel("Moderate");
            } else {
                if (crowdNumber <= lowerThreshold) {
                    location.setCrowdLevel("Quiet");
                } else if (crowdNumber >= upperThreshold) {
                    location.setCrowdLevel("Busy");
                } else {
                    location.setCrowdLevel("Moderate");
                }
            }
        }

        // Ensure we have at least some variation if all numbers are the same
        long distinctLevels = locations.stream()
                .map(LocationRecommendationResponse::getCrowdLevel)
                .distinct()
                .count();

        if (distinctLevels == 1 && locations.size() >= 3) {
            // Force some variation for better user experience
            locations.get(0).setCrowdLevel("Busy");
            locations.get(locations.size() - 1).setCrowdLevel("Quiet");
            if (locations.size() >= 2) {
                locations.get(locations.size() / 2).setCrowdLevel("Moderate");
            }
        }
    }

    private List<LocationActivityScore> filterByZone(List<LocationActivityScore> locations, String selectedZone) {
        List<String> zoneNames = MANHATTAN_ZONES.get(selectedZone.toLowerCase());
        if (zoneNames == null) return Collections.emptyList();

        return locations.stream()
                .filter(location -> zoneNames.contains(location.getTaxiZone().getZoneName()))
                .collect(Collectors.toList());
    }

    private List<LocationRecommendationResponse> filterByDistance(List<LocationRecommendationResponse> locations, double minDistanceMeters, int targetResults) {
        List<LocationRecommendationResponse> filtered = new ArrayList<>();

        for (LocationRecommendationResponse candidate : locations) {
            boolean tooClose = false;
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
                if (filtered.size() >= targetResults) break;
            }
        }
        return filtered;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    protected PredictionResponse[] callMLModelBatch(List<Map<String,Object>> bodies) {
        RestTemplate r = new RestTemplate();
        return r.postForObject("http://34.94.101.102:8080/predict_batch", bodies, PredictionResponse[].class);
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

    public List<String> getAvailableZones() {
        return new ArrayList<>(MANHATTAN_ZONES.keySet());
    }
}
