import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import OrderStepper from '../components/OrderStepper';
import { useSocket } from '../context/SocketContext';
import { useToast } from '../context/ToastContext';
import { getPizzaImage } from '../utils/pizzaImages';

const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const { isConnected, onOrderStatusUpdate } = useSocket();
  const { showToast } = useToast();
  const pollingTimerRef = useRef(null);

  const fetchOrders = useCallback(async (silent = false) => {
    try {
      if (!silent) setRefreshing(true);
      const res = await api.get('/orders/mine');
      if (res.data.success) {
        setOrders(res.data.orders || []);
      }
    } catch (err) {
      console.error('Error fetching orders:', err);
      if (!silent) {
        showToast('Could not refresh orders', 'error');
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchOrders(false);
  }, [fetchOrders]);

  // Real-time Socket.IO status listener
  useEffect(() => {
    const unsubscribe = onOrderStatusUpdate((data) => {
      console.log('Real-time order update received in MyOrders:', data);
      showToast(`Order status updated to: ${data.status}!`, 'info');
      setOrders((prevOrders) =>
        prevOrders.map((order) => {
          if (order._id === data.orderId) {
            return {
              ...order,
              status: data.status,
              updatedAt: new Date().toISOString()
            };
          }
          return order;
        })
      );
    });

    return () => {
      unsubscribe();
    };
  }, [onOrderStatusUpdate, showToast]);

  // Polling fallback: if socket is disconnected, poll every 5 seconds
  useEffect(() => {
    if (!isConnected) {
      pollingTimerRef.current = setInterval(() => {
        fetchOrders(true);
      }, 5000);
    } else {
      if (pollingTimerRef.current) {
        clearInterval(pollingTimerRef.current);
        pollingTimerRef.current = null;
      }
    }

    return () => {
      if (pollingTimerRef.current) {
        clearInterval(pollingTimerRef.current);
      }
    };
  }, [isConnected, fetchOrders]);

  return (
    <div className="main-content">
      <div className="container" style={{ maxWidth: '980px' }}>
        {/* Header & Status Indicator */}
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            marginBottom: '2.5rem'
          }}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
              <h1 style={{ fontSize: '2.4rem', letterSpacing: '-0.02em' }}>Live Kitchen Orders</h1>
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.45rem',
                  padding: '0.3rem 0.85rem',
                  borderRadius: 'var(--radius-full)',
                  background: isConnected ? 'var(--accent-basil-light)' : '#FEF3C7',
                  color: isConnected ? 'var(--accent-basil)' : '#92400E',
                  fontSize: '0.78rem',
                  fontWeight: 700,
                  border: isConnected ? '1px solid rgba(16, 185, 129, 0.25)' : '1px solid rgba(245, 158, 11, 0.25)'
                }}
              >
                <span
                  style={{
                    width: '8px',
                    height: '8px',
                    borderRadius: '50%',
                    backgroundColor: isConnected ? '#10B981' : '#F59E0B'
                  }}
                  className={isConnected ? 'pulse' : ''}
                />
                {isConnected ? '🟢 Live Socket Stream Connected' : '🟡 Polling (5s Fallback)'}
              </div>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '1rem', marginTop: '0.25rem' }}>
              Real-time updates as your artisanal pizza transitions through our wood-fired hearth and courier fleet.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            <button
              onClick={() => fetchOrders(false)}
              disabled={refreshing}
              className="btn-outline"
              style={{ minHeight: '42px', padding: '0.5rem 1.25rem', fontSize: '0.88rem', fontWeight: 600 }}
            >
              {refreshing ? 'Refreshing...' : '🔄 Refresh Queue'}
            </button>
            <Link
              to="/"
              className="btn-primary"
              style={{ minHeight: '42px', padding: '0.5rem 1.5rem', fontSize: '0.88rem', fontWeight: 700 }}
            >
              + Order More
            </Link>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div style={{ padding: '5rem 1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '3.5rem', animation: 'spin 1.5s infinite linear' }}>📦</div>
            <h3 style={{ marginTop: '1rem', color: 'var(--text-main)' }}>Locating Your Kitchen Orders...</h3>
            <p style={{ color: 'var(--text-muted)' }}>Retrieving preparation timeline and dispatch coordinates.</p>
          </div>
        )}

        {/* Empty State */}
        {!loading && orders.length === 0 && (
          <div
            className="card"
            style={{
              textAlign: 'center',
              padding: '4.5rem 2rem',
              border: '1px solid var(--border-light)',
              boxShadow: 'var(--shadow-card)'
            }}
          >
            <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🍕</div>
            <h2 style={{ fontSize: '1.85rem', marginBottom: '0.5rem', fontWeight: 800 }}>No Active Orders Found</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '2rem', maxWidth: '520px', margin: '0 auto 2rem' }}>
              You haven't ordered any pizzas yet! Choose from our wood-fired specialties or launch our live builder.
            </p>
            <Link to="/" className="btn-primary" style={{ padding: '0.85rem 2rem', fontWeight: 700 }}>
              Explore Chef Menu →
            </Link>
          </div>
        )}

        {/* Orders List */}
        {!loading && orders.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            {orders.map((order) => {
              const dateStr = new Date(order.createdAt).toLocaleString(undefined, {
                dateStyle: 'medium',
                timeStyle: 'short'
              });

              return (
                <div
                  key={order._id}
                  className="card"
                  style={{
                    padding: '2rem',
                    boxShadow: 'var(--shadow-card)',
                    border: '1px solid var(--border-light)',
                    borderRadius: 'var(--radius-xl)'
                  }}
                >
                  {/* Card Header */}
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      justifyContent: 'space-between',
                      alignItems: 'flex-start',
                      gap: '1rem',
                      borderBottom: '1px solid var(--border-light)',
                      paddingBottom: '1.25rem',
                      marginBottom: '1.5rem'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', flexWrap: 'wrap' }}>
                        <span style={{ fontWeight: 900, fontSize: '1.3rem', letterSpacing: '-0.01em' }}>
                          Order #{order._id.substring(order._id.length - 8).toUpperCase()}
                        </span>
                        <span
                          className={`badge ${
                            order.payment?.status === 'paid' ? 'badge-success' : 'badge-warning'
                          }`}
                        >
                          {order.payment?.status === 'paid' ? '✓ Paid via Razorpay' : 'Payment: Pending'}
                        </span>
                        <span
                          style={{
                            fontSize: '0.75rem',
                            background: '#F1F5F9',
                            color: '#475569',
                            padding: '0.25rem 0.65rem',
                            borderRadius: 'var(--radius-full)',
                            fontWeight: 600
                          }}
                        >
                          {order.items?.reduce((a, c) => a + c.quantity, 0)} Items
                        </span>
                      </div>
                      <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                        Placed on {dateStr} • Courier Express Dispatch
                      </div>
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
                        Total Amount Paid
                      </div>
                      <div
                        style={{
                          fontSize: '1.75rem',
                          fontWeight: 900,
                          color: 'var(--accent-tomato)',
                          fontFamily: 'var(--font-heading)'
                        }}
                      >
                        ₹{order.totalAmount}
                      </div>
                    </div>
                  </div>

                  {/* Real-time Order Stepper & Stage Alert */}
                  <div
                    style={{
                      background: 'linear-gradient(135deg, #FAF8F5 0%, #F5EFEB 100%)',
                      padding: '1.5rem',
                      borderRadius: 'var(--radius-lg)',
                      marginBottom: '1.75rem',
                      border: '1px solid rgba(0, 0, 0, 0.04)'
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: '1rem',
                        fontSize: '0.9rem',
                        flexWrap: 'wrap',
                        gap: '0.5rem'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ fontSize: '1.1rem' }}>🔥</span>
                        <strong style={{ color: 'var(--text-main)', fontSize: '0.95rem' }}>
                          Current Kitchen Stage:
                        </strong>
                      </div>
                      <span
                        className="badge"
                        style={{
                          backgroundColor:
                            order.status === 'Delivered'
                              ? '#dcfce7'
                              : order.status === 'Sent to Delivery'
                              ? '#ede9fe'
                              : order.status === 'In Kitchen'
                              ? '#dbeafe'
                              : '#fef3c7',
                          color:
                            order.status === 'Delivered'
                              ? '#15803d'
                              : order.status === 'Sent to Delivery'
                              ? '#6d28d9'
                              : order.status === 'In Kitchen'
                              ? '#1d4ed8'
                              : '#b45309',
                          fontSize: '0.82rem',
                          padding: '0.35rem 0.85rem'
                        }}
                      >
                        {order.status}
                      </span>
                    </div>

                    <OrderStepper currentStatus={order.status} />
                  </div>

                  {/* Itemized Pizza Breakdown */}
                  <div>
                    <h4
                      style={{
                        fontSize: '0.85rem',
                        fontWeight: 700,
                        textTransform: 'uppercase',
                        letterSpacing: '0.04em',
                        marginBottom: '0.85rem',
                        color: 'var(--text-muted)'
                      }}
                    >
                      Pizzas In This Batch
                    </h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      {order.items.map((item, idx) => (
                        <div
                          key={idx}
                          style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            padding: '0.85rem 1.15rem',
                            background: '#FFFFFF',
                            border: '1px solid var(--border-light)',
                            borderRadius: 'var(--radius-md)'
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                            <div
                              style={{
                                width: '48px',
                                height: '48px',
                                borderRadius: 'var(--radius-sm)',
                                overflow: 'hidden',
                                flexShrink: 0,
                                border: '1px solid var(--border-light)',
                                background: '#1E1F24'
                              }}
                            >
                              <img
                                src={getPizzaImage(item.pizza?.image || item.pizza?.name || item.name)}
                                alt={item.name}
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                onError={(e) => {
                                  e.target.style.display = 'none';
                                }}
                              />
                            </div>
                            <div>
                              <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-main)' }}>
                                {item.quantity}x {item.name}
                              </div>
                              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginTop: '0.35rem' }}>
                                {item.base && (
                                  <span
                                    style={{
                                      fontSize: '0.72rem',
                                      background: '#F1F5F9',
                                      padding: '0.15rem 0.45rem',
                                      borderRadius: 'var(--radius-sm)',
                                      color: '#475569',
                                      fontWeight: 600
                                    }}
                                  >
                                    🌾 {item.base.name}
                                  </span>
                                )}
                                {item.sauce && (
                                  <span
                                    style={{
                                      fontSize: '0.72rem',
                                      background: '#FEE2E2',
                                      padding: '0.15rem 0.45rem',
                                      borderRadius: 'var(--radius-sm)',
                                      color: '#991B1B',
                                      fontWeight: 600
                                    }}
                                  >
                                    🥫 {item.sauce.name}
                                  </span>
                                )}
                                {item.cheese && (
                                  <span
                                    style={{
                                      fontSize: '0.72rem',
                                      background: '#FEF3C7',
                                      padding: '0.15rem 0.45rem',
                                      borderRadius: 'var(--radius-sm)',
                                      color: '#92400E',
                                      fontWeight: 600
                                    }}
                                  >
                                    🧀 {item.cheese.name}
                                  </span>
                                )}
                                {item.veggies && item.veggies.length > 0 && (
                                  <span
                                    style={{
                                      fontSize: '0.72rem',
                                      background: '#ECFDF5',
                                      padding: '0.15rem 0.45rem',
                                      borderRadius: 'var(--radius-sm)',
                                      color: '#065F46',
                                      fontWeight: 600
                                    }}
                                  >
                                    🥦 {item.veggies.map((v) => v.name).join(', ')}
                                  </span>
                                )}
                              </div>
                            </div>
                          </div>

                          <div
                            style={{
                              fontWeight: 900,
                              fontSize: '1.15rem',
                              fontFamily: 'var(--font-heading)'
                            }}
                          >
                            ₹{item.unitPrice * item.quantity}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyOrders;
