const bcrypt = require('bcryptjs');
const User = require('../models/User');
const { generateRandomToken, hashToken, signJWT } = require('../utils/tokenUtils');
const { sendVerificationEmail, sendPasswordResetEmail } = require('../services/emailService');

/**
 * Register a new user
 * Accounts start unverified with an expiring verification token
 * Ignores any 'role' field passed in request
 */
const register = async (req, res, next) => {
  try {
    const { name, email, password } = req.body;

    const normalizedEmail = email.toLowerCase().trim();

    const existingUser = await User.findOne({ email: normalizedEmail });
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: 'An account with this email address already exists.'
      });
    }

    const salt = await bcrypt.genSalt(12);
    const passwordHash = await bcrypt.hash(password, salt);

    const { rawToken, hashedToken } = generateRandomToken();
    const verifyTokenExpires = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours

    const newUser = await User.create({
      name: name.trim(),
      email: normalizedEmail,
      passwordHash,
      role: 'user', // Strict enforcement: public registration cannot create admins
      isVerified: false,
      verifyToken: hashedToken,
      verifyTokenExpires
    });

    // Send verification email asynchronously
    sendVerificationEmail(newUser.email, newUser.name, rawToken).catch((err) =>
      console.error('Failed to send verification email:', err.message)
    );

    res.status(201).json({
      success: true,
      message: 'Registration successful! A verification link has been sent to your email.',
      user: newUser.toSafeObject()
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Verify user email with token
 */
const verifyEmail = async (req, res, next) => {
  try {
    const token = req.params.token || req.body.token;

    if (!token) {
      return res.status(400).json({
        success: false,
        message: 'Verification token is required.'
      });
    }

    const hashedToken = hashToken(token);

    const user = await User.findOne({
      verifyToken: hashedToken,
      verifyTokenExpires: { $gt: new Date() }
    });

    if (!user) {
      return res.status(400).json({
        success: false,
        message: 'Verification link is invalid or has expired. Please request a new one.'
      });
    }

    user.isVerified = true;
    user.verifyToken = null;
    user.verifyTokenExpires = null;
    await user.save();

    res.status(200).json({
      success: true,
      message: 'Email successfully verified! You may now log in to PizzaForge.'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Resend verification email
 */
const resendVerification = async (req, res, next) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Email address is required.'
      });
    }

    const user = await User.findOne({ email: email.toLowerCase().trim() });

    if (!user) {
      // Respond identically to prevent user enumeration
      return res.status(200).json({
        success: true,
        message: 'If an unverified account exists with this email, a new verification link has been sent.'
      });
    }

    if (user.isVerified) {
      return res.status(400).json({
        success: false,
        message: 'This account is already verified. Please log in.'
      });
    }

    const { rawToken, hashedToken } = generateRandomToken();
    user.verifyToken = hashedToken;
    user.verifyTokenExpires = new Date(Date.now() + 24 * 60 * 60 * 1000);
    await user.save();

    sendVerificationEmail(user.email, user.name, rawToken).catch((err) =>
      console.error('Failed to resend verification email:', err.message)
    );

    res.status(200).json({
      success: true,
      message: 'A new verification link has been dispatched to your email.'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Login user with JWT
 */
const login = async (req, res, next) => {
  try {
    const { email, password } = req.body;
    const normalizedEmail = email.toLowerCase().trim();

    const user = await User.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password.'
      });
    }

    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password.'
      });
    }

    // Block unverified users
    if (!user.isVerified) {
      return res.status(403).json({
        success: false,
        isUnverified: true,
        email: user.email,
        message: 'Your email address has not been verified yet. Please check your inbox or request a new verification email.'
      });
    }

    const token = signJWT({ userId: user._id, role: user.role });

    res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: user.toSafeObject()
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Separate Admin Login
 */
const adminLogin = async (req, res, next) => {
  try {
    const { email, password } = req.body;
    const normalizedEmail = email.toLowerCase().trim();

    const user = await User.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid administrative credentials.'
      });
    }

    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid administrative credentials.'
      });
    }

    if (user.role !== 'admin') {
      return res.status(403).json({
        success: false,
        message: 'Access forbidden: This portal is reserved for administrators only.'
      });
    }

    const token = signJWT({ userId: user._id, role: 'admin' });

    res.status(200).json({
      success: true,
      message: 'Administrator login successful.',
      token,
      user: user.toSafeObject()
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Forgot password - request reset link
 * Constant response to avoid account enumeration
 */
const forgotPassword = async (req, res, next) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Email address is required.'
      });
    }

    const user = await User.findOne({ email: email.toLowerCase().trim() });

    if (user) {
      const { rawToken, hashedToken } = generateRandomToken();
      user.resetToken = hashedToken;
      user.resetTokenExpires = new Date(Date.now() + 15 * 60 * 1000); // 15 minutes expiry
      await user.save();

      sendPasswordResetEmail(user.email, user.name, rawToken).catch((err) =>
        console.error('Failed to send reset email:', err.message)
      );
    }

    // Always return constant message to prevent enumeration
    res.status(200).json({
      success: true,
      message: 'If an account exists with that email address, a password reset link has been dispatched.'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Reset password using token
 */
const resetPassword = async (req, res, next) => {
  try {
    const token = req.params.token || req.body.token;
    const { password } = req.body;

    if (!token || !password) {
      return res.status(400).json({
        success: false,
        message: 'Reset token and new password are required.'
      });
    }

    const hashedToken = hashToken(token);

    const user = await User.findOne({
      resetToken: hashedToken,
      resetTokenExpires: { $gt: new Date() }
    });

    if (!user) {
      return res.status(400).json({
        success: false,
        message: 'Password reset link is invalid or has expired.'
      });
    }

    const salt = await bcrypt.genSalt(12);
    user.passwordHash = await bcrypt.hash(password, salt);
    user.resetToken = null;
    user.resetTokenExpires = null;
    await user.save();

    res.status(200).json({
      success: true,
      message: 'Your password has been successfully reset. You can now log in.'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get current authenticated user profile
 */
const getMe = async (req, res, next) => {
  try {
    res.status(200).json({
      success: true,
      user: req.user.toSafeObject()
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  register,
  verifyEmail,
  resendVerification,
  login,
  adminLogin,
  forgotPassword,
  resetPassword,
  getMe
};
