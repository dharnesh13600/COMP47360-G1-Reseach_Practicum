# tests/conftest.py
import sys
import os
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch

# Add the parent directory to the Python path so we can import main
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Now we can import main
import main
from main import app, model

@pytest.fixture(scope="session")
def client():
    """Create a test client for the FastAPI app"""
    return TestClient(app)

@pytest.fixture(scope="session")
def sample_request():
    """Provide sample request data"""
    return {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    }

@pytest.fixture(scope="session")
def sample_single_request(sample_request):
    """Provide sample single request as array"""
    return [sample_request]

@pytest.fixture(scope="session")
def sample_batch_request(sample_request):
    """Provide sample batch request"""
    return [
        sample_request,
        {
            "latitude": 40.7412,
            "longitude": -73.9897,
            "hour": 12,
            "month": 7,
            "day": 19,
            "cultural_activity_prefered": "Street photography"
        },
        {
            "latitude": 40.7505,
            "longitude": -73.9934,
            "hour": 18,
            "month": 7,
            "day": 20,
            "cultural_activity_prefered": "Landscape painting"
        }
    ]

@pytest.fixture(scope="session")
def invalid_request():
    """Provide invalid request data"""
    return {
        "latitude": 91.0,  # Invalid latitude
        "longitude": -73.9851,
        "hour": 25,  # Invalid hour
        "month": 13,  # Invalid month
        "day": 32,  # Invalid day
        "cultural_activity_prefered": ""  # Empty activity
    }

@pytest.fixture(scope="session")
def valid_request(sample_request):
    """Provide valid request as array"""
    return [sample_request]

@pytest.fixture(scope="function")
def external_service_config():
    """Configuration for external service tests"""
    return {
        "java_service_url": "http://localhost:8080",
        "timeout": 30,
        "retries": 3
    }

# CRITICAL FIX: Reset mocks after each test to prevent interference
@pytest.fixture(autouse=True)
def reset_mocks():
    """Reset all mocks after each test to prevent interference between tests."""
    yield  # This allows the test to run
    patch.stopall()  # This cleans up all active mocks after the test

# Pytest configuration
def pytest_configure(config):
    """Configure pytest with custom markers"""
    config.addinivalue_line(
        "markers", "performance: mark test as a performance test"
    )
    config.addinivalue_line(
        "markers", "slow: mark test as slow running"
    )
    config.addinivalue_line(
        "markers", "integration: mark test as an integration test"
    )

def pytest_collection_modifyitems(config, items):
    """Modify test collection to add markers"""
    for item in items:
        # Add integration marker to integration tests
        if "integration" in str(item.fspath):
            item.add_marker(pytest.mark.integration)
        
        # Add performance marker to performance tests
        if "performance" in item.name or "load" in item.name:
            item.add_marker(pytest.mark.performance)