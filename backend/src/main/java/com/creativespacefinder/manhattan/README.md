# Manhattan Package Root Documentation

## Overview

This document covers the main package level of the Creative Space Finder application at `src/main/java/com/creativespacefinder/manhattan/`. This level contains the main Spring Boot application class and all the core application packages that implement the recommendation engine functionality.

## Package Structure

```
src/main/java/com/creativespacefinder/manhattan/
├── CreativeSpaceFinderApplication.java    # Main Spring Boot application entry point
├── config/                                # Infrastructure configuration classes
├── controller/                            # REST API endpoint controllers
├── dto/                                   # Data Transfer Objects for API communication
├── entity/                                # JPA entities representing database schema
├── exception/                             # Custom exceptions and global error handling
├── repository/                            # Data access layer with JPA repositories
├── service/                               # Core business logic services
└── utils/                                 # Utility classes for specialized processing
```

---

## CreativeSpaceFinderApplication.java

### Purpose
The main Spring Boot application class that serves as the entry point for the entire Creative Space Finder application. This class bootstraps the Spring application context, enables core framework features, and starts the embedded web server.

### Class Structure
```java
package com.creativespacefinder.manhattan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CreativeSpaceFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreativeSpaceFinderApplication.class, args);
    }
}
```

### Core Annotations

#### @SpringBootApplication
```java
@SpringBootApplication
```

**Composite Meta-Annotation:**
- `@Configuration` - Marks this as a configuration class
- `@EnableAutoConfiguration` - Enables Spring Boot's intelligent auto-configuration
- `@ComponentScan` - Activates component scanning for dependency injection

**Component Scanning Scope:**
- **Base Package:** `com.creativespacefinder.manhattan`
- **Discovered Components:** All classes annotated with `@Service`, `@Repository`, `@Controller`, `@Component`
- **Configuration Classes:** All `@Configuration` classes in the config package
- **Recursive Scanning:** Includes all sub-packages automatically

#### @EnableCaching
```java
@EnableCaching
```

**Caching Infrastructure Activation:**
- **Cache Provider:** Caffeine high-performance caching library
- **Cache Configuration:** Managed by `CacheConfig.java` in config package
- **Method-Level Caching:** Enables `@Cacheable`, `@CacheEvict`, `@CachePut` annotations
- **Cache Management:** Administrative cache operations through Spring's CacheManager

**Primary Cache Usage:**
```java
// LocationRecommendationService
@Cacheable(cacheNames = "recommendations", 
           key = "#request.activity + '_' + #request.dateTime.toString() + '_' + (#request.selectedZone != null ? #request.selectedZone : 'all')")
public RecommendationResponse getLocationRecommendations(RecommendationRequest request)
```

**Cache Configuration Details:**
- **Cache Name:** "recommendations"
- **Expiration:** 24 hours (matches daily cache warming schedule)
- **Maximum Size:** 1000 entries
- **Statistics:** Enabled for monitoring and optimization

#### @EnableScheduling
```java
@EnableScheduling
```

**Scheduled Task Infrastructure:**
- **Cron Job Support:** Enables `@Scheduled` annotations with cron expressions
- **Fixed Rate Tasks:** Supports fixed-rate and fixed-delay scheduling
- **Async Execution:** Works with async task execution configuration

**Key Scheduled Operations:**
```java
// DailyPrecomputationService - Daily cache warming
@Scheduled(cron = "0 0 3 * * *")
public void dailyPrecomputation()

// ConnectionCleanupService - Aggressive connection management
@Scheduled(fixedRate = 30000)
public void monitorAndCleanConnections()
```

### Application Initialization Process

#### Bootstrap Sequence
1. **JVM Startup:** Load and initialize `CreativeSpaceFinderApplication` class
2. **Spring Context Creation:** Create and configure `ApplicationContext`
3. **Component Scanning:** Discover all annotated classes in `com.creativespacefinder.manhattan` package tree
4. **Auto-Configuration:** Apply Spring Boot's intelligent configuration based on classpath
5. **Bean Registration:** Register all discovered services, repositories, controllers, and configurations
6. **Database Initialization:** Configure HikariCP connection pool for Supabase PostgreSQL
7. **Cache Setup:** Initialize Caffeine cache with configured parameters
8. **Async Infrastructure:** Setup thread pools for scheduled and async operations
9. **Web Server Startup:** Start embedded Tomcat server on port 8080
10. **Scheduler Activation:** Enable cron jobs and scheduled tasks
11. **Application Ready:** Application context fully initialized and ready to serve requests

#### Key Beans Initialized
```java
// Infrastructure Beans
- DataSource (HikariCP with ultra-conservative Supabase settings)
- CacheManager (Caffeine implementation with 24-hour expiration)
- ThreadPoolTaskExecutor ("cacheWarmingExecutor" for async operations)

// Repository Beans (JPA Proxies)
- ActivityRepository
- LocationActivityScoreRepository  
- MLPredictionLogRepository
- RequestAnalyticsRepository

// Service Beans
- LocationRecommendationService (core recommendation engine)
- AnalyticsService (usage tracking and optimization)
- WeatherForecastService (OpenWeather API integration)
- SystemHealthService (comprehensive monitoring)
- DailyPrecomputationService (cache warming orchestration)
- ConnectionCleanupService (Supabase connection management)

// Controller Beans
- RecommendationController (main API endpoints)
- AdminController (administrative functions)
- AnalyticsController (usage analytics)
- HealthController (system health monitoring)
- WeatherForecastController (weather data endpoints)

// Configuration Beans
- AsyncConfig (async task execution setup)
- CacheConfig (Caffeine cache configuration)
- SecurityConfig (authentication and authorization)
- WebConfig (CORS and web configuration)
```

### Package Integration Architecture

#### Component Discovery and Wiring
The `@SpringBootApplication` annotation enables automatic discovery and wiring of components across all packages:

**Service Layer Integration:**
```java
@Service
public class LocationRecommendationService {
    @Autowired
    private LocationActivityScoreRepository locationActivityScoreRepository;
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
}
```

**Controller Layer Integration:**
```java
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private LocationRecommendationService locationRecommendationService;
    
    @Autowired
    private AnalyticsService analyticsService;
}
```

**Configuration Integration:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "cacheWarmingExecutor")
    public Executor cacheWarmingExecutor() {
        // Thread pool configuration for async operations
    }
}
```

### Environment and Profile Support

#### Application Configuration Loading
The main application class works in conjunction with the YAML configuration in `src/main/resources/application.yaml`:

**Configuration Binding:**
```java
// Service classes use @Value for configuration injection
@Service
public class WeatherForecastService {
    @Value("${openweather.api-key}")
    private String apiKey;
}

@Service  
public class LocationRecommendationService {
    @Value("${ml.predict.url}")
    private String mlPredictUrl;
}
```

#### Environment Variables Integration
```bash
# Database Configuration (Supabase)
SPRING_DATASOURCE_URL=postgresql://user:password@host:port/database
SPRING_DATASOURCE_USERNAME=postgres_user
SPRING_DATASOURCE_PASSWORD=secure_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# External API Configuration
OPENWEATHER_API_KEY=your_openweather_pro_api_key
ML_PREDICT_URL=http://ml-service:8000/predict_batch

# Admin Authentication
ADMIN_USERNAME=admin
ADMIN_PASSWORD_HASH=$2a$10$hashed_bcrypt_password
```

### Application Lifecycle Management

#### Startup Events
The application supports various lifecycle hooks for proper initialization:

```java
// Potential startup listeners (if implemented)
@EventListener
public void handleApplicationReady(ApplicationReadyEvent event) {
    System.out.println("Creative Space Finder application is ready!");
    // Optional: Trigger initial cache warming
    // Optional: Validate external service connectivity
}
```

#### Graceful Shutdown
Configured through YAML for proper resource cleanup:

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  shutdown: graceful
```

**Shutdown Process:**
1. **Stop Accepting Requests:** Web server stops accepting new requests
2. **Complete Active Requests:** Allow in-flight requests to complete (up to 30 seconds)
3. **Cache Warming Completion:** Wait for async cache warming to finish
4. **Connection Pool Cleanup:** Force close all database connections
5. **Thread Pool Shutdown:** Terminate all async task executors
6. **Application Context Shutdown:** Clean shutdown of Spring context

### Performance and Monitoring Integration

#### Built-in Observability
The main application enables comprehensive monitoring through various endpoints:

```java
// Health checks available at /api/health
// Cache statistics available through SystemHealthService
// Performance metrics tracked through AnalyticsService
// Connection pool monitoring via ConnectionCleanupService
```

#### JVM and Application Metrics
```java
// Memory usage monitoring in SystemHealthService
MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

// Connection pool metrics via HikariCP
HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();

// Cache performance via Caffeine statistics
CaffeineCache.getNativeCache().stats()
```

### Error Handling and Resilience

#### Global Exception Handling
The application leverages the exception package for comprehensive error management:

```java
// GlobalExceptionHandler processes all unhandled exceptions
// ApiException handles external service failures
// Graceful degradation for ML and Weather API failures
// Analytics tracking never breaks main application flow
```

#### Circuit Breaker Patterns
```java
// Weather service fallback
public WeatherData getWeatherForDateTime(LocalDateTime target) {
    try {
        // Call OpenWeather API
    } catch (Exception e) {
        return createDefaultWeatherData(target);  // Graceful fallback
    }
}

// Analytics resilience
public void trackRequest(...) {
    try {
        // Track analytics
    } catch (Exception e) {
        // Log error but don't break main flow
        System.err.println("Analytics tracking failed: " + e.getMessage());
    }
}
```

---

## Package Overview and Responsibilities

### config/ - Infrastructure Configuration
- **AsyncConfig:** Thread pool configuration for background processing
- **CacheConfig:** Caffeine cache setup with 24-hour expiration
- **SecurityConfig:** Authentication, session management, CORS policies
- **WebConfig:** Web MVC configuration and cross-origin support

### controller/ - REST API Layer  
- **RecommendationController:** Core location recommendation endpoints
- **AdminController:** Administrative functions and cache management
- **AnalyticsController:** Usage analytics and performance metrics
- **HealthController:** System health monitoring
- **WeatherForecastController:** Weather data integration

### dto/ - Data Transfer Objects
- **Request DTOs:** RecommendationRequest with validation
- **Response DTOs:** RecommendationResponse, LocationRecommendationResponse, WeatherData
- **API Integration DTOs:** ForecastResponse, PredictionResponse for external services

### entity/ - Domain Model
- **Core Entities:** Activity, EventLocation, LocationActivityScore, TaxiZone
- **Analytics Entities:** RequestAnalytics for usage tracking
- **Audit Entities:** MLPredictionLog for ML operation tracking

### exception/ - Error Handling
- **ApiException:** Custom exception for external service failures
- **GlobalExceptionHandler:** Centralized error processing with proper HTTP status codes

### repository/ - Data Access Layer
- **Performance-Optimized Queries:** Two-step approach for complex location scoring queries
- **Analytics Repositories:** Statistical aggregation and performance analysis
- **Audit Repositories:** Simple CRUD for ML prediction logging

### service/ - Business Logic
- **LocationRecommendationService:** Core recommendation engine with ML integration
- **AnalyticsService:** Usage tracking and cache optimization intelligence
- **DailyPrecomputationService:** Cache warming orchestration
- **SystemHealthService:** Comprehensive system monitoring
- **WeatherForecastService:** OpenWeather API integration
- **ConnectionCleanupService:** Database connection management for Supabase

### utils/ - Specialized Utilities
- **LocationNameUtils:** NYC-specific geographic text processing and abbreviation

---

## Integration Patterns and Best Practices

### Dependency Injection Strategy
```java
// Constructor injection preferred for required dependencies
@Service
public class LocationRecommendationService {
    private final LocationActivityScoreRepository repository;
    private final ActivityRepository activityRepository;
    
    public LocationRecommendationService(LocationActivityScoreRepository repository,
                                       ActivityRepository activityRepository) {
        this.repository = repository;
        this.activityRepository = activityRepository;
    }
}

// Field injection used for configuration values
@Value("${ml.predict.url}")
private String mlPredictUrl;
```

### Transactional Boundaries
```java
// Service layer manages transactions
@Transactional
public RecommendationResponse getLocationRecommendations(RecommendationRequest request) {
    // All database operations within single transaction
    // Automatic rollback on exceptions
}
```

### Async Processing Patterns
```java
// Background cache warming
@Async("cacheWarmingExecutor")
public CompletableFuture<String> triggerAsyncDailyPrecomputation() {
    // Non-blocking cache warming
    // Dedicated thread pool prevents blocking main operations
}
```

### Caching Strategies
```java
// Method-level caching with sophisticated key generation
@Cacheable(cacheNames = "recommendations", 
           key = "#request.activity + '_' + #request.dateTime.toString() + '_' + (#request.selectedZone != null ? #request.selectedZone : 'all')")
```

### Error Handling Philosophy
- **Service Isolation:** External API failures don't break core functionality
- **Graceful Degradation:** Default values when external services unavailable
- **Analytics Resilience:** Usage tracking never breaks main application flow
- **Resource Management:** Aggressive connection cleanup for Supabase limits

---

## Deployment and Operations

### Application Packaging
The main application class produces an executable JAR with embedded Tomcat:

```bash
mvn clean package
java -jar target/creative-space-finder-3.0.0.jar
```

### Environment Configuration
All sensitive configuration externalized to environment variables:

```bash
# Required for startup
export SPRING_DATASOURCE_URL="postgresql://..."
export SPRING_DATASOURCE_USERNAME="user"
export SPRING_DATASOURCE_PASSWORD="password"
export OPENWEATHER_API_KEY="api_key"
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD_HASH="$2a$10$..."

# Optional with defaults
export ML_PREDICT_URL="http://ml-service:8000/predict_batch"
```

### Monitoring and Health Checks
```bash
# Application health
curl http://localhost:8080/api/health

# Comprehensive system status including:
# - Database connection pool status
# - Cache performance metrics  
# - External API connectivity
# - ML model health
# - Weather API status
# - Resource utilization
```

### Performance Characteristics
- **Startup Time:** ~15-30 seconds (including database connectivity)
- **Memory Usage:** ~200-500MB (depending on cache size)
- **Request Latency:** 50-300ms (with cache hits), 2000-5000ms (cache misses)
- **Cache Hit Rate:** >80% after warming
- **Database Connections:** 1 maximum (Supabase optimized)

---

## Summary

The `CreativeSpaceFinderApplication.java` class serves as the central orchestrator for a sophisticated Spring Boot application that implements an AI-powered location recommendation engine. Through strategic use of Spring Boot annotations and careful package organization, the application provides:

**Core Capabilities:**
- ML-powered location recommendations for creative activities
- Intelligent caching with automatic warming strategies
- Comprehensive analytics and performance monitoring
- Weather integration for enhanced recommendations
- Administrative interfaces for system management

**Technical Excellence:**
- Performance-optimized database queries (10-20x improvement)
- Sophisticated caching strategies with 80%+ hit rates
- Aggressive connection management for Supabase compatibility
- Comprehensive error handling with graceful degradation
- Async processing for non-blocking operations

**Operational Readiness:**
- Environment-based configuration management
- Comprehensive health monitoring and metrics
- Graceful shutdown with resource cleanup
- Production-ready logging and error handling
- Scalable architecture with clear separation of concerns

The application demonstrates enterprise-grade Spring Boot development with particular attention to performance optimization, resource management, and operational excellence.