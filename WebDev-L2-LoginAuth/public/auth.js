/**
 * SecureGate - Client-side Authentication & UI Controller
 * Oasis Infobyte Web Development Level 2 - Task 4
 * Author: Mudassir Ahmed
 */

document.addEventListener('DOMContentLoaded', () => {
  initUrlParamAlerts();
  initPasswordToggles();
  initRegisterForm();
  initLoginForm();
  initDashboard();
});

// ==========================================
// 1. URL QUERY PARAM ALERT HANDLER
// ==========================================
function initUrlParamAlerts() {
  const alertBanner = document.getElementById('global-alert-banner');
  const alertMessage = document.getElementById('global-alert-message');
  if (!alertBanner || !alertMessage) return;

  const urlParams = new URLSearchParams(window.location.search);

  if (urlParams.get('registered') === '1') {
    showAlertBanner(alertBanner, alertMessage, 'Account created successfully! Please log in with your credentials.', 'success');
  } else if (urlParams.get('logout') === '1') {
    showAlertBanner(alertBanner, alertMessage, 'You have been logged out successfully.', 'success');
  } else if (urlParams.get('session_expired') === '1') {
    showAlertBanner(alertBanner, alertMessage, 'Your session has expired. Please log in again.', 'error');
  }
}

function showAlertBanner(bannerEl, messageEl, text, type = 'error') {
  bannerEl.classList.remove('alert-error', 'alert-success', 'show');
  bannerEl.classList.add(type === 'success' ? 'alert-success' : 'alert-error', 'show');
  // Strict XSS safety: use textContent exclusively
  messageEl.textContent = text;
}

function hideAlertBanner(bannerEl) {
  if (bannerEl) {
    bannerEl.classList.remove('show');
  }
}

// ==========================================
// 2. ACCESSIBLE PASSWORD TOGGLE BUTTONS
// ==========================================
function initPasswordToggles() {
  const toggleButtons = document.querySelectorAll('.password-toggle-btn');

  toggleButtons.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const targetInputId = btn.getAttribute('data-target');
      const targetInput = document.getElementById(targetInputId);
      if (!targetInput) return;

      const isPassword = targetInput.getAttribute('type') === 'password';
      const newType = isPassword ? 'text' : 'password';
      targetInput.setAttribute('type', newType);
      btn.setAttribute('aria-pressed', isPassword ? 'true' : 'false');
      btn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');

      // Swap eye SVG icon safely
      const eyeOpenIcon = btn.querySelector('.icon-eye-open');
      const eyeClosedIcon = btn.querySelector('.icon-eye-closed');
      if (eyeOpenIcon && eyeClosedIcon) {
        if (isPassword) {
          eyeOpenIcon.style.display = 'none';
          eyeClosedIcon.style.display = 'block';
        } else {
          eyeOpenIcon.style.display = 'block';
          eyeClosedIcon.style.display = 'none';
        }
      }
    });
  });
}

// ==========================================
// 3. REGISTRATION FORM CONTROLLER
// ==========================================
function initRegisterForm() {
  const form = document.getElementById('register-form');
  if (!form) return;

  const usernameInput = document.getElementById('reg-username');
  const emailInput = document.getElementById('reg-email');
  const passwordInput = document.getElementById('reg-password');
  const confirmPasswordInput = document.getElementById('reg-confirm-password');
  const submitBtn = document.getElementById('reg-submit-btn');
  const alertBanner = document.getElementById('register-alert-banner');
  const alertMessage = document.getElementById('register-alert-message');

  // Live password rules checklist
  const ruleLength = document.getElementById('rule-length');
  const ruleNumber = document.getElementById('rule-number');

  if (passwordInput && ruleLength && ruleNumber) {
    passwordInput.addEventListener('input', () => {
      const val = passwordInput.value;
      const hasMinLength = val.length >= 8;
      const hasNumber = /\d/.test(val);

      updateRuleItem(ruleLength, hasMinLength);
      updateRuleItem(ruleNumber, hasNumber);

      // Clear inline error when typing valid input
      if (hasMinLength && hasNumber) {
        clearFieldError('reg-password');
      }
    });
  }

  // Clear errors on field input
  [usernameInput, emailInput, confirmPasswordInput].forEach(input => {
    if (input) {
      input.addEventListener('input', () => {
        clearFieldError(input.id);
        hideAlertBanner(alertBanner);
      });
    }
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlertBanner(alertBanner);
    clearAllFieldErrors();

    const username = usernameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    let hasClientErrors = false;

    // Validate Username
    if (!username) {
      showFieldError('reg-username', 'Username is required.');
      hasClientErrors = true;
    } else if (username.length < 3 || username.length > 20) {
      showFieldError('reg-username', 'Username must be between 3 and 20 characters.');
      hasClientErrors = true;
    } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(username)) {
      showFieldError('reg-username', 'Username can only contain letters, numbers, and underscores.');
      hasClientErrors = true;
    }

    // Validate Email
    if (!email) {
      showFieldError('reg-email', 'Email address is required.');
      hasClientErrors = true;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      showFieldError('reg-email', 'Please enter a valid email address.');
      hasClientErrors = true;
    }

    // Validate Password
    if (!password) {
      showFieldError('reg-password', 'Password is required.');
      hasClientErrors = true;
    } else if (password.length < 8) {
      showFieldError('reg-password', 'Password must be at least 8 characters.');
      hasClientErrors = true;
    } else if (!/\d/.test(password)) {
      showFieldError('reg-password', 'Password must contain at least 1 number.');
      hasClientErrors = true;
    }

    // Validate Confirm Password
    if (!confirmPassword) {
      showFieldError('reg-confirm-password', 'Please confirm your password.');
      hasClientErrors = true;
    } else if (password !== confirmPassword) {
      showFieldError('reg-confirm-password', 'Passwords do not match.');
      hasClientErrors = true;
    }

    if (hasClientErrors) return;

    // In-flight state: disable submit button and show loading spinner
    setButtonLoading(submitBtn, true);

    try {
      const response = await fetch('/api/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          username,
          email,
          password,
          confirmPassword
        })
      });

      const data = await response.json().catch(() => ({}));

      if (response.status === 201) {
        // Successful registration -> redirect to login with query param
        window.location.href = '/login?registered=1';
      } else if (response.status === 400 && data.errors) {
        // Field validation errors from server
        Object.keys(data.errors).forEach(fieldKey => {
          const fieldMap = {
            username: 'reg-username',
            email: 'reg-email',
            password: 'reg-password',
            confirmPassword: 'reg-confirm-password'
          };
          const targetId = fieldMap[fieldKey];
          if (targetId) {
            showFieldError(targetId, data.errors[fieldKey]);
          }
        });
      } else if (response.status === 409) {
        // Duplicate username or email error
        showAlertBanner(alertBanner, alertMessage, data.error || 'An account with this username or email already exists.', 'error');
      } else if (response.status === 429) {
        // Rate limited
        showAlertBanner(alertBanner, alertMessage, data.error || 'Too many attempts. Please try again later.', 'error');
      } else {
        showAlertBanner(alertBanner, alertMessage, data.error || 'Registration failed. Please try again.', 'error');
      }
    } catch (err) {
      showAlertBanner(alertBanner, alertMessage, 'Network error. Please check your connection and try again.', 'error');
    } finally {
      setButtonLoading(submitBtn, false);
    }
  });
}

function updateRuleItem(itemEl, isValid) {
  if (isValid) {
    itemEl.classList.add('valid');
  } else {
    itemEl.classList.remove('valid');
  }
}

// ==========================================
// 4. LOGIN FORM CONTROLLER
// ==========================================
function initLoginForm() {
  const form = document.getElementById('login-form');
  if (!form) return;

  const identifierInput = document.getElementById('login-identifier');
  const passwordInput = document.getElementById('login-password');
  const submitBtn = document.getElementById('login-submit-btn');
  const alertBanner = document.getElementById('global-alert-banner');
  const alertMessage = document.getElementById('global-alert-message');

  [identifierInput, passwordInput].forEach(input => {
    if (input) {
      input.addEventListener('input', () => {
        clearFieldError(input.id);
        hideAlertBanner(alertBanner);
      });
    }
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlertBanner(alertBanner);
    clearAllFieldErrors();

    const identifier = identifierInput.value.trim();
    const password = passwordInput.value;

    let hasErrors = false;

    if (!identifier) {
      showFieldError('login-identifier', 'Please enter your username or email.');
      hasErrors = true;
    }

    if (!password) {
      showFieldError('login-password', 'Please enter your password.');
      hasErrors = true;
    }

    if (hasErrors) return;

    setButtonLoading(submitBtn, true);

    try {
      const response = await fetch('/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ identifier, password })
      });

      const data = await response.json().catch(() => ({}));

      if (response.status === 200) {
        // Successful login -> navigate to protected dashboard
        window.location.href = '/dashboard';
      } else if (response.status === 401) {
        // Generic invalid credentials message
        showAlertBanner(alertBanner, alertMessage, data.error || 'Invalid credentials. Please try again.', 'error');
      } else if (response.status === 429) {
        // Rate limit reached
        showAlertBanner(alertBanner, alertMessage, data.error || 'Too many failed attempts. Please wait 1 minute.', 'error');
      } else {
        showAlertBanner(alertBanner, alertMessage, data.error || 'Login failed. Please check your inputs.', 'error');
      }
    } catch (err) {
      showAlertBanner(alertBanner, alertMessage, 'Network connection failure. Please try again.', 'error');
    } finally {
      setButtonLoading(submitBtn, false);
    }
  });
}

// ==========================================
// 5. PROTECTED DASHBOARD CONTROLLER
// ==========================================
function initDashboard() {
  const dashboardContainer = document.getElementById('dashboard-app');
  if (!dashboardContainer) return;

  // Handle browser back button cache edge case
  window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
      window.location.reload();
    }
  });

  const userNameGreeting = document.getElementById('dash-user-greeting');
  const userAvatar = document.getElementById('dash-user-avatar');
  const navUsername = document.getElementById('dash-nav-username');
  const valUsername = document.getElementById('dash-val-username');
  const valEmail = document.getElementById('dash-val-email');
  const valMemberSince = document.getElementById('dash-val-member-since');
  const valUserId = document.getElementById('dash-val-userid');
  const logoutBtn = document.getElementById('logout-btn');

  // Fetch verified profile from /api/me
  fetch('/api/me')
    .then(async (res) => {
      if (res.status === 401) {
        window.location.href = '/login?session_expired=1';
        return null;
      }
      return res.json();
    })
    .then((data) => {
      if (!data || !data.user) return;
      const user = data.user;

      // Safe DOM injection via textContent to strictly prevent XSS
      const displayName = user.username || 'User';
      if (userNameGreeting) userNameGreeting.textContent = displayName;
      if (navUsername) navUsername.textContent = displayName;
      if (valUsername) valUsername.textContent = displayName;
      if (valEmail) valEmail.textContent = user.email || 'N/A';
      if (valUserId) valUserId.textContent = user.id ? user.id.slice(0, 18) + '...' : 'SEC-' + Math.random().toString(36).substring(2, 8);

      if (userAvatar) {
        userAvatar.textContent = displayName.charAt(0).toUpperCase();
      }

      if (valMemberSince) {
        if (user.createdAt) {
          try {
            const date = new Date(user.createdAt);
            valMemberSince.textContent = date.toLocaleDateString(undefined, {
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            });
          } catch {
            valMemberSince.textContent = 'Active Member';
          }
        } else {
          valMemberSince.textContent = 'Active Member';
        }
      }
    })
    .catch((err) => {
      console.error('Failed to load user info:', err);
    });

  // Logout Handler
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async (e) => {
      e.preventDefault();
      logoutBtn.disabled = true;
      try {
        await fetch('/api/logout', { method: 'POST' });
      } catch (err) {
        console.error('Logout error:', err);
      } finally {
        // Always redirect to login with logout query parameter
        window.location.replace('/login?logout=1');
      }
    });
  }
}

// ==========================================
// 6. SHARED UI HELPERS
// ==========================================
function showFieldError(inputId, message) {
  const inputEl = document.getElementById(inputId);
  const errorEl = document.getElementById(`${inputId}-error`);

  if (inputEl) {
    inputEl.classList.add('has-error');
  }

  if (errorEl) {
    errorEl.textContent = message;
    errorEl.classList.add('show');
  }
}

function clearFieldError(inputId) {
  const inputEl = document.getElementById(inputId);
  const errorEl = document.getElementById(`${inputId}-error`);

  if (inputEl) {
    inputEl.classList.remove('has-error');
  }

  if (errorEl) {
    errorEl.textContent = '';
    errorEl.classList.remove('show');
  }
}

function clearAllFieldErrors() {
  document.querySelectorAll('.form-input').forEach(input => input.classList.remove('has-error'));
  document.querySelectorAll('.field-error').forEach(err => {
    err.textContent = '';
    err.classList.remove('show');
  });
}

function setButtonLoading(btn, isLoading) {
  if (!btn) return;
  btn.disabled = isLoading;
  if (isLoading) {
    btn.classList.add('loading');
  } else {
    btn.classList.remove('loading');
  }
}
