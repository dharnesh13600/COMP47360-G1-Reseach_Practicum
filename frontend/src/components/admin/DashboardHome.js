import React from 'react';
import { BarChart3, Zap, Heart } from 'lucide-react';

const DashboardHome = ({ setActiveTab }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
      <div 
       className="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl hover:text-black active:text-orange-500 transition-all duration-200 ease-in-out cursor-pointer transform hover:scale-105"

        onClick={() => setActiveTab('analytics')}
      >
        <div className="flex items-center justify-center mb-4">
          <BarChart3 className="h-12 w-12 text-blue-500" />
        </div>
        <h3 className="text-xl font-bold text-center mb-2">Analytics Dashboard</h3>
        <p className="text-gray-600 text-center">
          View system analytics, popular combinations, cache performance, and usage trends
        </p>
      </div>

      <div 
        className="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer transform hover:scale-105"
        onClick={() => setActiveTab('admin')}
      >
        <div className="flex items-center justify-center mb-4">
          <Zap className="h-12 w-12 text-green-500" />
        </div>
        <h3 className="text-xl font-bold text-center mb-2">Admin Controls</h3>
        <p className="text-gray-600 text-center">
          Manage cache warming, system operations, and administrative tasks
        </p>
      </div>

      <div 
        className="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer transform hover:scale-105"
        onClick={() => setActiveTab('health')}
      >
        <div className="flex items-center justify-center mb-4">
          <Heart className="h-12 w-12 text-red-500" />
        </div>
        <h3 className="text-xl font-bold text-center mb-2">System Health</h3>
        <p className="text-gray-600 text-center">
          Monitor system status, database health, and performance metrics
        </p>
      </div>
    </div>
  );
};

export default DashboardHome;