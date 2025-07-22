# tests/fixtures/conftest.py
import pytest
import numpy as np
from unittest.mock import MagicMock

@pytest.fixture
def mock_model_predictions():
    """Mock model predictions for consistent testing"""
    return {
        'single': np.array([[25.0, 3.5, 7.8]]),
        'batch_3': np.array([
            [25.0, 3.5, 7.8],
            [30.0, 4.2, 8.1],
            [15.0, 2.8, 6.9]
        ]),
        'large_batch': np.array([[20.0 + i, 3.0 + (i * 0.1), 7.0 + (i * 0.1)] for i in range(100)]),
        'edge_case': np.array([[0.0, 0.0, 0.0]]),
        'high_values': np.array([[100.0, 10.0, 10.0]])
    }

@pytest.fixture
def mock_model_errors():
    """Mock model error scenarios for testing"""
    return {
        'none_result': None,
        'value_error': ValueError("Invalid input shape"),
        'runtime_error': RuntimeError("Model prediction failed"),
        'memory_error': MemoryError("Insufficient memory"),
        'generic_error': Exception("Generic model error")
    }

@pytest.fixture
def performance_thresholds():
    """Performance testing thresholds"""
    return {
        'max_response_time_ms': 200,
        'max_cpu_usage_percent': 200,
        'min_success_rate': 0.90,
        'max_memory_usage_mb': 512
    }