# tests/fixtures/mock_responses.py
"""
Mock responses for API testing
"""

# Health check responses
HEALTH_RESPONSE_SUCCESS = {
    "status": "healthy",
    "model_loaded": True,
    "version": "3.0",
    "timestamp": "2025-01-19T10:30:00.000Z"
}

HEALTH_RESPONSE_FAILURE = {
    "detail": "Service unhealthy"
}

# Prediction responses
PREDICTION_RESPONSE_SINGLE = [
    {
        "muse_score": None,
        "estimated_crowd_number": 25,
        "crowd_score": 3.5,
        "creative_activity_score": 7.8
    }
]

PREDICTION_RESPONSE_BATCH = [
    {
        "muse_score": None,
        "estimated_crowd_number": 25,
        "crowd_score": 3.5,
        "creative_activity_score": 7.8
    },
    {
        "muse_score": None,
        "estimated_crowd_number": 30,
        "crowd_score": 4.2,
        "creative_activity_score": 8.1
    },
    {
        "muse_score": None,
        "estimated_crowd_number": 15,
        "crowd_score": 2.8,
        "creative_activity_score": 6.9
    }
]

# Error responses
ERROR_RESPONSES = {
    "validation_error": {
        "error": "Validation Error",
        "detail": "Hour must be between 0 and 23"
    },
    "empty_request": {
        "error": "Validation Error",
        "detail": "Request list cannot be empty"
    },
    "model_error": {
        "error": "Internal Server Error", 
        "detail": "Prediction failed: Model prediction failed"
    },
    "unsupported_media_type": {
        "error": "Unsupported Media Type",
        "detail": "Content-Type must be application/json"
    },
    "not_found": {
        "error": "Not Found",
        "detail": "Path /invalid_endpoint not found"
    },
    "internal_server_error": {
        "error": "Internal Server Error",
        "detail": "An unexpected error occurred"
    }
}

# Java service mock responses (for integration tests)
JAVA_SERVICE_RESPONSES = {
    "service_health": {
        "status": "UP",
        "components": {
            "ml-service": {
                "status": "UP",
                "details": {
                    "url": "http://localhost:8000",
                    "responseTime": "50ms"
                }
            }
        }
    },
    "muse_score_calculation": {
        "muse_score": 8.2,
        "calculation_details": {
            "crowd_factor": 0.3,
            "activity_factor": 0.7,
            "location_factor": 0.8
        }
    }
}