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
          className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" 
          onClick={handleLogoutCancel}
        ></div>
        
        {/* Modal */}
        <div className="flex min-h-full items-center justify-center p-4">
          <div className="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6">
            {/* Close button */}
            <div className="absolute right-0 top-0 pr-4 pt-4">
              <button
                type="button"
                className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                onClick={handleLogoutCancel}
              >
                <span className="sr-only">Close</span>
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* Content */}
            <div className="sm:flex sm:items-start">
              {/* Icon */}
              <div className="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
                <LogOut className="h-6 w-6 text-blue-600" />
              </div>
              
              {/* Text */}
              <div className="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                <h3 className="text-base font-semibold leading-6 text-gray-900">
                  Sign out of Admin Dashboard
                </h3>
                <div className="mt-2">
                  <p className="text-sm text-gray-500">
  Are you sure you want to sign out? You&apos;ll need to log in again to access the admin dashboard.
</p>
                </div>
              </div>
            </div>

            {/* Buttons */}
            <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
              <button
                type="button"
                className="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:ml-3 sm:w-auto transition-colors"
                onClick={handleLogoutConfirm}
              >
                Sign Out
              </button>
              <button
                type="button"
                className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 sm:mt-0 sm:w-auto transition-colors"
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