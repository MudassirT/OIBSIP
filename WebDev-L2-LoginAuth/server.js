/**
 * SecureGate - Authentication & Protected Dashboard System
 * Oasis Infobyte Web Development Level 2 - Task 4
 * Author: Mudassir Ahmed
 */

require('dotenv').config();

const express = require('express');
const session = require('express-session');
const bcrypt = require('bcryptjs');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

// ==========================================
// 1. CONFIGURATION & ENVIRONMENT VALIDATION
// ==========================================
const SESSION_SECRET = process.env.SESSION_SECRET;
const PORT = parseInt(process.env.PORT, 10) || 3000;
const IS_PRODUCTION = process.env.NODE_ENV === 'production';

if (!SESSION_SECRET || SESSION_SECRET.trim() === '' || (IS_PRODUCTION && SESSION_SECRET === 'change_me')) {
  console.error('\x1b[31m%s\x1b[0m', '--------------------------------------------------------------------------------');
  console.error('\x1b[31m%s\x1b[0m', '[FATAL CONFIGURATION ERROR] SESSION_SECRET is missing or invalid in .env!');
  console.error('\x1b[31m%s\x1b[0m', 'Please create a .env file based on .env.example and supply a strong secret key.');
  console.error('\x1b[31m%s\x1b[0m', '--------------------------------------------------------------------------------');
  process.exit(1);
}

const DATA_DIR = path.join(__dirname, 'data');
const USERS_FILE = path.join(DATA_DIR, 'users.json');

// Constant dummy hash to prevent user enumeration timing attacks
// (Precomputed bcrypt hash of a random 16-character string with cost 12)
const DUMMY_HASH = '$2a$12$Z07LwXWv78sBqUf5EAgIe.j3k3mU0gB0O4cZp9d2e1q7y9x5v8uOi';

// ==========================================
// 2. USER STORE SERVICE (ASYNC & SERIALIZED)
// ==========================================

// Ensure data folder and users.json exist on startup
function initializeDataStore() {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
  if (!fs.existsSync(USERS_FILE)) {
    fs.writeFileSync(USERS_FILE, JSON.stringify([], null, 2), 'utf8');
  }
}
initializeDataStore();

// Read users array asynchronously
async function readUsers() {
  try {
    const data = await fs.promises.readFile(USERS_FILE, 'utf8');
    if (!data.trim()) return [];
    return JSON.parse(data);
  } catch (error) {
    if (error.code === 'ENOENT') {
      await fs.promises.writeFile(USERS_FILE, '[]', 'utf8');
      return [];
    }
    console.error('Error reading users store:', error);
    throw new Error('Data store read error');
  }
}

// Mutex / Queue to serialize concurrent writes and prevent file corruption
let writeQueue = Promise.resolve();

function serializeWrite(task) {
  const result = writeQueue.then(task);
  // Keep queue moving forward even if a write fails
  writeQueue = result.catch(() => {});
  return result;
}

// Atomically save users by writing to a temporary file then renaming
async function saveUsers(users) {
  return serializeWrite(async () => {
    const tempFile = path.join(DATA_DIR, `users.${Date.now()}.${crypto.randomBytes(4).toString('hex')}.tmp`);
    await fs.promises.writeFile(tempFile, JSON.stringify(users, null, 2), 'utf8');
    await fs.promises.rename(tempFile, USERS_FILE);
  });
}

// Find user by username or email (case-insensitive)
async function findUserByUsernameOrEmail(identifier) {
  const users = await readUsers();
  const lower = identifier.trim().toLowerCase();
  return users.find(u => u.username.toLowerCase() === lower || u.email.toLowerCase() === lower) || null;
}

// ==========================================
// 3. IN-MEMORY RATE LIMITER
// ==========================================
// Tracks failed attempts per IP. Resets after 60 seconds. Limit: 5 failed attempts per min.
const rateLimitMap = new Map();

function getClientIp(req) {
  return req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';
}

function rateLimitAuth(req, res, next) {
  const ip = getClientIp(req);
  const now = Date.now();
  const entry = rateLimitMap.get(ip);

  if (entry) {
    if (now > entry.resetTime) {
      rateLimitMap.delete(ip);
    } else if (entry.attempts >= 5) {
      const waitSeconds = Math.ceil((entry.resetTime - now) / 1000);
      return res.status(429).json({
        error: `Too many failed attempts. Please wait ${waitSeconds} second${waitSeconds !== 1 ? 's' : ''} before trying again.`
      });
    }
  }
  next();
}

function recordFailedAuth(req) {
  const ip = getClientIp(req);
  const now = Date.now();
  const entry = rateLimitMap.get(ip);

  if (!entry || now > entry.resetTime) {
    rateLimitMap.set(ip, { attempts: 1, resetTime: now + 60 * 1000 });
  } else {
    entry.attempts += 1;
  }
}

function recordSuccessfulAuth(req) {
  const ip = getClientIp(req);
  rateLimitMap.delete(ip);
}

// ==========================================
// 4. INPUT VALIDATION LOGIC
// ==========================================
const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,20}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateRegistrationInput({ username, email, password, confirmPassword }) {
  const errors = {};

  // Username validation
  if (!username || typeof username !== 'string' || !username.trim()) {
    errors.username = 'Username is required.';
  } else {
    const trimmed = username.trim();
    if (trimmed.length < 3 || trimmed.length > 20) {
      errors.username = 'Username must be between 3 and 20 characters.';
    } else if (!USERNAME_REGEX.test(trimmed)) {
      errors.username = 'Username can only contain letters, numbers, and underscores.';
    }
  }

  // Email validation
  if (!email || typeof email !== 'string' || !email.trim()) {
    errors.email = 'Email address is required.';
  } else {
    const trimmed = email.trim();
    if (!EMAIL_REGEX.test(trimmed)) {
      errors.email = 'Please provide a valid email address.';
    }
  }

  // Password validation: minimum 8 characters and at least 1 number
  if (!password || typeof password !== 'string') {
    errors.password = 'Password is required.';
  } else {
    if (password.length < 8) {
      errors.password = 'Password must be at least 8 characters long.';
    } else if (!/\d/.test(password)) {
      errors.password = 'Password must contain at least 1 number.';
    }
  }

  // Confirm password
  if (password !== confirmPassword) {
    errors.confirmPassword = 'Passwords do not match.';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
}

// ==========================================
// 5. APPLICATION SETUP & MIDDLEWARE
// ==========================================
const app = express();

// Security: parse JSON with strict body size limit (10kb) to prevent DoS
app.use(express.json({ limit: '10kb' }));
app.use(express.urlencoded({ extended: false, limit: '10kb' }));

// Express session setup
app.use(session({
  name: 'securegate.sid',
  secret: SESSION_SECRET,
  resave: false,
  saveUninitialized: false,
  cookie: {
    httpOnly: true,
    sameSite: 'lax',
    secure: IS_PRODUCTION,
    maxAge: 60 * 60 * 1000 // 1 hour session lifetime
  }
}));

// Cache prevention middleware for protected pages and routes
function noCache(req, res, next) {
  res.set({
    'Cache-Control': 'no-store, no-cache, must-revalidate, private',
    'Pragma': 'no-cache',
    'Expires': '0'
  });
  next();
}

// Auth check middleware
function requireAuth(req, res, next) {
  if (!req.session || !req.session.user) {
    if (req.path.startsWith('/api/')) {
      return res.status(401).json({ error: 'Unauthorized. Please log in.' });
    }
    return res.redirect('/login');
  }
  next();
}

// Redirect already logged-in users away from guest pages
function redirectIfAuthenticated(req, res, next) {
  if (req.session && req.session.user) {
    return res.redirect('/dashboard');
  }
  next();
}

// ==========================================
// 6. PAGE SERVING ROUTES
// ==========================================

// Root route: redirect based on session state
app.get('/', (req, res) => {
  if (req.session && req.session.user) {
    return res.redirect('/dashboard');
  }
  return res.redirect('/login');
});

// Login page
app.get('/login', redirectIfAuthenticated, (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Register page
app.get('/register', redirectIfAuthenticated, (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

// Protected Dashboard (strictly served from views/, NOT publicly exposed)
app.get('/dashboard', requireAuth, noCache, (req, res) => {
  res.sendFile(path.join(__dirname, 'views', 'dashboard.html'));
});

// Serve static assets (CSS, JS, images) from public/
app.use(express.static(path.join(__dirname, 'public')));

// ==========================================
// 7. API ENDPOINTS
// ==========================================

/**
 * POST /api/register
 * Validates registration data, checks duplicates, hashes password with bcrypt, and stores user.
 */
app.post('/api/register', rateLimitAuth, async (req, res) => {
  try {
    const { username, email, password, confirmPassword } = req.body;

    // Validate inputs
    const { isValid, errors } = validateRegistrationInput({ username, email, password, confirmPassword });
    if (!isValid) {
      return res.status(400).json({ error: 'Validation failed', errors });
    }

    const trimmedUsername = username.trim();
    const normalizedEmail = email.trim().toLowerCase();

    // Check duplicate username or email (case-insensitive)
    const users = await readUsers();
    const duplicate = users.find(u =>
      u.username.toLowerCase() === trimmedUsername.toLowerCase() ||
      u.email.toLowerCase() === normalizedEmail
    );

    if (duplicate) {
      return res.status(409).json({
        error: 'An account with this username or email already exists.'
      });
    }

    // Hash password with bcryptjs (salt cost = 12)
    const saltRounds = 12;
    const passwordHash = await bcrypt.hash(password, saltRounds);

    const newUser = {
      id: crypto.randomUUID(),
      username: trimmedUsername,
      email: normalizedEmail,
      passwordHash,
      createdAt: new Date().toISOString()
    };

    users.push(newUser);
    await saveUsers(users);

    return res.status(201).json({
      success: true,
      message: 'Account created successfully. Please log in.'
    });
  } catch (err) {
    console.error('Registration error:', err);
    return res.status(500).json({ error: 'An unexpected server error occurred. Please try again later.' });
  }
});

/**
 * POST /api/login
 * Validates credentials with timing attack mitigation, regenerates session ID, and authenticates.
 */
app.post('/api/login', rateLimitAuth, async (req, res, next) => {
  try {
    const { identifier, password } = req.body;

    if (!identifier || !password || typeof identifier !== 'string' || typeof password !== 'string') {
      recordFailedAuth(req);
      return res.status(400).json({ error: 'Username/Email and password are required.' });
    }

    const user = await findUserByUsernameOrEmail(identifier);

    let passwordMatches = false;

    if (user) {
      passwordMatches = await bcrypt.compare(password, user.passwordHash);
    } else {
      // Execute dummy comparison to equalize execution time and prevent username enumeration timing leaks
      await bcrypt.compare(password, DUMMY_HASH);
      passwordMatches = false;
    }

    if (!passwordMatches) {
      recordFailedAuth(req);
      // Generic error message for both non-existent user and wrong password
      return res.status(401).json({ error: 'Invalid credentials. Please try again.' });
    }

    // Credential verified -> clear failed attempt counter
    recordSuccessfulAuth(req);

    // Regenerate session ID on login to protect against session fixation attacks
    req.session.regenerate((err) => {
      if (err) {
        console.error('Session regeneration error:', err);
        return res.status(500).json({ error: 'Internal login error. Please try again.' });
      }

      // Store safe session payload (never store passwordHash!)
      req.session.user = {
        id: user.id,
        username: user.username,
        email: user.email,
        createdAt: user.createdAt
      };

      req.session.save((saveErr) => {
        if (saveErr) {
          console.error('Session save error:', saveErr);
          return res.status(500).json({ error: 'Internal login error. Please try again.' });
        }
        return res.status(200).json({
          success: true,
          message: 'Login successful',
          user: req.session.user
        });
      });
    });
  } catch (err) {
    console.error('Login error:', err);
    return res.status(500).json({ error: 'An unexpected server error occurred. Please try again later.' });
  }
});

/**
 * POST /api/logout
 * Destroys session, clears cookie, and invalidates auth.
 */
app.post('/api/logout', (req, res) => {
  if (!req.session) {
    res.clearCookie('securegate.sid');
    return res.status(200).json({ success: true, message: 'Logged out successfully.' });
  }

  req.session.destroy((err) => {
    if (err) {
      console.error('Session destruction error:', err);
      return res.status(500).json({ error: 'Logout failed. Please try again.' });
    }
    res.clearCookie('securegate.sid');
    return res.status(200).json({ success: true, message: 'Logged out successfully.' });
  });
});

/**
 * GET /api/me
 * Returns current authenticated user's safe profile, or 401.
 */
app.get('/api/me', noCache, (req, res) => {
  if (!req.session || !req.session.user) {
    return res.status(401).json({ error: 'Unauthorized' });
  }

  return res.status(200).json({
    user: req.session.user
  });
});

// ==========================================
// 8. 404 & CENTRALIZED ERROR HANDLING
// ==========================================
app.use((req, res) => {
  if (req.path.startsWith('/api/')) {
    return res.status(404).json({ error: 'Resource not found' });
  }
  return res.status(404).sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Centralized error handler: never leak stack traces to client
app.use((err, req, res, next) => {
  console.error('[UNCAUGHT SERVER ERROR]', err);
  if (res.headersSent) {
    return next(err);
  }
  return res.status(500).json({ error: 'Internal server error' });
});

// ==========================================
// 9. SERVER START
// ==========================================
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`\n=================================================`);
    console.log(`  SecureGate Authentication System is Running!`);
    console.log(`  URL: http://localhost:${PORT}`);
    console.log(`  Environment: ${process.env.NODE_ENV || 'development'}`);
    console.log(`=================================================\n`);
  });
}

module.exports = app;
