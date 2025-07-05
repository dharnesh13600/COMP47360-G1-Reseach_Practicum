package com.creativespacefinder.manhattan.service;

import com.creativespacefinder.manhattan.dto.*;
import com.creativespacefinder.manhattan.entity.Activity;
import com.creativespacefinder.manhattan.entity.LocationActivityScore;
import com.creativespacefinder.manhattan.repository.ActivityRepository;
import com.creativespacefinder.manhattan.repository.LocationActivityScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationRecommendationService {

    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private WeatherForecastService weatherForecastService;

    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        LocalDateTime requestDateTime = request.getDateTime();
        LocalDate eventDate = requestDateTime.toLocalDate();
        LocalTime eventTime = requestDateTime.toLocalTime();

        List<LocationActivityScore> topLocations = locationActivityScoreRepository
                .findTop5ByActivityNameAndEventDateAndEventTimeOrderByMuseScoreDesc(
                        request.getActivity(), eventDate, eventTime
                );

        // Fallback logic to ensure 5 results
        if (topLocations.size() < 5) {
            int needed = 5 - topLocations.size();

            List<LocationActivityScore> fallbackLocations = locationActivityScoreRepository
                    .findTopByActivityNameIgnoreDateTime(request.getActivity(), PageRequest.of(0, needed));

            Set<UUID> existingIds = topLocations.stream()
                    .map(score -> score.getLocation().getId())
                    .collect(Collectors.toSet());

            fallbackLocations.stream()
                    .filter(score -> !existingIds.contains(score.getLocation().getId()))
                    .forEach(topLocations::add);
        }

        List<LocationRecommendationResponse> locationResponses = topLocations.stream()
                .map(this::convertToLocationResponse)
                .collect(Collectors.toList());

        WeatherData weatherData = weatherForecastService.getWeatherForDateTime(requestDateTime);

        return new RecommendationResponse(
                locationResponses,
                weatherData,
                request.getActivity(),
                requestDateTime.toString()
        );
    }

    private LocationRecommendationResponse convertToLocationResponse(LocationActivityScore score) {
        BigDecimal combinedScore = calculateCombinedScore(score);

        return new LocationRecommendationResponse(
                score.getLocation().getId(),
                score.getLocation().getLocationName(),
                score.getLocation().getLatitude(),
                score.getLocation().getLongitude(),
                combinedScore,
                score.getHistoricalActivityScore(),
                score.getMuseScore(),
                score.getCrowdScore(),
                score.getEstimatedCrowdNumber()
        );
    }

    private BigDecimal calculateCombinedScore(LocationActivityScore score) {
        if (score.getMuseScore() != null) {
            return score.getMuseScore();
        }

        if (score.getHistoricalActivityScore() != null) {
            BigDecimal activityWeight = new BigDecimal("0.7");
            BigDecimal crowdWeight = new BigDecimal("0.3");

            BigDecimal activityComponent = score.getHistoricalActivityScore().multiply(activityWeight);
            BigDecimal crowdComponent = BigDecimal.TEN
                    .subtract(score.getHistoricalTaxiZoneCrowdScore() != null ?
                            score.getHistoricalTaxiZoneCrowdScore() : BigDecimal.valueOf(5))
                    .multiply(crowdWeight);

            return activityComponent.add(crowdComponent).setScale(2, RoundingMode.HALF_UP);
        }

        return new BigDecimal("5.0");
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

    public MLPredictionStats getMLPredictionStats() {
        Long totalRecords = locationActivityScoreRepository.count();
        Long recordsWithMLPredictions = locationActivityScoreRepository.countRecordsWithMLPredictions();
        Long recordsWithHistoricalData = locationActivityScoreRepository.countRecordsWithHistoricalData();

        return new MLPredictionStats(
                totalRecords,
                recordsWithMLPredictions,
                recordsWithHistoricalData,
                calculateCoveragePercentage(recordsWithMLPredictions, totalRecords),
                calculateCoveragePercentage(recordsWithHistoricalData, totalRecords)
        );
    }

    private BigDecimal calculateCoveragePercentage(Long covered, Long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(covered)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public static class MLPredictionStats {
        private final Long totalRecords;
        private final Long recordsWithMLPredictions;
        private final Long recordsWithHistoricalData;
        private final BigDecimal mlCoveragePercentage;
        private final BigDecimal historicalCoveragePercentage;

        public MLPredictionStats(Long totalRecords, Long recordsWithMLPredictions,
                                 Long recordsWithHistoricalData, BigDecimal mlCoveragePercentage,
                                 BigDecimal historicalCoveragePercentage) {
            this.totalRecords = totalRecords;
            this.recordsWithMLPredictions = recordsWithMLPredictions;
            this.recordsWithHistoricalData = recordsWithHistoricalData;
            this.mlCoveragePercentage = mlCoveragePercentage;
            this.historicalCoveragePercentage = historicalCoveragePercentage;
        }

        public Long getTotalRecords() { return totalRecords; }
        public Long getRecordsWithMLPredictions() { return recordsWithMLPredictions; }
        public Long getRecordsWithHistoricalData() { return recordsWithHistoricalData; }
        public BigDecimal getMlCoveragePercentage() { return mlCoveragePercentage; }
        public BigDecimal getHistoricalCoveragePercentage() { return historicalCoveragePercentage; }
    }
}
