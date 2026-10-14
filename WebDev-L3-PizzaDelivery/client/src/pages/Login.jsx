import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const Login = () => {
  const { login, resendVerification } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [unverifiedEmail, setUnverifiedEmail] = useState(null);
  const [resending, setResending] = useState(false);

  const from = location.state?.from?.pathname || '/';

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errors[e.target.name]) {
      setErrors({ ...errors, [e.target.name]: '' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!formData.email.trim()) errs.email = 'Email address is required';
    if (!formData.password) errs.password = 'Password is required';

    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setLoading(true);
    setUnverifiedEmail(null);

    try {
      const res = await login({
        email: formData.email,
        password: formData.password
      });

      if (res.success) {
        showToast(`Welcome back, ${res.user.name}!`, 'success');
        navigate(from, { replace: true });
      }
    } catch (err) {
      if (err.response?.status === 403 && err.response?.data?.isUnverified) {
        setUnverifiedEmail(err.response.data.email || formData.email);
        showToast('Please verify your email before logging in.', 'warning');
      } else {
        const msg = err.response?.data?.message || 'Invalid email or password.';
        showToast(msg, 'error');
        setErrors({ general: msg });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!unverifiedEmail) return;
    setResending(true);
    try {
      const res = await resendVerification(unverifiedEmail);
      showToast(res.message || 'Verification email resent!', 'info');
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not send verification email', 'error');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '440px', margin: '2.5rem auto' }}>
      <div className="card" style={{ padding: '2.5rem 2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <span style={{ fontSize: '3rem' }}>🍕</span>
          <h1 style={{ fontSize: '1.75rem', marginTop: '0.5rem' }}>Welcome Back</h1>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
            Sign in to track orders or configure handcrafted pies
          </p>
        </div>

        {unverifiedEmail && (
          <div
            style={{
              background: '#fef3c7',
              border: '1px solid #fde68a',
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              marginBottom: '1.5rem',
              textAlign: 'center'
            }}
          >
            <div style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>⚠️</div>
            <h4 style={{ color: '#92400e', marginBottom: '0.4rem', fontSize: '1rem' }}>
              Email Not Verified
            </h4>
            <p style={{ fontSize: '0.825rem', color: '#78350f', marginBottom: '0.85rem', lineHeight: 1.5 }}>
              Your account for <strong>{unverifiedEmail}</strong> requires email activation.
            </p>
            <button
              onClick={handleResend}
              disabled={resending}
              className="btn-secondary"
              style={{ width: '100%', minHeight: '38px', fontSize: '0.85rem' }}
            >
              {resending ? 'Sending...' : 'Resend Verification Link'}
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {errors.general && (
            <div
              style={{
                background: '#fee2e2',
                color: '#b91c1c',
                padding: '0.75rem',
                borderRadius: 'var(--radius-sm)',
                marginBottom: '1.25rem',
                fontSize: '0.85rem',
                textAlign: 'center'
              }}
            >
              {errors.general}
            </div>
          )}

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="name@example.com"
              className="form-input"
              value={formData.email}
              onChange={handleChange}
            />
            {errors.email && <div className="form-error">{errors.email}</div>}
          </div>

          <div className="form-group">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="form-label" htmlFor="password">Password</label>
              <Link
                to="/forgot-password"
                style={{ fontSize: '0.8rem', color: 'var(--accent-tomato)', fontWeight: 600 }}
              >
                Forgot?
              </Link>
            </div>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="Your password"
              className="form-input"
              value={formData.password}
              onChange={handleChange}
            />
            {errors.password && <div className="form-error">{errors.password}</div>}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn-primary"
            style={{ width: '100%', marginTop: '1rem', minHeight: '48px', fontSize: '1rem' }}
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>

          <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem' }}>
            New to PizzaForge?{' '}
            <Link to="/register" style={{ color: 'var(--accent-tomato)', fontWeight: 600 }}>
              Create an account
            </Link>
          </div>

          <div style={{ borderTop: '1px solid var(--border-light)', marginTop: '1.5rem', paddingTop: '1rem', textAlign: 'center' }}>
            <Link to="/admin/login" style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              🔒 Are you an Administrator? Access Admin Portal
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;
