import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('pizzaforge_token') || null);
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('pizzaforge_user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(true);

  // Sync token & user to localStorage
  const saveAuthSession = useCallback((authToken, authUser) => {
    setToken(authToken);
    setUser(authUser);
    localStorage.setItem('pizzaforge_token', authToken);
    localStorage.setItem('pizzaforge_user', JSON.stringify(authUser));
  }, []);

  const clearAuthSession = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('pizzaforge_token');
    localStorage.removeItem('pizzaforge_user');
  }, []);

  // Fetch current user profile on initial app load if token exists
  useEffect(() => {
    const initAuth = async () => {
      const savedToken = localStorage.getItem('pizzaforge_token');
      if (savedToken) {
        try {
          const res = await api.get('/auth/me');
          if (res.data.success) {
            setUser(res.data.user);
            localStorage.setItem('pizzaforge_user', JSON.stringify(res.data.user));
          }
        } catch (err) {
          console.warn('Failed to validate session token on startup:', err.message);
          clearAuthSession();
        }
      }
      setLoading(false);
    };

    initAuth();

    // Listen for custom token expiry events dispatched by axios interceptor
    const handleTokenExpired = () => {
      clearAuthSession();
    };

    window.addEventListener('auth_token_expired', handleTokenExpired);
    return () => {
      window.removeEventListener('auth_token_expired', handleTokenExpired);
    };
  }, [clearAuthSession]);

  const register = async ({ name, email, password }) => {
    const res = await api.post('/auth/register', { name, email, password });
    return res.data;
  };

  const login = async ({ email, password }) => {
    const res = await api.post('/auth/login', { email, password });
    if (res.data.success && res.data.token) {
      saveAuthSession(res.data.token, res.data.user);
    }
    return res.data;
  };

  const adminLogin = async ({ email, password }) => {
    const res = await api.post('/auth/admin-login', { email, password });
    if (res.data.success && res.data.token) {
      saveAuthSession(res.data.token, res.data.user);
    }
    return res.data;
  };

  const resendVerification = async (email) => {
    const res = await api.post('/auth/resend-verification', { email });
    return res.data;
  };

  const verifyEmail = async (token) => {
    const res = await api.post('/auth/verify', { token });
    return res.data;
  };

  const forgotPassword = async (email) => {
    const res = await api.post('/auth/forgot-password', { email });
    return res.data;
  };

  const resetPassword = async (token, password) => {
    const res = await api.post('/auth/reset-password', { token, password });
    return res.data;
  };

  const logout = () => {
    clearAuthSession();
  };

  const isAuthenticated = !!token && !!user;
  const isAdmin = isAuthenticated && user.role === 'admin';

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        loading,
        isAuthenticated,
        isAdmin,
        register,
        login,
        adminLogin,
        logout,
        verifyEmail,
        resendVerification,
        forgotPassword,
        resetPassword,
        saveAuthSession
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
