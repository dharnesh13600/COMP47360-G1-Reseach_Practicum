class TestConfig:
    BASE_URL = "http://localhost:8000"
    TIMEOUT = 30
    MAX_BATCH_SIZE = 100
    PREDICT_ENDPOINT = "/predict_batch"
    HEALTH_ENDPOINT = "/health"
    TEST_ENV = "development"
    
    # Missing attributes that caused the failures:
    MAX_RESPONSE_TIME_MS = 1000
    JAVA_SERVICE_URL = "http://localhost:8080"
    
    SAMPLE_REQUEST = {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    }