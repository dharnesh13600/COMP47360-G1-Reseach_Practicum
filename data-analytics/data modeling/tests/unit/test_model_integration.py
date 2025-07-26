# tests/unit/test_model_integration.py
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
import numpy as np
import pandas as pd
import sys
import os

# Add the parent directory to the Python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from main import app, model

class TestModelIntegration:
    """Test suite for model integration."""
    
    @pytest.fixture(scope="class")
    def client(self):
        return TestClient(app)
    
    @pytest.fixture
    def sample_request(self):
        return [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography"
        }]
    
    def test_model_loading_and_initialization(self, client):
        """Test that model is properly loaded and initialized."""
        # Test that model exists
        assert model is not None
        
        # Test that model has predict method
        assert hasattr(model, 'predict')
        
        # Test basic model functionality with real prediction
        sample_data = pd.DataFrame({
            "Latitude": [40.7589],
            "Longitude": [-73.9851],
            "Hour": [15],
            "Month": [7],
            "Day": [18],
            "Cultural_activity_prefered": ["Portrait photography"]
        })
        
        predictions = model.predict(sample_data)
        assert predictions is not None
        assert len(predictions) > 0
    
    def test_model_predict_method_call(self, client, sample_request):
        """Test that model.predict is called with correct parameters."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8]])

            response = client.post("/predict_batch", json=sample_request)

            assert response.status_code == 200
            mock_predict.assert_called_once()
    
    def test_model_input_dataframe_structure(self, client, sample_request):
        """Test that input DataFrame has the correct structure for the model."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8]])

            response = client.post("/predict_batch", json=sample_request)

            call_args = mock_predict.call_args[0][0]
            
            # Check DataFrame structure
            assert isinstance(call_args, pd.DataFrame)
            expected_columns = ["Latitude", "Longitude", "Hour", "Month", "Day", "Cultural_activity_prefered"]
            assert list(call_args.columns) == expected_columns
            
            # Check data types
            assert call_args["Latitude"].dtype in [np.float64, np.float32]
            assert call_args["Longitude"].dtype in [np.float64, np.float32]
            assert call_args["Hour"].dtype in [np.int64, np.int32]
            assert call_args["Month"].dtype in [np.int64, np.int32]
            assert call_args["Day"].dtype in [np.int64, np.int32]
            assert call_args["Cultural_activity_prefered"].dtype == object
    
    def test_model_prediction_output_format(self, client, sample_request):
        """Test that model predictions are in the correct format."""
        response = client.post("/predict_batch", json=sample_request)
        
        assert response.status_code == 200
        data = response.json()
        
        assert isinstance(data, list)
        assert len(data) == 1
        
        prediction = data[0]
        assert "muse_score" in prediction
        assert "estimated_crowd_number" in prediction
        assert "crowd_score" in prediction
        assert "creative_activity_score" in prediction
        
        assert isinstance(prediction["estimated_crowd_number"], int)
        assert isinstance(prediction["crowd_score"], (int, float))
        assert isinstance(prediction["creative_activity_score"], (int, float))
    
    def test_model_batch_prediction(self, client):
        """Test model handling of batch predictions."""
        batch_request = [
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

        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([
                [25.0, 3.5, 7.8],
                [30.0, 4.2, 8.1],
                [15.0, 2.8, 6.9]
            ])

            response = client.post("/predict_batch", json=batch_request)

            assert response.status_code == 200
            data = response.json()
            assert len(data) == 3

            # Verify each prediction
            assert data[0]["estimated_crowd_number"] == 25
            assert data[1]["estimated_crowd_number"] == 30
            assert data[2]["estimated_crowd_number"] == 15
    
    def test_model_feature_engineering(self, client, sample_request):
        """Test that features are properly engineered for the model."""
        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8]])

            response = client.post("/predict_batch", json=sample_request)

            call_args = mock_predict.call_args[0][0]
            
            # Test that coordinate precision is maintained
            assert abs(call_args.iloc[0]["Latitude"] - 40.7589) < 0.0001
            assert abs(call_args.iloc[0]["Longitude"] - (-73.9851)) < 0.0001
            
            # Test that time fields are integers
            assert call_args.iloc[0]["Hour"] == 15
            assert call_args.iloc[0]["Month"] == 7
            assert call_args.iloc[0]["Day"] == 18
            
            # Test that activity is preserved
            assert call_args.iloc[0]["Cultural_activity_prefered"] == "Portrait photography"
    
    def test_model_prediction_consistency(self, client, sample_request):
        """Test that model predictions are consistent for the same input."""
        # Make multiple requests with same data
        responses = []
        for _ in range(3):
            response = client.post("/predict_batch", json=sample_request)
            assert response.status_code == 200
            responses.append(response.json())
        
        # All responses should be identical (deterministic model)
        for i in range(1, len(responses)):
            assert responses[i] == responses[0]
    
    def test_model_handles_different_activities(self, client):
        """Test that model handles different activity types."""
        activities = [
            "Portrait photography",
            "Street photography",
            "Landscape painting",
            "Performance art",
            "Filmmaking",
            "Dancing",
            "Portrait painting",
            "Digital art"
        ]

        requests = []
        for activity in activities:
            requests.append({
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": activity
            })

        with patch('main.model.predict') as mock_predict:
            mock_predictions = np.random.rand(len(activities), 3) * 10
            mock_predict.return_value = mock_predictions

            response = client.post("/predict_batch", json=requests)

            assert response.status_code == 200
            data = response.json()
            assert len(data) == len(activities)

            # Check that all activities were processed
            call_args = mock_predict.call_args[0][0]
            assert len(call_args) == len(activities)
            
            for i, activity in enumerate(activities):
                assert call_args.iloc[i]["Cultural_activity_prefered"] == activity
    
    def test_model_handles_coordinate_variations(self, client):
        """Test that model handles different coordinate ranges."""
        coordinate_requests = [
            {"latitude": -90.0, "longitude": -180.0},    # Minimum values
            {"latitude": 90.0, "longitude": 180.0},      # Maximum values
            {"latitude": 0.0, "longitude": 0.0},         # Zero values
            {"latitude": 40.7589, "longitude": -73.9851}, # NYC coordinates
            {"latitude": 51.5074, "longitude": -0.1278}   # London coordinates
        ]

        requests = []
        for coords in coordinate_requests:
            requests.append({
                "latitude": coords["latitude"],
                "longitude": coords["longitude"],
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Test activity"
            })

        response = client.post("/predict_batch", json=requests)
        assert response.status_code == 200
        data = response.json()
        assert len(data) == len(coordinate_requests)
    
    def test_model_handles_time_variations(self, client):
        """Test that model handles different time values."""
        time_requests = [
            {"hour": 0, "month": 1, "day": 1},      # Minimum values
            {"hour": 23, "month": 12, "day": 31},   # Maximum values
            {"hour": 12, "month": 6, "day": 15},    # Middle values
            {"hour": 15, "month": 7, "day": 18},    # Test values
            {"hour": 9, "month": 3, "day": 22}      # Random values
        ]

        requests = []
        for time_vals in time_requests:
            requests.append({
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": time_vals["hour"],
                "month": time_vals["month"],
                "day": time_vals["day"],
                "cultural_activity_prefered": "Test activity"
            })

        response = client.post("/predict_batch", json=requests)
        assert response.status_code == 200
        data = response.json()
        assert len(data) == len(time_requests)
    
    def test_model_prediction_value_ranges(self, client, sample_request):
        """Test that model predictions are within expected ranges."""
        response = client.post("/predict_batch", json=sample_request)
        
        assert response.status_code == 200
        data = response.json()
        
        prediction = data[0]
        
        # Test value ranges
        assert prediction["estimated_crowd_number"] >= 0  # Non-negative crowd
        assert 0 <= prediction["crowd_score"] <= 10  # Score in range
        assert 0 <= prediction["creative_activity_score"] <= 10  # Score in range
    
    def test_model_error_handling(self, client, sample_request):
        """Test handling of model prediction errors."""
        with patch('main.model.predict') as mock_predict:
            # Test different types of model errors
            error_cases = [
                ValueError("Invalid input shape"),
                RuntimeError("Model prediction failed"),
                MemoryError("Insufficient memory"),
                Exception("Generic model error")
            ]

            for error in error_cases:
                mock_predict.side_effect = error

                response = client.post("/predict_batch", json=sample_request)
                assert response.status_code == 500
    
    def test_model_performance_with_large_batches(self, client):
        """Test model performance with large batch sizes."""
        # Create large batch
        large_batch_size = 1000
        large_batch = []

        for i in range(large_batch_size):
            large_batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": (i % 12) + 1,
                "day": (i % 28) + 1,
                "cultural_activity_prefered": f"Activity {i % 10}"
            })

        with patch('main.model.predict') as mock_predict:
            mock_predictions = np.random.rand(large_batch_size, 3) * 10
            mock_predict.return_value = mock_predictions

            response = client.post("/predict_batch", json=large_batch)

            assert response.status_code == 200
            data = response.json()
            assert len(data) == large_batch_size

            # Verify model was called once with the entire batch
            mock_predict.assert_called_once()
    
    def test_model_feature_types_consistency(self, client):
        """Test that model receives features in consistent data types."""
        mixed_type_request = [{
            "latitude": 40.7589,      # float
            "longitude": -73.9851,    # float
            "hour": 15,               # int
            "month": 7,               # int
            "day": 18,                # int
            "cultural_activity_prefered": "Portrait photography"  # string
        }]

        with patch('main.model.predict') as mock_predict:
            mock_predict.return_value = np.array([[25.0, 3.5, 7.8]])

            response = client.post("/predict_batch", json=mixed_type_request)

            call_args = mock_predict.call_args[0][0]
            
            # Verify data types are consistent
            assert call_args["Latitude"].dtype in [np.float64, np.float32]
            assert call_args["Longitude"].dtype in [np.float64, np.float32]
            assert call_args["Hour"].dtype in [np.int64, np.int32]
            assert call_args["Month"].dtype in [np.int64, np.int32]
            assert call_args["Day"].dtype in [np.int64, np.int32]
            assert call_args["Cultural_activity_prefered"].dtype == object
    
    def test_model_memory_usage_optimization(self, client):
        """Test that model doesn't consume excessive memory."""
        import psutil
        import os
        
        process = psutil.Process(os.getpid())
        memory_before = process.memory_info().rss / 1024 / 1024  # MB

        # Create moderately large batch
        batch_size = 500
        batch = []
        for i in range(batch_size):
            batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": (i % 12) + 1,
                "day": (i % 28) + 1,
                "cultural_activity_prefered": f"Activity {i % 10}"
            })

        response = client.post("/predict_batch", json=batch)
        assert response.status_code == 200

        memory_after = process.memory_info().rss / 1024 / 1024  # MB
        memory_increase = memory_after - memory_before

        # Memory increase should be reasonable (less than 100MB for 500 requests)
        assert memory_increase < 100
    
    def test_model_concurrent_predictions(self, client, sample_request):
        """Test model handling of concurrent prediction requests."""
        import threading
        import queue

        results = queue.Queue()

        def make_request():
            response = client.post("/predict_batch", json=sample_request)
            results.put(response.status_code)

        # Create multiple concurrent threads
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
    
    def test_model_prediction_determinism(self, client):
        """Test that model predictions are deterministic for identical inputs."""
        request = [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography"
        }]

        # Make multiple requests
        responses = []
        for _ in range(3):
            response = client.post("/predict_batch", json=request)
            assert response.status_code == 200
            responses.append(response.json())

        # All responses should be identical
        for response in responses[1:]:
            assert response == responses[0]
    
    def test_model_integration_with_real_scenarios(self, client):
        """Test model with realistic scenario data."""
        real_scenarios = [
            {
                "latitude": 40.7831,  # Central Park
                "longitude": -73.9712,
                "hour": 14,
                "month": 6,
                "day": 15,
                "cultural_activity_prefered": "Portrait photography"
            },
            {
                "latitude": 40.7505,  # Times Square area
                "longitude": -73.9934,
                "hour": 20,
                "month": 12,
                "day": 31,
                "cultural_activity_prefered": "Street photography"
            },
            {
                "latitude": 40.7614,  # Museum District
                "longitude": -73.9776,
                "hour": 11,
                "month": 9,
                "day": 22,
                "cultural_activity_prefered": "Performance art"
            },
            {
                "latitude": 40.7282,  # SoHo
                "longitude": -74.0776,
                "hour": 16,
                "month": 4,
                "day": 8,
                "cultural_activity_prefered": "Digital art"
            }
        ]

        response = client.post("/predict_batch", json=real_scenarios)
        assert response.status_code == 200
        
        data = response.json()
        assert len(data) == len(real_scenarios)
        
        for prediction in data:
            # All predictions should be valid
            assert prediction["estimated_crowd_number"] >= 0
            assert 0 <= prediction["crowd_score"] <= 10
            assert 0 <= prediction["creative_activity_score"] <= 10