import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { useSocket } from '../context/SocketContext';
import { useToast } from '../context/ToastContext';

const STATUS_SEQUENCE = ['Order Received', 'In Kitchen', 'Sent to Delivery', 'Delivered'];

const OrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('all');
  const [updatingId, setUpdatingId] = useState(null);
  const { onNewOrderAlert, onOrderStatusUpdate } = useSocket();
  const { showToast } = useToast();

  const fetchOrders = useCallback(async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      const url = filterStatus !== 'all' ? `/admin/orders?status=${filterStatus}` : '/admin/orders';
      const res = await api.get(url);
      if (res.data.success) {
        setOrders(res.data.orders || []);
      }
    } catch (err) {
      console.error('Failed to fetch admin orders:', err);
      if (!silent) showToast('Could not load orders queue', 'error');
    } finally {
      setLoading(false);
    }
  }, [filterStatus, showToast]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // Real-time live new order listener for admins
  useEffect(() => {
    const unsubNewOrder = onNewOrderAlert((data) => {
      console.log('Admin received live new_order_placed event:', data);
      showToast(`🔥 New order received: #${data.order?._id.slice(-6)} (₹${data.order?.totalAmount})`, 'success');
      fetchOrders(true);
    });

    const unsubStatusUpdate = onOrderStatusUpdate((data) => {
      setOrders((prev) =>
        prev.map((ord) => (ord._id === data.orderId ? { ...ord, status: data.status } : ord))
      );
    });

    return () => {
      unsubNewOrder();
      unsubStatusUpdate();
    };
  }, [onNewOrderAlert, onOrderStatusUpdate, fetchOrders, showToast]);

  // Handle Forward-Only Status Advancement
  const handleAdvanceStatus = async (order, targetStatus) => {
    const currentIndex = STATUS_SEQUENCE.indexOf(order.status);
    const targetIndex = STATUS_SEQUENCE.indexOf(targetStatus);

    if (targetIndex <= currentIndex) {
      showToast('Status can only advance forward in the preparation cycle.', 'warning');
      return;
    }

    setUpdatingId(order._id);
    try {
      const res = await api.patch(`/admin/orders/${order._id}/status`, { status: targetStatus });
      if (res.data.success) {
        showToast(`Order status advanced to "${targetStatus}"!`, 'success');
        setOrders((prev) =>
          prev.map((o) => (o._id === order._id ? { ...o, status: targetStatus } : o))
        );
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to advance status', 'error');
    } finally {
      setUpdatingId(null);
    }
  };

  const getNextStatus = (current) => {
    const idx = STATUS_SEQUENCE.indexOf(current);
    if (idx >= 0 && idx < STATUS_SEQUENCE.length - 1) {
      return STATUS_SEQUENCE[idx + 1];
    }
    return null;
  };

  return (
    <div className="main-content">
      <div className="container">
        {/* Header Section */}
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            marginBottom: '2rem'
          }}
        >
          <div>
            <span className="badge badge-basil" style={{ marginBottom: '0.4rem' }}>
              Kitchen Dispatch Central
            </span>
            <h1 style={{ fontSize: '2.1rem', marginBottom: '0.2rem' }}>Order Management</h1>
            <p style={{ color: 'var(--text-muted)' }}>
              Real-time kitchen board with forward-only state progression and live customer notifications
            </p>
          </div>

          <button
            onClick={() => fetchOrders(false)}
            className="btn-outline"
            style={{ minHeight: '42px', padding: '0.5rem 1rem', fontSize: '0.9rem' }}
          >
            🔄 Refresh Queue
          </button>
        </div>

        {/* Filter Bar */}
        <div
          style={{
            display: 'flex',
            gap: '0.5rem',
            flexWrap: 'wrap',
            marginBottom: '2rem',
            alignItems: 'center'
          }}
        >
          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            Filter by Stage:
          </span>
          <button
            onClick={() => setFilterStatus('all')}
            className={filterStatus === 'all' ? 'btn-primary' : 'btn-outline'}
            style={{ minHeight: '36px', padding: '0.35rem 0.85rem', fontSize: '0.85rem' }}
          >
            All Orders ({orders.length})
          </button>
          {STATUS_SEQUENCE.map((s) => (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={filterStatus === s ? 'btn-primary' : 'btn-outline'}
              style={{ minHeight: '36px', padding: '0.35rem 0.85rem', fontSize: '0.85rem' }}
            >
              {s}
            </button>
          ))}
        </div>

        {/* Loading State */}
        {loading && (
          <div style={{ padding: '4rem 1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '2.5rem', animation: 'spin 1.5s infinite linear' }}>🍕</div>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.75rem' }}>Loading active kitchen orders...</p>
          </div>
        )}

        {/* Empty State */}
        {!loading && orders.length === 0 && (
          <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
            <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>✨</div>
            <h3 style={{ marginBottom: '0.5rem' }}>No Orders in this Category</h3>
            <p style={{ color: 'var(--text-muted)' }}>Orders will appear live when customers checkout via Razorpay</p>
          </div>
        )}

        {/* Orders Queue Cards */}
        {!loading && orders.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {orders.map((order) => {
              const nextStatus = getNextStatus(order.status);
              const isUpdating = updatingId === order._id;

              return (
                <div
                  key={order._id}
                  className="card"
                  style={{
                    padding: '1.5rem',
                    boxShadow: 'var(--shadow-sm)',
                    border: '1px solid var(--border-light)'
                  }}
                >
                  {/* Top Bar: Customer & Payment */}
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      justifyContent: 'space-between',
                      alignItems: 'flex-start',
                      gap: '1rem',
                      borderBottom: '1px solid var(--border-light)',
                      paddingBottom: '0.85rem',
                      marginBottom: '1rem'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                        <span style={{ fontWeight: 800, fontSize: '1.15rem' }}>
                          Order #{order._id.substring(order._id.length - 8).toUpperCase()}
                        </span>
                        <span
                          className={`badge ${
                            order.payment?.status === 'paid' ? 'badge-success' : 'badge-warning'
                          }`}
                        >
                          Payment: {order.payment?.status || 'pending'}
                        </span>
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
                                : '#b45309'
                          }}
                        >
                          {order.status}
                        </span>
                      </div>

                      <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>
                        Customer: <strong>{order.user?.name || 'Customer'}</strong> ({order.user?.email || 'N/A'}) •{' '}
                        {new Date(order.createdAt).toLocaleString()}
                      </div>
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Order Total</div>
                      <div
                        style={{
                          fontSize: '1.45rem',
                          fontWeight: 900,
                          color: 'var(--accent-tomato)',
                          fontFamily: 'var(--font-heading)'
                        }}
                      >
                        ₹{order.totalAmount}
                      </div>
                    </div>
                  </div>

                  {/* Items Summary */}
                  <div style={{ marginBottom: '1.25rem' }}>
                    <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.4rem' }}>
                      Items to Prepare:
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                      {order.items.map((item, idx) => (
                        <div
                          key={idx}
                          style={{
                            background: 'var(--bg-elevated)',
                            padding: '0.45rem 0.75rem',
                            borderRadius: 'var(--radius-sm)',
                            fontSize: '0.85rem',
                            border: '1px solid var(--border-subtle)'
                          }}
                        >
                          <strong>{item.quantity}x</strong> {item.name}
                          {item.base && <span style={{ color: 'var(--text-muted)' }}> ({item.base.name})</span>}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Forward Status Advancement Action Controls */}
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      gap: '1rem',
                      borderTop: '1px solid var(--border-subtle)',
                      paddingTop: '1rem'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Status Lifecycle:</span>
                      <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {order.status}
                      </span>
                    </div>

                    <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
                      {nextStatus ? (
                        <button
                          onClick={() => handleAdvanceStatus(order, nextStatus)}
                          disabled={isUpdating}
                          className="btn-primary"
                          style={{
                            padding: '0.5rem 1.25rem',
                            fontSize: '0.9rem',
                            minHeight: '38px',
                            background:
                              nextStatus === 'In Kitchen'
                                ? '#2563EB'
                                : nextStatus === 'Sent to Delivery'
                                ? '#7C3AED'
                                : '#16A34A'
                          }}
                        >
                          {isUpdating ? (
                            'Updating...'
                          ) : (
                            <>
                              Advance to: <strong>{nextStatus}</strong> →
                            </>
                          )}
                        </button>
                      ) : (
                        <span
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.35rem',
                            color: 'var(--accent-basil)',
                            fontWeight: 700,
                            fontSize: '0.9rem'
                          }}
                        >
                          ✓ Order Fully Delivered
                        </span>
                      )}
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

export default OrderManagement;
