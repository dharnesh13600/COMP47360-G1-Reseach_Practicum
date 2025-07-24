// Base API configuration
const API_BASE = `${process.env.NEXT_PUBLIC_BACKEND_API_URL}/api`; 
// const API_BASE = process.env.NEXT_PUBLIC_BACKEND_API_URL; 


console.log('🔧 API Base URL:', API_BASE);

// Generic fetch wrapper with error handling and debugging
const fetchAPI = async (endpoint, options = {}) => {
  const url = `${API_BASE}${endpoint}`;
  
  console.log(`📡 Making request to: ${url}`);
  console.log(`📋 Request options:`, options);
  
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    console.log(`📊 Response status: ${response.status} ${response.statusText}`);

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`❌ HTTP Error ${response.status}:`, errorText);
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    console.log(`✅ Response data for ${endpoint}:`, data);
    return data;
  } catch (error) {
    console.error(`🚨 API Error for ${endpoint}:`, error);
    
    // If it's a network error, provide more context
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      console.error('🌐 Network Error: Backend might not be running or CORS issue');
      throw new Error(`Cannot connect to backend at ${url}. Is your Java backend running on port 8080?`);
    }
    
    throw error;
  }
};

// Analytics API functions
export const fetchAnalyticsData = async () => {
  console.log('🔄 Starting fetchAnalyticsData...');
  
  const endpoints = [
    '/analytics/dashboard',
    '/analytics/popular-combinations',
    '/analytics/cache-performance',
    '/analytics/activity-trends',
    '/analytics/hourly-patterns',
    '/analytics/recent-activity'
  ];

  const results = {};
  
  // Fetch all analytics endpoints
  for (const endpoint of endpoints) {
    try {
      console.log(`📈 Fetching ${endpoint}...`);
      const data = await fetchAPI(endpoint);
      const key = endpoint.split('/').pop();
      results[key] = data;
      console.log(`✅ Successfully fetched ${key}:`, data);
    } catch (error) {
      console.warn(`⚠️ Failed to fetch ${endpoint}:`, error.message);
      const key = endpoint.split('/').pop();
      results[key] = null;
    }
  }

  console.log('📊 Final analytics results:', results);
  return results;
};

// Admin API functions - Handle text responses
export const fetchAdminData = async () => {
  console.log('⚙️ Starting fetchAdminData...');
  
  try {
    const response = await fetch(`${API_BASE}/admin/cache-status`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    console.log(`📊 Admin response status: ${response.status} ${response.statusText}`);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    // Admin endpoints return plain text, not JSON
    const cacheStatusText = await response.text();
    console.log('✅ Admin cache status fetched:', cacheStatusText);
    
    return { 
      cacheStatus: cacheStatusText,
      lastChecked: new Date().toLocaleString(),
      status: 'success'
    };
  } catch (error) {
    console.error('❌ Error fetching admin data:', error.message);
    return { 
      cacheStatus: 'Error loading cache status: ' + error.message,
      lastChecked: new Date().toLocaleString(),
      status: 'error'
    };
  }
};

export const warmCache = async () => {
  console.log('🔥 Starting cache warming...');
  
  try {
    const response = await fetch(`${API_BASE}/admin/warm-cache`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    console.log(`📊 Cache warming response status: ${response.status} ${response.statusText}`);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    // Admin warm-cache endpoint returns plain text, not JSON
    const resultText = await response.text();
    console.log('✅ Cache warming response:', resultText);
    
    return { 
      message: resultText,
      success: true,
      timestamp: new Date().toLocaleString()
    };
  } catch (error) {
    console.error('❌ Cache warming failed:', error.message);
    throw new Error(`Cache warming failed: ${error.message}`);
  }
};

// Health API functions
export const fetchHealthData = async () => {
  console.log('🏥 Starting fetchHealthData...');
  
  try {
    const health = await fetchAPI('/health');
    console.log('✅ Health data fetched successfully:', health);
    return { health };
  } catch (error) {
    console.error('❌ Error fetching health data:', error.message);
    throw error;
  }
};

// Forecast API functions (for your weather endpoints)
export const fetchForecastData = async () => {
  console.log('🌤️ Starting fetchForecastData...');
  
  try {
    const forecast = await fetchAPI('/forecast');
    console.log('✅ Forecast data fetched successfully:', forecast);
    return forecast;
  } catch (error) {
    console.error('❌ Error fetching forecast data:', error.message);
    throw error;
  }
};

export const fetchForecastForDateTime = async (dateTime) => {
  console.log(`🌤️ Fetching forecast for: ${dateTime}`);
  
  try {
    const weather = await fetchAPI(`/forecast?datetime=${dateTime}`);
    console.log('✅ DateTime forecast fetched successfully:', weather);
    return weather;
  } catch (error) {
    console.error('❌ Error fetching datetime forecast:', error.message);
    throw error;
  }
};

export const fetchAvailableDateTimes = async () => {
  console.log('📅 Fetching available forecast datetimes...');
  
  try {
    const dateTimes = await fetchAPI('/forecast/available-datetimes');
    console.log('✅ Available datetimes fetched successfully:', dateTimes);
    return dateTimes;
  } catch (error) {
    console.error('❌ Error fetching available datetimes:', error.message);
    throw error;
  }
};

// Recommendations API functions
export const fetchRecommendations = async (requestData) => {
  console.log('🎯 Starting fetchRecommendations with data:', requestData);
  
  try {
    const recommendations = await fetchAPI('/recommendations', {
      method: 'POST',
      body: JSON.stringify(requestData),
    });
    console.log('✅ Recommendations fetched successfully:', recommendations);
    return recommendations;
  } catch (error) {
    console.error('❌ Error fetching recommendations:', error.message);
    throw error;
  }
};

export const fetchActivities = async () => {
  console.log('🎨 Fetching available activities...');
  
  try {
    const activities = await fetchAPI('/recommendations/activities');
    console.log('✅ Activities fetched successfully:', activities);
    return activities;
  } catch (error) {
    console.error('❌ Error fetching activities:', error.message);
    throw error;
  }
};

export const fetchZones = async () => {
  console.log('🗺️ Fetching available zones...');
  
  try {
    const zones = await fetchAPI('/recommendations/zones');
    console.log('✅ Zones fetched successfully:', zones);
    return zones;
  } catch (error) {
    console.error('❌ Error fetching zones:', error.message);
    throw error;
  }
};

// Test function to check backend connectivity
export const testBackendConnection = async () => {
  console.log('🔌 Testing backend connection...');
  
  try {
    // Try the simplest endpoint first
    const response = await fetch(`${API_BASE}/health`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      console.log('✅ Backend connection successful!');
      return { connected: true, status: response.status };
    } else {
      console.error('❌ Backend responded with error:', response.status);
      return { connected: false, status: response.status };
    }
  } catch (error) {
    console.error('❌ Cannot connect to backend:', error.message);
    return { connected: false, error: error.message };
  }
};