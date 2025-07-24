'use client';
import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import AdminDashboard from '../../../components/admin/AdminDashboard';
import '../../styles/admin.module.css'
import '../../styles/layout.css';

const API_BASE = `${process.env.NEXT_PUBLIC_BACKEND_API_URL}/api`; 

export default function AdminDashboardPage() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    validateSession();
  }, [validateSession]);

  const validateSession = useCallback(async () => {
    try {
      console.log('🔍 Validating session with backend...');
      
      const response = await fetch(`${API_BASE}/admin/validate-session`, {
        method: 'GET',
        credentials: 'include', // Include session cookies
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();
      
      if (response.ok && data.valid) {
        console.log('✅ Session valid - user authenticated');
        setIsAuthenticated(true);
        // Sync with localStorage for consistency
        localStorage.setItem('adminAuthenticated', 'true');
        localStorage.setItem('adminLoginTime', Date.now().toString());
      } else {
        console.log('❌ Session invalid - redirecting to login');
        // Clear any existing localStorage
        localStorage.removeItem('adminAuthenticated');
        localStorage.removeItem('adminLoginTime');
        setIsAuthenticated(false);
        router.push('/admin');
      }
    } catch (error) {
      console.error('❌ Session validation error:', error);
      // Clear localStorage and redirect on any error
      localStorage.removeItem('adminAuthenticated');
      localStorage.removeItem('adminLoginTime');
      setIsAuthenticated(false);
      router.push('/admin');
    } finally {
      setLoading(false);
    }
  }, [router]);

  const handleLogout = async () => {
    try {
      console.log('🚪 Logging out...');
      
      // Call backend logout endpoint
      await fetch(`${API_BASE}/admin/logout`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      console.log('✅ Backend logout successful');
    } catch (error) {
      console.error('❌ Logout error:', error);
    } finally {
      // Always clear localStorage and redirect, even if backend call fails
      localStorage.removeItem('adminAuthenticated');
      localStorage.removeItem('adminLoginTime');
      console.log('🔄 Redirecting to login page');
      router.push('/admin');
    }
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
          <p className="text-gray-600 mb-4">You don&apos;t have permission to access this page.</p>
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
