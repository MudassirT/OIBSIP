import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';

const CATEGORIES = [
  { key: 'all', label: 'All Ingredients' },
  { key: 'base', label: '🌾 Pizza Bases' },
  { key: 'sauce', label: '🥫 Sauces' },
  { key: 'cheese', label: '🧀 Cheeses' },
  { key: 'veggie', label: '🥦 Vegetables' }
];

const InventoryDashboard = () => {
  const [items, setItems] = useState([]);
  const [lowStockCount, setLowStockCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [activeCategory, setActiveCategory] = useState('all');
  const [editingItem, setEditingItem] = useState(null); // Item currently being edited in modal
  const [checkLoading, setCheckLoading] = useState(false);
  const { showToast } = useToast();

  const fetchInventory = useCallback(async () => {
    try {
      setLoading(true);
      const res = await api.get('/admin/inventory');
      if (res.data.success) {
        setItems(res.data.items || []);
        setLowStockCount(res.data.lowStockCount || 0);
      }
    } catch (err) {
      console.error('Failed to fetch admin inventory:', err);
      showToast('Could not load inventory database', 'error');
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchInventory();
  }, [fetchInventory]);

  // Run manual low-stock check immediately
  const handleTriggerLowStockCheck = async () => {
    setCheckLoading(true);
    try {
      const res = await api.post('/admin/low-stock-check');
      if (res.data.success) {
        showToast(res.data.message, res.data.count > 0 ? 'warning' : 'success');
        fetchInventory();
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Error executing manual check', 'error');
    } finally {
      setCheckLoading(false);
    }
  };

  // Submit manual stock update
  const handleSaveStock = async (e) => {
    e.preventDefault();
    if (!editingItem) return;

    const payload = {};
    if (editingItem.mode === 'set') {
      const val = parseInt(editingItem.stockInput, 10);
      if (isNaN(val) || val < 0) {
        showToast('Stock must be a non-negative integer', 'warning');
        return;
      }
      payload.stock = val;
    } else if (editingItem.mode === 'add') {
      const val = parseInt(editingItem.addStockInput, 10);
      if (isNaN(val) || val < 0) {
        showToast('Added stock must be a non-negative integer', 'warning');
        return;
      }
      payload.addStock = val;
    }

    if (editingItem.thresholdInput !== undefined && editingItem.thresholdInput !== '') {
      const thresh = parseInt(editingItem.thresholdInput, 10);
      if (isNaN(thresh) || thresh < 0) {
        showToast('Threshold must be a non-negative integer', 'warning');
        return;
      }
      payload.threshold = thresh;
    }

    try {
      const res = await api.patch(`/admin/inventory/${editingItem._id}`, payload);
      if (res.data.success) {
        showToast(`Stock updated for "${res.data.item.name}"!`, 'success');
        setEditingItem(null);
        fetchInventory();
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update stock', 'error');
    }
  };

  const filteredItems = items.filter((item) => {
    if (activeCategory === 'all') return true;
    return item.category === activeCategory;
  });

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
            <span className="badge badge-tomato" style={{ marginBottom: '0.4rem' }}>
              Admin Operations Portal
            </span>
            <h1 style={{ fontSize: '2.1rem', marginBottom: '0.2rem' }}>Inventory Control</h1>
            <p style={{ color: 'var(--text-muted)' }}>
              Real-time stock monitoring, threshold configuration, and manual restock management
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button
              onClick={handleTriggerLowStockCheck}
              disabled={checkLoading}
              className="btn-danger"
              style={{
                minHeight: '42px',
                padding: '0.5rem 1.25rem',
                fontSize: '0.9rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem'
              }}
            >
              <span>🚨</span>
              <span>{checkLoading ? 'Checking...' : 'Run Low-Stock Check Now'}</span>
            </button>
            <button
              onClick={fetchInventory}
              className="btn-outline"
              style={{ minHeight: '42px', padding: '0.5rem 1rem', fontSize: '0.9rem' }}
            >
              🔄 Refresh
            </button>
          </div>
        </div>

        {/* Metrics Row */}
        <div className="grid-3" style={{ marginBottom: '2.5rem' }}>
          <div className="card" style={{ padding: '1.25rem' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Total Ingredients
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 900, marginTop: '0.25rem', color: 'var(--text-main)' }}>
              {items.length}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
              Across 4 core pizza categories
            </div>
          </div>

          <div
            className="card"
            style={{
              padding: '1.25rem',
              borderColor: lowStockCount > 0 ? '#FECACA' : 'var(--border-light)',
              backgroundColor: lowStockCount > 0 ? '#FEF2F2' : '#FFFFFF'
            }}
          >
            <div style={{ fontSize: '0.8rem', color: lowStockCount > 0 ? '#DC2626' : 'var(--text-muted)', textTransform: 'uppercase' }}>
              Low Stock Alerts (&lt; Threshold)
            </div>
            <div
              style={{
                fontSize: '2rem',
                fontWeight: 900,
                marginTop: '0.25rem',
                color: lowStockCount > 0 ? '#DC2626' : 'var(--accent-basil)'
              }}
            >
              {lowStockCount} items
            </div>
            <div style={{ fontSize: '0.75rem', color: lowStockCount > 0 ? '#991B1B' : 'var(--accent-basil)', marginTop: '0.25rem' }}>
              {lowStockCount > 0 ? '⚠️ Immediate restock required' : '✓ All ingredients healthy'}
            </div>
          </div>

          <div className="card" style={{ padding: '1.25rem' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Automated Cron Monitor
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 800, marginTop: '0.4rem', color: 'var(--text-main)' }}>
              Node-Cron Active
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-light)', marginTop: '0.25rem' }}>
              Spam protected: <code>lowStockNotified</code> flag active
            </div>
          </div>
        </div>

        {/* Category Tabs */}
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
          {CATEGORIES.map((cat) => (
            <button
              key={cat.key}
              onClick={() => setActiveCategory(cat.key)}
              className={activeCategory === cat.key ? 'btn-primary' : 'btn-outline'}
              style={{ minHeight: '38px', padding: '0.4rem 1rem', fontSize: '0.85rem' }}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Loading state */}
        {loading && (
          <div style={{ padding: '4rem 1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '2.5rem', animation: 'spin 1.5s infinite linear' }}>📦</div>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Syncing stock levels...</p>
          </div>
        )}

        {/* Inventory Table */}
        {!loading && (
          <div
            className="card"
            style={{
              padding: '0',
              overflow: 'hidden',
              border: '1px solid var(--border-light)'
            }}
          >
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ background: '#F9FAFB', borderBottom: '1px solid #E5E7EB', fontSize: '0.8rem', color: '#4B5563', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                    <th style={{ padding: '1rem 1.25rem' }}>Ingredient</th>
                    <th style={{ padding: '1rem' }}>Category</th>
                    <th style={{ padding: '1rem' }}>Current Stock</th>
                    <th style={{ padding: '1rem' }}>Safety Threshold</th>
                    <th style={{ padding: '1rem' }}>Unit Price</th>
                    <th style={{ padding: '1rem' }}>Status</th>
                    <th style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredItems.map((item) => {
                    const isLowStock = item.stock < item.threshold;
                    const isOutOfStock = item.stock <= 0;

                    return (
                      <tr
                        key={item._id}
                        style={{
                          borderBottom: '1px solid #F3F4F6',
                          backgroundColor: isOutOfStock
                            ? '#FEF2F2'
                            : isLowStock
                            ? '#FFFBEB'
                            : '#FFFFFF',
                          transition: 'background-color 0.2s'
                        }}
                      >
                        <td style={{ padding: '1rem 1.25rem', fontWeight: 600 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            {isLowStock && <span title="Low stock alert!">⚠️</span>}
                            <span>{item.name}</span>
                          </div>
                        </td>

                        <td style={{ padding: '1rem', textTransform: 'capitalize', color: 'var(--text-muted)' }}>
                          <span
                            style={{
                              fontSize: '0.75rem',
                              padding: '0.2rem 0.5rem',
                              borderRadius: '4px',
                              background: '#F3F4F6',
                              fontWeight: 600
                            }}
                          >
                            {item.category}
                          </span>
                        </td>

                        <td style={{ padding: '1rem' }}>
                          <span
                            style={{
                              fontWeight: 800,
                              fontSize: '1.05rem',
                              color: isOutOfStock
                                ? '#DC2626'
                                : isLowStock
                                ? '#D97706'
                                : 'var(--text-main)'
                            }}
                          >
                            {item.stock} units
                          </span>
                        </td>

                        <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>
                          {item.threshold} units
                        </td>

                        <td style={{ padding: '1rem', fontWeight: 600 }}>
                          ₹{item.price}
                        </td>

                        <td style={{ padding: '1rem' }}>
                          {isOutOfStock ? (
                            <span className="badge badge-danger">Out of Stock</span>
                          ) : isLowStock ? (
                            <span className="badge badge-warning">Low Stock</span>
                          ) : (
                            <span className="badge badge-success">Optimal</span>
                          )}
                        </td>

                        <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                          <button
                            onClick={() =>
                              setEditingItem({
                                ...item,
                                mode: 'add',
                                addStockInput: '25',
                                stockInput: item.stock,
                                thresholdInput: item.threshold
                              })
                            }
                            className="btn-outline"
                            style={{
                              padding: '0.35rem 0.85rem',
                              fontSize: '0.8rem',
                              minHeight: '34px'
                            }}
                          >
                            ✏️ Restock / Edit
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Edit / Restock Modal Dialog */}
        {editingItem && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              width: '100vw',
              height: '100vh',
              backgroundColor: 'rgba(0, 0, 0, 0.55)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1000,
              padding: '1rem'
            }}
          >
            <div
              className="card"
              style={{
                maxWidth: '460px',
                width: '100%',
                background: '#FFFFFF',
                padding: '2rem',
                borderRadius: 'var(--radius-lg)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.3rem' }}>Restock: {editingItem.name}</h3>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    Current Stock: <strong>{editingItem.stock}</strong> • Threshold: <strong>{editingItem.threshold}</strong>
                  </p>
                </div>
                <button
                  onClick={() => setEditingItem(null)}
                  style={{ minHeight: '32px', width: '32px', padding: 0, fontSize: '1.2rem', color: 'var(--text-muted)' }}
                >
                  ✕
                </button>
              </div>

              <form onSubmit={handleSaveStock}>
                {/* Mode Selector */}
                <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem' }}>
                  <button
                    type="button"
                    onClick={() => setEditingItem({ ...editingItem, mode: 'add' })}
                    className={editingItem.mode === 'add' ? 'btn-primary' : 'btn-outline'}
                    style={{ flex: 1, minHeight: '36px', fontSize: '0.85rem' }}
                  >
                    + Add to Stock
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingItem({ ...editingItem, mode: 'set' })}
                    className={editingItem.mode === 'set' ? 'btn-primary' : 'btn-outline'}
                    style={{ flex: 1, minHeight: '36px', fontSize: '0.85rem' }}
                  >
                    Set Stock Exactly
                  </button>
                </div>

                {editingItem.mode === 'add' ? (
                  <div className="form-group">
                    <label className="form-label" htmlFor="addStockInput">Units to Add</label>
                    <input
                      id="addStockInput"
                      type="number"
                      min="0"
                      className="form-input"
                      value={editingItem.addStockInput}
                      onChange={(e) => setEditingItem({ ...editingItem, addStockInput: e.target.value })}
                      required
                    />
                  </div>
                ) : (
                  <div className="form-group">
                    <label className="form-label" htmlFor="stockInput">New Exact Stock Value</label>
                    <input
                      id="stockInput"
                      type="number"
                      min="0"
                      className="form-input"
                      value={editingItem.stockInput}
                      onChange={(e) => setEditingItem({ ...editingItem, stockInput: e.target.value })}
                      required
                    />
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label" htmlFor="thresholdInput">Alert Safety Threshold</label>
                  <input
                    id="thresholdInput"
                    type="number"
                    min="0"
                    className="form-input"
                    value={editingItem.thresholdInput}
                    onChange={(e) => setEditingItem({ ...editingItem, thresholdInput: e.target.value })}
                    required
                  />
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-light)' }}>
                    Trigger email alert when stock drops strictly below this value
                  </span>
                </div>

                <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                  <button
                    type="button"
                    onClick={() => setEditingItem(null)}
                    className="btn-outline"
                    style={{ flex: 1 }}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                    style={{ flex: 1 }}
                  >
                    Save Changes
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InventoryDashboard;
