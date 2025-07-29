// References:
// https://www.createwithdata.com/react-chartjs-dashboard/
// https://medium.com/@mohdkhan.mk99/interactive-dashboards-recharts-react-grid-layout-a12952bbd0e0
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat

// Import the hooks for client-side
import React, { useState, useEffect } from 'react';
import { 
  Cpu, Database, Cloud, Activity, Zap, RefreshCw, 
  CheckCircle, XCircle, AlertCircle, Users 
} from 'lucide-react'; // I use these icons for the UI frm lucide

// Here I import from the Health api
import { fetchHealthData } from '../../lib/api';

// This is the main for the system health, showing the system status data, loading state and error handler setting
const HealthMonitor = () => {
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // When the component finally shows, get the data and do the auto refresh
  useEffect(() => {
    loadHealthData();
    // Refresh every 30 seconds automatically
    const interval = setInterval(loadHealthData, 30000);
    return () => clearInterval(interval);
  }, []);

  // This function loads in the health data from the API 
  const loadHealthData = async () => {
    setLoading(true);
    setError(null);
    try {
      const healthData = await fetchHealthData();
      setData(healthData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // This is all the various return icons that are given for the system health
  const getStatusIcon = (status) => {
    switch (status?.toUpperCase()) {
      case 'HEALTHY':
      case 'CONNECTED':
      case 'ACTIVE':
      case 'MONITORED':
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case 'ERROR':
      case 'DISCONNECTED':
        return <XCircle className="h-5 w-5 text-red-500" />;
      default:
        return <AlertCircle className="h-5 w-5 text-yellow-500" />;
    }
  };

  // This sets the colour based on the health status
  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'HEALTHY':
      case 'CONNECTED':
      case 'ACTIVE':
      case 'MONITORED':
        return 'text-green-600';
      case 'ERROR':
      case 'DISCONNECTED':
        return 'text-red-600';
      default:
        return 'text-yellow-600';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">System Health Monitor</h2>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-500">Auto-refresh: 30s</span>
          <button 
            onClick={loadHealthData}
            disabled={loading}
            className="text-white px-4 py-2 rounded flex items-center disabled:opacity-50"
            style={{backgroundColor: loading ? '#93c5fd' : '#3b82f6'}}
            onMouseEnter={(e) => !loading && (e.target.style.backgroundColor = '#2563eb')}
            onMouseLeave={(e) => !loading && (e.target.style.backgroundColor = '#3b82f6')}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <XCircle className="h-5 w-5 text-red-400 mr-2" />
            <p className="text-red-800">Error loading health data: {error}</p>
          </div>
        </div>
      )}
        
      {data.health && (
        <>
          {/* System Overview */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold mb-4 flex items-center">
              <Cpu className="h-5 w-5 mr-2 text-blue-500" />
              System Overview
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="text-center p-4 bg-gray-50 rounded">
                <div className="flex items-center justify-center mb-2">
                  {getStatusIcon(data.health.system?.status)}
                </div>
                <p className="text-sm text-gray-600">Status</p>
                <p className={`font-semibold ${getStatusColor(data.health.system?.status)}`}>
                  {data.health.system?.status}
                </p>
              </div>
              <div className="text-center p-4 bg-gray-50 rounded">
                <p className="text-sm text-gray-600">Version</p>
                <p className="font-semibold text-lg">{data.health.system?.version}</p>
              </div>
              <div className="text-center p-4 bg-gray-50 rounded">
                <p className="text-sm text-gray-600">Uptime</p>
                <p className="font-semibold text-lg">
                  {data.health.system?.uptimeHours}h {data.health.system?.uptimeMinutes}m
                </p>
              </div>
              <div className="text-center p-4 bg-gray-50 rounded">
                <p className="text-sm text-gray-600">Java Version</p>
                <p className="font-semibold">{data.health.system?.javaVersion}</p>
              </div>
            </div>
          </div>

          {/* Database Health */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold mb-4 flex items-center">
              <Database className="h-5 w-5 mr-2 text-green-500" />
              Database Health
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h4 className="font-semibold mb-3 flex items-center">
                  {getStatusIcon(data.health.database?.status)}
                  <span className="ml-2">Connection Pool</span>
                </h4>
                {data.health.database?.connectionPool && (
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Active Connections:</span>
                      <span className="font-medium">{data.health.database.connectionPool.activeConnections}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Idle Connections:</span>
                      <span className="font-medium">{data.health.database.connectionPool.idleConnections}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Total Connections:</span>
                      <span className="font-medium">{data.health.database.connectionPool.totalConnections}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Max Pool Size:</span>
                      <span className="font-medium">{data.health.database.connectionPool.maxPoolSize}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                      <div className="w-full rounded-full h-2" style={{backgroundColor: '#e5e7eb'}}>
                        <div 
                          className="h-2 rounded-full" 
                          style={{
                            width: `${(data.health.database.connectionPool.totalConnections / data.health.database.connectionPool.maxPoolSize) * 100}%`,
                            backgroundColor: '#2563eb'
                          }}
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>
              <div>
                <h4 className="font-semibold mb-3">Data Statistics</h4>
                {data.health.database?.dataStatistics && (
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Total Activities:</span>
                      <span className="font-medium">{data.health.database.dataStatistics.totalActivities}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Location Scores:</span>
                      <span className="font-medium">{data.health.database.dataStatistics.totalLocationScores?.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>ML Coverage:</span>
                      <span className="font-medium">{data.health.database.dataStatistics.mlCoveragePercentage?.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between">
                      <span>ML Prediction Logs:</span>
                      <span className="font-medium">{data.health.database.dataStatistics.mlPredictionLogs}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                      <div className="w-full rounded-full h-2" style={{backgroundColor: '#e5e7eb'}}>
                        <div 
                          className="h-2 rounded-full" 
                          style={{
                            width: `${data.health.database.dataStatistics.mlCoveragePercentage}%`,
                            backgroundColor: '#059669'
                          }}
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* External Services */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* ML Model Health */}
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Activity className="h-5 w-5 mr-2 text-purple-500" />
                ML Model Service
              </h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span>Status:</span>
                  <div className="flex items-center">
                    {getStatusIcon(data.health.mlModel?.status)}
                    <span className={`ml-2 font-medium ${getStatusColor(data.health.mlModel?.status)}`}>
                      {data.health.mlModel?.status}
                    </span>
                  </div>
                </div>
                {data.health.mlModel?.responseTimeMs && (
                  <div className="flex justify-between">
                    <span>Response Time:</span>
                    <span className="font-medium">{data.health.mlModel.responseTimeMs}ms</span>
                  </div>
                )}
                {data.health.mlModel?.lastTestedTimestamp && (
                  <div className="flex justify-between">
                    <span>Last Tested:</span>
                    <span className="font-medium text-sm">
                      {new Date(data.health.mlModel.lastTestedTimestamp).toLocaleString()}
                    </span>
                  </div>
                )}
                <div className="mt-4 p-3 bg-gray-50 rounded">
                  <p className="text-sm text-gray-600">
                    XGBoost model running on FastAPI at localhost:8000
                  </p>
                </div>
              </div>
            </div>

            {/* Weather API Health */}
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Cloud className="h-5 w-5 mr-2 text-blue-500" />
                Weather API
              </h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span>Status:</span>
                  <div className="flex items-center">
                    {getStatusIcon(data.health.weatherApi?.status)}
                    <span className={`ml-2 font-medium ${getStatusColor(data.health.weatherApi?.status)}`}>
                      {data.health.weatherApi?.status}
                    </span>
                  </div>
                </div>
                {data.health.weatherApi?.responseTimeMs && (
                  <div className="flex justify-between">
                    <span>Response Time:</span>
                    <span className="font-medium">{data.health.weatherApi.responseTimeMs}ms</span>
                  </div>
                )}
                {data.health.weatherApi?.forecastHours && (
                  <div className="flex justify-between">
                    <span>Forecast Hours:</span>
                    <span className="font-medium">{data.health.weatherApi.forecastHours}</span>
                  </div>
                )}
                <div className="mt-4 p-3 bg-gray-50 rounded">
                  <p className="text-sm text-gray-600">
                    OpenWeather Pro API - 96-hour hourly forecasts
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Cache Performance */}
          {data.health.cache && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Zap className="h-5 w-5 mr-2 text-yellow-500" />
                Cache Performance
              </h3>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {data.health.cache.statistics && (
                  <>
                    <div className="text-center p-3 bg-green-50 rounded">
                      <p className="text-sm text-gray-600">Hit Rate</p>
                      <p className="font-bold text-lg text-green-600">
                        {(data.health.cache.statistics.hitRate * 100).toFixed(1)}%
                      </p>
                    </div>
                    <div className="text-center p-3 bg-blue-50 rounded">
                      <p className="text-sm text-gray-600">Total Requests</p>
                      <p className="font-bold text-lg text-blue-600">
                        {data.health.cache.statistics.requestCount?.toLocaleString()}
                      </p>
                    </div>
                    <div className="text-center p-3 bg-purple-50 rounded">
                      <p className="text-sm text-gray-600">Cache Size</p>
                      <p className="font-bold text-lg text-purple-600">
                        {data.health.cache.statistics.estimatedSize}
                      </p>
                    </div>
                    <div className="text-center p-3 bg-orange-50 rounded">
                      <p className="text-sm text-gray-600">Evictions</p>
                      <p className="font-bold text-lg text-orange-600">
                        {data.health.cache.statistics.evictionCount}
                      </p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 rounded">
                      <p className="text-sm text-gray-600">Max Size</p>
                      <p className="font-bold text-lg text-gray-600">
                        {data.health.cache.maxSize || 1000}
                      </p>
                    </div>
                  </>
                )}
              </div>
            </div>
          )}

          {/* Resource Utilization */}
          {data.health.resources && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Users className="h-5 w-5 mr-2 text-indigo-500" />
                Resource Utilization
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="font-semibold mb-3">Memory Usage</h4>
                  {data.health.resources.memory && (
                    <div className="space-y-3">
                      <div className="flex justify-between text-sm">
                        <span>Heap Used:</span>
                        <span className="font-medium">
                          {(data.health.resources.memory.heapUsedBytes / 1024 / 1024).toFixed(1)} MB
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span>Heap Max:</span>
                        <span className="font-medium">
                          {(data.health.resources.memory.heapMaxBytes / 1024 / 1024).toFixed(1)} MB
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span>Utilization:</span>
                        <span className="font-medium">
                          {data.health.resources.memory.heapUtilizationPercent?.toFixed(1)}%
                        </span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-3">
                        <div className="w-full rounded-full h-3" style={{backgroundColor: '#e5e7eb'}}>
                          <div 
                            className="h-3 rounded-full"
                            style={{
                              width: `${Math.min(data.health.resources.memory.heapUtilizationPercent, 100)}%`,
                              backgroundColor: data.health.resources.memory.heapUtilizationPercent > 80 
                                ? '#ef4444' 
                                : data.health.resources.memory.heapUtilizationPercent > 60 
                                  ? '#eab308' 
                                  : '#10b981'
                            }}
                          />
                        </div>
                      </div>
                    </div>
                  )}
                </div>
                <div>
                  <h4 className="font-semibold mb-3">System Info</h4>
                  {data.health.resources.system && (
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span>Available Processors:</span>
                        <span className="font-medium">{data.health.resources.system.availableProcessors}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Total Memory:</span>
                        <span className="font-medium">
                          {(data.health.resources.system.totalMemoryBytes / 1024 / 1024).toFixed(1)} MB
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Free Memory:</span>
                        <span className="font-medium">
                          {(data.health.resources.system.freeMemoryBytes / 1024 / 1024).toFixed(1)} MB
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>Max Memory:</span>
                        <span className="font-medium">
                          {(data.health.resources.system.maxMemoryBytes / 1024 / 1024).toFixed(1)} MB
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Performance Metrics */}
          {data.health.performance && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-lg font-semibold mb-4">Performance Metrics</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-blue-50 rounded">
                  <p className="text-sm text-gray-600">Avg Cache Hit Rate</p>
                  <p className="font-bold text-xl text-blue-600">
                    {data.health.performance.averageCacheHitRate 
                      ? (data.health.performance.averageCacheHitRate * 100).toFixed(1) + '%'
                      : 'N/A'
                    }
                  </p>
                </div>
                <div className="text-center p-4 bg-green-50 rounded">
                  <p className="text-sm text-gray-600">Avg Response Time</p>
                  <p className="font-bold text-xl text-green-600">
                    {data.health.performance.averageResponseTimeMs?.toFixed(0) || 'N/A'}ms
                  </p>
                </div>
                <div className="text-center p-4 bg-purple-50 rounded">
                  <p className="text-sm text-gray-600">Requests (7 days)</p>
                  <p className="font-bold text-xl text-purple-600">
                    {data.health.performance.requestsLast7Days || 0}
                  </p>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

// Export component
export default HealthMonitor;