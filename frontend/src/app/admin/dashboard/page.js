//References:
// https://clerk.com/blog/complete-guide-session-management-nextjs
// https://nextjs.org/docs/app/api-reference/functions/use-router
// https://nextjs.org/learn/dashboard-app/adding-authentication

// Enable the hooks for client-side
'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import AdminDashboard from '../../../components/admin/AdminDashboard';
import '../../styles/admin.module.css'
import '../../styles/layout.css';

// Set variable to API endpoint
const API_BASE = `${process.env.NEXT_PUBLIC_BACKEND_API_URL}/api`; 

// This is the page for the admin dashboard
// Where the user is authenticated, they can access the admin dashboard
// If not authenticated, they are redirected to the login page
export default function AdminDashboardPage() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    validateSession();
  }, []);

  // This checks with the backend if we are logged in aka validated
  const validateSession = async () => {
    try {
      console.log('Validating session with backend...');
      
      const response = await fetch(`${API_BASE}/admin/validate-session`, {
        method: 'GET',
        credentials: 'include', // Included session cookies
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();
      
      if (response.ok && data.valid) {
        console.log('Session valid - user authenticated');
        setIsAuthenticated(true);
        // This will keep the session status in the local storage!!
        localStorage.setItem('adminAuthenticated', 'true');
        localStorage.setItem('adminLoginTime', Date.now().toString());
      } else {
        console.log('Session invalid - redirecting to login');
        // Clear any existing local storage and redirect to login page
        localStorage.removeItem('adminAuthenticated');
        localStorage.removeItem('adminLoginTime');
        setIsAuthenticated(false);
        router.push('/admin');
      }
    } catch (error) {
      console.error('Session validation error:', error);
      // Clear the local storage and redirect on the error
      localStorage.removeItem('adminAuthenticated');
      localStorage.removeItem('adminLoginTime');
      setIsAuthenticated(false);
      router.push('/admin'); // If there is ever a fuck up go back to login
    } finally {
      setLoading(false); // Obv stop the loading spin
    }
  };

  const handleLogout = async () => {
    try {
      console.log('Logging out...');
      
      // This is to terminate session via the backend
      await fetch(`${API_BASE}/admin/logout`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      console.log('Backend logout successful');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Always clear local storage/state and redirect, even if the backend call fails
      localStorage.removeItem('adminAuthenticated');
      localStorage.removeItem('adminLoginTime');
      console.log('Redirecting to login page');
      router.push('/admin');
    }
  };

  // This is the loading display
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

  // This is if not validated
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

  // Show dashboard if validated
  return <AdminDashboard onLogout={handleLogout} />;
}
