import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
import numpy as np
import json
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from main import app, model

class TestPredictionEndpoint:
    """Test suite for prediction endpoint functionality."""
    
    @pytest.fixture(scope="class")
    def client(self):
        return TestClient(app)
    
    @pytest.fixture
    def sample_single_request(self):
        return [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography"
        }]
    
    @pytest.fixture
    def sample_batch_request(self):
        return [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            },
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
    
    def test_predict_batch_success_single_request(self, client, sample_single_request):
        """Test successful prediction with single request."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8]])

            response = client.post("/predict_batch", json=sample_single_request)

            assert response.status_code == 200
            data = response.json()

            assert len(data) == 1
            assert data[0]["muse_score"] is None
            assert data[0]["estimated_crowd_number"] == 25
            assert data[0]["crowd_score"] == 3.5
            assert data[0]["creative_activity_score"] == 7.8

            mock_predict.assert_called_once()
    
    def test_predict_batch_success_multiple_requests(self, client, sample_batch_request):
        """Test successful prediction with multiple requests."""
        mock_predictions = np.array([
            [25.0, 3.5, 7.8],
            [30.0, 5.2, 6.9],
            [15.0, 2.1, 8.5]
        ])

        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = mock_predictions

            response = client.post("/predict_batch", json=sample_batch_request)

            assert response.status_code == 200
            data = response.json()

            assert len(data) == 3

            # Verify first prediction
            assert data[0]["estimated_crowd_number"] == 25
            assert data[0]["crowd_score"] == 3.5
            assert data[0]["creative_activity_score"] == 7.8

            # Verify second prediction
            assert data[1]["estimated_crowd_number"] == 30
            assert data[1]["crowd_score"] == 5.2
            assert data[1]["creative_activity_score"] == 6.9

            # Verify third prediction
            assert data[2]["estimated_crowd_number"] == 15
            assert data[2]["crowd_score"] == 2.1
            assert data[2]["creative_activity_score"] == 8.5
    
    def test_predict_batch_empty_request(self, client):
        """Test prediction with empty request array."""
        response = client.post("/predict_batch", json=[])

        assert response.status_code == 200
        data = response.json()
        assert data == []
    
    def test_predict_batch_invalid_json(self, client):
        """Test prediction with invalid JSON."""
        invalid_json = '{"invalid": "json", "missing": "bracket"'
        
        response = client.post("/predict_batch", 
                             content=invalid_json,
                             headers={"Content-Type": "application/json"})
        
        assert response.status_code == 422
    
    def test_predict_batch_missing_required_fields(self, client):
        """Test prediction with missing required fields."""
        incomplete_requests = [
            # Missing latitude
            [{
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }],
            # Missing cultural_activity_prefered
            [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18
            }]
        ]

        for request in incomplete_requests:
            response = client.post("/predict_batch", json=request)
            assert response.status_code == 422
    
    def test_predict_batch_invalid_data_types(self, client):
        """Test prediction with invalid data types."""
        invalid_requests = [
            # String latitude
            [{
                "latitude": "invalid",
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }],
            # Negative hour
            [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": -1,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }]
        ]

        for request in invalid_requests:
            response = client.post("/predict_batch", json=request)
            assert response.status_code == 422
    
    def test_predict_batch_model_exception(self, client, sample_single_request):
        """Test handling of model prediction exceptions."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.side_effect = Exception("Model prediction failed")

            response = client.post("/predict_batch", json=sample_single_request)

            assert response.status_code == 500
            error_data = response.json()
            assert "error" in error_data or "detail" in error_data
    
    def test_predict_batch_response_formatting(self, client, sample_single_request):
        """Test that responses are correctly formatted."""
        with patch('main.model.predict') as mock_predict:
            # Test with values that need rounding
            mock_predict.return_value = np.array([[25.7, 3.567, 7.834]])

            response = client.post("/predict_batch", json=sample_single_request)

            assert response.status_code == 200
            data = response.json()

            # Verify rounding and type conversion
            assert data[0]["estimated_crowd_number"] == 26  # Rounded to int
            assert abs(data[0]["crowd_score"] - 3.567) < 0.001  # Float preserved
            assert abs(data[0]["creative_activity_score"] - 7.834) < 0.001
    
    def test_predict_batch_dataframe_structure(self, client, sample_batch_request):
        """Test that DataFrame is correctly structured for model."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 5.2, 6.9], [15.0, 2.1, 8.5]])

            response = client.post("/predict_batch", json=sample_batch_request)

            assert response.status_code == 200

            # Verify the DataFrame structure passed to model
            call_args = mock_predict.call_args[0][0]
            expected_columns = ["Latitude", "Longitude", "Hour", "Month", "Day", "Cultural_activity_prefered"]
            assert list(call_args.columns) == expected_columns
            assert len(call_args) == 3
    
    def test_predict_batch_edge_case_values(self, client):
        """Test handling of edge case values."""
        edge_case_request = [
            {
                "latitude": 0.0,
                "longitude": 0.0,
                "hour": 0,
                "month": 1,
                "day": 1,
                "cultural_activity_prefered": "Test Activity"
            }
        ]

        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[0.0, 0.0, 0.0]])

            response = client.post("/predict_batch", json=edge_case_request)

            assert response.status_code == 200
            data = response.json()

            assert data[0]["estimated_crowd_number"] == 0
            assert data[0]["crowd_score"] == 0.0
            assert data[0]["creative_activity_score"] == 0.0
    
    def test_predict_batch_large_batch(self, client):
        """Test handling of large batch requests."""
        # Create a large batch of requests
        large_batch = []
        for i in range(100):
            large_batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": (i % 12) + 1,
                "day": (i % 28) + 1,
                "cultural_activity_prefered": f"Activity {i}"
            })

        response = client.post("/predict_batch", json=large_batch)

        assert response.status_code == 200
        data = response.json()
        assert len(data) == 100

        # Verify all predictions have the required structure
        for prediction in data:
            assert "muse_score" in prediction
            assert "estimated_crowd_number" in prediction
            assert "crowd_score" in prediction
            assert "creative_activity_score" in prediction
    
    def test_predict_batch_special_characters_in_activity(self, client):
        """Test handling of special characters in activity names."""
        special_char_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Photography & Art 🎨📸"
            }
        ]

        response = client.post("/predict_batch", json=special_char_request)

        assert response.status_code == 200
        data = response.json()
        assert len(data) == 1
    
    def test_predict_batch_concurrent_requests(self, client, sample_single_request):
        """Test handling of concurrent requests."""
        import threading
        import queue

        results = queue.Queue()

        def make_request():
            response = client.post("/predict_batch", json=sample_single_request)
            results.put(response.status_code)

        # Create multiple threads
        threads = []
        for _ in range(5):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()

        # Wait for all threads to complete
        for thread in threads:
            thread.join()

        # All should succeed
        success_count = 0
        while not results.empty():
            status_code = results.get()
            if status_code == 200:
                success_count += 1

        assert success_count == 5
    
    def test_predict_batch_response_headers(self, client, sample_single_request):
        """Test that response headers are correctly set."""
        response = client.post("/predict_batch", json=sample_single_request)

        assert response.status_code == 200
        assert response.headers["content-type"] == "application/json"
    
    def test_predict_batch_request_validation_order(self, client):
        """Test that request validation happens in the correct order."""
        # Test that data type validation happens before business logic validation
        invalid_request = [
            {
                "latitude": "not_a_number",  # This should fail first
                "longitude": -73.9851,
                "hour": 25,  # This would also fail but shouldn't be reached
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]

        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
    
    def test_predict_batch_performance_timing(self, client, sample_single_request):
        """Test that prediction response time is reasonable."""
        import time

        start_time = time.time()
        response = client.post("/predict_batch", json=sample_single_request)
        end_time = time.time()

        assert response.status_code == 200
        response_time = (end_time - start_time) * 1000  # Convert to milliseconds

        # Response should be under 1 second for single request
        assert response_time < 1000