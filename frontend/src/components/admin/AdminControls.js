'use client';
import React, { useState, useEffect } from 'react';
import { Zap, Heart, BarChart3, RefreshCw, CheckCircle, Clock, Activity, XCircle } from 'lucide-react';
import { fetchAdminData, warmCache } from '../../lib/api';

const AdminControls = ({ setActiveTab }) => {
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [operationLoading, setOperationLoading] = useState(false);

  useEffect(() => {
    console.log('⚙️ AdminControls mounted, loading admin data...');
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    console.log('🔄 Loading admin data...');
    setLoading(true);
    setError(null);
    try {
      const adminData = await fetchAdminData();
      console.log('📋 Admin data received:', adminData);
      setData(adminData);
    } catch (err) {
      console.error('❌ Admin data error:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleWarmCache = async () => {
    console.log('🔥 Initiating ASYNC cache warming...');
    setOperationLoading(true);
    setError(null);
    
    try {
      const result = await warmCache();
      console.log('✅ Cache warming initiated:', result);
      
      // Show detailed success message for async operation
      const successMessage = `Cache Warming Started Successfully!\n\n` +
                            `${result.message || 'Process initiated in background'}\n\n` +
                            `Expected Duration: 10-15 minutes\n` +
                            `Process runs in asynchronously!\n` +
                            `✅ You can continue using the app normally`;
      
      alert(successMessage);
      
      // Update UI to show cache warming in progress
      setData(prev => ({ 
        ...prev, 
        cacheStatus: "Cache warming in progress... Check server logs for updates.",
        lastChecked: new Date().toLocaleString()
      }));
      
      // Refresh admin data
      await loadAdminData();
      
    } catch (err) {
      console.error('Cache warming error:', err);
      setError(err.message);
      
      let errorMessage = `❌ Cache Warming Failed\n\n${err.message}`;
      
      if (err.message.includes('502')) {
        errorMessage += `\n\n🔧 This 502 error indicates a timeout issue.\n` +
                       `The async implementation should fix this.\n` +
                       `If you still see this error, there may be:\n` +
                       `• Kubernetes ingress timeout limits\n` +
                       `• Load balancer timeout settings\n` +
                       `• Network infrastructure limits`;
      }
      
      alert(errorMessage);
    } finally {
      setOperationLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Admin Controls</h2>
        <button 
          onClick={loadAdminData}
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

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <XCircle className="h-5 w-5 text-red-400 mr-2" />
            <p className="text-red-800">Error: {error}</p>
          </div>
        </div>
      )}
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Cache Management */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4 flex items-center">
            <Zap className="h-5 w-5 mr-2 text-blue-500" />
            Cache Management
          </h3>
          <div className="space-y-4">
            <button 
              onClick={handleWarmCache}
              disabled={operationLoading || loading}
              className="w-full text-white px-4 py-3 rounded flex items-center justify-center transition-colors disabled:opacity-50"
              style={{backgroundColor: operationLoading || loading ? '#93c5fd' : '#3b82f6'}}
              onMouseEnter={(e) => !operationLoading && !loading && (e.target.style.backgroundColor = '#2563eb')}
              onMouseLeave={(e) => !operationLoading && !loading && (e.target.style.backgroundColor = '#3b82f6')}
            >
              {operationLoading ? (
                <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Zap className="h-4 w-4 mr-2" />
              )}
              {operationLoading ? 'Starting Cache Warming...' : 'Warm Cache Now'}
            </button>
            
            {data.cacheStatus && (
              <div className="mt-4 p-4 bg-gray-50 rounded">
                <h4 className="font-semibold mb-2">Cache Status</h4>
                <p className="text-sm text-gray-600">{data.cacheStatus}</p>
                {data.lastChecked && (
                  <p className="text-xs text-gray-500 mt-2">Last checked: {data.lastChecked}</p>
                )}
              </div>
            )}

            <div className="border-t pt-4">
              <h4 className="font-semibold mb-2">Cache Information</h4>
              <div className="text-sm text-gray-600 space-y-1">
                <p>• Daily cache warming runs at 3 AM automatically</p>
                <p>• Manual warming processes ~280 combinations</p>
                <p>• Cache expires after 24 hours</p>
                <p>• Maximum 1000 cached entries</p>
                <p>• <strong>NEW:</strong> Async warming prevents timeouts</p>
              </div>
            </div>
          </div>
        </div>

        {/* System Operations */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4 flex items-center">
            <Activity className="h-5 w-5 mr-2 text-green-500" />
            System Operations
          </h3>
          <div className="space-y-4">
            <button 
              onClick={() => setActiveTab('health')}
              className="w-full text-white px-4 py-3 rounded flex items-center justify-center transition-colors"
              style={{backgroundColor: '#10b981'}}
              onMouseEnter={(e) => e.target.style.backgroundColor = '#059669'}
              onMouseLeave={(e) => e.target.style.backgroundColor = '#10b981'}
            >
              <Heart className="h-4 w-4 mr-2" />
              Check System Health
            </button>
            
            <button 
              onClick={() => setActiveTab('analytics')}
              className="w-full text-white px-4 py-3 rounded flex items-center justify-center transition-colors"
              style={{backgroundColor: '#8b5cf6'}}
              onMouseEnter={(e) => e.target.style.backgroundColor = '#7c3aed'}
              onMouseLeave={(e) => e.target.style.backgroundColor = '#8b5cf6'}
            >
              <BarChart3 className="h-4 w-4 mr-2" />
              View Analytics
            </button>

            <div className="border-t pt-4">
              <h4 className="font-semibold mb-2">System Information</h4>
              <div className="text-sm text-gray-600 space-y-1">
                <p>• Backend: Spring Boot with Java</p>
                <p>• Database: PostgreSQL (Supabase)</p>
                <p>• ML Service: FastAPI + XGBoost</p>
                <p>• Weather API: OpenWeather Pro</p>
                <p>• <strong>Cache:</strong> Caffeine with statistics enabled</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Operations Log */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4">System Status & Scheduled Tasks</h3>
        <div className="space-y-3">
          <div className="flex items-center text-sm">
            <CheckCircle className="h-4 w-4 text-green-500 mr-3" />
            <span className="text-gray-700">Daily cache warming scheduled for 3:00 AM</span>
            <span className="ml-auto text-gray-500">Active</span>
          </div>
          <div className="flex items-center text-sm">
            <Clock className="h-4 w-4 text-blue-500 mr-3" />
            <span className="text-gray-700">Connection monitoring every 30 seconds</span>
            <span className="ml-auto text-gray-500">Running</span>
          </div>
          <div className="flex items-center text-sm">
            <Activity className="h-4 w-4 text-purple-500 mr-3" />
            <span className="text-gray-700">Analytics collection and tracking</span>
            <span className="ml-auto text-gray-500">Enabled</span>
          </div>
          <div className="flex items-center text-sm">
            <Heart className="h-4 w-4 text-red-500 mr-3" />
            <span className="text-gray-700">Health monitoring dashboard</span>
            <span className="ml-auto text-gray-500">Active</span>
          </div>
          <div className="flex items-center text-sm">
            <Zap className="h-4 w-4 text-yellow-500 mr-3" />
            <span className="text-gray-700">Async cache warming system</span>
            <span className="ml-auto text-gray-500">Ready</span>
          </div>
        </div>
      </div>

      {/* Cache Status Indicator */}
      {data.cacheStatus && data.cacheStatus.includes('in progress') && (
        <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
          <div className="flex items-center">
            <RefreshCw className="h-5 w-5 text-blue-500 mr-2 animate-spin" />
            <div>
              <p className="text-blue-800 font-medium">Cache Warming in Progress</p>
              <p className="text-blue-600 text-sm">Background process is running. Check server logs for detailed progress updates.</p>
            </div>
          </div>
        </div>
      )}

      {/* Loading State */}
      {loading && (
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center justify-center">
            <RefreshCw className="h-6 w-6 animate-spin text-blue-500 mr-2" />
            <span className="text-gray-600">Loading admin data...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminControls;
