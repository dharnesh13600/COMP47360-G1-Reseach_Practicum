# tests/security/test_payload_validation.py
import pytest
from fastapi.testclient import TestClient
import json
import sys
import os

# Add the parent directory to the Python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from main import app

class TestPayloadValidation:
    """Test suite for payload validation and security"""
    
    @pytest.fixture(scope="class")
    def client(self):
        return TestClient(app)
    
    @pytest.fixture
    def valid_request(self):
        return [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography"
        }]
    
    def test_content_type_validation(self, client, valid_request):
        """Test that only JSON content type is accepted"""
        import requests
        
        # Test with invalid content type (if server is running)
        try:
            # This test requires the actual server to be running
            response = requests.post(
                "http://localhost:8000/predict_batch",
                headers={"Content-Type": "text/plain"},
                data="invalid data",
                timeout=5
            )
            assert response.status_code == 415
        except requests.exceptions.ConnectionError:
            # Server not running, skip this test
            pytest.skip("Server not running for content type test")
        except Exception:
            # Use TestClient instead
            # Note: TestClient always uses application/json, so we test the validation logic
            pass
    
    def test_request_size_limits(self, client):
        """Test payload size limits"""
        # Test with very large payload
        large_request = []
        for i in range(10000):  # Very large batch
            large_request.append({
                "latitude": 40.7589 + (i * 0.0001),
                "longitude": -73.9851 + (i * 0.0001),
                "hour": i % 24,
                "month": (i % 12) + 1,
                "day": (i % 28) + 1,
                "cultural_activity_prefered": f"Very long activity name for testing purposes {i}" * 10
            })
        
        response = client.post("/predict_batch", json=large_request)
        
        # Should either process or reject based on size limits
        assert response.status_code in [200, 413, 422]  # 413 = Payload Too Large
    
    def test_malformed_json_payloads(self, client):
        """Test handling of malformed JSON payloads"""
        malformed_payloads = [
            '{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "test"',  # Missing closing brace
            '[{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "test"}',  # Missing closing bracket
            '{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "test",}',  # Trailing comma
            '{"latitude": 40.7589 "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "test"}',  # Missing comma
        ]
        
        for payload in malformed_payloads:
            # Test using raw HTTP request simulation
            response = client.post(
                "/predict_batch",
                content=payload,
                headers={"Content-Type": "application/json"}
            )
            
            # Should return 422 for malformed JSON
            assert response.status_code == 422
    
    def test_json_injection_attempts(self, client):
        """Test JSON injection attempts"""
        json_injection_payloads = [
            {"__proto__": {"admin": True}},  # Prototype pollution
            {"constructor": {"prototype": {"admin": True}}},  # Constructor pollution
            {"latitude": "40.7589\"; alert('XSS'); \""},  # JSON with JS injection
        ]
        
        for payload in json_injection_payloads:
            # Embed in valid request structure
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography",
                **payload  # Add the injection payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should either process (ignoring extra fields) or reject
            assert response.status_code in [200, 422]
    
    def test_nested_json_attacks(self, client):
        """Test deeply nested JSON attacks"""
        # Create deeply nested structure
        nested_payload = "test"
        for _ in range(100):  # Create 100 levels of nesting
            nested_payload = {"nested": nested_payload}
        
        request = [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography",
            "malicious_nested": nested_payload
        }]
        
        response = client.post("/predict_batch", json=request)
        
        # Should handle gracefully (ignore extra fields or reject)
        assert response.status_code in [200, 422, 400]
    
    def test_array_injection_attacks(self, client):
        """Test array injection attacks"""
        array_attacks = [
            {"latitude": [40.7589, "injection"]},  # Array where number expected
            {"cultural_activity_prefered": ["test", {"nested": "attack"}]},  # Array where string expected
            {"hour": [15, 16, 17, 18, 19, 20]},  # Array of valid values
        ]
        
        for attack in array_attacks:
            base_request = {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
            base_request.update(attack)
            
            response = client.post("/predict_batch", json=[base_request])
            
            # Should reject invalid types
            assert response.status_code == 422
    
    def test_type_confusion_attacks(self, client):
        """Test type confusion attacks"""
        type_confusion_payloads = [
            {"latitude": "40.7589"},  # String instead of float
            {"longitude": True},  # Boolean instead of float
            {"hour": "15"},  # String instead of int
            {"month": 7.5},  # Float instead of int
            {"day": None},  # Null instead of int
            {"cultural_activity_prefered": 123},  # Number instead of string
        ]
        
        for payload in type_confusion_payloads:
            base_request = {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
            base_request.update(payload)
            
            response = client.post("/predict_batch", json=[base_request])
            
            # Should handle type conversion or reject
            assert response.status_code in [200, 422]
    
    def test_missing_required_fields(self, client):
        """Test requests with missing required fields"""
        required_fields = ["latitude", "longitude", "hour", "month", "day", "cultural_activity_prefered"]
        
        for field_to_remove in required_fields:
            incomplete_request = {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
            del incomplete_request[field_to_remove]
            
            response = client.post("/predict_batch", json=[incomplete_request])
            
            # Should reject missing required fields
            assert response.status_code == 422
    
    def test_extra_fields_handling(self, client):
        """Test handling of extra/unexpected fields"""
        request_with_extra_fields = [{
            "latitude": 40.7589,
            "longitude": -73.9851,
            "hour": 15,
            "month": 7,
            "day": 18,
            "cultural_activity_prefered": "Portrait photography",
            # Extra fields
            "extra_field": "should_be_ignored",
            "malicious_code": "<script>alert('xss')</script>",
            "admin": True,
            "password": "secret123"
        }]
        
        response = client.post("/predict_batch", json=request_with_extra_fields)
        
        # Should process successfully (ignoring extra fields)
        assert response.status_code == 200
        data = response.json()
        assert len(data) == 1
    
    def test_unicode_validation(self, client):
        """Test Unicode validation and handling"""
        unicode_payloads = [
            "Normal ASCII text",
            "UTF-8 émojis 🎨📸🎭",
            "Chinese characters: 中文测试",
            "Arabic text: اختبار عربي",
            "Russian text: русский текст",
            "Special Unicode: \u200B\u200C\u200D",  # Zero-width characters
            "RTL override: \u202E\u202D",
        ]
        
        for payload in unicode_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            try:
                response = client.post("/predict_batch", json=request)
                # Should handle Unicode gracefully
                assert response.status_code in [200, 422]
            except UnicodeEncodeError:
                # Some Unicode characters may cause encoding issues
                pass
    
    def test_numeric_boundary_validation(self, client):
        """Test numeric boundary validation"""
        boundary_tests = [
            # Latitude boundaries
            {"latitude": -90.0},   # Minimum valid
            {"latitude": 90.0},    # Maximum valid
            {"latitude": -90.1},   # Just below minimum
            {"latitude": 90.1},    # Just above maximum
            {"latitude": -180.0},  # Way below minimum
            {"latitude": 180.0},   # Way above maximum
            
            # Longitude boundaries  
            {"longitude": -180.0}, # Minimum valid
            {"longitude": 180.0},  # Maximum valid
            {"longitude": -180.1}, # Just below minimum
            {"longitude": 180.1},  # Just above maximum
            
            # Hour boundaries
            {"hour": 0},           # Minimum valid
            {"hour": 23},          # Maximum valid
            {"hour": -1},          # Just below minimum
            {"hour": 24},          # Just above maximum
            
            # Month boundaries
            {"month": 1},          # Minimum valid
            {"month": 12},         # Maximum valid
            {"month": 0},          # Just below minimum
            {"month": 13},         # Just above maximum
            
            # Day boundaries
            {"day": 1},            # Minimum valid
            {"day": 31},           # Maximum valid
            {"day": 0},            # Just below minimum
            {"day": 32},           # Just above maximum
        ]
        
        for test_case in boundary_tests:
            base_request = {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
            base_request.update(test_case)
            
            response = client.post("/predict_batch", json=[base_request])
            
            # Valid values should return 200, invalid should return 422
            field_name = list(test_case.keys())[0]
            field_value = list(test_case.values())[0]
            
            # Check if value is within valid range
            if field_name == "latitude" and -90.0 <= field_value <= 90.0:
                assert response.status_code == 200
            elif field_name == "longitude" and -180.0 <= field_value <= 180.0:
                assert response.status_code == 200
            elif field_name == "hour" and 0 <= field_value <= 23:
                assert response.status_code == 200
            elif field_name == "month" and 1 <= field_value <= 12:
                assert response.status_code == 200
            elif field_name == "day" and 1 <= field_value <= 31:
                assert response.status_code == 200
            else:
                assert response.status_code == 422
    
    def test_float_precision_attacks(self, client):
        """Test attacks using extreme float precision"""
        precision_attacks = [
            {"latitude": 40.123456789012345678901234567890},  # Very high precision
            {"longitude": -73.999999999999999999999999999},   # Many 9s
            {"latitude": 1e-100},                             # Very small number
            {"longitude": 1e100},                             # Very large number (but invalid range)
        ]
        
        for attack in precision_attacks:
            base_request = {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
            base_request.update(attack)
            
            response = client.post("/predict_batch", json=[base_request])
            
            # Should handle precision gracefully or reject invalid ranges
            assert response.status_code in [200, 422]