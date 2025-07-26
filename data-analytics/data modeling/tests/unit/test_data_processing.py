"""
Test module for data processing functionality.
"""

import pytest
import pandas as pd
import numpy as np
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
from main import app


class TestDataProcessing:
    """Test data processing and transformation functionality."""
    
    @pytest.fixture
    def client(self):
        """Create a test client for the FastAPI app."""
        return TestClient(app)
    
    @pytest.fixture
    def sample_request_data(self):
        """Sample request data for testing."""
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
            }
        ]

    def test_json_to_dataframe_conversion(self, client, sample_request_data):
        """Test conversion of JSON request to pandas DataFrame."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 4.0, 8.5]])
            
            response = client.post("/predict_batch", json=sample_request_data)
            assert response.status_code == 200
            
            # Verify that model.predict was called with a DataFrame
            call_args = mock_model.predict.call_args[0][0]
            assert isinstance(call_args, pd.DataFrame)

    def test_dataframe_column_mapping(self, client, sample_request_data):
        """Test that DataFrame columns are correctly mapped."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 4.0, 8.5]])
            
            response = client.post("/predict_batch", json=sample_request_data)
            
            # Check that the DataFrame has the expected columns
            call_args = mock_model.predict.call_args[0][0]
            expected_columns = ["Latitude", "Longitude", "Hour", "Month", "Day", "Cultural_activity_prefered"]
            assert list(call_args.columns) == expected_columns

    def test_dataframe_data_types(self, client, sample_request_data):
        """Test that DataFrame has correct data types."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 4.0, 8.5]])
            
            response = client.post("/predict_batch", json=sample_request_data)
            
            call_args = mock_model.predict.call_args[0][0]
            
            # Check data types
            assert pd.api.types.is_numeric_dtype(call_args["Latitude"])
            assert pd.api.types.is_numeric_dtype(call_args["Longitude"])
            assert pd.api.types.is_integer_dtype(call_args["Hour"])
            assert pd.api.types.is_integer_dtype(call_args["Month"])
            assert pd.api.types.is_integer_dtype(call_args["Day"])
            assert pd.api.types.is_object_dtype(call_args["Cultural_activity_prefered"])

    def test_dataframe_values_preservation(self, client, sample_request_data):
        """Test that DataFrame preserves the original values correctly."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 4.0, 8.5]])
            
            response = client.post("/predict_batch", json=sample_request_data)
            
            call_args = mock_model.predict.call_args[0][0]
            
            # Check first row values
            assert call_args.iloc[0]["Latitude"] == 40.7589
            assert call_args.iloc[0]["Longitude"] == -73.9851
            assert call_args.iloc[0]["Hour"] == 15
            assert call_args.iloc[0]["Month"] == 7
            assert call_args.iloc[0]["Day"] == 18
            assert call_args.iloc[0]["Cultural_activity_prefered"] == "Portrait photography"
            
            # Check second row values
            assert call_args.iloc[1]["Latitude"] == 40.7412
            assert call_args.iloc[1]["Longitude"] == -73.9897
            assert call_args.iloc[1]["Hour"] == 12
            assert call_args.iloc[1]["Month"] == 7
            assert call_args.iloc[1]["Day"] == 19
            assert call_args.iloc[1]["Cultural_activity_prefered"] == "Street photography"

    def test_model_output_processing(self, client, sample_request_data):
        """Test processing of model output into response format."""
        with patch('main.model') as mock_model:
            # Mock model returns numpy array with 3 columns
            mock_model.predict.return_value = np.array([
                [25.0, 3.5, 7.8],
                [30.0, 4.2, 8.1]
            ])
            
            response = client.post("/predict_batch", json=sample_request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert len(data) == 2
            
            # Check first prediction
            assert data[0]["muse_score"] is None
            assert data[0]["estimated_crowd_number"] == 25
            assert data[0]["crowd_score"] == 3.5
            assert data[0]["creative_activity_score"] == 7.8
            
            # Check second prediction
            assert data[1]["muse_score"] is None
            assert data[1]["estimated_crowd_number"] == 30
            assert data[1]["crowd_score"] == 4.2
            assert data[1]["creative_activity_score"] == 8.1

    def test_numeric_rounding_and_conversion(self, client):
        """Test proper rounding and type conversion of numeric values."""
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
            # Return values that need rounding
            mock_model.predict.return_value = np.array([[25.7, 3.567, 7.834]])
            
            response = client.post("/predict_batch", json=request_data)
            data = response.json()
            
            # Check that crowd number is rounded to nearest integer
            assert data[0]["estimated_crowd_number"] == 26  # 25.7 rounded to 26
            
            # Check that float values are preserved as floats
            assert isinstance(data[0]["crowd_score"], float)
            assert isinstance(data[0]["creative_activity_score"], float)

    def test_empty_batch_processing(self, client):
        """Test processing of empty batch request."""
        response = client.post("/predict_batch", json=[])
        assert response.status_code == 200
        assert response.json() == []

    def test_single_item_batch_processing(self, client):
        """Test processing of single-item batch."""
        single_request = [
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
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=single_request)
            assert response.status_code == 200
            
            data = response.json()
            assert len(data) == 1
            assert data[0]["estimated_crowd_number"] == 25

    def test_large_batch_processing(self, client):
        """Test processing of large batch requests."""
        large_batch = []
        for i in range(100):
            large_batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": f"Activity {i % 5}"
            })
        
        with patch('main.model') as mock_model:
            mock_predictions = np.random.rand(100, 3) * 10
            mock_model.predict.return_value = mock_predictions
            
            response = client.post("/predict_batch", json=large_batch)
            assert response.status_code == 200
            
            data = response.json()
            assert len(data) == 100
            
            # Verify all responses have required fields
            for item in data:
                assert "muse_score" in item
                assert "estimated_crowd_number" in item
                assert "crowd_score" in item
                assert "creative_activity_score" in item

    def test_special_float_values_handling(self, client):
        """Test handling of special float values (NaN, Infinity)."""
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
            # Return array with NaN and infinity values
            mock_model.predict.return_value = np.array([[np.nan, np.inf, -np.inf]])
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            # Check how the API handles these special values
            # (behavior depends on JSON serialization implementation)
            assert len(data) == 1

    def test_zero_and_negative_values_processing(self, client):
        """Test processing of zero and negative prediction values."""
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
            mock_model.predict.return_value = np.array([[0.0, -1.5, 0.0]])
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert data[0]["estimated_crowd_number"] == 0
            assert data[0]["crowd_score"] == -1.5
            assert data[0]["creative_activity_score"] == 0.0

    def test_very_large_numeric_values(self, client):
        """Test handling of very large numeric values."""
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
            # Very large values
            mock_model.predict.return_value = np.array([[1e6, 1e10, 1e15]])
            
            response = client.post("/predict_batch", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert data[0]["estimated_crowd_number"] == 1000000
            assert isinstance(data[0]["crowd_score"], float)
            assert isinstance(data[0]["creative_activity_score"], float)

    def test_string_activity_processing(self, client):
        """Test processing of different string formats for activity names."""
        diverse_activities = [
            "Portrait photography",
            "street photography",
            "LANDSCAPE PAINTING",
            "Performance Art",
            "3D modeling & design",
            "Art/Craft Workshop",
            "Dance & Movement",
            "Café sketching"
        ]
        
        request_batch = []
        for activity in diverse_activities:
            request_batch.append({
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": activity
            })
        
        with patch('main.model') as mock_model:
            mock_predictions = np.random.rand(len(diverse_activities), 3) * 10
            mock_model.predict.return_value = mock_predictions
            
            response = client.post("/predict_batch", json=request_batch)
            assert response.status_code == 200
            
            # Verify that the DataFrame passed to the model contains all activities
            call_args = mock_model.predict.call_args[0][0]
            activities_in_df = call_args["Cultural_activity_prefered"].tolist()
            assert activities_in_df == diverse_activities

    def test_dataframe_index_consistency(self, client, sample_request_data):
        """Test that DataFrame maintains consistent indexing."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8], [30.0, 4.0, 8.5]])
            
            response = client.post("/predict_batch", json=sample_request_data)
            
            call_args = mock_model.predict.call_args[0][0]
            
            # Check that DataFrame index is correct
            assert list(call_args.index) == [0, 1]
            assert call_args.shape == (2, 6)  # 2 rows, 6 columns

    def test_model_prediction_array_shape_validation(self, client):
        """Test validation of model prediction array shape."""
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
        
        # Test with incorrect prediction array shape
        with patch('main.model') as mock_model:
            # Wrong number of columns (should be 3)
            mock_model.predict.return_value = np.array([[25.0, 3.5]])  # Only 2 columns
            
            response = client.post("/predict_batch", json=request_data)
            # Should handle gracefully or return error
            assert response.status_code in [200, 500]

    def test_coordinate_precision_preservation(self, client):
        """Test that coordinate precision is preserved through processing."""
        high_precision_request = [
            {
                "latitude": 40.758912345,
                "longitude": -73.985123456,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=high_precision_request)
            
            call_args = mock_model.predict.call_args[0][0]
            
            # Check that precision is maintained
            assert abs(call_args.iloc[0]["Latitude"] - 40.758912345) < 1e-9
            assert abs(call_args.iloc[0]["Longitude"] - (-73.985123456)) < 1e-9

    def test_time_field_boundary_values(self, client):
        """Test processing of boundary values for time fields."""
        boundary_requests = [
            {"latitude": 40.7589, "longitude": -73.9851, "hour": 0, "month": 1, "day": 1, "cultural_activity_prefered": "Test"},
            {"latitude": 40.7589, "longitude": -73.9851, "hour": 23, "month": 12, "day": 31, "cultural_activity_prefered": "Test"},
            {"latitude": 40.7589, "longitude": -73.9851, "hour": 12, "month": 6, "day": 15, "cultural_activity_prefered": "Test"}
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]] * len(boundary_requests))
            
            response = client.post("/predict_batch", json=boundary_requests)
            assert response.status_code == 200
            
            call_args = mock_model.predict.call_args[0][0]
            
            # Verify boundary values are preserved
            assert call_args.iloc[0]["Hour"] == 0
            assert call_args.iloc[0]["Month"] == 1
            assert call_args.iloc[0]["Day"] == 1
            
            assert call_args.iloc[1]["Hour"] == 23
            assert call_args.iloc[1]["Month"] == 12
            assert call_args.iloc[1]["Day"] == 31

    def test_concurrent_request_processing(self, client):
        """Test that concurrent requests are processed correctly."""
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
                mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
                response = client.post("/predict_batch", json=request_data)
                results.put(response.status_code)
        
        # Create multiple threads making concurrent requests
        threads = []
        for _ in range(5):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
            thread.start()
        
        # Wait for all threads to complete
        for thread in threads:
            thread.join()
        
        # Check that all requests succeeded
        while not results.empty():
            status_code = results.get()
            assert status_code == 200

    def test_memory_efficiency_large_batches(self, client):
        """Test memory efficiency with large batch processing."""
        # Create a very large batch to test memory handling
        large_batch_size = 10000
        large_batch = []
        
        for i in range(large_batch_size):
            large_batch.append({
                "latitude": 40.7589 + (i * 0.0001),
                "longitude": -73.9851 + (i * 0.0001),
                "hour": i % 24,
                "month": (i % 12) + 1,
                "day": (i % 28) + 1,
                "cultural_activity_prefered": f"Activity {i % 100}"
            })
        
        with patch('main.model') as mock_model:
            # Mock large prediction array
            mock_predictions = np.random.rand(large_batch_size, 3) * 10
            mock_model.predict.return_value = mock_predictions
            
            response = client.post("/predict_batch", json=large_batch)
            
            # Should handle large batches without memory issues
            assert response.status_code == 200
            data = response.json()
            assert len(data) == large_batch_size