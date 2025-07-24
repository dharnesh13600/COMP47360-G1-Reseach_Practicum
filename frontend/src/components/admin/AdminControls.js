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
    console.log('🔥 Initiating cache warming...');
    setOperationLoading(true);
    setError(null);
    try {
      const result = await warmCache();
      console.log('✅ Cache warming result:', result);
      
      // Show success message
      alert(result.message || 'Cache warming initiated successfully!');
      
      // Refresh admin data
      await loadAdminData();
    } catch (err) {
      console.error('❌ Cache warming error:', err);
      setError(err.message);
      alert('Cache warming failed: ' + err.message);
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
          className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white px-4 py-2 rounded flex items-center"
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
              className="w-full bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white px-4 py-3 rounded flex items-center justify-center transition-colors"
            >
              {operationLoading ? (
                <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Zap className="h-4 w-4 mr-2" />
              )}
              {operationLoading ? 'Warming Cache...' : 'Warm Cache Now'}
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
              className="w-full bg-green-500 hover:bg-green-600 text-white px-4 py-3 rounded flex items-center justify-center transition-colors active:text-orange-500"

            >
              <Heart className="h-4 w-4 mr-2" />
              Check System Health
            </button>
            
            <button 
              onClick={() => setActiveTab('analytics')}
              className="w-full bg-purple-500 hover:bg-purple-600 text-white px-4 py-3 rounded flex items-center justify-center transition-colors active:text-orange-500"
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
        </div>
      </div>

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