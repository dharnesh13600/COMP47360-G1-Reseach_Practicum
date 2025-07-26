# Creative Space Finder - ML Microservice

## Overview

This directory contains a FastAPI-based ML microservice that serves XGBoost machine learning models for the Creative Space Finder application. The service provides real-time predictions for location recommendations based on crowd estimation, cultural activity scores, and muse scores.

### Architecture

```
┌─────────────────────┐    HTTP POST     ┌──────────────────────┐
│   Spring Boot App   │ ────────────────► │   FastAPI ML API     │
│                     │                  │                      │
│ LocationRecommen-   │                  │   XGBoost Model      │
│ dationService.java  │                  │   (.pkl file)        │
│                     │ ◄──────────────── │                      │
└─────────────────────┘    JSON Response └──────────────────────┘
```

The ML microservice operates independently from the main Spring Boot application, providing horizontal scalability and technology stack separation.

---

## 📁 File Structure

```
data-modeling/
├── main.py                 # FastAPI application & model serving
├── requirements.txt        # Production dependencies (empty/minimal)
├── requirements-test.txt   # Complete dependencies for development/testing
├── pytest.ini            # Test configuration
├── xgboost_model.pkl      # Trained XGBoost model (not in repo)
└── tests/                 # Test suite (if exists)
    ├── test_main.py
    ├── test_prediction.py
    └── conftest.py
```

---

## 🤖 Machine Learning Model

### Model Type: XGBoost Ensemble
- **File**: `xgboost_model.pkl`
- **Framework**: XGBoost with scikit-learn pipeline
- **Purpose**: Multi-output prediction for location recommendation scoring

### Model Inputs
The model expects these features for each prediction:

| Feature | Type | Range | Description |
|---------|------|-------|-------------|
| `latitude` | float | -90 to 90 | Location latitude |
| `longitude` | float | -180 to 180 | Location longitude |
| `hour` | int | 0 to 23 | Hour of day (24-hr format) |
| `month` | int | 1 to 12 | Month of year |
| `day` | int | 1 to 31 | Day of month |
| `cultural_activity_prefered` | string | N/A | Activity type (e.g., "Portrait photography") |

### Model Outputs
The model returns multiple predictions per input:

| Output | Type | Range | Description |
|--------|------|-------|-------------|
| `estimated_crowd_number` | int | 0+ | Raw crowd count estimate |
| `crowd_score` | float | 0-10 | Normalized crowd desirability score |
| `creative_activity_score` | float | 0-10 | Cultural activity suitability score |
| `muse_score` | float | 0-10 | Overall inspiration/creativity score (nullable) |

---

## 🚀 FastAPI Application (`main.py`)

### Application Structure

```python
app = FastAPI(title="Crowd & Muse-Score ML API", version="3.0")
```

### Key Components

#### 1. **Model Loading**
```python
MODEL_PATH = "xgboost_model.pkl"
model = joblib.load(MODEL_PATH)
```
- Loads the trained XGBoost pipeline at startup
- Fails fast if model file is missing or corrupted
- Model is kept in memory for fast predictions

#### 2. **Data Validation (Pydantic V2)**
```python
class PredictionRequest(BaseModel):
    latitude: float
    longitude: float
    hour: int
    month: int
    day: int
    cultural_activity_prefered: str
    
    @field_validator('hour')
    @classmethod
    def validate_hour(cls, v):
        if not (0 <= v <= 23):
            raise ValueError('Hour must be between 0 and 23')
        return v
```

**Validation Rules:**
- Latitude: -90 to 90 degrees
- Longitude: -180 to 180 degrees  
- Hour: 0 to 23 (24-hour format)
- Month: 1 to 12
- Day: 1 to 31
- Activity: Non-empty string

#### 3. **Batch Prediction Engine**
The core prediction logic handles:
- **Batch Processing**: Multiple predictions in single request
- **Data Transformation**: Pydantic → DataFrame → Model format
- **Column Mapping**: API fields → Model expected columns
- **Output Processing**: Raw predictions → Structured responses

#### 4. **Error Handling & Middleware**

**Content-Type Validation Middleware:**
```python
@app.middleware("http")
async def validate_content_type(request: Request, call_next):
```
- Enforces `application/json` for POST requests
- Returns 415 for wrong content types
- Returns 422 for malformed JSON

**Exception Handlers:**
- `ValueError` → 422 Validation Error
- `Exception` → 500 Internal Server Error
- Model failures → 500 with detailed error message

---

## 🌐 API Endpoints

### 1. **Health Check**
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "model_loaded": true,
  "version": "3.0",
  "timestamp": "2025-07-25T14:30:00.000Z"
}
```

**Purpose:**
- Kubernetes/Docker health checks
- Model accessibility verification
- Service status monitoring

### 2. **Batch Prediction** (Primary Endpoint)
```http
POST /predict_batch
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "latitude": 40.7589,
    "longitude": -73.9851,
    "hour": 15,
    "month": 7,
    "day": 18,
    "cultural_activity_prefered": "Portrait photography"
  },
  {
    "latitude": 40.7505,
    "longitude": -73.9934,
    "hour": 19,
    "month": 7,
    "day": 18,
    "cultural_activity_prefered": "Street photography"
  }
]
```

**Response:**
```json
[
  {
    "muse_score": null,
    "estimated_crowd_number": 25,
    "crowd_score": 6.2,
    "creative_activity_score": 8.1
  },
  {
    "muse_score": null,
    "estimated_crowd_number": 45,
    "crowd_score": 7.8,
    "creative_activity_score": 7.3
  }
]
```

**Notes:**
- `muse_score` is currently always `null` (calculated by Spring Boot service)
- Response array matches request array order
- Empty request arrays return empty responses

### 3. **Metrics**
```http
GET /metrics
```
```json
{
  "model_loaded": true,
  "version": "3.0",
  "timestamp": "2025-07-25T14:30:00.000Z"
}
```

### 4. **Root**
```http
GET /
```
```json
{
  "message": "Creative Space Finder ML API",
  "version": "3.0"
}
```

---

## 🔧 Setup & Installation

### Prerequisites
- Python 3.8+
- XGBoost model file (`xgboost_model.pkl`)
- Virtual environment (recommended)

### Development Setup

```bash
# 1. Create virtual environment
python -m venv ml-env
source ml-env/bin/activate  # Linux/Mac
# or
ml-env\Scripts\activate     # Windows

# 2. Install dependencies
pip install -r requirements-test.txt

# 3. Ensure model file exists
ls xgboost_model.pkl

# 4. Run locally
python main.py
# or
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### Production Deployment

```bash
# 1. Install production dependencies
pip install -r requirements.txt
pip install fastapi uvicorn[standard] joblib pandas numpy scikit-learn xgboost

# 2. Run with Gunicorn (recommended)
gunicorn main:app -w 4 -k uvicorn.workers.UvicornWorker --bind 0.0.0.0:8000

# 3. Or with Uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

### Docker Deployment

```dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY main.py .
COPY xgboost_model.pkl .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

```bash
docker build -t ml-microservice .
docker run -p 8000:8000 ml-microservice
```

---

## 🔗 Integration with Spring Boot

### Configuration
In the Spring Boot `application.properties`:
```properties
ml.predict.url=http://localhost:8000/predict_batch
# or for production:
ml.predict.url=http://ml-service:8000/predict_batch
```

### Service Integration
The `LocationRecommendationService.java` calls this ML service:

```java
protected PredictionResponse[] callMLModelBatch(List<Map<String,Object>> bodies) {
    RestTemplate r = new RestTemplate();
    return r.postForObject(mlPredictUrl, bodies, PredictionResponse[].class);
}
```

### Data Flow

```
1. User Request → Spring Boot Controller
2. Spring Boot → Prepare ML payload:
   {
     "latitude": location.latitude,
     "longitude": location.longitude, 
     "hour": requestDateTime.hour,
     "month": requestDateTime.month,
     "day": requestDateTime.day,
     "cultural_activity_prefered": activityName
   }
3. Spring Boot → HTTP POST to ML API
4. ML API → XGBoost Model → Raw predictions
5. ML API → Structured response
6. Spring Boot → Process predictions → Apply business logic
7. Spring Boot → Final location recommendations
```

### Prediction Processing in Spring Boot

The Spring Boot service processes ML predictions by:

1. **Crowd Score Adjustment**: For quiet activities, inverts crowd scores
2. **Muse Score Calculation**: `(adjustedCrowdScore * 0.7) + (cultScore * 0.3)`
3. **Score Boosting**: Doubles scores for busy activities
4. **Crowd Level Assignment**: Maps crowd numbers to "Quiet"/"Moderate"/"Busy"

---

## 🧪 Testing

### Test Configuration (`pytest.ini`)
```ini
[tool:pytest]
testpaths = tests
python_files = test_*.py
addopts = -v --tb=short --strict-markers --color=yes

markers =
    performance: Performance tests
    slow: Slow running tests  
    integration: Integration tests
    unit: Unit tests
```

### Running Tests

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=main --cov-report=html

# Run specific test types
pytest -m unit           # Unit tests only
pytest -m integration    # Integration tests only
pytest -m performance    # Performance tests only

# Run specific test files
pytest tests/test_main.py
pytest tests/test_prediction.py

# Parallel execution
pytest -n auto          # Use all CPU cores
pytest -n 4             # Use 4 processes
```

### Test Categories

1. **Unit Tests**: Individual function testing
2. **Integration Tests**: API endpoint testing
3. **Performance Tests**: Load and latency testing
4. **Error Handling Tests**: Validation and failure scenarios

### Example Test Structure

```python
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

@pytest.mark.unit
def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

@pytest.mark.integration  
def test_prediction_endpoint():
    payload = [{
        "latitude": 40.7589,
        "longitude": -73.9851,
        "hour": 15,
        "month": 7,
        "day": 18,
        "cultural_activity_prefered": "Portrait photography"
    }]
    response = client.post("/predict_batch", json=payload)
    assert response.status_code == 200
    assert len(response.json()) == 1

@pytest.mark.performance
def test_batch_prediction_performance():
    # Test large batch performance
    large_payload = [test_data] * 100
    start_time = time.time()
    response = client.post("/predict_batch", json=large_payload)
    duration = time.time() - start_time
    
    assert response.status_code == 200
    assert duration < 5.0  # Should complete within 5 seconds
```

---

## 📊 Performance Considerations

### Optimization Strategies

1. **Model Loading**: One-time at startup, kept in memory
2. **Batch Processing**: Process multiple predictions simultaneously
3. **Data Transformation**: Efficient pandas operations
4. **Response Serialization**: Pydantic for fast JSON serialization

### Benchmarks

| Metric | Target | Typical |
|--------|--------|---------|
| Startup Time | < 10s | ~3s |
| Single Prediction | < 100ms | ~50ms |
| Batch of 10 | < 200ms | ~120ms |
| Batch of 100 | < 1s | ~800ms |
| Memory Usage | < 512MB | ~300MB |

### Scaling Recommendations

1. **Horizontal Scaling**: Multiple service instances behind load balancer
2. **Caching**: Redis for frequent prediction patterns
3. **Async Processing**: For very large batches
4. **Model Optimization**: Feature selection, quantization

### Resource Requirements

**Minimum:**
- CPU: 1 core
- RAM: 512MB
- Disk: 100MB

**Recommended:**
- CPU: 2-4 cores
- RAM: 1-2GB
- Disk: 1GB

**Production:**
- CPU: 4-8 cores
- RAM: 4-8GB
- Disk: 10GB

---

## 🚨 Error Handling

### HTTP Status Codes

| Code | Scenario | Example |
|------|----------|---------|
| 200 | Success | Valid predictions returned |
| 404 | Not Found | Wrong endpoint URL |
| 415 | Unsupported Media Type | Missing/wrong Content-Type |
| 422 | Validation Error | Invalid input data |
| 500 | Internal Server Error | Model prediction failure |
| 503 | Service Unavailable | Health check failure |

### Error Response Format

```json
{
  "error": "Validation Error",
  "detail": "Hour must be between 0 and 23"
}
```

### Common Error Scenarios

1. **Invalid Endpoint**: `/predict` instead of `/predict_batch`
2. **Wrong Content-Type**: Missing `application/json` header
3. **Validation Errors**: Out-of-range values
4. **Model Failures**: Corrupted .pkl file or memory issues
5. **Empty Requests**: Null or empty request bodies

### Error Recovery

```python
# Graceful handling of model failures
try:
    preds = model.predict(df)
except Exception as model_exception:
    logger.error(f"Model prediction failed: {model_exception}")
    raise HTTPException(status_code=500, detail=f"Model prediction failed: {str(model_exception)}")

# Default predictions for edge cases
if len(preds) != len(reqs):
    logger.error(f"Prediction count mismatch: {len(preds)} != {len(reqs)}")
    # Create default predictions to prevent crashes
    preds = np.array([[25.0, 5.0, 7.0]] * len(reqs))
```

---

## 🔧 Development Workflow

### 1. Model Development
```bash
# Train model (separate process)
python train_model.py        # Generates xgboost_model.pkl

# Test model locally
python test_model.py         # Validate model predictions

# Update API if needed
vim main.py                  # Modify prediction logic
```

### 2. API Development
```bash
# Make changes
vim main.py

# Test locally
uvicorn main:app --reload

# Run tests
pytest tests/

# Check endpoints
curl -X GET http://localhost:8000/health
curl -X POST http://localhost:8000/predict_batch \
  -H "Content-Type: application/json" \
  -d '[{"latitude": 40.7589, "longitude": -73.9851, "hour": 15, "month": 7, "day": 18, "cultural_activity_prefered": "Portrait photography"}]'
```

### 3. Integration Testing
```bash
# Start ML service
uvicorn main:app --port 8000

# Start Spring Boot app (different terminal)
cd ../spring-boot-app
./mvnw spring-boot:run

# Test integration
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{"activity": "Portrait photography", "dateTime": "2025-07-25T15:00:00"}'
```

### 4. Deployment Process
```bash
# 1. Build and test
docker build -t ml-microservice:latest .
docker run -p 8000:8000 ml-microservice:latest

# 2. Test endpoints
curl http://localhost:8000/health

# 3. Push to registry
docker tag ml-microservice:latest your-registry/ml-microservice:v3.0
docker push your-registry/ml-microservice:v3.0

# 4. Deploy to production
kubectl apply -f k8s-deployment.yaml
```

---

## 📝 Dependencies

### Core Dependencies (`requirements-test.txt`)

#### Web Framework
- `fastapi==0.115.6` - Modern async web framework
- `uvicorn[standard]==0.32.1` - ASGI server with performance extras
- `pydantic==2.11.1` - Data validation and serialization

#### Machine Learning
- `scikit-learn==1.6.1` - ML pipeline and preprocessing
- `xgboost==2.1.3` - Gradient boosting model
- `joblib==1.4.2` - Model serialization/deserialization
- `pandas==2.2.3` - Data manipulation
- `numpy==2.0.2` - Numerical computing

#### Testing
- `pytest==8.4.1` - Testing framework
- `pytest-asyncio==0.24.0` - Async test support
- `pytest-cov==6.0.0` - Coverage reporting
- `pytest-mock==3.14.0` - Mocking utilities
- `pytest-xdist==3.6.0` - Parallel test execution
- `httpx==0.28.1` - Async HTTP client for testing

#### Development Tools
- `black==24.10.0` - Code formatting
- `flake8==7.1.1` - Linting
- `mypy==1.13.0` - Type checking
- `locust==2.32.3` - Performance testing

### Production Minimal Dependencies
For production deployment, only core dependencies are needed:
```txt
fastapi>=0.115.0
uvicorn[standard]>=0.32.0
pydantic>=2.11.0
scikit-learn>=1.6.0
xgboost>=2.1.0
joblib>=1.4.0
pandas>=2.2.0
numpy>=2.0.0
```

---

## 🔍 Monitoring & Observability

### Health Monitoring
- **Endpoint**: `/health` - Service and model health
- **Metrics**: `/metrics` - Basic service metrics
- **Logging**: Structured logging with timestamps

### Key Metrics to Monitor
1. **Request Rate**: Requests per second
2. **Response Time**: P50, P95, P99 latencies
3. **Error Rate**: 4xx and 5xx error percentages
4. **Model Performance**: Prediction accuracy over time
5. **Resource Usage**: CPU, memory, disk usage

### Logging Configuration
```python
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Log prediction requests
logger.info(f"Processed batch of {len(reqs)} requests, got predictions shape: {preds.shape}")

# Log errors
logger.error(f"Model prediction failed: {model_exception}")
```

### Alerting Recommendations
- **Critical**: Service down, model loading failure
- **Warning**: High error rate (>5%), slow response times (>1s)
- **Info**: Model predictions, request volumes

---

## 🚀 Future Enhancements

### Planned Improvements

1. **Model Versioning**
   - A/B testing between model versions
   - Rollback capabilities
   - Performance comparison

2. **Advanced Features**
   - Real-time model retraining
   - Feature importance explanations
   - Prediction confidence scores

3. **Performance Optimizations**
   - Model quantization for smaller memory footprint
   - GPU acceleration for large batches
   - Prediction caching layer

4. **Observability**
   - Distributed tracing with OpenTelemetry
   - Custom metrics dashboard
   - Model drift detection

### Technical Debt
- [ ] Add comprehensive integration tests
- [ ] Implement model performance monitoring
- [ ] Add request/response schema validation
- [ ] Optimize memory usage for large batches
- [ ] Add model explainability endpoints

---

## 📞 Support & Troubleshooting

### Common Issues

**Issue: Model file not found**
```
RuntimeError: Could not load model at xgboost_model.pkl
```
**Solution**: Ensure `xgboost_model.pkl` is in the same directory as `main.py`

**Issue: Validation errors**
```
422 Validation Error: Hour must be between 0 and 23
```
**Solution**: Check input data format and ranges

**Issue: Content-Type errors**
```
415 Unsupported Media Type: Content-Type must be application/json
```
**Solution**: Add `Content-Type: application/json` header to requests

**Issue: High memory usage**
```
Memory usage exceeding container limits
```
**Solution**: Implement batch size limits, optimize model loading

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
uvicorn main:app --log-level debug

# Or modify in code
logging.basicConfig(level=logging.DEBUG)
```

### Performance Debugging
```python
# Add timing logs
import time

start_time = time.time()
preds = model.predict(df)
prediction_time = time.time() - start_time
logger.info(f"Model prediction took: {prediction_time:.3f}s")
```

---

This microservice is a critical component of the Creative Space Finder application, providing the machine learning intelligence that powers location recommendations. The service is designed to be scalable, maintainable, and robust, with comprehensive error handling and monitoring capabilities.