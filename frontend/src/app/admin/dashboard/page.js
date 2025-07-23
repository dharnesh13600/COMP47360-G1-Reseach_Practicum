'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import AdminDashboard from '../../../components/admin/AdminDashboard';
import '../../styles/admin.module.css'
import '../../styles/layout.css';
export default function AdminDashboardPage() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthentication();
  }, []);

  const checkAuthentication = () => {
    try {
      const isLoggedIn = localStorage.getItem('adminAuthenticated') === 'true';
      const loginTime = localStorage.getItem('adminLoginTime');
      
      if (isLoggedIn && loginTime) {
        // Check if login is still valid (24 hours)
        const twentyFourHours = 24 * 60 * 60 * 1000;
        const now = Date.now();
        const timeSinceLogin = now - parseInt(loginTime);
        
        if (timeSinceLogin < twentyFourHours) {
          setIsAuthenticated(true);
        } else {
          // Session expired
          localStorage.removeItem('adminAuthenticated');
          localStorage.removeItem('adminLoginTime');
          router.push('/admin');
        }
      } else {
        // Not logged in
        router.push('/admin');
      }
    } catch (error) {
      // If localStorage is not available or there's an error, redirect to login
      console.error('Auth check error:', error);
      router.push('/admin');
    }
    
    setLoading(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('adminAuthenticated');
    localStorage.removeItem('adminLoginTime');
    router.push('/admin');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">Checking authentication...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">Access Denied</h1>
          <p className="text-gray-600 mb-4">You don't have permission to access this page.</p>
          <button 
            onClick={() => router.push('/admin')}
            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
          >
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  return <AdminDashboard onLogout={handleLogout} />;
}