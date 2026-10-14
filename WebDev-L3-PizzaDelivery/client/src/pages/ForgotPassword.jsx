import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const ForgotPassword = () => {
  const { forgotPassword } = useAuth();
  const { showToast } = useToast();

  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [responseMsg, setResponseMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) {
      showToast('Please enter your email', 'warning');
      return;
    }

    setLoading(true);
    try {
      const res = await forgotPassword(email.trim());
      setResponseMsg(
        res.message ||
          'If an account exists with that email address, a password reset link has been dispatched.'
      );
      setSubmitted(true);
      showToast('Reset request received!', 'info');
    } catch (err) {
      // Always show generic message to prevent account enumeration
      setResponseMsg(
        'If an account exists with that email address, a password reset link has been dispatched.'
      );
      setSubmitted(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '440px', margin: '3rem auto' }}>
      <div className="card" style={{ padding: '2.5rem 2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <span style={{ fontSize: '3rem' }}>🔑</span>
          <h1 style={{ fontSize: '1.75rem', marginTop: '0.5rem' }}>Reset Password</h1>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
            Enter your email to receive an expiring reset link
          </p>
        </div>

        {submitted ? (
          <div
            style={{
              background: 'var(--accent-basil-light)',
              border: '1px solid #bbf7d0',
              padding: '1.5rem',
              borderRadius: 'var(--radius-md)',
              textAlign: 'center'
            }}
          >
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📬</div>
            <h4 style={{ color: 'var(--accent-basil)', marginBottom: '0.5rem' }}>Request Processed</h4>
            <p style={{ fontSize: '0.85rem', color: '#166534', lineHeight: 1.6, marginBottom: '1.5rem' }}>
              {responseMsg}
            </p>
            <Link to="/login" className="btn-primary" style={{ width: '100%', minHeight: '44px' }}>
              Return to Login
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="email">Registered Email Address</label>
              <input
                id="email"
                type="email"
                placeholder="name@example.com"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary"
              style={{ width: '100%', marginTop: '1rem', minHeight: '48px', fontSize: '1rem' }}
            >
              {loading ? 'Sending Link...' : 'Dispatch Reset Link'}
            </button>

            <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
              <Link to="/login" style={{ color: 'var(--accent-tomato)', fontWeight: 600, fontSize: '0.9rem' }}>
                Remember your password? Sign in
              </Link>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
