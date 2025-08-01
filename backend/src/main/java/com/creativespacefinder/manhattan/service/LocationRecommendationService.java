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

// References:
// https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html
// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/annotation/Cacheable.html
// https://spring.io/guides/gs/caching
// https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html
// https://www.baeldung.com/java-find-distance-between-points
// https://spring.io/guides/gs/managing-transactions
// https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
// https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.entity-graph


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

    // URL for ML prediction model/microservice
    @Value("${ML_PREDICT_URL}")
    private String mlPredictUrl;

    // Predefined Manhattan zones in relation to their sub-zones for zone-specific filtering
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

    /**
     * This is the main service method to get locational recommendations for a specific activity, datetime, and zone
     * The results are then cached for identical inputs at a later date
     */
    @Cacheable(cacheNames = "recommendations", key = "#request.activity + '_' + #request.dateTime.toString() + '_' + (#request.selectedZone != null ? #request.selectedZone : 'all')")
    @Transactional
    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        long startTime = System.currentTimeMillis();

        // Here we extract the user inputs
        String zoneInfo = (request.getSelectedZone() != null) ? request.getSelectedZone() : "ALL_MANHATTAN";
        System.out.println("Processing recommendation request for activity: " + request.getActivity() +
                ", dateTime: " + request.getDateTime() +
                ", zone: " + zoneInfo);

        LocalDateTime requestDateTime = request.getDateTime();
        String activityName = request.getActivity();
        String selectedZone = request.getSelectedZone();

        try {
            // Lookup the related activity object from the database
            long dbStartTime = System.currentTimeMillis();
            Activity activity = activityRepository.findByName(activityName)
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + activityName));
            System.out.println("Activity lookup took: " + (System.currentTimeMillis() - dbStartTime) + "ms");

            dbStartTime = System.currentTimeMillis();

            // Get a list of candidate location identifications relevant to the activity
            List<String> locationIds = locationActivityScoreRepository
                    .findDistinctLocationIdsByActivityName(activityName, 500);
            System.out.println("Location ID query took: " + (System.currentTimeMillis() - dbStartTime) + "ms, found: " + locationIds.size() + " location IDs");

            if (locationIds.isEmpty()) {
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            dbStartTime = System.currentTimeMillis();

            // Covert these string ids to UUIDs
            // Fetch the full location activity score with eager loading
            // Eager loading is the opp of lazy loading, meaning i do not initialise the object until i need it
            // This stops the slowing down of the ML microservice call
            List<UUID> uuids = locationIds.stream().map(UUID::fromString).collect(Collectors.toList());
            List<LocationActivityScore> universe = locationActivityScoreRepository.findByIdsWithEagerLoading(uuids);
            System.out.println("Eager loading query took: " + (System.currentTimeMillis() - dbStartTime) + "ms, loaded: " + universe.size() + " locations");

            if (universe.isEmpty()) {
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            // If there is a zone selected then filter locations to that zone!
            if (selectedZone != null && !selectedZone.trim().isEmpty()) {
                long filterStartTime = System.currentTimeMillis();
                universe = filterByZone(universe, selectedZone);
                System.out.println("Zone filtering took: " + (System.currentTimeMillis() - filterStartTime) + "ms, filtered to: " + universe.size() + " locations");
            }

            // If no locations in the selected zone, return empty
            if (universe.isEmpty()) {
                System.out.println("No locations found in selected zone: " + selectedZone);
                return new RecommendationResponse(Collections.emptyList(), activityName, requestDateTime.toString());
            }

            Collections.shuffle(universe);
            List<LocationActivityScore> sample = new ArrayList<>(universe);

            // Now we will prepare the data/input for that ML model
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

            // Call is made to the ML model via api and predictions are got
            mlStartTime = System.currentTimeMillis();
            PredictionResponse[] predictions = callMLModelBatch(mlPayload);
            System.out.println("ML API call took: " + (System.currentTimeMillis() - mlStartTime) + "ms");

            // We will apply ML predictions to the sample locations called
            long scoreStartTime = System.currentTimeMillis();
            int limit = Math.min(predictions.length, sample.size());
            Map<UUID, BigDecimal> mlScores = new HashMap<>();

            // Get the activity name in lowercase for condition checking
            String activityLowerCase = activityName.toLowerCase();

            for (int i = 0; i < limit; i++) {
                LocationActivityScore las = sample.get(i);
                PredictionResponse p = predictions[i];

                double cultScore = p.getCreativeActivityScore();
                double crowdScore = p.getCrowdScore();
                int crowdNumber = p.getEstimatedCrowdNumber();

                // Crowd score inversion for activities other than busking and art sale (explaned in the report)
                double adjustedCrowdScore = crowdScore;
                if (!activityLowerCase.equals("busking") && !activityLowerCase.equals("art sale")) {
                    adjustedCrowdScore = 10.0 - crowdScore;
                }
                
                // Muse Score calculation with weightings dependent on activity (justified and referenced in the final report)
                double museValue;
                if (activityLowerCase.equals("busking")) {
                    museValue = (adjustedCrowdScore * 0.65) + (cultScore * 0.35);
                } else if (activityLowerCase.equals("art sale")) {
                    museValue = (adjustedCrowdScore * 0.60) + (cultScore * 0.40);
                } else if (activityLowerCase.equals("filmmaking")) {
                    museValue = (adjustedCrowdScore * 0.55) + (cultScore * 0.45);
                } else if (activityLowerCase.equals("street photography")) {
                    museValue = (adjustedCrowdScore * 0.40) + (cultScore * 0.60);
                } else if (activityLowerCase.equals("portrait photography")) {
                    museValue = (adjustedCrowdScore * 0.30) + (cultScore * 0.70);
                } else if (activityLowerCase.equals("portrait painting")) {
                    museValue = (adjustedCrowdScore * 0.35) + (cultScore * 0.65);
                } else if (activityLowerCase.equals("landscape painting")) {
                    museValue = (adjustedCrowdScore * 0.35) + (cultScore * 0.65);
                } else {
                    // Default case for any other activities
                    museValue = (adjustedCrowdScore * 0.6) + (cultScore * 0.4);
                }
                
                // Don't need to but just incase: make sure the muse score is between 1.0 and 10.0
                museValue = Math.max(1.0, Math.min(10.0, museValue));

                // Convert it to a BigDecimal
                BigDecimal cult = BigDecimal.valueOf(cultScore);
                BigDecimal crowd = BigDecimal.valueOf(crowdScore);
                BigDecimal muse = new BigDecimal(String.format("%.1f", museValue));

                las.setCulturalActivityScore(cult);
                las.setCrowdScore(crowd);
                las.setEstimatedCrowdNumber(crowdNumber);
                las.setMuseScore(muse);
                mlScores.put(las.getLocation().getId(), cult);
            }
            System.out.println("Score calculation took: " + (System.currentTimeMillis() - scoreStartTime) + "ms");

            // Save the processed scores into the database, this is done in a batch for performance
            // As this is a batch save and one of the most slow operations            
            long saveStartTime = System.currentTimeMillis();
            List<LocationActivityScore> processed = sample.subList(0, limit);
            locationActivityScoreRepository.saveAll(processed);
            System.out.println("Database save took: " + (System.currentTimeMillis() - saveStartTime) + "ms");

            // Log the ML prediction for an activity for debugging and model comparison
            MLPredictionLog log = new MLPredictionLog();
            log.setId(UUID.randomUUID());
            log.setModelVersion("3.0");
            log.setPredictionType("location_recommendation");
            log.setRecordsProcessed(processed.size());
            log.setRecordsUpdated(processed.size());
            log.setPredictionDate(OffsetDateTime.now());
            mlPredictionLogRepository.save(log);

            // For each of the data given we will put them all into a response object for Frontend
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

            // If there is no locations then return empty
            long filterStartTime = System.currentTimeMillis();
            double minDistance = (selectedZone != null && !selectedZone.trim().isEmpty()) ? 25.0 : 50.0;
            List<LocationRecommendationResponse> top10 = filterByDistance(mapped, minDistance, 10);
            System.out.println("Distance filtering took: " + (System.currentTimeMillis() - filterStartTime) + "ms (min distance: " + minDistance + "m)");

            // We assign the crowd levels too based on Quiet, Medium or Busy
            long crowdStartTime = System.currentTimeMillis();
            assignCrowdLevels(top10);
            System.out.println("Crowd level assignment took: " + (System.currentTimeMillis() - crowdStartTime) + "ms");

            RecommendationResponse response = new RecommendationResponse(top10, activityName, requestDateTime.toString());

            return response;

        } catch (Exception e) {
            // Throw an error handler to deal with mess up!
            throw e;
        }
    }

    /**
     * This is for the crowd level assignment via the estimated crowd number
     * Instead, we give human readable english for a number
     */
    private void assignCrowdLevels(List<LocationRecommendationResponse> locations) {
        if (locations.isEmpty()) {
            return;
        }

        List<Integer> crowdNumbers = locations.stream()
                .map(LocationRecommendationResponse::getEstimatedCrowdNumber)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (crowdNumbers.isEmpty()) {
            for (LocationRecommendationResponse location : locations) {
                location.setCrowdLevel("Moderate");
            }
            return;
        }


        // Here I attempt to do a percentile threshold on 33rd and 67th percentile, to get a nice spread in comparison to one another
        // You cannot have all for Busking and Art Sale saying "Busy" etc.
        int size = crowdNumbers.size();
        int lowerThreshold, upperThreshold;

        if (size == 1) {
            lowerThreshold = crowdNumbers.get(0);
            upperThreshold = crowdNumbers.get(0);
        } else if (size == 2) {
            lowerThreshold = crowdNumbers.get(0);
            upperThreshold = crowdNumbers.get(1);
        } else {
            int lowerIndex = (int) Math.floor(size * 0.33);
            int upperIndex = (int) Math.floor(size * 0.67);
            lowerThreshold = crowdNumbers.get(Math.max(0, lowerIndex));
            upperThreshold = crowdNumbers.get(Math.min(size - 1, upperIndex));
        }

        // English words are assigned based on the percentiles
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

        // Ensure that we have variation based on Muse Score
        long distinctLevels = locations.stream()
                .map(LocationRecommendationResponse::getCrowdLevel)
                .distinct()
                .count();

        if (distinctLevels == 1 && locations.size() >= 3) {
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

    /**
     * Filters all the locations by a minimum distance in meters, preventing the clustering of location suggestions to one peak cultural activity
     * This ensures a spread of locations to the user as always in their preferenced locations
     * Spatial diversity is important for cultural activities, so we avoid recommending locations that are too close to each other
     * 
     * Paper suggesting this highlights the importance of spatial diversity recommendations too close together... return in feeling redundant or overly similar in context
     * Also, "spatially distinct suggestions let users experience different environments and regional flavours"
     * https://go.exlibris.link/nkl1NttS
     * https://jcst.ict.ac.cn/fileup/1000-9000/PDF/2019-4-8-8747.pdf#:~:text=travel%20time%20is%20less%20than,In%20addition%2C%20many
     */
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

    /**
     * This calculates the distance between two locational points via "Haversine" formula
     * This is ensures that we do not recommend locations that are too close to one another
     */
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

    /**
     * A batch call to the ML model is used to get predictions for multiple locations
     */
    protected PredictionResponse[] callMLModelBatch(List<Map<String,Object>> bodies) {
        RestTemplate r = new RestTemplate();
        return r.postForObject(
                mlPredictUrl,
                bodies,
                PredictionResponse[].class
        );
    }

    // These methods are for the frontend to get all of the activities, xones, dates and times 
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
