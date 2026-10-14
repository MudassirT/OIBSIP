import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request Interceptor: Attach JWT Token from localStorage
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('pizzaforge_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle Expired Sessions / 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Don't auto-redirect on login or register failure endpoints
      const url = error.config.url || '';
      if (!url.includes('/login') && !url.includes('/register') && !url.includes('/verify')) {
        console.warn('Session expired. Redirecting to login...');
        localStorage.removeItem('pizzaforge_token');
        localStorage.removeItem('pizzaforge_user');
        window.dispatchEvent(new Event('auth_token_expired'));
      }
    }
    return Promise.reject(error);
  }
);

export default api;
