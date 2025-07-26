# Repository Package Documentation

## Overview

The `/repository/` package contains Spring Data JPA repository interfaces that define database access patterns for the Creative Space Finder application. These repositories provide both standard CRUD operations and custom query methods optimized for the application's specific data access requirements.

## Package Structure

```
com.creativespacefinder.manhattan.repository/
├── ActivityRepository.java              # Activity lookup and validation queries
├── LocationActivityScoreRepository.java # Complex scoring queries with performance optimizations
├── MLPredictionLogRepository.java       # Simple audit log persistence
└── RequestAnalyticsRepository.java      # Analytics aggregation and statistical queries
```

---

## ActivityRepository.java

### Purpose
Provides data access methods for the `Activity` entity, focusing on activity lookup by name and existence validation. This repository supports the master data management for creative activities like "Portrait photography", "Street photography", etc.

### Interface Definition
```java
@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    
    Optional<Activity> findByNameIgnoreCase(String name);
    Optional<Activity> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
}
```

### Method Analysis

#### Case-Insensitive Name Lookup
```java
Optional<Activity> findByNameIgnoreCase(String name);
```

**Purpose:** Flexible activity matching that handles case variations
**Use Cases:**
- User input normalization ("portrait photography" → "Portrait photography")
- API robustness against case sensitivity issues
- Search functionality with case tolerance

**Generated SQL:**
```sql
SELECT * FROM activities 
WHERE LOWER(name) = LOWER(?)
```

**Integration Example:**
```java
// In LocationRecommendationService
Activity activity = activityRepository.findByNameIgnoreCase(activityName)
    .orElseThrow(() -> new RuntimeException("Activity not found: " + activityName));
```

#### Exact Name Lookup
```java
Optional<Activity> findByName(String name);
```

**Purpose:** Precise activity matching with exact case sensitivity
**Use Cases:**
- Internal system operations with known activity names
- Data integrity validation
- Cache key generation with exact matches

**Performance Benefit:** Leverages database index on `name` column with unique constraint

#### Existence Validation
```java
boolean existsByNameIgnoreCase(String name);
```

**Purpose:** Efficient existence checking without loading full entity
**Use Cases:**
- Input validation before processing
- Duplicate prevention during data import
- Quick availability checks

**Performance Advantage:**
- Returns boolean instead of full entity
- More efficient than loading entity and checking for null
- Database-level existence check

### Usage Patterns

#### Service Layer Integration
```java
// LocationRecommendationService
public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
    String activityName = request.getActivity();
    
    // Validate activity exists before expensive operations
    Activity activity = activityRepository.findByName(activityName)
        .orElseThrow(() -> new RuntimeException("Activity not found: " + activityName));
    
    // Continue with recommendation logic...
}
```

#### Controller Validation
```java
// RecommendationController - potential validation endpoint
@GetMapping("/activities/validate")
public ResponseEntity<Map<String, Boolean>> validateActivity(@RequestParam String name) {
    boolean exists = activityRepository.existsByNameIgnoreCase(name);
    return ResponseEntity.ok(Map.of("exists", exists));
}
```

### Database Optimizations

#### Index Strategy
```sql
-- Unique index for exact matching
CREATE UNIQUE INDEX idx_activities_name ON activities(name);

-- Functional index for case-insensitive queries (PostgreSQL)
CREATE INDEX idx_activities_name_lower ON activities(LOWER(name));
```

#### Query Performance
- **Unique Constraint:** Ensures O(1) lookup performance
- **Index Coverage:** All query methods utilize indexes
- **Minimal Data Transfer:** Boolean methods return only existence status

---

## LocationActivityScoreRepository.java

### Purpose
The most complex repository in the application, handling sophisticated queries for location scoring data. This repository implements performance optimizations to handle the core recommendation engine queries efficiently while avoiding common JPA performance pitfalls.

### Interface Definition
```java
@Repository
public interface LocationActivityScoreRepository extends JpaRepository<LocationActivityScore, UUID> {
    // Performance-optimized two-step query approach
    // Analytics and metadata queries
    // Date/time availability queries
}
```

### Performance-Optimized Query Strategy

#### Two-Step Query Approach
```java
@Query(value = """
    SELECT las.id FROM location_activity_scores las
    WHERE las.id IN (
        SELECT DISTINCT ON (las2.location_id) las2.id
        FROM location_activity_scores las2
        JOIN activities a ON las2.activity_id = a.id
        WHERE LOWER(a.name) = LOWER(:activityName)
        ORDER BY las2.location_id, las2.historical_activity_score DESC NULLS LAST
    )
    ORDER BY las.historical_activity_score DESC NULLS LAST
    LIMIT :limit
    """, nativeQuery = true)
List<String> findDistinctLocationIdsByActivityName(@Param("activityName") String activityName, @Param("limit") int limit);
```

**Performance Benefits:**
- **DISTINCT ON Optimization:** PostgreSQL-specific optimization for unique location selection
- **Native SQL:** Bypasses JPA overhead for complex queries
- **ID-Only Selection:** Minimal data transfer in first step
- **Explicit Ordering:** Ensures highest-scored locations selected per location

#### Eager Loading Follow-up
```java
@Query("""
    SELECT las FROM LocationActivityScore las
    JOIN FETCH las.location
    JOIN FETCH las.taxiZone
    WHERE las.id IN :ids
    """)
List<LocationActivityScore> findByIdsWithEagerLoading(@Param("ids") List<UUID> ids);
```

**Anti-N+1 Strategy:**
- **Explicit Eager Loading:** Prevents multiple queries for related entities
- **Batch Loading:** Single query for all required relationships
- **Controlled Fetching:** Only loads necessary associations

### Service Layer Integration
```java
// In LocationRecommendationService
long dbStartTime = System.currentTimeMillis();
List<String> locationIds = locationActivityScoreRepository
    .findDistinctLocationIdsByActivityName(activityName, 100);
System.out.println("Location ID query took: " + (System.currentTimeMillis() - dbStartTime) + "ms");

dbStartTime = System.currentTimeMillis();
List<UUID> uuids = locationIds.stream().map(UUID::fromString).collect(Collectors.toList());
List<LocationActivityScore> universe = locationActivityScoreRepository.findByIdsWithEagerLoading(uuids);
System.out.println("Eager loading query took: " + (System.currentTimeMillis() - dbStartTime) + "ms");
```

### Legacy Query (Backup Implementation)
```java
@Query(value = """
SELECT * FROM location_activity_scores las
WHERE las.id IN (
    SELECT DISTINCT ON (las2.location_id) las2.id
    FROM location_activity_scores las2
    JOIN activities a ON las2.activity_id = a.id
    WHERE LOWER(a.name) = LOWER(:activityName)
    ORDER BY las2.location_id, las2.historical_activity_score DESC NULLS LAST
)
ORDER BY las.historical_activity_score DESC NULLS LAST
""", nativeQuery = true)
List<LocationActivityScore> findDistinctLocationsByActivityName(@Param("activityName") String activityName, Pageable pageable);
```

**Kept for Comparison:**
- Original single-query approach
- Less optimal due to full entity loading
- Useful for performance testing and fallback

### Metadata and Analytics Queries

#### Data Coverage Analysis
```java
@Query("""
    SELECT COUNT(l)
      FROM LocationActivityScore l
     WHERE l.museScore IS NOT NULL
""")
Long countRecordsWithMLPredictions();

@Query("""
    SELECT COUNT(l)
      FROM LocationActivityScore l
     WHERE l.historicalActivityScore IS NOT NULL
""")
Long countRecordsWithHistoricalData();
```

**Health Monitoring:**
- **ML Coverage:** Percentage of records with machine learning predictions
- **Data Quality:** Historical data availability assessment
- **System Metrics:** Used in `/api/health` endpoint

#### Temporal Data Queries
```java
@Query("""
    SELECT DISTINCT l.eventDate
      FROM LocationActivityScore l
     WHERE l.activity.name = :activityName
""")
List<LocalDate> findAvailableDatesByActivity(@Param("activityName") String activityName);

@Query("""
    SELECT DISTINCT l.eventTime
      FROM LocationActivityScore l
     WHERE l.activity.name = :activityName
       AND l.eventDate = :eventDate
""")
List<LocalTime> findAvailableTimesByActivityAndDate(
    @Param("activityName") String activityName,
    @Param("eventDate") LocalDate eventDate
);
```

**Frontend Support:**
- **Date Availability:** Populate calendar widgets with valid dates
- **Time Availability:** Show available hourly slots for selected dates
- **Dynamic Filtering:** Prevent invalid datetime selections

#### Direct Entity Lookup
```java
List<LocationActivityScore> findByActivityIdAndEventDateAndEventTime(
    UUID activityId,
    LocalDate eventDate,
    LocalTime eventTime
);
```

**Specific Use Cases:**
- **Exact Match Queries:** When activity ID is already known
- **Cache Key Validation:** Verify cache entries against database
- **Testing Support:** Direct entity access for unit tests

### Query Performance Characteristics

#### Execution Time Analysis
```
Location ID query: ~50-100ms (returns UUID strings)
Eager loading query: ~100-200ms (loads full entities)
Total recommendation query time: ~150-300ms
```

**vs. Previous Implementation:**
```
Single query with DISTINCT + FETCH: ~2000-5000ms
Performance improvement: 10-20x faster
```

#### Memory Usage Optimization
- **Lazy Loading Prevention:** Explicit eager loading for required associations
- **Minimal Initial Load:** ID-only first query reduces memory footprint
- **Batch Processing:** Single query for all relationships

---

## MLPredictionLogRepository.java

### Purpose
Simple repository for ML prediction audit logs. Provides standard CRUD operations for tracking machine learning model execution metadata, including model versions, processing statistics, and prediction timestamps.

### Interface Definition
```java
public interface MLPredictionLogRepository extends JpaRepository<MLPredictionLog, UUID> {
    // Inherits all standard JPA methods:
    // save(), findById(), findAll(), delete(), etc.
}
```

### Standard JPA Operations

#### Entity Persistence
```java
// In LocationRecommendationService
MLPredictionLog log = new MLPredictionLog();
log.setId(UUID.randomUUID());
log.setModelVersion("3.0");
log.setPredictionType("location_recommendation");
log.setRecordsProcessed(processed.size());
log.setRecordsUpdated(processed.size());
log.setPredictionDate(OffsetDateTime.now());
mlPredictionLogRepository.save(log);
```

#### Batch Processing Logs
```java
// In DailyPrecomputationService
MLPredictionLog batchLog = new MLPredictionLog();
batchLog.setId(UUID.randomUUID());
batchLog.setModelVersion("3.0");
batchLog.setPredictionType("cache_warming");
batchLog.setRecordsProcessed(totalCombinations);
batchLog.setRecordsUpdated(successfulPredictions);
batchLog.setPredictionDate(OffsetDateTime.now());
batchLog.setNotes("Daily 3 AM cache warming process");
mlPredictionLogRepository.save(batchLog);
```

### Usage Patterns

#### Health Check Integration
```java
// In SystemHealthService
Map<String, Object> dataStats = new LinkedHashMap<>();
dataStats.put("mlPredictionLogs", mlPredictionLogRepository.count());
```

#### Audit Trail Queries
```java
// Future analytics queries (could be added as custom methods)
@Query("SELECT m FROM MLPredictionLog m WHERE m.predictionDate >= :since ORDER BY m.predictionDate DESC")
List<MLPredictionLog> findRecentPredictions(@Param("since") OffsetDateTime since);

@Query("SELECT m.modelVersion, COUNT(m), AVG(m.modelAccuracy) FROM MLPredictionLog m GROUP BY m.modelVersion")
List<Object[]> getModelVersionStats();
```

### Design Rationale

#### Minimal Interface
- **Standard Operations:** JPA provides all necessary CRUD functionality
- **Simple Auditing:** Straightforward log creation and retrieval
- **Future Extensibility:** Easy to add custom query methods as needed

#### Manual ID Management
```java
log.setId(UUID.randomUUID());
```
- **Explicit Control:** Consistent with application UUID strategy
- **Predictable IDs:** No auto-generation surprises
- **Cross-Environment Consistency:** Same ID generation across environments

---

## RequestAnalyticsRepository.java

### Purpose
Sophisticated analytics repository that provides aggregation queries, statistical calculations, and performance metrics for API usage patterns. This repository enables data-driven optimization of cache warming strategies and system performance monitoring.

### Interface Definition
```java
@Repository
public interface RequestAnalyticsRepository extends JpaRepository<RequestAnalytics, UUID> {
    // Core analytics lookup
    // Statistical aggregation queries
    // Performance analysis methods
    // Time-based analytics
}
```

### Core Analytics Queries

#### Primary Record Lookup
```java
Optional<RequestAnalytics> findByActivityNameAndRequestedHourAndRequestedDayOfWeek(
    String activityName, Integer requestedHour, Integer requestedDayOfWeek);
```

**Data Aggregation Strategy:**
- **Composite Key Lookup:** Activity + Hour + Day combination
- **Upsert Pattern:** Find existing or create new analytics record
- **Incremental Counting:** Avoid duplicate records for same pattern

**Service Integration:**
```java
// In AnalyticsService
Optional<RequestAnalytics> existing = analyticsRepository
    .findByActivityNameAndRequestedHourAndRequestedDayOfWeek(activityName, hour, dayOfWeek);

if (existing.isPresent()) {
    RequestAnalytics analytics = existing.get();
    analytics.incrementRequestCount();
    analytics.setCacheHit(cacheHit);
    analytics.setResponseTimeMs(responseTimeMs);
    analyticsRepository.save(analytics);
} else {
    RequestAnalytics analytics = new RequestAnalytics(
        activityName, hour, dayOfWeek, cacheHit, responseTimeMs, userAgent);
    analyticsRepository.save(analytics);
}
```

### Statistical Aggregation Queries

#### Popular Combinations Analysis
```java
@Query("""
    SELECT r FROM RequestAnalytics r 
    WHERE r.requestCount >= :minRequests 
    ORDER BY r.requestCount DESC, r.lastRequested DESC
    """)
List<RequestAnalytics> findPopularCombinations(@Param("minRequests") Integer minRequests);
```

**Cache Optimization Usage:**
- **Threshold Filtering:** Only include patterns with significant usage
- **Prioritization:** Order by request count for cache warming priority
- **Recency Factor:** Secondary sort by last requested for tie-breaking

#### Activity-Specific Patterns
```java
@Query("""
    SELECT r FROM RequestAnalytics r 
    WHERE r.activityName = :activityName 
    ORDER BY r.requestCount DESC, r.lastRequested DESC
    """)
List<RequestAnalytics> findPopularCombinationsForActivity(@Param("activityName") String activityName);
```

**Targeted Analysis:**
- **Activity Focus:** Analyze patterns for specific creative activities
- **User Behavior:** Understand time preferences for each activity
- **Resource Planning:** Activity-specific cache warming strategies

### Performance Analytics

#### Cache Hit Rate Analysis
```java
@Query("""
    SELECT 
        r.activityName,
        r.requestedHour,
        AVG(CASE WHEN r.cacheHit = true THEN 1.0 ELSE 0.0 END) as cacheHitRate,
        SUM(r.requestCount) as totalRequests,
        AVG(r.responseTimeMs) as avgResponseTime
    FROM RequestAnalytics r 
    GROUP BY r.activityName, r.requestedHour
    ORDER BY totalRequests DESC
    """)
List<Object[]> getCacheHitRateStats();
```

**Performance Insights:**
- **Hit Rate Calculation:** Boolean to numeric conversion for averaging
- **Request Volume:** Total request counts per activity/hour combination
- **Response Time Correlation:** Average response times for performance analysis
- **Optimization Targets:** Identify low-hit-rate combinations for improvement

**Result Processing:**
```java
// In AnalyticsController
List<Object[]> stats = analyticsService.getCachePerformanceStats();
List<Map<String, Object>> result = stats.stream().map(row -> {
    Map<String, Object> map = new HashMap<>();
    map.put("activity", row[0]);
    map.put("hour", row[1]);
    map.put("cacheHitRate", String.format("%.1f%%", ((Double) row[2]) * 100));
    map.put("totalRequests", row[3]);
    map.put("avgResponseTime", row[4] != null ? Math.round((Double) row[4]) : 0);
    return map;
}).toList();
```

### Temporal Analytics

#### Recent Activity Monitoring
```java
@Query("""
    SELECT r FROM RequestAnalytics r 
    WHERE r.lastRequested >= :since 
    ORDER BY r.lastRequested DESC
    """)
List<RequestAnalytics> findRecentActivity(@Param("since") LocalDateTime since);
```

**Operational Monitoring:**
- **Time Window Analysis:** Last 7 days activity tracking
- **Real-time Insights:** Recent usage patterns
- **Trend Detection:** Identify emerging or declining patterns

#### Activity Popularity Trends
```java
@Query("""
    SELECT 
        r.activityName,
        SUM(r.requestCount) as totalRequests,
        AVG(r.responseTimeMs) as avgResponseTime,
        MAX(r.lastRequested) as lastRequested
    FROM RequestAnalytics r 
    GROUP BY r.activityName
    ORDER BY totalRequests DESC
    """)
List<Object[]> getActivityPopularityStats();
```

**Business Intelligence:**
- **Activity Ranking:** Most to least popular activities
- **Performance Metrics:** Response time by activity
- **Engagement Tracking:** Recent activity timestamps
- **Content Strategy:** Focus development on popular activities

#### Hourly Usage Patterns
```java
@Query("""
    SELECT 
        r.requestedHour,
        SUM(r.requestCount) as totalRequests,
        COUNT(DISTINCT r.activityName) as uniqueActivities
    FROM RequestAnalytics r 
    GROUP BY r.requestedHour
    ORDER BY r.requestedHour
    """)
List<Object[]> getHourlyUsageStats();
```

**Operational Planning:**
- **Peak Hour Identification:** When to expect highest load
- **Activity Diversity:** How many different activities per hour
- **Infrastructure Scaling:** Plan resources around usage patterns
- **Cache Warming Schedule:** Optimize pre-computation timing

### Controller Integration Examples

#### Dashboard Analytics
```java
// In AnalyticsController
@GetMapping("/dashboard")
public ResponseEntity<Map<String, Object>> getDashboard() {
    Map<String, Object> dashboard = new HashMap<>();
    
    // Popular combinations
    List<RequestAnalytics> popular = analyticsService.getPopularCombinations();
    dashboard.put("popularCombinations", popular.size());
    
    // Cache performance
    List<Object[]> cacheStats = analyticsService.getCachePerformanceStats();
    double avgCacheHitRate = cacheStats.stream()
            .mapToDouble(row -> (Double) row[2])
            .average()
            .orElse(0.0);
    dashboard.put("avgCacheHitRate", String.format("%.1f%%", avgCacheHitRate * 100));
    
    return ResponseEntity.ok(dashboard);
}
```

---

## Repository Integration Patterns

### Service Layer Coordination

#### Multi-Repository Transactions
```java
@Service
@Transactional
public class LocationRecommendationService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;
    
    @Autowired
    private MLPredictionLogRepository mlPredictionLogRepository;
    
    public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
        // 1. Validate activity exists
        Activity activity = activityRepository.findByName(request.getActivity())
            .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        // 2. Get location scores (performance-optimized)
        List<String> locationIds = locationActivityScoreRepository
            .findDistinctLocationIdsByActivityName(request.getActivity(), 100);
        List<UUID> uuids = locationIds.stream().map(UUID::fromString).collect(Collectors.toList());
        List<LocationActivityScore> locations = locationActivityScoreRepository
            .findByIdsWithEagerLoading(uuids);
        
        // 3. Process ML predictions and save updates
        // ... ML processing ...
        locationActivityScoreRepository.saveAll(processedLocations);
        
        // 4. Log ML operation
        MLPredictionLog log = createPredictionLog(processedLocations.size());
        mlPredictionLogRepository.save(log);
        
        return buildResponse(processedLocations);
    }
}
```

### Analytics Integration Flow
```java
@Service  
public class AnalyticsService {
    
    @Autowired
    private RequestAnalyticsRepository analyticsRepository;
    
    public void trackRequest(String activityName, LocalDateTime requestedDateTime,
                             boolean cacheHit, long responseTimeMs) {
        Integer hour = requestedDateTime.getHour();
        Integer dayOfWeek = requestedDateTime.getDayOfWeek().getValue();
        
        // Upsert pattern with repository
        Optional<RequestAnalytics> existing = analyticsRepository
                .findByActivityNameAndRequestedHourAndRequestedDayOfWeek(activityName, hour, dayOfWeek);
                
        if (existing.isPresent()) {
            RequestAnalytics analytics = existing.get();
            analytics.incrementRequestCount();
            analytics.setCacheHit(cacheHit);
            analytics.setResponseTimeMs(responseTimeMs);
            analyticsRepository.save(analytics);
        } else {
            RequestAnalytics analytics = new RequestAnalytics(
                    activityName, hour, dayOfWeek, cacheHit, responseTimeMs, getUserAgent());
            analyticsRepository.save(analytics);
        }
    }
}
```

### Performance Monitoring Integration

#### Health Check Repository Usage
```java
// In SystemHealthService
private Map<String, Object> getDatabaseHealth() {
    Map<String, Object> dataStats = new LinkedHashMap<>();
    dataStats.put("totalActivities", activityRepository.count());
    dataStats.put("totalLocationScores", locationActivityScoreRepository.count());
    dataStats.put("recordsWithMLPredictions", locationActivityScoreRepository.countRecordsWithMLPredictions());
    dataStats.put("recordsWithHistoricalData", locationActivityScoreRepository.countRecordsWithHistoricalData());
    dataStats.put("mlPredictionLogs", mlPredictionLogRepository.count());
    
    // Calculate data coverage percentage
    long totalRecords = locationActivityScoreRepository.count();
    long mlRecords = locationActivityScoreRepository.countRecordsWithMLPredictions();
    double coveragePercentage = totalRecords > 0 ? (double) mlRecords / totalRecords * 100 : 0;
    dataStats.put("mlCoveragePercentage", coveragePercentage);
    
    return dataStats;
}
```

---

## Performance Optimization Strategies

### Query Performance Analysis

#### Before Optimization (LocationActivityScoreRepository)
```sql
-- Original problematic query
SELECT DISTINCT las.* 
FROM location_activity_scores las
JOIN activities a ON las.activity_id = a.id  
WHERE LOWER(a.name) = LOWER('Portrait photography')
-- Execution time: 2000-5000ms
-- Issues: DISTINCT on large result set, eager loading all relationships
```

#### After Optimization
```sql
-- Step 1: Get IDs only with DISTINCT ON
SELECT las.id FROM location_activity_scores las
WHERE las.id IN (
    SELECT DISTINCT ON (las2.location_id) las2.id
    FROM location_activity_scores las2
    JOIN activities a ON las2.activity_id = a.id
    WHERE LOWER(a.name) = LOWER('Portrait photography')
    ORDER BY las2.location_id, las2.historical_activity_score DESC NULLS LAST
)
ORDER BY las.historical_activity_score DESC NULLS LAST
LIMIT 100;
-- Execution time: 50-100ms

-- Step 2: Eager load entities by IDs
SELECT las.*, loc.*, tz.*
FROM location_activity_scores las
JOIN event_locations loc ON las.location_id = loc.id
JOIN taxi_zones tz ON las.taxi_zone_id = tz.id  
WHERE las.id IN (uuid1, uuid2, ..., uuid100);
-- Execution time: 100-200ms
-- Total: 150-300ms (10-20x improvement)
```

### Database Index Strategy

#### Essential Indexes
```sql
-- Activity repository indexes
CREATE UNIQUE INDEX idx_activities_name ON activities(name);
CREATE INDEX idx_activities_name_lower ON activities(LOWER(name));

-- LocationActivityScore repository indexes  
CREATE INDEX idx_las_activity_id ON location_activity_scores(activity_id);
CREATE INDEX idx_las_location_id ON location_activity_scores(location_id);
CREATE INDEX idx_las_historical_score ON location_activity_scores(historical_activity_score DESC NULLS LAST);
CREATE INDEX idx_las_muse_score ON location_activity_scores(muse_score DESC NULLS LAST);

-- RequestAnalytics repository indexes
CREATE INDEX idx_analytics_composite ON request_analytics(activity_name, requested_hour, requested_day_of_week);
CREATE INDEX idx_analytics_last_requested ON request_analytics(last_requested DESC);
CREATE INDEX idx_analytics_request_count ON request_analytics(request_count DESC);
```

### Connection Pool Optimization

#### Repository Usage Patterns
```java
// Efficient batch processing in DailyPrecomputationService
try {
    for (Activity activity : activities) {
        for (LocalTime time : reasonableTimes) {
            // Process recommendations...
            
            // CRITICAL: Force connection cleanup after each operation
            forceConnectionCleanup();
            Thread.sleep(2000); // Prevent connection pool exhaustion
            
            if (totalProcessed % 3 == 0) {
                // Extra cleanup every 3 operations
                forceConnectionCleanup();
                Thread.sleep(5000);
            }
        }
    }
} finally {
    // Always cleanup on completion
    forceConnectionCleanup();
}
```

---

## Testing Strategies

### Repository Testing Patterns

#### Unit Testing with @DataJpaTest
```java
@DataJpaTest
class ActivityRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired  
    private ActivityRepository activityRepository;
    
    @Test
    void findByNameIgnoreCase_ShouldReturnActivity_WhenCasesDiffer() {
        // Given
        Activity activity = new Activity("Portrait photography");
        entityManager.persistAndFlush(activity);
        
        // When
        Optional<Activity> result = activityRepository.findByNameIgnoreCase("PORTRAIT PHOTOGRAPHY");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Portrait photography");
    }
}
```

#### Performance Testing
```java
@Test
void findDistinctLocationIdsByActivityName_ShouldBePerformant() {
    // Given: Large dataset
    createTestDataset(1000); // 1000 location-activity combinations
    
    // When
    long startTime = System.currentTimeMillis();
    List<String> ids = locationActivityScoreRepository
        .findDistinctLocationIdsByActivityName("Portrait photography", 100);
    long duration = System.currentTimeMillis() - startTime;
    
    // Then
    assertThat(duration).isLessThan(500); // Should complete in < 500ms
    assertThat(ids).hasSize(100);
}
```

### Integration Testing

#### Multi-Repository Transactions
```java
@SpringBootTest
@Transactional
class RecommendationServiceIntegrationTest {
    
    @Test
    void getRecommendations_ShouldCreateAnalyticsAndMLLog() {
        // Given
        RecommendationRequest request = new RecommendationRequest(
            "Portrait photography", LocalDateTime.of(2025, 7, 25, 15, 0));
        
        long initialAnalyticsCount = analyticsRepository.count();
        long initialMLLogCount = mlPredictionLogRepository.count();
        
        // When
        RecommendationResponse response = recommendationService
            .getLocationRecommendations(request);
        
        // Then
        assertThat(response.getLocations()).isNotEmpty();
        assertThat(analyticsRepository.count()).isEqualTo(initialAnalyticsCount + 1);
        assertThat(mlPredictionLogRepository.count()).isEqualTo(initialMLLogCount + 1);
    }
}
```

---

## Best Practices Summary

### Repository Design Principles

#### Query Optimization
1. **Two-Step Queries:** Separate ID lookup from entity loading for complex queries
2. **Native SQL:** Use native queries for complex operations that JPA handles poorly
3. **Eager Loading:** Explicit JOIN FETCH to prevent N+1 problems
4. **Index Coverage:** Ensure all query methods utilize appropriate indexes

#### Data Access Patterns
1. **Optional Returns:** Use Optional for single entity lookups to handle null cases
2. **Batch Operations:** Process multiple entities efficiently with saveAll()
3. **Existence Checks:** Use exists() methods instead of loading full entities
4. **Pageable Support:** Use Pageable for large result sets to manage memory

### Performance Optimization
1. **Connection Management:** Force cleanup in long-running operations
2. **Query Monitoring:** Log execution times for performance tracking
3. **Index Strategy:** Create covering indexes for common query patterns
4. **Batch Size Tuning:** Optimize batch sizes for bulk operations

### Analytics and Monitoring
1. **Aggregation Queries:** Use database-level aggregations for statistics
2. **Time-Window Analysis:** Efficient date/time range queries
3. **Upsert Patterns:** Combine lookup and creation for analytics data
4. **Health Metrics:** Provide repository-level health and coverage metrics

### Error Handling
1. **Graceful Degradation:** Handle missing data gracefully in service layer
2. **Transaction Boundaries:** Use @Transactional appropriately
3. **Constraint Violations:** Handle unique constraint exceptions
4. **Connection Failures:** Implement retry logic for database connectivity issues