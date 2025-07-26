# Configuration Package Documentation

## Overview

The `/config/` package contains Spring Boot configuration classes that define the core infrastructure and behavior of the Creative Space Finder application. These configuration classes handle asynchronous processing, caching, security, and cross-origin resource sharing (CORS) policies.

## Package Structure

```
com.creativespacefinder.manhattan.config/
├── AsyncConfig.java        # Async task execution configuration
├── CacheConfig.java        # Caffeine cache configuration
├── SecurityConfig.java     # Spring Security configuration
└── WebConfig.java         # Web/CORS configuration
```

---

## AsyncConfig.java

### Purpose
Configures asynchronous task execution specifically for cache warming operations. This configuration prevents the cache warming process from blocking the main application thread and causing HTTP 502 errors during intensive precomputation tasks.

### Key Components

#### Thread Pool Configuration
```java
@Bean(name = "cacheWarmingExecutor")
public Executor cacheWarmingExecutor()
```

**Core Settings:**
- **Core Pool Size**: `1` - Only one core thread to prevent resource contention
- **Max Pool Size**: `1` - Maximum of one thread to ensure sequential cache warming
- **Queue Capacity**: `1` - Only one additional task can be queued
- **Thread Name Prefix**: `"CacheWarming-"` - Easy identification in logs

#### Graceful Shutdown Handling
- **Wait for Tasks**: `true` - Ensures running cache warming completes before shutdown
- **Termination Timeout**: `300 seconds` (5 minutes) - Maximum wait time for cache warming completion

### Design Rationale

**Single-Threaded Approach:**
- Prevents multiple concurrent cache warming processes
- Reduces database connection pressure (critical for Supabase integration)
- Ensures predictable resource utilization

**Async Benefits:**
- Cache warming runs in background without blocking API responses
- Admin can trigger cache warming and continue using the application
- Prevents HTTP timeouts during intensive operations

### Usage Context
This executor is specifically used by `DailyPrecomputationService.triggerAsyncDailyPrecomputation()` to run cache warming operations asynchronously.

---

## CacheConfig.java

### Purpose
Configures the Caffeine cache implementation for storing recommendation results. This cache dramatically improves response times by storing computed recommendations for 24-hour periods.

### Key Components

#### Cache Configuration Bean
```java
@Bean
public Caffeine<Object, Object> caffeineConfig()
```

**Cache Settings:**
- **Expiration**: `24 hours` after write - Matches daily cache warming schedule
- **Maximum Size**: `1000 entries` - Accommodates all activity/time combinations
- **Statistics**: `Enabled` - Allows monitoring of cache performance

#### Cache Manager
```java
@Bean
public CacheManager cacheManager(Caffeine<Object, Object> caffeine)
```

**Configuration:**
- **Cache Name**: `"recommendations"` - Single cache for all recommendation data
- **Implementation**: `CaffeineCacheManager` - High-performance, in-memory cache

### Cache Key Strategy
The cache uses composite keys in the format:
```
{activity}_{dateTime}_{selectedZone}
```

**Examples:**
- `"Portrait photography_2025-07-25T15:00_soho hudson square"`
- `"Street photography_2025-07-25T17:00_all"`

### Performance Benefits

**Response Time Improvement:**
- Cache hits: ~50-100ms response time
- Cache misses: ~2000-5000ms (requires ML model calls)
- Hit rate typically >80% after cache warming

**Resource Optimization:**
- Reduces ML model API calls
- Minimizes database queries
- Decreases server load during peak usage

### Cache Lifecycle

**Daily Refresh:**
- Automatic cache warming at 3 AM daily
- 24-hour expiration ensures fresh data
- Manual cache warming available via admin endpoints

**Memory Management:**
- LRU eviction when size limit reached
- Statistics tracking for monitoring
- Graceful degradation on cache failures

---

## SecurityConfig.java

### Purpose
Defines the security configuration for the application, including authentication requirements, endpoint access controls, and session management policies.

### Key Components

#### Password Encoding
```java
@Bean
public BCryptPasswordEncoder passwordEncoder()
```
- Uses BCrypt hashing algorithm for secure password storage
- Industry-standard password encryption

#### Security Filter Chain
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http)
```

### Endpoint Access Control

#### Public Endpoints (No Authentication Required)
```java
.requestMatchers("/api/recommendations/**").permitAll()
.requestMatchers("/api/forecast/**").permitAll()
.requestMatchers("/api/health/**").permitAll()
.requestMatchers("/api/admin/login").permitAll()
.requestMatchers("/api/admin/validate-session").permitAll()
.requestMatchers("/api/admin/logout").permitAll()
.requestMatchers("/api/admin/cache-status").permitAll()
.requestMatchers("/api/admin/warm-cache").permitAll()
```

**Rationale for Public Access:**
- **Recommendations**: Core API functionality must be accessible
- **Forecast**: Weather data should be freely available
- **Health**: System monitoring endpoints
- **Admin Login/Logout**: Authentication workflow endpoints
- **Cache Operations**: Administrative functions (session-based auth handled in controller)

#### Protected Endpoints
```java
.requestMatchers("/api/admin/**").authenticated()
```
- All other admin endpoints require authentication
- Covers sensitive administrative functions

#### Default Policy
```java
.anyRequest().permitAll()
```
- Allows access to any other endpoints not explicitly configured
- Provides flexibility for future endpoint additions

### Security Features Disabled

#### CSRF Protection
```java
.csrf(csrf -> csrf.disable())
```
**Reason**: API-first application using session-based authentication doesn't require CSRF protection

#### HTTP Basic Authentication
```java
.httpBasic(httpBasic -> httpBasic.disable())
```
**Reason**: Custom session-based authentication implemented in controllers

#### Form Login
```java
.formLogin(formLogin -> formLogin.disable())
```
**Reason**: JSON-based login via REST API, not HTML forms

### Session Management
```java
.sessionManagement(session -> session
    .maximumSessions(10)
    .maxSessionsPreventsLogin(false)
)
```

**Configuration:**
- **Maximum Sessions**: `10` concurrent sessions per user
- **Prevent Login**: `false` - New logins don't block existing sessions
- **Session Strategy**: Allow multiple admin sessions for operational flexibility

### Security Architecture

**Authentication Flow:**
1. Admin submits credentials to `/api/admin/login`
2. Controller validates against configured admin credentials
3. Session created with `adminAuthenticated=true`
4. Subsequent requests validated via session attributes
5. 24-hour session timeout for security

**Session Validation:**
- Automatic session validation on protected endpoints
- Time-based expiration (24 hours)
- Manual session invalidation on logout

---

## WebConfig.java

### Purpose
Configures Cross-Origin Resource Sharing (CORS) policies to enable frontend applications to communicate with the API from different domains.

### Key Components

#### CORS Configuration Bean
```java
@Bean
public WebMvcConfigurer corsConfigurer()
```

### CORS Policy Settings

#### Allowed Origins
```java
.allowedOriginPatterns("*")
```
**Scope**: Allows requests from any origin
**Use Case**: Supports development environments and multiple frontend deployments

#### Allowed HTTP Methods
```java
.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
```
**Coverage**: All standard REST API operations plus preflight OPTIONS requests

#### Allowed Headers
```java
.allowedHeaders("*")
```
**Flexibility**: Accepts any headers in requests (Content-Type, Authorization, etc.)

#### Credentials Support
```java
.allowCredentials(true)
```
**Purpose**: Enables sending cookies and session information across origins

### Security Considerations

**Production Recommendations:**
While the current configuration allows maximum flexibility, production deployments should consider:

```java
// More restrictive production configuration example
.allowedOriginPatterns("https://your-frontend-domain.com", "https://admin.your-domain.com")
.allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
```

### CORS Request Flow

**Simple Requests:**
1. Browser sends request with `Origin` header
2. Server responds with appropriate `Access-Control-*` headers
3. Browser allows/blocks based on CORS policy

**Preflight Requests:**
1. Browser sends OPTIONS request for complex operations
2. Server responds with allowed methods/headers
3. Browser sends actual request if preflight succeeds

### Integration with Frontend

**Supported Scenarios:**
- Local development (localhost:3000, localhost:8080, etc.)
- Production frontend on different domain
- Admin dashboard on subdomain
- Mobile app webview components

---

## Configuration Integration

### How Configurations Work Together

#### Cache + Async Integration
- `AsyncConfig` provides executor for background cache warming
- `CacheConfig` defines cache that gets populated asynchronously
- Results in non-blocking cache management

#### Security + Web Integration
- `SecurityConfig` defines authentication rules
- `WebConfig` ensures CORS compatibility with authenticated requests
- Enables secure cross-origin admin operations

#### Performance Optimization Chain
1. **Request arrives** → CORS validation (WebConfig)
2. **Security check** → Authentication if required (SecurityConfig)
3. **Cache lookup** → Fast response if cached (CacheConfig)
4. **Background warming** → Async precomputation (AsyncConfig)

### Environment-Specific Considerations

#### Development Environment
- CORS allows localhost origins
- Cache warming can be triggered manually
- Security is relaxed for easier testing

#### Production Environment
- CORS should be restricted to known domains
- Automatic cache warming ensures performance
- Security enforces proper authentication

### Monitoring and Observability

#### Cache Metrics
- Hit/miss rates available via `/api/health`
- Cache size and performance statistics
- Admin dashboard shows cache status

#### Security Logging
- Authentication attempts logged
- Session creation/expiration tracked
- Failed access attempts recorded

#### Async Task Monitoring
- Cache warming progress visible in logs
- Thread pool status available for monitoring
- Completion times tracked for optimization

---

## Best Practices and Recommendations

### Cache Configuration
- Monitor cache hit rates and adjust size if needed
- Consider cache warming during low-traffic periods
- Implement cache invalidation for data updates

### Security Configuration
- Regularly rotate admin credentials
- Monitor session activity for suspicious patterns
- Consider implementing rate limiting for admin endpoints

### CORS Configuration
- Restrict origins in production environments
- Regularly audit allowed headers and methods
- Monitor CORS-related errors in logs

### Async Configuration
- Monitor thread pool utilization
- Adjust timeout values based on actual cache warming duration
- Consider implementing progress tracking for long-running operations