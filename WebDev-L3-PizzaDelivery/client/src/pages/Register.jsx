import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const Register = () => {
  const { register, resendVerification } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState(null);
  const [resending, setResending] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errors[e.target.name]) {
      setErrors({ ...errors, [e.target.name]: '' });
    }
  };

  const validate = () => {
    const errs = {};
    if (!formData.name.trim()) errs.name = 'Full name is required';
    if (!formData.email.trim()) errs.email = 'Email address is required';
    else if (!/^\S+@\S+\.\S+$/.test(formData.email)) errs.email = 'Invalid email address format';

    if (!formData.password) errs.password = 'Password is required';
    else if (formData.password.length < 6) errs.password = 'Password must be at least 6 characters';

    if (formData.password !== formData.confirmPassword) {
      errs.confirmPassword = 'Passwords do not match';
    }
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      const res = await register({
        name: formData.name,
        email: formData.email,
        password: formData.password
      });

      if (res.success) {
        setRegisteredEmail(formData.email);
        showToast('Registration successful! Please verify your email.', 'success');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create account. Please try again.';
      showToast(msg, 'error');
      if (err.response?.data?.errors) {
        const fieldErrors = {};
        err.response.data.errors.forEach((e) => {
          fieldErrors[e.field] = e.message;
        });
        setErrors(fieldErrors);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!registeredEmail) return;
    setResending(true);
    try {
      const res = await resendVerification(registeredEmail);
      showToast(res.message || 'Verification email resent!', 'info');
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not resend email', 'error');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '480px', margin: '2rem auto' }}>
      <div className="card" style={{ padding: '2.5rem 2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <span style={{ fontSize: '3rem' }}>🍕</span>
          <h1 style={{ fontSize: '1.75rem', marginTop: '0.5rem' }}>Create Account</h1>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
            Join PizzaForge to build custom pizzas and track live deliveries
          </p>
        </div>

        {registeredEmail ? (
          <div
            style={{
              background: 'var(--accent-basil-light)',
              border: '1px solid #bbf7d0',
              padding: '1.5rem',
              borderRadius: 'var(--radius-md)',
              textAlign: 'center'
            }}
          >
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>📬</div>
            <h3 style={{ color: 'var(--accent-basil)', marginBottom: '0.5rem' }}>Verification Link Dispatched!</h3>
            <p style={{ fontSize: '0.875rem', color: '#166534', marginBottom: '1.25rem', lineHeight: 1.6 }}>
              We sent an activation link to <strong>{registeredEmail}</strong>. Please click the link to verify your
              email before logging in.
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <button
                onClick={handleResend}
                disabled={resending}
                className="btn-secondary"
                style={{ width: '100%' }}
              >
                {resending ? 'Sending...' : 'Resend Verification Email'}
              </button>
              <button
                onClick={() => navigate('/login')}
                className="btn-outline"
                style={{ width: '100%' }}
              >
                Proceed to Login
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="name">Full Name</label>
              <input
                id="name"
                name="name"
                type="text"
                placeholder="Marco Polo"
                className="form-input"
                value={formData.name}
                onChange={handleChange}
              />
              {errors.name && <div className="form-error">{errors.name}</div>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="email">Email Address</label>
              <input
                id="email"
                name="email"
                type="email"
                placeholder="marco@example.com"
                className="form-input"
                value={formData.email}
                onChange={handleChange}
              />
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="password">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                placeholder="Minimum 6 characters"
                className="form-input"
                value={formData.password}
                onChange={handleChange}
              />
              {errors.password && <div className="form-error">{errors.password}</div>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">Confirm Password</label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                placeholder="Repeat password"
                className="form-input"
                value={formData.confirmPassword}
                onChange={handleChange}
              />
              {errors.confirmPassword && <div className="form-error">{errors.confirmPassword}</div>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary"
              style={{ width: '100%', marginTop: '1rem', minHeight: '48px', fontSize: '1rem' }}
            >
              {loading ? 'Creating Account...' : 'Register Account'}
            </button>

            <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem' }}>
              Already have an account?{' '}
              <Link to="/login" style={{ color: 'var(--accent-tomato)', fontWeight: 600 }}>
                Sign In
              </Link>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default Register;
