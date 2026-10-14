const express = require('express');
const http = require('http');
const helmet = require('helmet');
const cors = require('cors');
const mongoSanitize = require('express-mongo-sanitize');

const config = require('./config/env');
const { connectDB } = require('./config/db');
const { initSocketIO } = require('./sockets/socketHandler');
const { initCronJobs } = require('./services/cronService');
const { apiLimiter } = require('./middleware/rateLimiter');
const { errorHandler, notFoundHandler } = require('./middleware/errorMiddleware');

const authRoutes = require('./routes/authRoutes');
const catalogueRoutes = require('./routes/catalogueRoutes');
const orderRoutes = require('./routes/orderRoutes');
const adminRoutes = require('./routes/adminRoutes');

const app = express();
const httpServer = http.createServer(app);

// 1. Security Headers
app.use(helmet());

// 2. CORS configuration restricted to client URL
const allowedOrigins = [
  config.clientUrl,
  'http://localhost:5173',
  'http://127.0.0.1:5173',
  'http://localhost:3000'
];

app.use(
  cors({
    origin: function (origin, callback) {
      if (!origin || allowedOrigins.includes(origin)) {
        return callback(null, true);
      }
      return callback(new Error(`Origin ${origin} not allowed by CORS`));
    },
    credentials: true,
    methods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
  })
);

// 3. Body parsers with payload size limits
app.use(express.json({ limit: '50kb' }));
app.use(express.urlencoded({ extended: true, limit: '50kb' }));

// 4. Data sanitization against NoSQL query injection
app.use(mongoSanitize());

// 5. Global API rate limiting
app.use('/api', apiLimiter);

// 6. Root & Health Check Endpoints
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    service: 'PizzaForge Backend API',
    timestamp: new Date().toISOString()
  });
});

// 7. Mount Application Routes
app.use('/api/auth', authRoutes);
app.use('/api', catalogueRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/admin', adminRoutes);

// 8. Error Handlers
app.use(notFoundHandler);
app.use(errorHandler);

// 9. Server Initialization
const startServer = async () => {
  try {
    await connectDB();

    // Auto-seed if database is freshly initialized or empty
    const InventoryItem = require('./models/InventoryItem');
    const count = await InventoryItem.countDocuments();
    if (count === 0) {
      console.log('Database empty: auto-running initial seed...');
      const seed = require('./seed/seed');
      await seed({ closeOnFinish: false });
    }

    // Initialize Socket.IO real-time engine
    initSocketIO(httpServer);

    // Initialize automated scheduled cron tasks
    initCronJobs();

    httpServer.listen(config.port, () => {
      console.log(`\n==================================================`);
      console.log(`🍕 PizzaForge Backend Server Running on Port ${config.port}`);
      console.log(`Environment: ${config.nodeEnv}`);
      console.log(`Allowed Client URL: ${config.clientUrl}`);
      console.log(`==================================================\n`);
    });
  } catch (error) {
    console.error('Failed to start PizzaForge server:', error);
    process.exit(1);
  }
};

startServer();

module.exports = { app, httpServer };
