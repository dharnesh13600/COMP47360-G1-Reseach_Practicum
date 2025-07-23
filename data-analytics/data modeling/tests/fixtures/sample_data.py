# tests/fixtures/sample_data.py
"""
Sample test data for ML API testing
"""

# Valid request samples
VALID_SINGLE_REQUEST = {
    "latitude": 40.7589,
    "longitude": -73.9851,
    "hour": 15,
    "month": 7,
    "day": 18,
    "cultural_activity_prefered": "Portrait photography"
}

VALID_BATCH_REQUESTS = [
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

# Invalid request samples
INVALID_REQUESTS = {
    "latitude_out_of_range": {
        "latitude": 91.0,  # Invalid
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    },
    "longitude_out_of_range": {
        "latitude": 40.7589,
        "longitude": 181.0,  # Invalid
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    },
    "hour_out_of_range": {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 25,  # Invalid
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    },
    "month_out_of_range": {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 13,  # Invalid
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    },
    "day_out_of_range": {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 32,  # Invalid
        "cultural_activity_prefered": "Portrait photography"
    },
    "empty_activity": {
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": ""  # Invalid
    }
}

# Edge case samples
EDGE_CASE_REQUESTS = {
    "minimum_values": {
        "latitude": -90.0,
        "longitude": -180.0,
        "hour": 0,
        "month": 1,
        "day": 1,
        "cultural_activity_prefered": "A"
    },
    "maximum_values": {
        "latitude": 90.0,
        "longitude": 180.0,
        "hour": 23,
        "month": 12,
        "day": 31,
        "cultural_activity_prefered": "Very long activity name " * 10
    },
    "zero_coordinates": {
        "latitude": 0.0,
        "longitude": 0.0,
        "hour": 12,
        "month": 6,
        "day": 15,
        "cultural_activity_prefered": "Test Activity"
    }
}

# Performance testing data
def generate_large_batch(size: int = 1000):
    """Generate a large batch of requests for performance testing"""
    batch = []
    for i in range(size):
        request = VALID_SINGLE_REQUEST.copy()
        request["latitude"] += (i * 0.001)
        request["longitude"] += (i * 0.001)
        request["hour"] = i % 24
        request["month"] = (i % 12) + 1
        request["day"] = (i % 28) + 1
        request["cultural_activity_prefered"] = f"Activity {i}"
        batch.append(request)
    return batch

# Activity types for testing
ACTIVITY_TYPES = [
    "Portrait photography",
    "Street photography", 
    "Landscape painting",
    "Performance art",
    "Filmmaking",
    "Dancing",
    "Portrait painting",
    "Digital art",
    "Sculpture",
    "Music composition"
]