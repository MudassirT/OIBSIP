import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { io } from 'socket.io-client';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';

const SocketContext = createContext(null);

const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || 'http://localhost:5000';

export const SocketProvider = ({ children }) => {
  const { token, user, isAdmin } = useAuth();
  const { showToast } = useToast();
  const [socket, setSocket] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const listenersRef = useRef({
    orderStatusUpdates: new Set(),
    newOrderAlerts: new Set()
  });

  useEffect(() => {
    // Initialize or reconnect socket when token changes
    const newSocket = io(SOCKET_URL, {
      auth: { token: token || '' },
      transports: ['websocket', 'polling'],
      reconnectionAttempts: 10,
      reconnectionDelay: 2000
    });

    newSocket.on('connect', () => {
      console.log('[Socket.IO] Connected to live kitchen server:', newSocket.id);
      setIsConnected(true);

      // Join user room
      if (user && user._id) {
        newSocket.emit('join_user_room', user._id);
      }
    });

    newSocket.on('disconnect', (reason) => {
      console.warn('[Socket.IO] Disconnected from server:', reason);
      setIsConnected(false);
    });

    // Handle Order Status Updated
    newSocket.on('order_status_updated', (data) => {
      console.log('[Socket.IO] Received order_status_updated event:', data);
      showToast(`Order status updated to: "${data.status}"`, 'info');
      // Notify all registered listener callbacks
      listenersRef.current.orderStatusUpdates.forEach((callback) => {
        try {
          callback(data);
        } catch (e) {
          console.error('Error in orderStatusUpdate listener:', e);
        }
      });
    });

    // Handle New Order Placed (for admins)
    newSocket.on('new_order_placed', (data) => {
      console.log('[Socket.IO] Received new_order_placed event:', data);
      if (isAdmin) {
        showToast(`🔔 New Order received! ₹${data.order?.totalAmount}`, 'success');
      }
      listenersRef.current.newOrderAlerts.forEach((callback) => {
        try {
          callback(data);
        } catch (e) {
          console.error('Error in newOrderAlert listener:', e);
        }
      });
    });

    setSocket(newSocket);

    return () => {
      newSocket.disconnect();
    };
  }, [token, user, isAdmin, showToast]);

  const onOrderStatusUpdate = (callback) => {
    listenersRef.current.orderStatusUpdates.add(callback);
    return () => {
      listenersRef.current.orderStatusUpdates.delete(callback);
    };
  };

  const onNewOrderAlert = (callback) => {
    listenersRef.current.newOrderAlerts.add(callback);
    return () => {
      listenersRef.current.newOrderAlerts.delete(callback);
    };
  };

  return (
    <SocketContext.Provider
      value={{
        socket,
        isConnected,
        onOrderStatusUpdate,
        onNewOrderAlert
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => {
  const context = useContext(SocketContext);
  if (!context) {
    throw new Error('useSocket must be used within a SocketProvider');
  }
  return context;
};
