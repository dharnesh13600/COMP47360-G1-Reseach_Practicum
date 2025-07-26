# Resources Directory README

## Overview

The `/src/main/resources/` directory contains the application configuration and static resources for the Creative Space Finder application. This directory follows Spring Boot conventions for external configuration management and resource loading.

## Directory Structure

```
src/main/resources/
├── application.yaml           # Main application configuration (YAML format)
├── static/                   # Static web resources (if any)
│   └── [Static files like CSS, JS, images]
└── templates/                # Template files (if using server-side rendering)
    └── [Thymeleaf or other template files]
```

**Note:** Currently, the application primarily uses `application.yaml` for configuration, with static and template directories available for future web UI enhancements.

---

## application.yaml

### Purpose

The `application.yaml` file is the main configuration file for the Creative Space Finder application. It uses YAML format for better readability and hierarchical organization compared to traditional `.properties` files. This file contains all application settings including database connections, external API configurations, caching parameters, and operational settings.

### Configuration Sections

#### 1. Spring Framework Configuration

```yaml
spring:
  application:
    name: creative-space-finder  # Application identifier for monitoring/tracing
```

**Application Identity:**
- Used by Spring Cloud services for service discovery
- Appears in logging and monitoring systems
- Helps identify the application in distributed environments

#### 2. Database Configuration (Supabase PostgreSQL)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: ${SPRING_DATASOURCE_DRIVER_CLASS_NAME}
```

**Environment Variables Required:**
```bash
SPRING_DATASOURCE_URL=postgresql://user:password@host:port/database
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
```

**Security Note:** All sensitive database credentials are externalized to environment variables, never hardcoded in the configuration file.

#### 3. HikariCP Connection Pool (Supabase Optimized)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 1           # ULTRA CONSERVATIVE for Supabase free tier
      minimum-idle: 0                # No idle connections ever
      connection-timeout: 10000      # 10 seconds
      idle-timeout: 10000            # Close idle connections after 10 seconds
      max-lifetime: 120000           # Force close connections after 2 minutes
      leak-detection-threshold: 15000 # 15 seconds leak detection
      connection-test-query: SELECT 1
      validation-timeout: 3000       # 3 seconds validation
      auto-commit: true              # Explicit auto-commit
      pool-name: "ProductionHikariPool"
      keepalive-time: 0              # No keepalive pings
      initialization-fail-timeout: 1 # Fail fast on startup issues
```

**Supabase-Specific Optimizations:**
- **Single Connection Pool:** Only 1 connection maximum to respect free tier limits
- **Aggressive Cleanup:** Connections closed quickly to prevent resource exhaustion
- **Fast Failure:** Quick detection and resolution of connection issues
- **No Idle Connections:** Immediate closure of unused connections

#### 4. JPA/Hibernate Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate             # Validate schema without changes
    show-sql: false                  # Disable SQL logging for performance
    open-in-view: false              # CRITICAL: Prevents connection leaks
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
          batch_size: 20             # Batch operations for efficiency
        connection:
          provider_disables_autocommit: false
        temp:
          use_jdbc_metadata_defaults: false
        boot:
          allow_jdbc_metadata_access: false
```

**Performance and Connection Optimizations:**
- **Schema Validation:** Ensures database schema matches entity definitions without modifications
- **Open-in-View Disabled:** Prevents lazy loading from keeping connections open
- **Minimal Metadata Access:** Reduces connection usage during startup
- **Batch Processing:** Groups database operations for efficiency

#### 5. Application Lifecycle Management

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # Graceful shutdown with 30-second timeout
```

**Graceful Shutdown Features:**
- Completes in-flight requests before shutdown
- Allows cache warming processes to finish
- Ensures proper connection pool cleanup
- Coordinates with server shutdown configuration

#### 6. Session Management (Admin Authentication)

```yaml
spring:
  session:
    store-type: memory    # In-memory session storage
    timeout: 24h         # 24-hour session timeout
```

**Admin Session Configuration:**
- **Memory-Based Storage:** Sessions stored in application memory
- **24-Hour Timeout:** Balances security with usability
- **Cookie Configuration:** Handled in server section

#### 7. Async Task Execution

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 2                 # 2 core threads
        max-size: 5                  # Maximum 5 threads
        queue-capacity: 10           # Queue up to 10 tasks
        thread-name-prefix: "async-" # Thread naming for debugging
        keep-alive: 60s             # Thread keep-alive time
```

**Async Processing Support:**
- **Cache Warming:** Background cache precomputation
- **Analytics Processing:** Non-blocking usage tracking
- **External API Calls:** Parallel processing when possible

#### 8. Admin Authentication

```yaml
admin:
  username: ${ADMIN_USERNAME}
  password:
    hash: ${ADMIN_PASSWORD_HASH}
```

**Environment Variables Required:**
```bash
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD_HASH=$2a$10$your_bcrypt_hashed_password
```

**Security Features:**
- **BCrypt Hashing:** Passwords stored as BCrypt hashes
- **Environment Variables:** Credentials never in source code
- **Session-Based Auth:** Secure session management

#### 9. External API Configuration

```yaml
openweather:
  api-key: ${OPENWEATHER_API_KEY}

ml:
  predict:
    url: ${ML_PREDICT_URL:http://ml-service.default.svc.cluster.local:8000/predict_batch}
    timeout:
      connect: 30000    # 30 seconds to connect
      read: 120000      # 2 minutes to read response
```

**Environment Variables:**
```bash
OPENWEATHER_API_KEY=your_openweather_pro_api_key
ML_PREDICT_URL=http://your-ml-service:8000/predict_batch  # Optional, has default
```

**API Integration Features:**
- **Weather Data:** OpenWeather Pro API for 96-hour forecasts
- **ML Predictions:** Machine learning model for location scoring
- **Timeout Configuration:** Prevents hanging requests
- **Default Fallbacks:** ML service has default URL for Kubernetes environments

#### 10. Server Configuration

```yaml
server:
  port: 8080
  servlet:
    context-path: /
    session:
      tracking-modes: cookie
      cookie:
        secure: false      # Set to true in production with HTTPS
        http-only: true    # Prevents XSS attacks
        max-age: 86400     # 24 hours
  shutdown: graceful
  tomcat:
    connection-timeout: 30000  # 30 seconds for regular requests
    max-connections: 50
    threads:
      max: 200
      min-spare: 10
    processor-cache: 200
    accept-count: 100
```

**Server Features:**
- **Graceful Shutdown:** Coordinates with Spring lifecycle management
- **Session Security:** HTTP-only cookies for security
- **Connection Management:** Optimized for concurrent requests
- **Admin Operations:** Longer timeouts for cache warming operations

#### 11. Logging Configuration

```yaml
logging:
  level:
    com.creativespacefinder.manhattan: INFO    # Application logging
    org.springframework.web: INFO              # Spring Web
    org.hibernate.SQL: WARN                    # Database queries
    org.hibernate.orm.deprecation: WARN        # Suppress deprecation warnings
    com.zaxxer.hikari: INFO                    # Connection pool
    com.zaxxer.hikari.pool: WARN               # Pool warnings only
    org.springframework.cache: DEBUG           # Cache operations
    com.github.benmanes.caffeine: DEBUG        # Cache statistics
    org.springframework.scheduling: INFO       # Scheduled tasks
    org.springframework.core.task: INFO        # Async tasks
```

**Logging Strategy:**
- **Application Level:** INFO for operational visibility
- **Framework Level:** WARN to reduce noise
- **Cache Level:** DEBUG for performance monitoring
- **Database Level:** WARN to minimize connection overhead

#### 12. Management Endpoints (Monitoring)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,shutdown,caches
  endpoint:
    health:
      show-details: always
    shutdown:
      enabled: true
    caches:
      enabled: true
```

**Monitoring Features:**
- **Health Checks:** Comprehensive system health at `/actuator/health`
- **Metrics:** Performance and usage metrics
- **Cache Inspection:** Cache statistics and management
- **Graceful Shutdown:** Remote shutdown capability

#### 13. Cache Warming Configuration

```yaml
cache:
  warming:
    async: true                    # Enable async processing
    batch-size: 5                  # Process 5 combinations per batch
    delay-between-batches: 3000    # 3 seconds between batches
    max-duration-minutes: 20       # Maximum 20 minutes safety limit
```

**Cache Warming Strategy:**
- **Async Processing:** Prevents HTTP 502 errors during warming
- **Batch Processing:** Reduces system load
- **Rate Limiting:** Delays between batches for stability
- **Safety Timeout:** Maximum duration to prevent runaway processes

---

## Environment Variables Reference

### Required Variables

```bash
# Database Configuration (Supabase)
SPRING_DATASOURCE_URL=postgresql://user:password@host:port/database
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Admin Authentication
ADMIN_USERNAME=admin
ADMIN_PASSWORD_HASH=$2a$10$your_bcrypt_hashed_password

# External APIs
OPENWEATHER_API_KEY=your_openweather_pro_api_key
```

### Optional Variables (with defaults)

```bash
# ML Service Configuration
ML_PREDICT_URL=http://ml-service:8000/predict_batch  # Default for Kubernetes

# Server Configuration
SERVER_PORT=8080  # Default port

# Logging Levels
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=INFO
```

### Environment Variable Examples

#### Development Environment
```bash
# Development database (local or dev Supabase)
export SPRING_DATASOURCE_URL="postgresql://localhost:5432/creative_space_finder_dev"
export SPRING_DATASOURCE_USERNAME="dev_user"
export SPRING_DATASOURCE_PASSWORD="dev_password"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"

# Development admin (weaker password acceptable)
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD_HASH='$2a$10$dev_hashed_password'

# Development APIs (may use test endpoints)
export OPENWEATHER_API_KEY="dev_api_key"
export ML_PREDICT_URL="http://localhost:8080/predict_batch"
```

#### Production Environment
```bash
# Production database (Supabase production)
export SPRING_DATASOURCE_URL="postgresql://user:pass@db.xxx.supabase.co:5432/postgres"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="secure_production_password"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"

# Production admin (strong BCrypt hash)
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD_HASH='$2a$10$strong_production_hash'

# Production APIs
export OPENWEATHER_API_KEY="production_api_key"
export ML_PREDICT_URL="http://production-ml-service:8000/predict_batch"
```

---

## Configuration Customization

### Profile-Specific Configuration

While the current setup uses a single `application.yaml`, you can create environment-specific configurations:

```yaml
# application-dev.yaml (for development)
spring:
  jpa:
    show-sql: true                # Enable SQL logging in development
    hibernate:
      ddl-auto: update           # Allow schema updates in development

logging:
  level:
    com.creativespacefinder.manhattan: DEBUG  # Verbose logging for development
```

```yaml
# application-prod.yaml (for production)
spring:
  jpa:
    show-sql: false              # Disable SQL logging in production
    hibernate:
      ddl-auto: validate         # Only validate schema in production

logging:
  level:
    com.creativespacefinder.manhattan: WARN   # Minimal logging for production
```

### Activating Profiles

```bash
# Set active profile via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Or via command line
java -jar app.jar --spring.profiles.active=prod
```

### Local Development Overrides

Create `application-local.yaml` for local development (add to `.gitignore`):

```yaml
# application-local.yaml (not committed to source control)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/local_db
    username: local_user
    password: local_password

admin:
  username: local_admin
  password:
    hash: $2a$10$local_hash

openweather:
  api-key: local_api_key
```

---

## Security Considerations

### Sensitive Data Protection

1. **Never Commit Secrets:** All sensitive values use environment variables
2. **BCrypt Passwords:** Admin passwords stored as BCrypt hashes
3. **HTTPS Cookies:** Set `server.servlet.session.cookie.secure: true` with HTTPS
4. **Session Security:** HTTP-only cookies prevent XSS attacks

### Environment Variable Security

```bash
# Use secrets management in production
# Examples for different platforms:

# Railway
railway vars set ADMIN_PASSWORD_HASH='$2a$10$hash'

# Docker
docker run -e ADMIN_PASSWORD_HASH='$2a$10$hash' app

# Kubernetes
kubectl create secret generic app-secrets \
  --from-literal=admin-password-hash='$2a$10$hash'
```

### BCrypt Password Generation

```bash
# Generate BCrypt hash for admin password
python3 -c "
import bcrypt
password = 'your_admin_password'
hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt(rounds=10))
print(hashed.decode('utf-8'))
"
```

---

## Monitoring and Observability

### Health Check Endpoints

```bash
# Comprehensive health check
curl http://localhost:8080/actuator/health

# Cache statistics
curl http://localhost:8080/actuator/caches

# Application metrics
curl http://localhost:8080/actuator/metrics
```

### Log Monitoring

Key log patterns to monitor:

```bash
# Connection pool warnings
grep "WARNING.*HikariPool" application.log

# Cache performance
grep "Cache.*hit\|miss" application.log

# External API failures
grep "ApiException\|API.*failed" application.log

# Admin authentication
grep "Login.*successful\|failed" application.log
```

### Performance Metrics

Monitor these configuration-related metrics:

- **Connection Pool:** Active/idle/total connections
- **Cache Performance:** Hit/miss rates, eviction counts
- **Session Activity:** Active sessions, authentication attempts
- **External APIs:** Response times, failure rates

---

## Troubleshooting

### Common Configuration Issues

#### Database Connection Problems
```yaml
# Issue: Connection timeouts
# Solution: Adjust timeout values
spring:
  datasource:
    hikari:
      connection-timeout: 20000    # Increase if needed
      validation-timeout: 5000     # Increase if needed
```

#### Cache Performance Issues
```yaml
# Issue: Low cache hit rates
# Solution: Monitor and adjust cache warming
cache:
  warming:
    batch-size: 3                  # Reduce batch size
    delay-between-batches: 5000    # Increase delay
```

#### External API Timeouts
```yaml
# Issue: ML service timeouts
# Solution: Increase timeout values
ml:
  predict:
    timeout:
      connect: 60000               # Increase connect timeout
      read: 180000                 # Increase read timeout (3 minutes)
```

### Configuration Validation

```bash
# Check configuration loading
java -jar app.jar --debug

# Validate YAML syntax
python3 -c "import yaml; yaml.safe_load(open('application.yaml'))"

# Test environment variable resolution
docker run --rm -e SPRING_DATASOURCE_URL=test app:latest \
  /bin/sh -c 'echo $SPRING_DATASOURCE_URL'
```

---

## Best Practices

### Configuration Management

1. **Environment Variables:** Use for all environment-specific values
2. **Defaults:** Provide sensible defaults where possible
3. **Validation:** Validate configuration on startup
4. **Documentation:** Keep this README updated with configuration changes

### Security Practices

1. **Secrets Rotation:** Regularly rotate API keys and passwords
2. **Least Privilege:** Minimize database permissions
3. **Monitoring:** Log authentication attempts and failures
4. **Backup:** Secure backup of configuration templates

### Performance Optimization

1. **Connection Pooling:** Monitor and tune HikariCP settings
2. **Cache Configuration:** Adjust cache sizes based on usage patterns
3. **Timeout Values:** Set appropriate timeouts for external services
4. **Logging Levels:** Use appropriate log levels for production

### Deployment Considerations

1. **Profile Management:** Use profiles for different environments
2. **Configuration Externalization:** Keep sensitive data in environment variables
3. **Health Checks:** Configure monitoring for all external dependencies
4. **Graceful Shutdown:** Ensure proper cleanup during deployments

---

## Summary

The `application.yaml` file in the resources directory serves as the central configuration hub for the Creative Space Finder application. It demonstrates enterprise-grade configuration management with:

**Key Features:**
- **Supabase-Optimized Connection Management:** Ultra-conservative settings for free tier compatibility
- **Comprehensive Security:** BCrypt passwords, HTTP-only cookies, environment variable externalization
- **Performance Tuning:** Optimized connection pools, caching, and async processing
- **Operational Excellence:** Health monitoring, graceful shutdown, comprehensive logging

**Production Readiness:**
- **Environment Variable Integration:** All sensitive data externalized
- **Monitoring Support:** Management endpoints for health checks and metrics
- **Graceful Degradation:** Timeout configurations and fallback strategies
- **Security Hardening:** Session management and authentication controls

This configuration file enables the application to run efficiently in production while maintaining excellent observability and operational characteristics.