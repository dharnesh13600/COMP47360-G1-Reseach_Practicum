import React, { useState, useEffect } from 'react';
import { TrendingUp, Activity, Zap, Clock, RefreshCw, XCircle } from 'lucide-react';
import { fetchAnalyticsData } from '../../lib/api';
import { getDayName } from '../../utils/helpers';

const AnalyticsDashboard = () => {
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAnalyticsData();
  }, []);

  const loadAnalyticsData = async () => {
    setLoading(true);
    setError(null);
    try {
      const analyticsData = await fetchAnalyticsData();
      setData(analyticsData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <RefreshCw className="h-8 w-8 animate-spin text-blue-500" />
        <span className="ml-2 text-gray-600">Loading analytics data...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <XCircle className="h-5 w-5 text-red-400 mr-2" />
          <p className="text-red-800">Error loading analytics: {error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Analytics Dashboard</h2>
        <button 
          onClick={loadAnalyticsData}
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded flex items-center"
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </button>
      </div>
      
      {/* Dashboard Summary */}
      {data.dashboard && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-blue-50 p-4 rounded-lg">
            <div className="flex items-center">
              <TrendingUp className="h-8 w-8 text-blue-500 mr-3" />
              <div>
                <p className="text-sm text-gray-600">Total Requests</p>
                <p className="text-2xl font-bold">{data.dashboard.totalRequests || 0}</p>
              </div>
            </div>
          </div>
          <div className="bg-green-50 p-4 rounded-lg">
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-green-500 mr-3" />
              <div>
                <p className="text-sm text-gray-600">Total Activities</p>
                <p className="text-2xl font-bold">{data.dashboard.totalActivities || 0}</p>
              </div>
            </div>
          </div>
          <div className="bg-purple-50 p-4 rounded-lg">
            <div className="flex items-center">
              <Zap className="h-8 w-8 text-purple-500 mr-3" />
              <div>
                <p className="text-sm text-gray-600">Cache Hit Rate</p>
                <p className="text-2xl font-bold">{data.dashboard.avgCacheHitRate || '0%'}</p>
              </div>
            </div>
          </div>
          <div className="bg-orange-50 p-4 rounded-lg">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-orange-500 mr-3" />
              <div>
                <p className="text-sm text-gray-600">Recent Activity</p>
                <p className="text-2xl font-bold">{data.dashboard.recentActivityCount || 0}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Popular Combinations */}
      {data['popular-combinations'] && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Popular Activity Combinations</h3>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead>
                <tr className="bg-gray-50">
                  <th className="px-4 py-2 text-left">Activity</th>
                  <th className="px-4 py-2 text-left">Hour</th>
                  <th className="px-4 py-2 text-left">Day of Week</th>
                  <th className="px-4 py-2 text-left">Requests</th>
                  <th className="px-4 py-2 text-left">Avg Response Time</th>
                </tr>
              </thead>
              <tbody>
                {data['popular-combinations'].slice(0, 10).map((item, index) => (
                  <tr key={index} className="border-b hover:bg-gray-50">
                    <td className="px-4 py-2 font-medium">{item.activity}</td>
                    <td className="px-4 py-2">{item.hour}:00</td>
                    <td className="px-4 py-2">{getDayName(item.dayOfWeek)}</td>
                    <td className="px-4 py-2">{item.requestCount}</td>
                    <td className="px-4 py-2">{item.avgResponseTime}ms</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Cache Performance */}
      {data['cache-performance'] && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Cache Performance Statistics</h3>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead>
                <tr className="bg-gray-50">
                  <th className="px-4 py-2 text-left">Activity</th>
                  <th className="px-4 py-2 text-left">Hour</th>
                  <th className="px-4 py-2 text-left">Cache Hit Rate</th>
                  <th className="px-4 py-2 text-left">Total Requests</th>
                  <th className="px-4 py-2 text-left">Avg Response Time</th>
                </tr>
              </thead>
              <tbody>
                {data['cache-performance'].slice(0, 10).map((item, index) => (
                  <tr key={index} className="border-b hover:bg-gray-50">
                    <td className="px-4 py-2 font-medium">{item.activity}</td>
                    <td className="px-4 py-2">{item.hour}:00</td>
                    <td className="px-4 py-2">
                      <span className={`px-2 py-1 rounded text-sm font-medium ${
                        parseFloat(item.cacheHitRate) > 80 ? 'bg-green-100 text-green-800' : 
                        parseFloat(item.cacheHitRate) > 50 ? 'bg-yellow-100 text-yellow-800' : 
                        'bg-red-100 text-red-800'
                      }`}>
                        {item.cacheHitRate}
                      </span>
                    </td>
                    <td className="px-4 py-2">{item.totalRequests}</td>
                    <td className="px-4 py-2">{item.avgResponseTime}ms</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Hourly Patterns */}
      {data['hourly-patterns'] && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Hourly Usage Patterns</h3>
          <div className="grid grid-cols-6 md:grid-cols-12 gap-2">
            {data['hourly-patterns'].map((pattern, index) => {
              const maxRequests = Math.max(...data['hourly-patterns'].map(p => p.totalRequests));
              const height = Math.max((pattern.totalRequests / maxRequests) * 64, 8);
              
              return (
                <div key={index} className="text-center">
                  <div 
                    className="bg-blue-500 rounded mb-1 mx-auto transition-all hover:bg-blue-600" 
                    style={{height: `${height}px`, width: '20px'}}
                    title={`${pattern.hour}:00 - ${pattern.totalRequests} requests`}
                  />
                  <div className="text-xs text-gray-600">{pattern.hour}:00</div>
                  <div className="text-xs font-semibold">{pattern.totalRequests}</div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Activity Trends */}
      {data['activity-trends'] && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Activity Trends</h3>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead>
                <tr className="bg-gray-50">
                  <th className="px-4 py-2 text-left">Activity</th>
                  <th className="px-4 py-2 text-left">Total Requests</th>
                  <th className="px-4 py-2 text-left">Avg Response Time</th>
                  <th className="px-4 py-2 text-left">Last Requested</th>
                </tr>
              </thead>
              <tbody>
                {data['activity-trends'].slice(0, 10).map((item, index) => (
                  <tr key={index} className="border-b hover:bg-gray-50">
                    <td className="px-4 py-2 font-medium">{item.activity}</td>
                    <td className="px-4 py-2">{item.totalRequests}</td>
                    <td className="px-4 py-2">{Math.round(item.avgResponseTime)}ms</td>
                    <td className="px-4 py-2">
                      {item.lastRequested ? new Date(item.lastRequested).toLocaleDateString() : 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default AnalyticsDashboard;