'use client';
import React, { useState } from 'react';
import { Activity, LogOut, X } from 'lucide-react';
import DashboardHome from './DashboardHome';
import AnalyticsDashboard from './AnalyticsDashboard';
import AdminControls from './AdminControls';
import HealthMonitor from './HealthMonitor';

const AdminDashboard = ({ onLogout }) => {
  const [activeTab, setActiveTab] = useState('home');
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const handleLogoutConfirm = () => {
    setShowLogoutModal(false);
    onLogout();
  };

  const handleLogoutCancel = () => {
    setShowLogoutModal(false);
  };

  // Logout Modal Component (inline)
const LogoutModal = () => {
  if (!showLogoutModal) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div 
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)'
        }}
        onClick={handleLogoutCancel}
      ></div>
      
      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div 
          className="relative transform overflow-hidden rounded-lg shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6"
          style={{
            backgroundColor: '#ffffff',
            padding: '24px'
          }}
        >
          {/* Close button */}
          <div className="absolute right-0 top-0 pr-4 pt-4">
            <button
              type="button"
              className="rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2"
              style={{
                backgroundColor: '#ffffff',
                color: '#9ca3af'
              }}
              onMouseEnter={(e) => e.target.style.color = '#6b7280'}
              onMouseLeave={(e) => e.target.style.color = '#9ca3af'}
              onClick={handleLogoutCancel}
            >
              <span className="sr-only">Close</span>
              <X className="h-6 w-6" />
            </button>
          </div>

          {/* Content */}
          <div className="sm:flex sm:items-start">
            {/* Icon */}
            <div 
              className="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full sm:mx-0 sm:h-10 sm:w-10"
              style={{backgroundColor: '#dbeafe'}}
            >
              <LogOut className="h-6 w-6" style={{color: '#2563eb'}} />
            </div>
            
            {/* Text */}
            <div className="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
              <h3 
                className="text-base font-semibold leading-6"
                style={{color: '#111827'}}
              >
                Sign out of Admin Dashboard
              </h3>
              <div className="mt-2">
                <p 
                  className="text-sm"
                  style={{color: '#6b7280'}}
                >
                  Are you sure you want to sign out? You'll need to log in again to access the admin dashboard.
                </p>
              </div>
            </div>
          </div>

          {/* Buttons */}
          <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
            <button
              type="button"
              className="inline-flex w-full justify-center rounded-md px-3 py-2 text-sm font-semibold shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto transition-colors"
              style={{
                backgroundColor: '#2563eb',
                color: '#ffffff'
              }}
              onMouseEnter={(e) => e.target.style.backgroundColor = '#3b82f6'}
              onMouseLeave={(e) => e.target.style.backgroundColor = '#2563eb'}
              onClick={handleLogoutConfirm}
            >
              Sign Out
            </button>
            <button
              type="button"
              className="mt-3 inline-flex w-full justify-center rounded-md px-3 py-2 text-sm font-semibold shadow-sm ring-1 ring-inset focus:outline-none focus:ring-2 focus:ring-offset-2 sm:mt-0 sm:w-auto transition-colors"
              style={{
                backgroundColor: '#ffffff',
                color: '#111827',
                border: '1px solid #d1d5db'
              }}
              onMouseEnter={(e) => e.target.style.backgroundColor = '#f9fafb'}
              onMouseLeave={(e) => e.target.style.backgroundColor = '#ffffff'}
              onClick={handleLogoutCancel}
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            {/* Logo and Title */}
            <div className="flex items-center">
              <Activity className="h-8 w-8 text-blue-500 mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">
                Manhattan Muse - Admin Dashboard
              </h1>
            </div>
            
            {/* Navigation and Logout */}
            <div className="flex items-center space-x-4">
              {/* Navigation Tabs */}
              <div className="flex space-x-2">
                <button 
                  onClick={() => setActiveTab('home')}
                  className={`px-4 py-2 rounded transition-colors ${
                    activeTab === 'home' 
                      ? 'bg-blue-500 text-white' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}>
                  Home
                </button>
                <button 
                  onClick={() => setActiveTab('analytics')}
                  className={`px-4 py-2 rounded transition-colors ${
                    activeTab === 'analytics' 
                      ? 'bg-blue-500 text-white' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}>
                  Analytics
                </button>
                <button 
                  onClick={() => setActiveTab('admin')}
                  className={`px-4 py-2 rounded transition-colors ${
                    activeTab === 'admin' 
                      ? 'bg-blue-500 text-white' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}>
                  Admin
                </button>
                <button 
                  onClick={() => setActiveTab('health')}
                  className={`px-4 py-2 rounded transition-colors ${
                    activeTab === 'health' 
                      ? 'bg-blue-500 text-white' 
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}>
                  Health
                </button>
              </div>

              {/* Logout Button */}
              <div className="border-l border-gray-300 pl-4">
                <button 
                  onClick={handleLogoutClick}
                  className="flex items-center px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded transition-colors"
                  title="Logout"
                >
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'home' && <DashboardHome setActiveTab={setActiveTab} />}
        {activeTab === 'analytics' && <AnalyticsDashboard />}
        {activeTab === 'admin' && <AdminControls setActiveTab={setActiveTab} />}
        {activeTab === 'health' && <HealthMonitor />}
      </div>

      {/* Logout Modal */}
      <LogoutModal />
    </div>
  );
};

export default AdminDashboard;