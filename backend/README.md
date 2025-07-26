# Manhattan Muse Backend

## Java Package Structure & Architecture Documentation

---

## Table of Contents

- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [Application Entry Point](#application-entry-point)
- [Package Documentation](#package-documentation)
  - [Configuration Layer](#configuration-layer)
  - [Controller Layer](#controller-layer)
  - [Data Transfer Objects](#data-transfer-objects)
  - [Entity Layer](#entity-layer)
  - [Exception Handling](#exception-handling)
  - [Repository Layer](#repository-layer)
  - [Service Layer](#service-layer)
  - [Utilities](#utilities)
- [Architecture Principles](#architecture-principles)

---

## Project Overview

Manhattan Muse Backend is a Spring Boot application that provides location recommendations for creative activities in Manhattan. The system integrates machine learning predictions, weather data, and analytics to deliver personalized recommendations through a RESTful API.

## Project Structure

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

## Application Entry Point

### CreativeSpaceFinderApplication.java

The main Spring Boot application class that bootstraps the entire system with three critical framework features:

| Annotation | Purpose |
|------------|---------|
| `@SpringBootApplication` | Enables auto-configuration, component scanning, and configuration |
| `@EnableCaching` | Activates Caffeine cache infrastructure for 24-hour recommendation caching |
| `@EnableScheduling` | Enables cron-based scheduled tasks for daily cache warming at 3 AM |

**Key Features:**
- Component scanning across all sub-packages
- Automatic bean registration
- Embedded Tomcat server startup on port 8080

---

## Package Documentation

### Configuration Layer

**Location:** `config/`

Contains Spring configuration classes that define core application behavior:

#### AsyncConfig.java
- **Purpose:** Configures asynchronous task execution
- **Features:**
  - Single-threaded executor for cache warming operations
  - Prevents HTTP 502 errors during intensive precomputation
  - 5-minute graceful shutdown timeout
  - Background process execution

#### CacheConfig.java
- **Purpose:** Implements Caffeine cache configuration
- **Specifications:**
  - 24-hour expiration policy
  - 1000-entry maximum size
  - Statistics tracking enabled
  - LRU eviction strategy
  - High-performance in-memory caching

#### SecurityConfig.java
- **Purpose:** Defines Spring Security filter chain
- **Configuration:**
  - Public access for API endpoints
  - Authenticated access for admin functions
  - BCrypt password encoding
  - Session-based authentication
  - 10 concurrent session limits

#### WebConfig.java
- **Purpose:** Configures Cross-Origin Resource Sharing (CORS)
- **Settings:**
  - Allows all origins, methods, and headers
  - Enables credential support
  - Cross-origin authenticated requests

---

### Controller Layer

**Location:** `controller/`

REST controllers handling HTTP requests and responses:

#### RecommendationController.java
- **Primary Endpoint:** Core location recommendation API
- **Features:**
  - Cache integration and analytics tracking
  - POST requests with activity/datetime/zone parameters
  - Returns scored location lists
  - Input validation

#### AdminController.java
- **Purpose:** Administrative interface
- **Capabilities:**
  - Session-based authentication
  - Cache management and debugging
  - System control operations
  - Async cache warming
  - Login/logout functionality

#### AnalyticsController.java
- **Purpose:** Analytics and metrics exposure
- **Endpoints:**
  - Usage patterns analysis
  - Cache performance metrics
  - Popular combinations insights
  - Hourly usage patterns
  - System performance statistics

#### HealthController.java
- **Purpose:** System health monitoring
- **Features:**
  - Comprehensive component status checks
  - Database connectivity status
  - External service health
  - Resource utilization metrics

#### WeatherForecastController.java
- **Purpose:** Weather data integration
- **Services:**
  - 96-hour forecast data
  - Available datetime lists
  - Specific datetime weather queries
  - OpenWeather Pro API integration

---

### Data Transfer Objects

**Location:** `dto/`

Type-safe data structures for API communication:

#### Request DTOs

| Class | Purpose | Validation |
|-------|---------|------------|
| `RecommendationRequest.java` | Input validation DTO | Required activity/datetime, optional zone |
| `LoginRequest.java` | Admin authentication | Username/password validation |

#### Response DTOs

| Class | Purpose | Content |
|-------|---------|---------|
| `RecommendationResponse.java` | Main API response wrapper | Location list, metadata, result counts |
| `LocationRecommendationResponse.java` | Individual location data | Coordinates, AI scores, crowd info, breakdowns |
| `PredictionResponse.java` | ML model response mapping | Muse scores, crowd numbers, activity scores |
| `WeatherData.java` | Weather information | Temperature, conditions, timezone conversion |
| `ForecastResponse.java` | 96-hour forecast structure | Nested weather data with error handling |

---

### Entity Layer

**Location:** `entity/`

JPA entities representing the database schema:

#### Core Entities

**Activity.java**
- Master data for creative activities
- Unique name constraints
- UUID primary keys
- Examples: Portrait photography, Street photography

**EventLocation.java**
- Physical locations in Manhattan
- Precise geographic coordinates
- Descriptive names
- Taxi zone relationships

**TaxiZone.java**
- NYC taxi zone data
- Geographic organization
- Neighborhood-based filtering
- Zone names and center coordinates

#### Data Entities

**LocationActivityScore.java**
- Core recommendation engine data
- Combines locations, activities, and temporal information
- Historical scores and ML-generated predictions
- Audit timestamps and metadata

**RequestAnalytics.java**
- Usage analytics tracking
- Activity/hour/day combinations
- Request counts and cache hit rates
- Response times for optimization

**MLPredictionLog.java**
- Machine learning model audit trail
- Version tracking and processing statistics
- Accuracy metrics and execution logs

---

### Exception Handling

**Location:** `exception/`

Centralized exception management system:

#### ApiException.java
- **Purpose:** Custom runtime exception for external API failures
- **Scope:** OpenWeather API, ML model communications
- **Features:** Chained exception support, descriptive error messages

#### GlobalExceptionHandler.java
- **Purpose:** Spring `@ControllerAdvice` for consistent error responses
- **Handles:**
  - Validation errors
  - HTTP method errors
  - API failures
  - Standardized JSON error format
- **Status Code Mapping:** Automatic HTTP status code assignment

---

### Repository Layer

**Location:** `repository/`

JPA repository interfaces for database operations:

#### ActivityRepository.java
- **Operations:** Simple CRUD operations
- **Features:**
  - Case-insensitive name lookups
  - Existence validation
  - Activity master data management

#### LocationActivityScoreRepository.java
- **Optimization:** Performance-optimized queries
- **Approach:** Two-step query strategy
- **Technology:** PostgreSQL DISTINCT ON optimization
- **Performance:** 10-20x improvement over naive approaches
- **Features:**
  - Analytics aggregation
  - Metadata queries
  - Complex filtering

#### RequestAnalyticsRepository.java
- **Purpose:** Statistical aggregation queries
- **Capabilities:**
  - Usage analytics
  - Cache performance analysis
  - Business intelligence reporting
  - Trend analysis

#### MLPredictionLogRepository.java
- **Purpose:** Standard JPA operations
- **Scope:** ML model execution audit logging
- **Data:** Processing statistics and execution history

---

### Service Layer

**Location:** `service/`

Core application services implementing business rules:

#### LocationRecommendationService.java
- **Role:** Primary recommendation engine
- **Integration:**
  - ML predictions
  - Scoring algorithms
  - Geographic filtering
  - Caching system
- **Features:**
  - Sophisticated crowd preference logic
  - Weighted scoring calculations
  - Zone-based filtering
  - Distance optimization

#### AnalyticsService.java
- **Purpose:** Usage tracking and performance analytics
- **Capabilities:**
  - Request pattern analysis
  - Cache optimization insights
  - User behavior monitoring
  - Performance metrics

#### SystemHealthService.java
- **Role:** Comprehensive system monitoring
- **Coverage:**
  - Database connections
  - Cache performance
  - External API status
  - Resource utilization
  - Memory and CPU metrics

#### WeatherForecastService.java
- **Purpose:** OpenWeather API integration
- **Features:**
  - Timezone conversion
  - Graceful fallback strategies
  - NYC-specific geographic configuration
  - Error handling and retries

#### DailyPrecomputationService.java
- **Role:** Cache warming orchestration
- **Scheduling:**
  - Daily execution at 3 AM
  - Async admin-triggered operations
- **Optimization:** Aggressive connection management for Supabase

#### ConnectionCleanupService.java
- **Purpose:** Database connection pool monitoring
- **Optimization:** Supabase connection limits compatibility
- **Frequency:** 30-second monitoring intervals
- **Features:** Automatic cleanup and resource management

---

### Utilities

**Location:** `utils/`

Helper classes for domain-specific processing:

#### LocationNameUtils.java
- **Purpose:** NYC-specific text processing
- **Capabilities:**
  - Location name abbreviation and standardization
  - Complex Manhattan naming conventions
  - Street intersection handling
  - Plaza name processing
  - Geographic abbreviations
  - Intelligent truncation strategies

**Key Features:**
- Handles duplicated plaza names
- Manages street intersections with "between" clauses
- Applies NYC-specific abbreviations
- Smart length management with natural breakpoints
- Cleans hanging punctuation and geographic suffixes

---

## Architecture Principles

### Layered Architecture

The package structure follows clean architecture principles with clear separation of concerns:

| Layer | Responsibility | Dependencies |
|-------|---------------|--------------|
| **Controllers** | HTTP concerns and request routing | Delegate to services |
| **Services** | Business logic and orchestration | Coordinate between repositories |
| **Repositories** | Data access abstraction | JPA entities and database |
| **DTOs** | Type safety and validation | API boundary contracts |
| **Entities** | Domain model representation | Rich business behavior |
| **Configuration** | Infrastructure setup | Framework integration |

### Design Principles

**Performance Optimization**
- Intelligent caching strategies
- Database query optimization
- External service integration
- Connection pool management

**Code Quality**
- Clean architecture boundaries
- Comprehensive error handling
- Type safety throughout
- Consistent naming conventions

**Scalability**
- Async processing capabilities
- Resource monitoring
- Connection management
- Cache warming strategies

**Maintainability**
- Clear package organization
- Comprehensive documentation
- Consistent patterns
- Modular design

---

## Technical Stack

- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL with JPA/Hibernate
- **Caching:** Caffeine Cache
- **Security:** Spring Security
- **API Integration:** RestTemplate
- **Monitoring:** Custom health checks
- **Validation:** Jakarta Validation
- **Architecture:** Layered architecture with dependency injection