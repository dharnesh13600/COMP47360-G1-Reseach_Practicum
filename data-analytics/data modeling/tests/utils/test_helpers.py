# tests/utils/test_helpers.py
"""
Helper functions for testing
"""
import time
import psutil
import numpy as np
from typing import Dict, List, Any, Callable
from unittest.mock import patch
from fastapi.testclient import TestClient

def measure_response_time(client: TestClient, method: str, url: str, **kwargs) -> float:
    """Measure API response time in milliseconds"""
    start_time = time.perf_counter()
    if method.upper() == 'GET':
        response = client.get(url, **kwargs)
    elif method.upper() == 'POST':
        response = client.post(url, **kwargs)
    else:
        raise ValueError(f"Unsupported method: {method}")
    end_time = time.perf_counter()
    
    return (end_time - start_time) * 1000  # Convert to milliseconds

def measure_cpu_usage(func: Callable) -> tuple:
    """Measure CPU usage during function execution"""
    process = psutil.Process()
    cpu_before = process.cpu_percent()
    
    # Wait a bit for initial measurement
    time.sleep(0.1)
    
    start_time = time.perf_counter()
    result = func()
    end_time = time.perf_counter()
    
    cpu_after = process.cpu_percent()
    execution_time = (end_time - start_time) * 1000  # milliseconds
    
    return result, cpu_after, execution_time

def measure_memory_usage(func: Callable) -> tuple:
    """Measure memory usage during function execution"""
    process = psutil.Process()
    memory_before = process.memory_info().rss / 1024 / 1024  # MB
    
    result = func()
    
    memory_after = process.memory_info().rss / 1024 / 1024  # MB
    memory_diff = memory_after - memory_before
    
    return result, memory_after, memory_diff

def mock_model_with_predictions(predictions: np.ndarray):
    """Create a context manager that mocks the model with specific predictions"""
    return patch('main.model.predict', return_value=predictions)

def mock_model_with_error(error: Exception):
    """Create a context manager that mocks the model to raise an error"""
    return patch('main.model.predict', side_effect=error)

def assert_prediction_response_format(response_data: List[Dict[str, Any]]):
    """Assert that prediction response has the correct format"""
    assert isinstance(response_data, list), "Response should be a list"
    
    for item in response_data:
        assert isinstance(item, dict), "Each item should be a dictionary"
        
        # Check required fields
        required_fields = ["muse_score", "estimated_crowd_number", "crowd_score", "creative_activity_score"]
        for field in required_fields:
            assert field in item, f"Missing required field: {field}"
        
        # Check field types
        assert item["muse_score"] is None or isinstance(item["muse_score"], (int, float)), \
            "muse_score should be None or number"
        assert isinstance(item["estimated_crowd_number"], int), \
            "estimated_crowd_number should be integer"
        assert isinstance(item["crowd_score"], (int, float)), \
            "crowd_score should be number"
        assert isinstance(item["creative_activity_score"], (int, float)), \
            "creative_activity_score should be number"
        
        # Check value ranges
        assert item["estimated_crowd_number"] >= 0, \
            "estimated_crowd_number should be non-negative"
        assert 0.0 <= item["crowd_score"] <= 10.0, \
            "crowd_score should be between 0 and 10"
        assert 0.0 <= item["creative_activity_score"] <= 10.0, \
            "creative_activity_score should be between 0 and 10"

def assert_error_response_format(response_data: Dict[str, Any]):
    """Assert that error response has the correct format"""
    assert isinstance(response_data, dict), "Error response should be a dictionary"
    assert "error" in response_data, "Error response should have 'error' field"
    assert "detail" in response_data, "Error response should have 'detail' field"
    assert isinstance(response_data["error"], str), "Error field should be string"
    assert isinstance(response_data["detail"], str), "Detail field should be string"

def create_concurrent_requests(client: TestClient, request_data: List[Dict], num_threads: int = 5):
    """Create concurrent requests for performance testing"""
    import threading
    import queue
    
    results = queue.Queue()
    
    def make_request():
        try:
            response = client.post("/predict_batch", json=request_data)
            results.put({
                'status_code': response.status_code,
                'response_time': 0,  # Would need to measure properly
                'success': response.status_code == 200
            })
        except Exception as e:
            results.put({
                'status_code': 500,
                'response_time': 0,
                'success': False,
                'error': str(e)
            })
    
    # Create and start threads
    threads = []
    for _ in range(num_threads):
        thread = threading.Thread(target=make_request)
        threads.append(thread)
        thread.start()
    
    # Wait for all threads to complete
    for thread in threads:
        thread.join()
    
    # Collect results
    thread_results = []
    while not results.empty():
        thread_results.append(results.get())
    
    return thread_results

def validate_dataframe_structure(df, expected_columns: List[str]):
    """Validate that DataFrame has expected structure"""
    assert list(df.columns) == expected_columns, \
        f"Expected columns {expected_columns}, got {list(df.columns)}"
    
    for col in expected_columns:
        assert col in df.columns, f"Missing column: {col}"
    
    assert len(df) > 0, "DataFrame should not be empty"

def skip_if_model_unavailable():
    """Decorator to skip tests if model file is not available"""
    import os
    import pytest
    
    def decorator(func):
        if not os.path.exists("xgboost_model.pkl"):
            return pytest.mark.skip("Model file not available")(func)
        return func
    return decorator