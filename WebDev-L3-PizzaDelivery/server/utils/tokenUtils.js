const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const config = require('../config/env');

const generateRandomToken = () => {
  const rawToken = crypto.randomBytes(32).toString('hex');
  const hashedToken = crypto.createHash('sha256').update(rawToken).digest('hex');
  return { rawToken, hashedToken };
};

const hashToken = (rawToken) => {
  return crypto.createHash('sha256').update(rawToken).digest('hex');
};

const signJWT = (payload) => {
  return jwt.sign(payload, config.jwtSecret, { expiresIn: config.jwtExpiresIn });
};

const verifyJWT = (token) => {
  return jwt.verify(token, config.jwtSecret);
};

module.exports = {
  generateRandomToken,
  hashToken,
  signJWT,
  verifyJWT
};
