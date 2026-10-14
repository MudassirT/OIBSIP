import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '2rem' }}>🔒</div>
          <p style={{ marginTop: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Verifying administrative privileges...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }

  if (!isAdmin) {
    return (
      <div className="container" style={{ padding: '4rem 1rem', textAlign: 'center' }}>
        <div
          style={{
            maxWidth: '500px',
            margin: '0 auto',
            background: '#FFFFFF',
            padding: '2.5rem',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid #fee2e2'
          }}
        >
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⛔</div>
          <h2 style={{ color: '#dc2626', marginBottom: '0.5rem' }}>403 Access Forbidden</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            You do not possess the required administrator credentials to access this console.
          </p>
          <a href="/" className="btn-primary" style={{ display: 'inline-flex' }}>
            Return to Customer Menu
          </a>
        </div>
      </div>
    );
  }

  return children;
};

export default AdminRoute;
