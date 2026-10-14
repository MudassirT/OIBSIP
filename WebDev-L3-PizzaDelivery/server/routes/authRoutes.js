const express = require('express');
const { body, param } = require('express-validator');
const {
  register,
  verifyEmail,
  resendVerification,
  login,
  adminLogin,
  forgotPassword,
  resetPassword,
  getMe
} = require('../controllers/authController');
const { validate } = require('../middleware/validateMiddleware');
const { protect } = require('../middleware/authMiddleware');
const { authLimiter } = require('../middleware/rateLimiter');

const router = express.Router();

// Apply auth rate limiter to public authentication endpoints
router.use('/login', authLimiter);
router.use('/admin-login', authLimiter);
router.use('/register', authLimiter);
router.use('/forgot-password', authLimiter);

router.post(
  '/register',
  [
    body('name').trim().isLength({ min: 2 }).withMessage('Name must be at least 2 characters long'),
    body('email').isEmail().withMessage('Please provide a valid email address').normalizeEmail(),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long')
  ],
  validate,
  register
);

router.get('/verify/:token', verifyEmail);
router.post('/verify', verifyEmail);

router.post(
  '/resend-verification',
  [body('email').isEmail().withMessage('Please provide a valid email address').normalizeEmail()],
  validate,
  resendVerification
);

router.post(
  '/login',
  [
    body('email').isEmail().withMessage('Please provide a valid email address').normalizeEmail(),
    body('password').notEmpty().withMessage('Password is required')
  ],
  validate,
  login
);

router.post(
  '/admin-login',
  [
    body('email').isEmail().withMessage('Please provide a valid email address').normalizeEmail(),
    body('password').notEmpty().withMessage('Password is required')
  ],
  validate,
  adminLogin
);

router.post(
  '/forgot-password',
  [body('email').isEmail().withMessage('Please provide a valid email address').normalizeEmail()],
  validate,
  forgotPassword
);

router.post(
  '/reset-password/:token',
  [body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long')],
  validate,
  resetPassword
);

router.post(
  '/reset-password',
  [
    body('token').notEmpty().withMessage('Reset token is required'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long')
  ],
  validate,
  resetPassword
);

router.get('/me', protect, getMe);

// Development/testing helper endpoint for headless automated tests
if (process.env.NODE_ENV !== 'production') {
  const { getDevToken } = require('../services/emailService');
  router.get('/dev-token/:email', (req, res) => {
    const data = getDevToken(req.params.email);
    if (!data) {
      return res.status(404).json({ success: false, message: 'No dev token found for email' });
    }
    res.json({ success: true, ...data });
  });
}

module.exports = router;
