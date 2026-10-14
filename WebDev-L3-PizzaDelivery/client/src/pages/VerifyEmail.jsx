import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const VerifyEmail = () => {
  const { token } = useParams();
  const { verifyEmail, resendVerification } = useAuth();
  const { showToast } = useToast();

  const [status, setStatus] = useState('verifying'); // 'verifying' | 'success' | 'error'
  const [message, setMessage] = useState('Verifying your email address...');
  const [resendEmail, setResendEmail] = useState('');
  const [resending, setResending] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const performVerification = async () => {
      if (!token) {
        setStatus('error');
        setMessage('No verification token provided in URL.');
        return;
      }

      try {
        const res = await verifyEmail(token);
        if (isMounted) {
          setStatus('success');
          setMessage(res.message || 'Email verified successfully! You may now log in.');
          showToast('Email verified successfully!', 'success');
        }
      } catch (err) {
        if (isMounted) {
          setStatus('error');
          setMessage(err.response?.data?.message || 'Verification link is invalid or has expired.');
          showToast('Verification failed', 'error');
        }
      }
    };

    performVerification();

    return () => {
      isMounted = false;
    };
  }, [token, verifyEmail, showToast]);

  const handleResend = async (e) => {
    e.preventDefault();
    if (!resendEmail.trim()) {
      showToast('Please enter your email', 'warning');
      return;
    }

    setResending(true);
    try {
      const res = await resendVerification(resendEmail.trim());
      showToast(res.message || 'New verification email sent!', 'info');
      setResendEmail('');
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not send verification email', 'error');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '480px', margin: '3rem auto' }}>
      <div className="card" style={{ padding: '2.5rem 2rem', textAlign: 'center' }}>
        {status === 'verifying' && (
          <div>
            <div style={{ fontSize: '3rem', animation: 'spin 1.5s infinite linear' }}>⏳</div>
            <h2 style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>Verifying Account</h2>
            <p style={{ color: 'var(--text-muted)' }}>{message}</p>
          </div>
        )}

        {status === 'success' && (
          <div>
            <div style={{ fontSize: '3.5rem', marginBottom: '0.75rem' }}>🎉</div>
            <h2 style={{ color: 'var(--accent-basil)', marginBottom: '0.5rem' }}>Email Verified!</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.75rem', lineHeight: 1.6 }}>{message}</p>
            <Link to="/login" className="btn-primary" style={{ width: '100%', minHeight: '46px' }}>
              Proceed to Sign In
            </Link>
          </div>
        )}

        {status === 'error' && (
          <div>
            <div style={{ fontSize: '3.5rem', marginBottom: '0.75rem' }}>⚠️</div>
            <h2 style={{ color: '#dc2626', marginBottom: '0.5rem' }}>Verification Link Expired</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.75rem', lineHeight: 1.6 }}>{message}</p>

            <div
              style={{
                background: 'var(--bg-elevated)',
                padding: '1.25rem',
                borderRadius: 'var(--radius-md)',
                marginTop: '1.5rem',
                textAlign: 'left'
              }}
            >
              <h4 style={{ fontSize: '0.95rem', marginBottom: '0.5rem' }}>Request New Link</h4>
              <form onSubmit={handleResend}>
                <div className="form-group" style={{ marginBottom: '0.75rem' }}>
                  <input
                    type="email"
                    placeholder="Enter registered email"
                    className="form-input"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    required
                  />
                </div>
                <button
                  type="submit"
                  disabled={resending}
                  className="btn-secondary"
                  style={{ width: '100%', minHeight: '40px' }}
                >
                  {resending ? 'Sending...' : 'Dispatch New Link'}
                </button>
              </form>
            </div>

            <div style={{ marginTop: '1.5rem' }}>
              <Link to="/login" style={{ color: 'var(--accent-tomato)', fontWeight: 600, fontSize: '0.9rem' }}>
                Back to Login
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;
