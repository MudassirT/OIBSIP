import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import PizzaCard from '../components/PizzaCard';
import { useToast } from '../context/ToastContext';

const Dashboard = () => {
  const [pizzas, setPizzas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeFilter, setActiveFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const { showToast } = useToast();

  const fetchPizzas = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await api.get('/pizzas');
      if (res.data.success) {
        setPizzas(res.data.pizzas || []);
      }
    } catch (err) {
      console.error('Error fetching pizzas:', err);
      setError('Unable to load pizza catalogue. Please make sure the backend server is running.');
      showToast('Could not load pizza catalogue', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPizzas();
  }, []);

  const filteredPizzas = pizzas.filter((p) => {
    // Search query match
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchName = p.name.toLowerCase().includes(q);
      const matchDesc = p.description?.toLowerCase().includes(q);
      if (!matchName && !matchDesc) return false;
    }

    // Category filter match
    if (activeFilter === 'available') return p.isAvailable;
    if (activeFilter === 'spicy') return p.name.toLowerCase().includes('peri') || p.name.toLowerCase().includes('bbq');
    if (activeFilter === 'classic') return p.name.toLowerCase().includes('margherita') || p.name.toLowerCase().includes('farmhouse');
    return true;
  });

  return (
    <div className="main-content">
      <div className="container">
        {/* Figma Hero Banner */}
        <div
          style={{
            background: 'linear-gradient(135deg, #141519 0%, #20232B 55%, #18191E 100%)',
            color: '#FFFFFF',
            borderRadius: 'var(--radius-xl)',
            padding: '3.5rem 3rem',
            marginBottom: '3.5rem',
            position: 'relative',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-xl)',
            border: '1px solid rgba(255, 255, 255, 0.08)'
          }}
        >
          {/* Ambient light glow behind hero title */}
          <div
            style={{
              position: 'absolute',
              top: '-60px',
              right: '-60px',
              width: '320px',
              height: '320px',
              background: 'radial-gradient(circle, rgba(226, 55, 68, 0.35) 0%, rgba(245, 158, 11, 0.15) 50%, transparent 70%)',
              borderRadius: '50%',
              pointerEvents: 'none'
            }}
          />

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: '1.2fr 0.8fr',
              gap: '2.5rem',
              alignItems: 'center',
              position: 'relative',
              zIndex: 1
            }}
            className="hero-split-grid"
          >
            <div>
              {/* Promo Pill Badge */}
              <div
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.6rem',
                  background: 'rgba(255, 255, 255, 0.08)',
                  border: '1px solid rgba(255, 255, 255, 0.16)',
                  padding: '0.4rem 1rem',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.82rem',
                  fontWeight: 700,
                  color: '#FED7AA',
                  marginBottom: '1.25rem',
                  backdropFilter: 'blur(8px)'
                }}
              >
                <span>🔥 SPECIAL OFFER:</span>
                <span style={{ color: '#FCD34D' }}>Use Code FORGE20 for 20% OFF</span>
              </div>

              <h1
                style={{
                  color: '#FFFFFF',
                  fontSize: 'clamp(2.2rem, 4vw, 3.2rem)',
                  fontWeight: 900,
                  lineHeight: 1.1,
                  marginBottom: '1.2rem',
                  letterSpacing: '-0.03em'
                }}
              >
                Wood-Fired Perfection, <br />
                <span className="gradient-text">Crafted Just for You.</span>
              </h1>

              <p style={{ color: '#CBD5E1', fontSize: '1.05rem', lineHeight: 1.6, marginBottom: '2.25rem' }}>
                Experience authentic artisanal pizza baked at 450°C. Order chef-curated masterpieces with fresh real
                ingredients or enter the interactive studio to sculpt your own signature pie.
              </p>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
                <Link
                  to="/builder"
                  className="btn-primary"
                  style={{
                    padding: '0.9rem 2rem',
                    fontSize: '1.05rem',
                    fontWeight: 700,
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.6rem',
                    boxShadow: 'var(--shadow-glow-tomato)'
                  }}
                >
                  <span>🛠️</span>
                  <span>Launch Pizza Builder</span>
                </Link>
                <a
                  href="#menu-section"
                  className="btn-outline"
                  style={{
                    color: '#FFFFFF',
                    borderColor: 'rgba(255, 255, 255, 0.25)',
                    padding: '0.85rem 1.75rem',
                    fontSize: '1rem',
                    fontWeight: 600,
                    background: 'rgba(255, 255, 255, 0.05)'
                  }}
                >
                  Browse Menu ({pizzas.length})
                </a>
              </div>

              {/* Quick Stats Pill Strip */}
              <div
                style={{
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '1.5rem',
                  marginTop: '2.5rem',
                  paddingTop: '1.75rem',
                  borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '1.2rem' }}>⚡</span>
                  <span style={{ fontSize: '0.85rem', color: '#E2E8F0', fontWeight: 600 }}>25 Min Express Delivery</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '1.2rem' }}>🪵</span>
                  <span style={{ fontSize: '0.85rem', color: '#E2E8F0', fontWeight: 600 }}>450°C Oak Wood Oven</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '1.2rem' }}>🧀</span>
                  <span style={{ fontSize: '0.85rem', color: '#E2E8F0', fontWeight: 600 }}>100% Real Fior di Latte</span>
                </div>
              </div>
            </div>

            {/* Right Side: Showcase Real Pizza Photography Card */}
            <div
              style={{
                position: 'relative',
                borderRadius: 'var(--radius-xl)',
                overflow: 'hidden',
                boxShadow: '0 20px 40px -10px rgba(0, 0, 0, 0.65)',
                border: '2px solid rgba(255, 255, 255, 0.15)'
              }}
              className="hero-image-card"
            >
              <img
                src="/images/hero-pizza.jpg"
                alt="Wood-Fired Pizza with Molten Cheese"
                style={{
                  width: '100%',
                  height: '320px',
                  objectFit: 'cover',
                  display: 'block'
                }}
                loading="eager"
              />
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  background: 'linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.7) 100%)',
                  pointerEvents: 'none'
                }}
              />
              <div
                style={{
                  position: 'absolute',
                  bottom: '1.25rem',
                  left: '1.25rem',
                  right: '1.25rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-end',
                  zIndex: 2
                }}
              >
                <div>
                  <span
                    style={{
                      background: 'rgba(226, 55, 68, 0.85)',
                      color: '#FFFFFF',
                      padding: '0.25rem 0.65rem',
                      borderRadius: 'var(--radius-full)',
                      fontSize: '0.72rem',
                      fontWeight: 700,
                      textTransform: 'uppercase',
                      letterSpacing: '0.04em'
                    }}
                  >
                    🔥 450°C Hearth Pulled
                  </span>
                  <div style={{ color: '#FFFFFF', fontWeight: 800, fontSize: '1.15rem', marginTop: '0.35rem' }}>
                    Artisanal Handcrafted
                  </div>
                </div>
                <span
                  style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    backdropFilter: 'blur(8px)',
                    color: '#FCD34D',
                    padding: '0.3rem 0.75rem',
                    borderRadius: 'var(--radius-full)',
                    fontSize: '0.78rem',
                    fontWeight: 700
                  }}
                >
                  Fresh Daily
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Menu Section Header, Search & Filters */}
        <div
          id="menu-section"
          style={{
            marginBottom: '2.5rem'
          }}
        >
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'flex-end',
              justifyContent: 'space-between',
              gap: '1.5rem',
              marginBottom: '1.5rem'
            }}
          >
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  color: 'var(--accent-tomato)',
                  marginBottom: '0.35rem'
                }}
              >
                From The Wood-Fired Hearth
              </div>
              <h2 style={{ fontSize: '2rem', letterSpacing: '-0.02em' }}>Chef's Specialty Pizzas</h2>
            </div>

            {/* Live Search Input */}
            <div style={{ minWidth: '280px', maxWidth: '360px', width: '100%' }}>
              <input
                type="text"
                placeholder="🔍 Search pizzas or ingredients..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="form-input"
                style={{
                  minHeight: '44px',
                  borderRadius: 'var(--radius-full)',
                  padding: '0.65rem 1.25rem',
                  fontSize: '0.9rem',
                  background: '#FFFFFF'
                }}
              />
            </div>
          </div>

          {/* Filter Pills Bar */}
          <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <button
              onClick={() => setActiveFilter('all')}
              className={activeFilter === 'all' ? 'btn-primary' : 'btn-outline'}
              style={{
                minHeight: '40px',
                padding: '0.45rem 1.15rem',
                fontSize: '0.88rem',
                borderRadius: 'var(--radius-full)'
              }}
            >
              All Varieties ({pizzas.length})
            </button>
            <button
              onClick={() => setActiveFilter('available')}
              className={activeFilter === 'available' ? 'btn-primary' : 'btn-outline'}
              style={{
                minHeight: '40px',
                padding: '0.45rem 1.15rem',
                fontSize: '0.88rem',
                borderRadius: 'var(--radius-full)'
              }}
            >
              ● In Stock Only
            </button>
            <button
              onClick={() => setActiveFilter('classic')}
              className={activeFilter === 'classic' ? 'btn-primary' : 'btn-outline'}
              style={{
                minHeight: '40px',
                padding: '0.45rem 1.15rem',
                fontSize: '0.88rem',
                borderRadius: 'var(--radius-full)'
              }}
            >
              🌿 Classic & Margherita
            </button>
            <button
              onClick={() => setActiveFilter('spicy')}
              className={activeFilter === 'spicy' ? 'btn-primary' : 'btn-outline'}
              style={{
                minHeight: '40px',
                padding: '0.45rem 1.15rem',
                fontSize: '0.88rem',
                borderRadius: 'var(--radius-full)'
              }}
            >
              🌶️ Fiery & Bold
            </button>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div
            style={{
              padding: '5rem 1rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1rem'
            }}
          >
            <div style={{ fontSize: '3.5rem', animation: 'spin 1.5s infinite linear' }}>🍕</div>
            <h3 style={{ fontWeight: 700, color: 'var(--text-main)' }}>Heating Wood-Fired Hearth...</h3>
            <p style={{ color: 'var(--text-muted)' }}>Retrieving freshest menu batches & stock availability.</p>
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div
            className="card"
            style={{
              textAlign: 'center',
              padding: '3rem 2rem',
              borderColor: '#fee2e2',
              background: '#fffaf0'
            }}
          >
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>⚠️</div>
            <h3 style={{ color: '#dc2626', marginBottom: '0.5rem' }}>Failed to Load Menu</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>{error}</p>
            <button onClick={fetchPizzas} className="btn-secondary">
              Retry Connection
            </button>
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && filteredPizzas.length === 0 && (
          <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
            <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>🔍</div>
            <h3 style={{ marginBottom: '0.5rem' }}>No Pizzas Match "{searchQuery || activeFilter}"</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
              Try clearing your search or launch the pizza builder to craft your personal custom recipe!
            </p>
            <button
              onClick={() => {
                setActiveFilter('all');
                setSearchQuery('');
              }}
              className="btn-outline"
            >
              Reset Filters
            </button>
          </div>
        )}

        {/* Pizza Varieties Grid */}
        {!loading && !error && filteredPizzas.length > 0 && (
          <div className="grid-3" style={{ marginBottom: '4rem' }}>
            {filteredPizzas.map((pizza) => (
              <PizzaCard key={pizza._id} pizza={pizza} />
            ))}
          </div>
        )}

        {/* Figma Feature Guarantee Cards */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '1.5rem',
            marginBottom: '4rem'
          }}
          className="grid-3"
        >
          <div
            className="card"
            style={{
              padding: '2rem 1.75rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem'
            }}
          >
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'var(--accent-tomato-light)',
                color: 'var(--accent-tomato)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem'
              }}
            >
              🪵
            </div>
            <h4 style={{ fontSize: '1.2rem', fontWeight: 800 }}>Artisanal 450°C Bake</h4>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>
              Crafted with authentic Neapolitan high-temperature stone ovens for airy, blistered crusts and molten,
              bubbling toppings.
            </p>
          </div>

          <div
            className="card"
            style={{
              padding: '2rem 1.75rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem'
            }}
          >
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: 'var(--accent-basil-light)',
                color: 'var(--accent-basil)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem'
              }}
            >
              📡
            </div>
            <h4 style={{ fontSize: '1.2rem', fontWeight: 800 }}>Live WebSocket Tracker</h4>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>
              Watch your pizza transition from oven prep to delivery vehicle in real-time with zero-refresh bi-directional
              socket alerts.
            </p>
          </div>

          <div
            className="card"
            style={{
              padding: '2rem 1.75rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem'
            }}
          >
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '12px',
                background: '#FEF3C7',
                color: '#B45309',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem'
              }}
            >
              🛡️
            </div>
            <h4 style={{ fontSize: '1.2rem', fontWeight: 800 }}>Zero Client Trust Security</h4>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>
              Prices recalculated server-side, HMAC cryptographic Razorpay signature checks, and atomic stock reservation
              guarantee fair ordering.
            </p>
          </div>
        </div>

        {/* Custom Studio Banner */}
        <div
          style={{
            background: 'linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%)',
            border: '2px dashed #F59E0B',
            borderRadius: 'var(--radius-xl)',
            padding: '3rem',
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '2rem',
            boxShadow: 'var(--shadow-sm)'
          }}
        >
          <div>
            <span
              style={{
                background: '#FDE68A',
                color: '#92400E',
                padding: '0.3rem 0.85rem',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.78rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.04em',
                display: 'inline-block',
                marginBottom: '0.75rem'
              }}
            >
              Artisan Custom Studio
            </span>
            <h3 style={{ fontSize: '1.85rem', color: '#78350F', marginBottom: '0.5rem', fontWeight: 900 }}>
              Craving A Bespoke Masterpiece?
            </h3>
            <p style={{ color: '#92400E', maxWidth: '600px', fontSize: '1rem', lineHeight: 1.6 }}>
              Customize your dough crust, signature sauce glaze, artisanal cheeses, and fresh farm vegetables with our
              live 3D SVG visualizer and instant price calculation.
            </p>
          </div>

          <Link
            to="/builder"
            className="btn-primary"
            style={{
              padding: '1rem 2.4rem',
              fontSize: '1.05rem',
              backgroundColor: '#D97706',
              boxShadow: '0 8px 20px rgba(217, 119, 6, 0.35)'
            }}
          >
            Open Pizza Forge Studio →
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
