const dotenv = require('dotenv');
const path = require('path');

// Load environment variables from .env if present
dotenv.config({ path: path.join(__dirname, '..', '.env') });

const requiredInProduction = ['JWT_SECRET', 'MONGO_URI'];

if (process.env.NODE_ENV === 'production') {
  const missing = requiredInProduction.filter((key) => !process.env[key]);
  if (missing.length > 0) {
    console.error(`FATAL: Missing required environment variables in production: ${missing.join(', ')}`);
    process.exit(1);
  }
}

const config = {
  port: parseInt(process.env.PORT || '5000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  clientUrl: (process.env.CLIENT_URL || 'http://localhost:5173').replace(/\/$/, ''),
  mongoUri: process.env.MONGO_URI || '',
  jwtSecret: process.env.JWT_SECRET || 'pizzaforge_dev_jwt_secret_key_minimum_32_characters_random_string!',
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '1d',
  admin: {
    name: process.env.ADMIN_NAME || 'PizzaForge Admin',
    email: (process.env.ADMIN_EMAIL || 'admin@pizzaforge.com').toLowerCase().trim(),
    password: process.env.ADMIN_PASSWORD || 'Admin@123456'
  },
  razorpay: {
    keyId: process.env.RAZORPAY_KEY_ID || 'rzp_test_placeholder',
    keySecret: process.env.RAZORPAY_KEY_SECRET || 'placeholder_secret'
  },
  email: {
    host: process.env.SMTP_HOST || '',
    port: parseInt(process.env.SMTP_PORT || '587', 10),
    user: process.env.SMTP_USER || '',
    pass: process.env.SMTP_PASS || '',
    from: process.env.SMTP_FROM || '"PizzaForge Kitchen" <noreply@pizzaforge.com>'
  },
  cronSchedule: process.env.CRON_SCHEDULE || '0 * * * *'
};

module.exports = config;
