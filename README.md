# Manhattan Muse 🎨🗽

A location recommendation system for artists in Manhattan that suggests optimal locations for artistic activities based on crowd levels and past event data for the creative disciplines.

## Project Overview

Manhattan Muse is a summer research project that creates a web application to help artists find the perfect spots in Manhattan to perform their artistic activities. Users input their activity type, preferred date/time, and receive personalized location recommendations with real-time weather forecasts.

### Key Features
- **Activity-Based Recommendations**: Get location suggestions tailored to your artistic activity
- **Crowd Level Analysis**: Find spots with optimal crowd density for your needs
- **Creative Proximity Scoring**: Locations are scored based on nearby art and cultural centres, past events in the area.
- **Weather Integration**: Real-time weather forecasts for planned activities
- **Location Comparision**: Compare up to three potential locations
- **Zone-Specific Search**: Browse recommendations by Manhattan neighborhoods
- **Interactive Map**: Visual exploration of recommended locations with MapBox integration
- **Admin Dashboard**: Backend management and cache warming capabilities

## Architecture

### Frontend (Next.js)
- **Design Mockup**: Figma
- **Framework**: Next.js with React
- **Styling**: CSS Modules with responsive design
- **Maps**: MapBox GL JS integration
- **State Management**: React hooks and context

### Backend (Java Spring Boot)
- **Framework**: Spring Boot with REST APIs
- **Authentication**: Session-based admin authentication with BCrypt
- **Caching**: Precomputed recommendations with daily cache warming
- **Database**: SQL database with comprehensive ERD design
- **Microservices**: Modular architecture for scalability

### Data Analytics (Python)
- **ML Pipeline**: Python microservice processing analytics data
- **Data Storage**: Pickled model files (.pkl) for fast inference
- **Integration**: Seamless connection with Java backend services

### External Services
- **Weather API**: OpenWeather API integration
- **Maps**: MapBox for interactive mapping
- **Geocoding**: Google Maps API for location services

## Web Design 
**Design Process, Key Elements and Changes**: [https://drive.google.com/file/d/1nqOlq860SyPoxcr4U6ZHBvl_H_NyLCZY/view?usp=sharing](https://drive.google.com/drive/folders/1WgBpJfn9HCT-EWYug160Cbf_rA_pVuan?usp=sharing)

--- 

## Project Structure
```plaintext
.
├── backend/
│   ├── src/main/java/com/creativespacefinder/manhattan/
│   │   ├── controller/      # REST API endpoints (e.g., RecommendationController)
│   │   ├── service/         # Core business logic (e.g., LocationRecommendationService)
│   │   ├── repository/      # Data access layer using Spring Data JPA
│   │   └── entity/          # Database models/entities (e.g., EventLocation, TaxiZone)
│   ├── src/test/            # Unit and integration tests (JUnit, Mockito, Testcontainers)
│   ├── pom.xml              # Maven project dependencies and build configuration
│   └── Dockerfile           # Container definition for the Java API
│
├── data-analytics/
│   ├── data pre-processing/ # Jupyter notebooks for initial data cleaning (e.g., Yellow_Taxi_cleaning.ipynb)
│   ├── data processing/     # Scripts for feature engineering (e.g., creative_activity_score.ipynb)
│   └── data modeling/
│       ├── notebooks/       # Experimental models (RandomForest, CatBoost, LightGBM)
│       ├── main.py          # FastAPI application to serve the model
│       └── xgboost_model.pkl  # Final serialized prediction model
│
├── frontend/
│   ├── src/app/
│   │   ├── (site)/          # Public-facing pages (map, about, home)
│   │   ├── admin/           # Protected admin dashboard routes
│   │   ├── api/             # Next.js API routes for server-side functions
│   │   └── components/      # Reusable React components (map, sidebar, dropdowns)
│   ├── public/              # Static assets (images, icons)
│   ├── next.config.mjs      # Next.js application configuration
│   └── Dockerfile           # Multi-stage container definition for the Next.js app
│
├── k8s/
│   ├── backend-deployment.tmpl   # Kubernetes manifest for deploying the backend
│   ├── frontend-deployment.tmpl  # Kubernetes manifest for deploying the frontend
│   └── ingress.tmpl              # Kubernetes Ingress resource configuration
│
├── load-testing/
│   └── ManhattanMuse-Heavy-Test.jmx # JMeter test plan for high-traffic simulation
│
├── nginx/
│   ├── nginx.conf           # Reverse proxy rules for the Ingress controller
│   └── Dockerfile           # Container definition for the Nginx proxy
│
└── README.md
```
---

## Setup & Installation

### Prerequisites
- **Java 17+** for backend services
- **Node.js 18+** for frontend development
- **Python 3.8+** for data analytics microservice
- **PostgreSQL** for database
- **Docker** for containerized deployments

### Environment Variables

#### Backend (.env)
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/manhattan_muse_database
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# Admin Authentication
admin.username=admin
admin.password.hash=$2a$10$your_bcrypt_hash_here

# External APIs
openweather.api.key=your_openweather_api_key
google.maps.api.key=your_google_maps_api_key

# Backend API URL
BACKEND_API_URL=http://localhost:8080/api
```

#### Frontend (.env.local)
```env
NEXT_PUBLIC_BACKEND_API_URL=http://localhost:8080
NEXT_PUBLIC_MAPBOX_TOKEN=your_mapbox_token
NEXT_PUBLIC_MAPBOX_STYLE_URL=your_mapbox_style_url
NEXT_PUBLIC_MAPBOX_STYLE_DARK_URL=your_mapbox_dark_style_url
NEXT_PUBLIC_GOOGLE_TOKEN=your_google_maps_api_key
NEXT_PUBLIC_USE_MAPBOX=true
```

### Installation Steps

1. **Clone the repository**
```bash
git clone https://github.com/dharnesh13600/COMP47360-G1-Reseach_Practicum.git
cd COMP47360-G1-Reseach_Practicum
```

2. **Backend Setup**
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

3. **Frontend Setup**
```bash
cd frontend
npm install
npm run dev
```

4. **Python Analytics Service**
```bash
cd data-analytics
pip install -r requirements.txt
uvicorn main:app --reload
```

5. **Database Setup**
- Import the provided SQL schema
- Run initial data migrations
- Verify database connectivity to Java backend

## Usage

### For Artists
1. **Select Activity**: Choose your artistic pursuit from the dropdown
2. **Pick Date & Time**: Select when you plan to create
3. **Choose Search Mode**:
   - **Recommended Area**: Get top locations across all of Manhattan
   - **Select Area**: Browse specific neighborhoods first
4. **View Results**: Explore recommendations with scores and weather
5. **Map Integration**: Visualize locations and get directions

### For Administrators
1. **Login**: Access admin dashboard with credentials
2. **Cache Management**: Manually trigger recommendation cache warming
3. **System Monitoring**: Check API health and cache status
4. **Session Management**: Monitor active admin sessions
5. **User Analytics**: View user selection insights

## Development

### Backend Development
- **Framework**: Spring Boot with Maven
- **Testing**: JUnit with comprehensive test coverage
- **Monitoring**: Actuator endpoints for health checks

### Frontend Development
- **Hot Reload**: Next.js development server
- **Component Structure**: Modular React components
- **Responsive Design**: Mobile-first approach
- **Map Integration**: Custom MapBox components

### Data Pipeline
- **Model Training**: Python scripts for recommendation algorithms
- **Data Processing**: ETL pipelines for location and activity data
- **Model Deployment**: Automated .pkl file generation and deployment

## Testing

### API Testing
Use the provided `test-all.http` file for comprehensive endpoint testing:
```bash
# Test basic recommendations
POST /api/recommendations
{
  "activity": "photography",
  "dateTime": "2025-07-18T10:00"
}

# Test zone-specific search
POST /api/recommendations
{
  "activity": "street photography",
  "dateTime": "2025-07-18T14:00",
  "selectedZone": "soho hudson square"
}
```

### Admin Authentication Testing
```bash
# Login first
POST /api/admin/login
{
  "username": "admin",
  "password": "your_password"
}

# Then access protected endpoints
POST /api/admin/warm-cache
```

### Analytics Testing
```bash
# Test analytics endpoints
GET /api/analytics/dashboard
GET /api/analytics/popular-combinations
GET /api/analytics/cache-performance
GET /api/analytics/activity-trends
GET /api/analytics/hourly-patterns
GET /api/analytics/recent-activity
```

## Deployment

### Our Production Deployment
- **Environment Configuration**: Separate configs for dev/staging/prod
- **Database Migration**: Automated schema updates
- **Cache Warming**: Scheduled daily precomputation at 3 AM
- **API Rate Limiting**: Implement rate limiting for external API calls
- **Security**: HTTPS, secure session management, input validation

### Monitoring & Logging
- **Application Logs**: Structured logging with appropriate levels
- **Performance Metrics**: API response times and success rates
- **Error Tracking**: Comprehensive error handling and reporting
- **Cache Analytics**: Monitor cache hit rates and performance

### CI/CD Pipelines
- **We use Jenkins to fully automate build, test, containerization and deploy steps for both frontend and backend services. 
- **The Docker images are created and stored in Google Artifact Registry(AR) and deployeed in Google Kubernetes Engine.
- **For the backend CICD we have used SonarQube for security testing and coverage.

## The Team

- **Backend Team Lead**: Mark Tully
- **Data Analytics Lead**: Rahul Murali
- **Frontend Lead**: Diviyya Shree Iyappan
- **Maintenance Lead**: Dharnesh Vasudev Indumathi Ramesh Kumar
- **Customer Lead**: Phirada Kanjanangkuplpunt
- **Coordination Lead**: Ting Li

## License

This project is developed as part of a summer research program, for COMP47360 via group 1.

---

**Manhattan Muse** - Connecting artists with the perfect creative spaces in the heart of New York City 🎨🏙️
