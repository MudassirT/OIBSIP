const { Server } = require('socket.io');
const { verifyJWT } = require('../utils/tokenUtils');
const config = require('../config/env');

let io = null;

const initSocketIO = (httpServer) => {
  io = new Server(httpServer, {
    cors: {
      origin: [config.clientUrl, 'http://localhost:5173', 'http://127.0.0.1:5173'],
      methods: ['GET', 'POST', 'PATCH'],
      credentials: true
    }
  });

  // Socket authentication middleware
  io.use((socket, next) => {
    const token =
      socket.handshake.auth?.token ||
      (socket.handshake.headers?.authorization &&
        socket.handshake.headers.authorization.split(' ')[1]);

    if (token) {
      try {
        const decoded = verifyJWT(token);
        socket.user = decoded;
      } catch (err) {
        // Continue unauthenticated, but log warning
        console.warn('[Socket.IO] Invalid token on handshake:', err.message);
      }
    }
    next();
  });

  io.on('connection', (socket) => {
    if (socket.user) {
      const userRoom = `user_${socket.user.userId}`;
      socket.join(userRoom);
      console.log(`[Socket.IO] User connected: ${socket.user.userId} joined room ${userRoom}`);

      if (socket.user.role === 'admin') {
        socket.join('admin_room');
        console.log(`[Socket.IO] Admin socket connected and joined admin_room`);
      }
    } else {
      console.log(`[Socket.IO] Anonymous visitor connected: ${socket.id}`);
    }

    // Client can also manually join their user room if authenticated after connection
    socket.on('join_user_room', (userId) => {
      if (userId) {
        socket.join(`user_${userId}`);
        console.log(`[Socket.IO] Socket ${socket.id} manually joined user_${userId}`);
      }
    });

    socket.on('disconnect', () => {
      console.log(`[Socket.IO] Socket disconnected: ${socket.id}`);
    });
  });

  return io;
};

const getIO = () => {
  if (!io) {
    throw new Error('Socket.IO has not been initialized yet.');
  }
  return io;
};

const notifyOrderStatusUpdate = (userId, order) => {
  if (!io) return;
  const userRoom = `user_${userId}`;
  console.log(`[Socket.IO] Emitting order_status_updated to ${userRoom} and admin_room for Order ${order._id}`);
  io.to(userRoom).emit('order_status_updated', {
    orderId: order._id,
    status: order.status,
    order
  });
  io.to('admin_room').emit('order_status_updated', {
    orderId: order._id,
    status: order.status,
    order
  });
};

const notifyAdminNewOrder = (order) => {
  if (!io) return;
  console.log(`[Socket.IO] Emitting new_order_placed to admin_room for Order ${order._id}`);
  io.to('admin_room').emit('new_order_placed', {
    orderId: order._id,
    order
  });
};

module.exports = {
  initSocketIO,
  getIO,
  notifyOrderStatusUpdate,
  notifyAdminNewOrder
};
