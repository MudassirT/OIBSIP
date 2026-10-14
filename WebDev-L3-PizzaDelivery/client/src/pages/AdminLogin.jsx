import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const AdminLogin = () => {
  const { adminLogin } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email.trim() || !formData.password) {
      setError('Both email and password are required.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const res = await adminLogin({
        email: formData.email,
        password: formData.password
      });

      if (res.success) {
        showToast('Administrative authorization granted.', 'success');
        navigate('/admin/inventory');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Access denied: Invalid administrative credentials.';
      setError(msg);
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 70px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem 1rem',
        background: 'linear-gradient(135deg, #16171A 0%, #1F2128 100%)'
      }}
    >
      <div
        className="card"
        style={{
          maxWidth: '440px',
          width: '100%',
          background: '#FFFFFF',
          padding: '2.5rem 2rem',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.45)',
          borderRadius: 'var(--radius-lg)'
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: '50%',
              background: '#FEE2E2',
              color: 'var(--accent-tomato)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.75rem',
              margin: '0 auto 1rem'
            }}
          >
            🔒
          </div>
          <h1 style={{ fontSize: '1.65rem', marginBottom: '0.35rem' }}>Admin Control Hub</h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Restricted staff access for inventory control and live kitchen operations
          </p>
        </div>

        {error && (
          <div
            style={{
              background: '#FEE2E2',
              color: '#B91C1C',
              padding: '0.75rem 1rem',
              borderRadius: 'var(--radius-sm)',
              marginBottom: '1.25rem',
              fontSize: '0.85rem',
              textAlign: 'center'
            }}
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="email">Admin Email</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="admin@pizzaforge.com"
              className="form-input"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Secret Password</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="••••••••••••"
              className="form-input"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn-dark"
            style={{ width: '100%', minHeight: '48px', fontSize: '1rem', marginTop: '1rem' }}
          >
            {loading ? 'Authenticating Staff...' : 'Access Administration'}
          </button>

          <div style={{ textAlign: 'center', marginTop: '1.5rem', borderTop: '1px solid var(--border-light)', paddingTop: '1rem' }}>
            <Link to="/" style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              ← Return to Customer Storefront
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdminLogin;
