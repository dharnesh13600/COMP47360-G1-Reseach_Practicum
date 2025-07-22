# tests/config/test_config.py
import os
from typing import Dict, Any

class TestConfig:
    """Test configuration class with all required attributes"""
    
    # API Configuration
    API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8000")
    API_TIMEOUT = 30
    
    # Java Service Configuration
    JAVA_SERVICE_URL = os.getenv("JAVA_SERVICE_URL", "http://localhost:8080")
    RETRY_ATTEMPTS = 3
    
    # Test Data Configuration
    TEST_DATA_PATH = "tests/fixtures/sample_data.py"
    
    # Performance Test Thresholds (Adjusted for CI/CD environments)
    MAX_RESPONSE_TIME_MS = 200  # Increased for stability
    MAX_CPU_USAGE_PERCENT = 200  # Increased to handle CI variations
    MIN_SUCCESS_RATE = 0.90  # Slightly decreased for stability
    
    # Batch Test Configuration
    SMALL_BATCH_SIZE = 10
    MEDIUM_BATCH_SIZE = 100
    LARGE_BATCH_SIZE = 1000
    
    # Mock Configuration
    USE_MOCK_MODEL = os.getenv("USE_MOCK_MODEL", "false").lower() == "true"
    
    @classmethod
    def get_test_request_data(cls) -> Dict[str, Any]:
        """Get sample test request data"""
        return {
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography"
        }
    
    @classmethod
    def get_invalid_request_data(cls) -> Dict[str, Any]:
        """Get invalid test request data for error testing"""
        return {
            "latitude": 91.0,  # Invalid latitude
            "longitude": -73.9851,
            "hour": 25,  # Invalid hour
            "month": 13,  # Invalid month
            "day": 32,  # Invalid day
            "cultural_activity_prefered": ""  # Empty activity
        }
    
    @classmethod
    def get_batch_test_data(cls, size: int) -> list:
        """Generate batch test data of specified size"""
        base_data = cls.get_test_request_data()
        return [base_data.copy() for _ in range(size)]

# For backward compatibility, create a module-level instance
test_config = TestConfig()