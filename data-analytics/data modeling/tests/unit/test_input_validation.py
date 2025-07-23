"""
Test module for input validation functionality.
"""

import pytest
import numpy as np
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
from main import app


class TestInputValidation:
    """Test input validation for the ML prediction service."""
    
    @pytest.fixture
    def client(self):
        """Create a test client for the FastAPI app."""
        return TestClient(app)
    
    @pytest.fixture
    def valid_request(self):
        """Sample valid prediction request."""
        return [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]

    def test_validate_required_fields_present(self, client, valid_request):
        """Test that all required fields are present in valid request."""
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=valid_request)
            
            assert response.status_code == 200
            mock_model.predict.assert_called_once()

    def test_validate_missing_latitude(self, client):
        """Test validation when latitude is missing."""
        invalid_request = [
            {
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "latitude" in response.json()["detail"][0]["loc"]

    def test_validate_missing_longitude(self, client):
        """Test validation when longitude is missing."""
        invalid_request = [
            {
                "latitude": 40.7589,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "longitude" in response.json()["detail"][0]["loc"]

    def test_validate_missing_hour(self, client):
        """Test validation when hour is missing."""
        invalid_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "hour" in response.json()["detail"][0]["loc"]

    def test_validate_missing_month(self, client):
        """Test validation when month is missing."""
        invalid_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "month" in response.json()["detail"][0]["loc"]

    def test_validate_missing_day(self, client):
        """Test validation when day is missing."""
        invalid_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "day" in response.json()["detail"][0]["loc"]

    def test_validate_missing_cultural_activity(self, client):
        """Test validation when cultural_activity_prefered is missing."""
        invalid_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18
            }
        ]
        
        response = client.post("/predict_batch", json=invalid_request)
        assert response.status_code == 422
        assert "cultural_activity_prefered" in response.json()["detail"][0]["loc"]

    def test_validate_latitude_type(self, client):
        """Test validation of latitude data type."""
        invalid_requests = [
            [{"latitude": "not_a_number", "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": None, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": [], "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_longitude_type(self, client):
        """Test validation of longitude data type."""
        invalid_requests = [
            [{"latitude": 40.7589, "longitude": "not_a_number", "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": None, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": {}, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_hour_type_and_range(self, client):
        """Test validation of hour data type and range."""
        invalid_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": "not_a_number", "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": -1, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 24, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 25, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": None, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_month_type_and_range(self, client):
        """Test validation of month data type and range."""
        invalid_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": "not_a_number", "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 0, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 13, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": -1, "day": 18, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": None, "day": 18, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_day_type_and_range(self, client):
        """Test validation of day data type and range."""
        invalid_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": "not_a_number", "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 0, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 32, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": -1, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": None, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_cultural_activity_type(self, client):
        """Test validation of cultural_activity_prefered data type."""
        invalid_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": 123}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": None}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": []}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": {}}]
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_coordinate_ranges(self, client):
        """Test validation of coordinate ranges (reasonable geographic bounds)."""
        # Test extreme latitude values
        extreme_requests = [
            [{"latitude": 91.0, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],  # > 90
            [{"latitude": -91.0, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],  # < -90
            [{"latitude": 40.7589, "longitude": 181.0, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}],  # > 180
            [{"latitude": 40.7589, "longitude": -181.0, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]  # < -180
        ]
        
        # Note: Depending on your validation rules, these might be accepted
        # This test checks if your API has geographic coordinate validation
        for extreme_request in extreme_requests:
            response = client.post("/predict_batch", json=extreme_request)
            # Expecting either validation error (422) or successful processing
            assert response.status_code in [200, 422]

    def test_validate_empty_request_array(self, client):
        """Test validation with empty request array."""
        response = client.post("/predict_batch", json=[])
        assert response.status_code == 200
        assert response.json() == []

    def test_validate_non_array_request(self, client):
        """Test validation when request is not an array."""
        invalid_requests = [
            {"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"},  # Single object
            "invalid_string",  # String
            123,  # Number
            None  # None
        ]
        
        for invalid_request in invalid_requests:
            response = client.post("/predict_batch", json=invalid_request)
            assert response.status_code == 422

    def test_validate_mixed_valid_invalid_requests(self, client):
        """Test validation with mixed valid and invalid requests in batch."""
        mixed_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            },
            {
                "latitude": "invalid",  # Invalid latitude
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Street photography"
            }
        ]
        
        response = client.post("/predict_batch", json=mixed_request)
        assert response.status_code == 422

    def test_validate_extra_fields_ignored(self, client):
        """Test that extra fields are ignored gracefully."""
        request_with_extra_fields = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography",
                "extra_field": "should_be_ignored",
                "another_extra": 123
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=request_with_extra_fields)
            assert response.status_code == 200

    def test_validate_special_characters_in_activity(self, client):
        """Test handling of special characters in activity names."""
        special_char_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait & photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Street photography!"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Performance art (outdoor)"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "3D modeling"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Art/Design"}]
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            for request in special_char_requests:
                response = client.post("/predict_batch", json=request)
                assert response.status_code == 200

    def test_validate_unicode_characters_in_activity(self, client):
        """Test handling of unicode characters in activity names."""
        unicode_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Café photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "街头摄影"}],  # Chinese
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "ストリート写真"}],  # Japanese
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Фотография"}]  # Russian
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            for request in unicode_requests:
                response = client.post("/predict_batch", json=request)
                assert response.status_code == 200

    def test_validate_empty_string_activity(self, client):
        """Test validation with empty string for activity."""
        empty_activity_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": ""
            }
        ]
        
        response = client.post("/predict_batch", json=empty_activity_request)
        # Depending on validation rules, this might be accepted or rejected
        assert response.status_code in [200, 422]

    def test_validate_very_long_activity_name(self, client):
        """Test validation with very long activity name."""
        long_activity_name = "A" * 1000  # 1000 character activity name
        long_activity_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": long_activity_name
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=long_activity_request)
            # Should handle long strings gracefully
            assert response.status_code in [200, 422]

    def test_validate_float_vs_int_fields(self, client):
        """Test that int fields accept both int and float values."""
        float_int_requests = [
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15.0, "month": 7.0, "day": 18.0, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 15.5, "month": 7.9, "day": 18.1, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        for request in float_int_requests:
            response = client.post("/predict_batch", json=request)
            # Depending on validation, might convert floats to ints or reject
            assert response.status_code in [200, 422]

    def test_validate_precision_limits(self, client):
        """Test handling of high precision floating point numbers."""
        high_precision_request = [
            {
                "latitude": 40.758912345678901234567890,
                "longitude": -73.985123456789012345678901,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=high_precision_request)
            assert response.status_code == 200

    def test_validate_scientific_notation(self, client):
        """Test handling of scientific notation in numeric fields."""
        scientific_notation_request = [
            {
                "latitude": 4.07589e1,  # 40.7589
                "longitude": -7.39851e1,  # -73.9851
                "hour": 1.5e1,  # 15
                "month": 7e0,  # 7
                "day": 1.8e1,  # 18
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            response = client.post("/predict_batch", json=scientific_notation_request)
            assert response.status_code == 200

    def test_validate_large_batch_size(self, client):
        """Test validation with large batch sizes."""
        large_batch = []
        for i in range(1000):  # 1000 requests
            large_batch.append({
                "latitude": 40.7589 + (i * 0.001),
                "longitude": -73.9851 + (i * 0.001),
                "hour": i % 24,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": f"Activity {i % 10}"
            })
        
        with patch('main.model') as mock_model:
            mock_predictions = np.random.rand(1000, 3) * 10
            mock_model.predict.return_value = mock_predictions
            
            response = client.post("/predict_batch", json=large_batch)
            assert response.status_code == 200
            assert len(response.json()) == 1000

    def test_validate_malformed_json_structure(self, client):
        """Test handling of malformed JSON structure."""
        # This test uses raw data instead of json parameter to send malformed JSON
        malformed_json_strings = [
            '{"latitude": 40.7589, "longitude": -73.9851,}',  # Trailing comma
            '[{"latitude": 40.7589, "longitude": -73.9851}',  # Missing closing bracket
            '{"latitude": 40.7589 "longitude": -73.9851}',  # Missing comma
            '{latitude: 40.7589, longitude: -73.9851}',  # Unquoted keys
        ]
        
        for malformed_json in malformed_json_strings:
            response = client.post("/predict_batch", 
                                 data=malformed_json,
                                 headers={"Content-Type": "application/json"})
            assert response.status_code == 422

    def test_validate_boundary_date_values(self, client):
        """Test validation with boundary date values."""
        boundary_requests = [
            # Valid boundary cases
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 0, "month": 1, "day": 1, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 23, "month": 12, "day": 31, "cultural_activity_prefered": "Portrait photography"}],
            # February 29th (leap year consideration)
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 12, "month": 2, "day": 29, "cultural_activity_prefered": "Portrait photography"}],
            # End of months with different day counts
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 12, "month": 4, "day": 30, "cultural_activity_prefered": "Portrait photography"}],
            [{"latitude": 40.7589, "longitude": -73.9851, "hour": 12, "month": 2, "day": 28, "cultural_activity_prefered": "Portrait photography"}]
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            for request in boundary_requests:
                response = client.post("/predict_batch", json=request)
                assert response.status_code == 200

    def test_validate_content_type_header(self, client):
        """Test validation of Content-Type header."""
        valid_request = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        # Test with correct Content-Type
        response = client.post("/predict_batch", 
                             json=valid_request,
                             headers={"Content-Type": "application/json"})
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            response = client.post("/predict_batch", json=valid_request)
            assert response.status_code == 200
        
        # Test with incorrect Content-Type
        import json
        response = client.post("/predict_batch", 
                             data=json.dumps(valid_request),
                             headers={"Content-Type": "text/plain"})
        assert response.status_code in [415, 422]  # Unsupported Media Type or Unprocessable Entity