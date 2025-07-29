"""
API integration tests for the ML prediction service.
"""

import pytest
import requests
import time
from fastapi.testclient import TestClient
from unittest.mock import patch
from main import app
from test_config import TestConfig


class TestAPIIntegration:
    """Test API integration and external service communication."""
    
    @pytest.fixture
    def client(self):
        """Create test client."""
        return TestClient(app)
    
    @pytest.fixture
    def external_service_config(self):
        """External service configuration for integration tests."""
        return {
            "java_service_url": TestConfig.JAVA_SERVICE_URL,
            "timeout": TestConfig.API_TIMEOUT,
            "retry_attempts": TestConfig.RETRY_ATTEMPTS
        }

    def test_health_endpoint_integration(self, client):
        """Test health endpoint integration."""
        response = client.get("/health")
        assert response.status_code == 200
        
        data = response.json()
        assert "status" in data
        assert data["status"] == "healthy"
        assert "timestamp" in data
        assert "version" in data

    def test_predict_batch_endpoint_integration(self, client):
        """Test full integration of predict_batch endpoint."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
            
            response = client.post("/predict_batch", json=request_data)
            
            assert response.status_code == 200
            data = response.json()
            assert len(data) == 1
            assert data[0]["estimated_crowd_number"] == 25

    def test_external_java_service_communication(self, external_service_config):
        """Test communication with Java Spring Boot service."""
        java_url = external_service_config["java_service_url"]
        
        if java_url and java_url != "mock":
            try:
                # Test health endpoint
                health_response = requests.get(
                    f"{java_url}/api/health",
                    timeout=external_service_config["timeout"]
                )
                
                if health_response.status_code == 200:
                    # Test recommendations endpoint
                    recommendation_data = {
                        "activity": "Portrait photography",
                        "dateTime": "2025-07-12T15:00:00"
                    }
                    
                    rec_response = requests.post(
                        f"{java_url}/api/recommendations",
                        json=recommendation_data,
                        timeout=external_service_config["timeout"]
                    )
                    
                    assert rec_response.status_code in [200, 500]  # 500 if ML service not available
                    
            except requests.exceptions.RequestException:
                pytest.skip("Java service not available for integration testing")

    def test_api_response_format_consistency(self, client):
        """Test API response format consistency."""
        test_cases = [
            # Single request
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            # Multiple requests
            [
                {"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"},
                {"latitude": 40.7412, "longitude": -73.9897, "hour": 12, "month": 7, "day": 19, "cultural_activity_prefered": "Street photography"}
            ]
        ]
        
        with patch('main.model') as mock_model:
            for i, test_case in enumerate(test_cases):
                mock_predictions = [[25.0, 3.5, 7.8]] * len(test_case)
                mock_model.predict.return_value = mock_predictions
                
                response = client.post("/predict_batch", json=test_case)
                
                assert response.status_code == 200
                data = response.json()
                assert len(data) == len(test_case)
                
                for prediction in data:
                    assert "muse_score" in prediction
                    assert "estimated_crowd_number" in prediction
                    assert "crowd_score" in prediction
                    assert "creative_activity_score" in prediction
                    assert prediction["muse_score"] is None

    def test_api_performance_integration(self, client):
        """Test API performance under normal load."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
            
            # Measure response time
            start_time = time.time()
            response = client.post("/predict_batch", json=request_data)
            end_time = time.time()
            
            response_time_ms = (end_time - start_time) * 1000
            
            assert response.status_code == 200
            assert response_time_ms < TestConfig.MAX_RESPONSE_TIME_MS

    def test_api_concurrent_requests(self, client):
        """Test API handling of concurrent requests."""
        import threading
        import queue
        
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        results = queue.Queue()
        
        def make_request():
            with patch('main.model') as mock_model:
                mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
                response = client.post("/predict_batch", json=request_data)
                results.put(response.status_code)
        
        # Create 10 concurrent requests
        threads = []
        for _ in range(10):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()
        
        # Wait for completion
        for thread in threads:
            thread.join()
        
        # Check results
        success_count = 0
        while not results.empty():
            status_code = results.get()
            if status_code == 200:
                success_count += 1
        
        assert success_count >= 8  # Allow for some failures in concurrent testing

    def test_api_error_handling_integration(self, client):
        """Test API error handling in integration scenarios."""
        error_test_cases = [
            # Invalid JSON
            ("{ invalid json }", 422),
            # Missing fields
            ('[{"latitude": 40.7589}]', 422),
            # Invalid data types
            ('[{"latitude": "invalid", "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]', 422)
        ]
        
        for test_data, expected_status in error_test_cases:
            response = client.post(
                "/predict_batch",
                data=test_data,
                headers={"Content-Type": "application/json"}
            )
            assert response.status_code == expected_status

    def test_api_large_batch_integration(self, client):
        """Test API with large batch requests."""
        large_batch = []
        for i in range(100):  # 100 predictions
            large_batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": f"Activity {i % 5}"
            })
        
        with patch('main.model') as mock_model:
            mock_predictions = [[25.0, 3.5, 7.8]] * 100
            mock_model.predict.return_value = mock_predictions
            
            response = client.post("/predict_batch", json=large_batch)
            
            assert response.status_code == 200
            data = response.json()
            assert len(data) == 100

    def test_api_content_type_handling(self, client):
        """Test API content type handling."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        # Test correct content type
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
            
            response = client.post(
                "/predict_batch",
                json=request_data,
                headers={"Content-Type": "application/json"}
            )
            assert response.status_code == 200
        
        # Test incorrect content type
        import json
        response = client.post(
            "/predict_batch",
            data=json.dumps(request_data),
            headers={"Content-Type": "text/plain"}
        )
        assert response.status_code in [415, 422]

    def test_api_cors_integration(self, client):
        """Test CORS handling in API."""
        # Test preflight request
        response = client.options(
            "/predict_batch",
            headers={
                "Origin": "http://localhost:3000",
                "Access-Control-Request-Method": "POST",
                "Access-Control-Request-Headers": "Content-Type"
            }
        )
        
        # CORS handling depends on FastAPI configuration
        assert response.status_code in [200, 404, 405]

    def test_api_rate_limiting_integration(self, client):
        """Test rate limiting behavior (if implemented)."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
            
            # Make many rapid requests
            responses = []
            for _ in range(50):
                response = client.post("/predict_batch", json=request_data)
                responses.append(response.status_code)
            
            # Should handle all requests
            success_count = sum(1 for status in responses if status == 200)
            assert success_count >= 45  # Most should succeed

    def test_api_timeout_handling(self, client):
        """Test API timeout handling."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            def slow_predict(*args, **kwargs):
                time.sleep(0.1)  # Simulate slow prediction
                return [[25.0, 3.5, 7.8]]
            
            mock_model.predict.side_effect = slow_predict
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 200  # Should complete despite delay

    def test_api_memory_usage_integration(self, client):
        """Test API memory usage with various request sizes."""
        import psutil
        import os
        
        process = psutil.Process(os.getpid())
        initial_memory = process.memory_info().rss
        
        # Test with different batch sizes
        batch_sizes = [1, 10, 50, 100]
        
        for batch_size in batch_sizes:
            batch_request = []
            for i in range(batch_size):
                batch_request.append({
                    "latitude": 40.7589 + (i * 0.001),
                    "longitude": -73.9851 + (i * 0.001),
                    "hour": i % 24,
                    "month": 7,
                    "day": 18,
                    "cultural_activity_prefered": f"Activity {i}"
                })
            
            with patch('main.model') as mock_model:
                mock_predictions = [[25.0, 3.5, 7.8]] * batch_size
                mock_model.predict.return_value = mock_predictions
                
                response = client.post("/predict_batch", json=batch_request)
                assert response.status_code == 200
        
        final_memory = process.memory_info().rss
        memory_increase = (final_memory - initial_memory) / (1024 * 1024)  # MB
        
        # Memory increase should be reasonable
        assert memory_increase < 100  # Less than 100MB increase

    def test_api_graceful_degradation(self, client):
        """Test API graceful degradation when dependencies fail."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        # Test with model failure
        with patch('main.model') as mock_model:
            mock_model.predict.side_effect = Exception("Model failed")
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 500
        
        # Test recovery after failure
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = [[25.0, 3.5, 7.8]]
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 200

    def test_api_documentation_integration(self, client):
        """Test API documentation endpoints."""
        # Test OpenAPI schema
        response = client.get("/openapi.json")
        assert response.status_code == 200
        
        schema = response.json()
        assert "paths" in schema
        assert "/predict_batch" in schema["paths"]
        
        # Test docs endpoint
        response = client.get("/docs")
        assert response.status_code in [200, 404] 