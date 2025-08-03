## Testing

This directory contains the unit tests for the Manhattan Muse backend application.

we have 2 layers of testing:
* **Unit tests - 124 tests**
Validates the functionality of controllers, services, repositories, DTOs, entities and excpetion handlers.
Mocks are used in place of external HTTP requests and database

* **Integration tests - 7 tests**
Boots the whole spring context, wires up an H2 in memory database, and starts a wiremock server for the

 3rd party APIs.

### Unit Testing

**Controllers**  

* `AdminControllerTest`- authenticates, warmsand clears the cache and protects the endpoints.
* `WeatherForecastControllerTest` - tests the flow of the weather forecast endpoint, 200/400/500 flows, forecast time limits and wrong methods.
* `RecommendationControllerTest` - zone and activity helpers and happy path tests and edge cases.
* `HealthControllerTest` - tests basic health checks as well as error paths.
* `HealthControllerBadRequestTest` - tests invalid paths and internal failures.

**Services** 

* `LocationRecommendationServiceTest` - tests the service's workflow, behaviourwhen there are no location match and error is raised when there is no activity.
* `LocationRecommendationServiceSimpleTests` - tests the getters for activities, dates, time and zones.
* `LocationRecommendationServiceHelpersTest` - tests the hepler methods that forward queries to the repository, hamdles the user's "quite-location" preference.
* `LocationRecommendationServiceHelperTest` - tests all the hepler methods that builds repository query ensuring zome filters are correctly applied or skipped.
* `LocationRecommendationServicePrivateHelpersTest` - tests crowd level, zone filters and distance.
* `LocationRecommendationServicePublicTest` - tests the public helper which enforces maax-distance limit and ensures result list and honours the crowd diversity rules
* `WeatherForecastServiceTest` - tests the happy path of openweather, handling of 404 errors and json errors and the default 70F when the waether API is down.
* `WeatherForecastServiceErrorTests` - tests the malformed responses and the API failures

**Repositories** - 

* `ActivityRepositoryTest`- case-insensitive finds and exists queries.
* `LocationActivityScoreRepositoryTest` - finds date or time, ML vs historical counts, eager loading and H2-limitation guard.

**DTOs** 

* `ForeccastResponseTest` - tests whether the forecast response can make a complete round trip, checks whether the hepler methods return null pointer exceptions
* `ForecastResponseUnknownPropsTest` - ignores extra fields in json.
* `LocationRecommendationResponseTest` - tests all getters,setters, constructors work as expected
* `PredictionResponseTest` - handles null muse scores,checks, checks all the variants of constructors.

**Entities**

* `ActivityTest` - Tests all the getters and setters work, checks the equals() and hashcode() methods.
* `EventLocationTest` - helper constructor + tests for basic getters and setters.
* `LocatrionActivityScoreEntityTest` - Instantiates the entity with its full-args constructor and tests the helper methods and calculates or expose the score meta data.
* `TaxiZonetest` - simple sanity tests for each and every constructor, accessor and a readable toString()
* `RequestAnalyticsTest` - tests the default values, round-trip setters and counter increment.

**Exceptions**

* `ApiExceptionTest` - ensures both constructors preserve the message and confirms the excpetion serialises with no side effects.
* `GlobalExceptionHandlerTest` - maps 8 error categories to REST json responses.

### Integration Testing

**`RecommendationControllerIT`** - it hits `/api/recommendations`, `/api/activities`, `/api/zones` , seeds the database with Flyway and uses wiremock for external calls.

