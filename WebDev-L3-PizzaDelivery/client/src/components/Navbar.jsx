import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useSocket } from '../context/SocketContext';

const Navbar = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { totalItemsCount } = useCart();
  const { isConnected } = useSocket();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 100,
        boxShadow: '0 4px 20px -2px rgba(15, 23, 42, 0.05)'
      }}
    >
      {/* Enterprise Top Command Strip for Admin */}
      {isAdmin && (
        <div
          style={{
            background: 'linear-gradient(90deg, #0F172A 0%, #1E293B 100%)',
            color: '#E2E8F0',
            padding: '0.4rem 1.75rem',
            fontSize: '0.74rem',
            fontWeight: 700,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
            letterSpacing: '0.02em'
          }}
          className="admin-command-strip"
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span
              style={{
                background: 'rgba(239, 68, 68, 0.25)',
                color: '#FCA5A5',
                padding: '0.15rem 0.55rem',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.66rem',
                fontWeight: 800,
                border: '1px solid rgba(239, 68, 68, 0.45)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.3rem'
              }}
            >
              <span>🛡️</span>
              <span>ADMIN COMMAND CONSOLE</span>
            </span>
            <span style={{ color: '#94A3B8' }} className="hide-on-tablet">
              Operations Mode • Authoritative Kitchen & Warehouse Access
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#CBD5E1' }}>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#10B981' }} className="pulse" />
              <span>450°C Hearth Telemetry: <strong style={{ color: '#86EFAC' }}>Active</strong></span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#CBD5E1' }} className="hide-on-tablet">
              <span>Cron Scanner: <strong style={{ color: '#86EFAC' }}>24/7 Monitored</strong></span>
            </div>
          </div>
        </div>
      )}

      {/* Main Navigation Bar */}
      <div
        style={{
          background: 'rgba(255, 255, 255, 0.92)',
          backdropFilter: 'blur(16px)',
          WebkitBackdropFilter: 'blur(16px)',
          borderBottom: '1px solid rgba(226, 232, 240, 0.85)'
        }}
      >
        <div
          className="container"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            minHeight: '70px',
            gap: '1rem'
          }}
        >
          {/* Brand Logo & Workplace Tag */}
          <Link
            to={isAdmin ? '/admin/orders' : '/'}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              textDecoration: 'none',
              flexShrink: 0
            }}
          >
            <span style={{ fontSize: '2.1rem', lineHeight: 1, filter: 'drop-shadow(0 2px 4px rgba(226, 55, 68, 0.25))' }}>
              🍕
            </span>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                <span
                  style={{
                    fontFamily: 'var(--font-heading)',
                    fontSize: '1.45rem',
                    fontWeight: 900,
                    color: 'var(--accent-tomato)',
                    letterSpacing: '-0.025em'
                  }}
                >
                  Pizza<span style={{ color: 'var(--text-main)' }}>Forge</span>
                </span>
                {isAdmin && (
                  <span
                    style={{
                      background: '#0F172A',
                      color: '#FCD34D',
                      fontSize: '0.62rem',
                      fontWeight: 800,
                      padding: '0.12rem 0.45rem',
                      borderRadius: 'var(--radius-sm)',
                      letterSpacing: '0.05em'
                    }}
                  >
                    STAFF
                  </span>
                )}
              </div>
              <div
                style={{
                  fontSize: '0.64rem',
                  color: 'var(--text-muted)',
                  fontWeight: 700,
                  letterSpacing: '0.05em',
                  textTransform: 'uppercase'
                }}
              >
                {isAdmin ? 'Kitchen Dispatch & Inventory Hub' : 'Artisanal Delivery & Live Hearth'}
              </div>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '1.25rem'
            }}
            className="desktop-nav"
          >
            {isAdmin ? (
              /* High-End Segmented Control for Operations */
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                {/* Operations Segmented Pill */}
                <div
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    background: '#F1F5F9',
                    padding: '0.3rem',
                    borderRadius: 'var(--radius-full)',
                    border: '1.5px solid #E2E8F0',
                    boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.04)'
                  }}
                >
                  <Link
                    to="/admin/orders"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.4rem',
                      padding: '0.4rem 1.05rem',
                      borderRadius: 'var(--radius-full)',
                      fontSize: '0.84rem',
                      fontWeight: isActive('/admin/orders') ? 800 : 600,
                      color: isActive('/admin/orders') ? '#FFFFFF' : '#475569',
                      background: isActive('/admin/orders') ? '#0F172A' : 'transparent',
                      boxShadow: isActive('/admin/orders') ? '0 2px 8px rgba(15, 23, 42, 0.25)' : 'none',
                      transition: 'all 0.2s ease',
                      whiteSpace: 'nowrap'
                    }}
                  >
                    <span>👨‍🍳</span>
                    <span>Kitchen Dispatch</span>
                  </Link>

                  <Link
                    to="/admin/inventory"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.4rem',
                      padding: '0.4rem 1.05rem',
                      borderRadius: 'var(--radius-full)',
                      fontSize: '0.84rem',
                      fontWeight: isActive('/admin/inventory') ? 800 : 600,
                      color: isActive('/admin/inventory') ? '#FFFFFF' : '#475569',
                      background: isActive('/admin/inventory') ? '#0F172A' : 'transparent',
                      boxShadow: isActive('/admin/inventory') ? '0 2px 8px rgba(15, 23, 42, 0.25)' : 'none',
                      transition: 'all 0.2s ease',
                      whiteSpace: 'nowrap'
                    }}
                  >
                    <span>📦</span>
                    <span>Warehouse Inventory</span>
                  </Link>
                </div>

                {/* Storefront Quick Previews */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.85rem',
                    borderLeft: '1.5px solid var(--border-light)',
                    paddingLeft: '1rem'
                  }}
                >
                  <Link
                    to="/"
                    style={{
                      fontSize: '0.84rem',
                      fontWeight: isActive('/') ? 800 : 600,
                      color: isActive('/') ? 'var(--accent-tomato)' : '#64748B',
                      transition: 'color 0.2s ease'
                    }}
                    title="Preview Customer Storefront"
                  >
                    🏪 Storefront Menu
                  </Link>
                  <Link
                    to="/builder"
                    style={{
                      fontSize: '0.84rem',
                      fontWeight: isActive('/builder') ? 800 : 600,
                      color: isActive('/builder') ? 'var(--accent-tomato)' : '#64748B',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.3rem',
                      transition: 'color 0.2s ease'
                    }}
                    title="Launch Atelier Studio"
                  >
                    <span>🛠️ Studio</span>
                    <span
                      style={{
                        fontSize: '0.62rem',
                        background: 'var(--accent-tomato-light)',
                        color: 'var(--accent-tomato)',
                        padding: '0.1rem 0.4rem',
                        borderRadius: 'var(--radius-full)',
                        fontWeight: 800
                      }}
                    >
                      LIVE
                    </span>
                  </Link>
                </div>
              </div>
            ) : (
              /* Standard Customer Navigation Links */
              <>
                <Link
                  to="/"
                  style={{
                    fontWeight: isActive('/') ? 800 : 600,
                    color: isActive('/') ? 'var(--accent-tomato)' : 'var(--text-main)',
                    fontSize: '0.94rem',
                    position: 'relative'
                  }}
                >
                  Menu
                </Link>

                <Link
                  to="/builder"
                  style={{
                    fontWeight: isActive('/builder') ? 800 : 600,
                    color: isActive('/builder') ? 'var(--accent-tomato)' : 'var(--text-main)',
                    fontSize: '0.94rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.35rem'
                  }}
                >
                  <span>Custom Studio</span>
                  <span
                    style={{
                      fontSize: '0.64rem',
                      background: 'var(--accent-tomato-light)',
                      color: 'var(--accent-tomato)',
                      padding: '0.12rem 0.45rem',
                      borderRadius: 'var(--radius-full)',
                      fontWeight: 800
                    }}
                  >
                    LIVE
                  </span>
                </Link>

                {isAuthenticated && (
                  <Link
                    to="/orders"
                    style={{
                      fontWeight: isActive('/orders') ? 800 : 600,
                      color: isActive('/orders') ? 'var(--accent-tomato)' : 'var(--text-main)',
                      fontSize: '0.94rem'
                    }}
                  >
                    My Orders
                  </Link>
                )}
              </>
            )}
          </nav>

          {/* Right Action Suite (Strict 38px Harmonized Height) */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexShrink: 0 }}>
            {/* Customer Cart Icon (Only relevant or subtle in Admin) */}
            {(!isAdmin || totalItemsCount > 0) && (
              <Link
                to="/summary"
                style={{
                  position: 'relative',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '38px',
                  height: '38px',
                  borderRadius: 'var(--radius-md)',
                  background: totalItemsCount > 0 ? 'var(--accent-tomato-light)' : 'var(--bg-elevated)',
                  color: 'var(--text-main)',
                  fontSize: '1.2rem',
                  border: totalItemsCount > 0 ? '1px solid rgba(226, 55, 68, 0.25)' : '1px solid var(--border-light)',
                  transition: 'all var(--transition-fast)'
                }}
                aria-label="View Order Summary"
                title="Cart / Order Summary"
              >
                🛒
                {totalItemsCount > 0 && (
                  <span
                    style={{
                      position: 'absolute',
                      top: '-4px',
                      right: '-4px',
                      background: 'var(--accent-tomato)',
                      color: '#FFFFFF',
                      fontSize: '0.7rem',
                      fontWeight: 800,
                      width: '20px',
                      height: '20px',
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 2px 6px rgba(226, 55, 68, 0.4)'
                    }}
                  >
                    {totalItemsCount}
                  </span>
                )}
              </Link>
            )}

            {/* Live Kitchen Stream Indicator */}
            <div
              title={isConnected ? 'Live WebSocket Connected to Kitchen Hearth' : 'Reconnecting WebSocket...'}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.35rem',
                height: '38px',
                fontSize: '0.74rem',
                color: isConnected ? 'var(--accent-basil)' : 'var(--status-pending)',
                fontWeight: 700,
                background: isConnected ? 'var(--accent-basil-light)' : '#FEF3C7',
                padding: '0 0.75rem',
                borderRadius: 'var(--radius-full)',
                border: isConnected ? '1px solid rgba(16, 185, 129, 0.25)' : '1px solid rgba(245, 158, 11, 0.25)'
              }}
              className="hide-on-mobile"
            >
              <span
                style={{
                  width: '7px',
                  height: '7px',
                  borderRadius: '50%',
                  backgroundColor: isConnected ? '#10B981' : '#F59E0B'
                }}
                className={isConnected ? 'pulse' : ''}
              />
              <span>{isConnected ? 'Socket Live' : 'Polling 5s'}</span>
            </div>

            {/* User Session Profile & Controls */}
            {isAuthenticated ? (
              isAdmin ? (
                /* Ultra-Professional Admin Identity Suite */
                <div
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    height: '38px',
                    gap: '0.55rem',
                    padding: '0 0.35rem 0 0.75rem',
                    background: '#0F172A',
                    borderRadius: 'var(--radius-full)',
                    boxShadow: '0 2px 8px rgba(15, 23, 42, 0.15)',
                    border: '1px solid rgba(255, 255, 255, 0.12)'
                  }}
                  className="hide-on-mobile"
                >
                  <div
                    style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: '50%',
                      background: 'linear-gradient(135deg, #EF4444 0%, #F59E0B 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '0.75rem',
                      color: '#FFFFFF'
                    }}
                  >
                    🛡️
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', lineHeight: 1 }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: 800, color: '#F8FAFC', whiteSpace: 'nowrap' }}>
                      {user.name}
                    </span>
                    <span
                      style={{
                        fontSize: '0.6rem',
                        fontWeight: 800,
                        background: 'rgba(252, 211, 77, 0.2)',
                        color: '#FCD34D',
                        padding: '0.1rem 0.35rem',
                        borderRadius: '4px',
                        border: '1px solid rgba(252, 211, 77, 0.3)'
                      }}
                    >
                      ADMIN
                    </span>
                  </div>
                  <button
                    onClick={handleLogout}
                    style={{
                      background: 'rgba(255, 255, 255, 0.12)',
                      border: 'none',
                      color: '#F1F5F9',
                      padding: '0.3rem 0.65rem',
                      borderRadius: 'var(--radius-full)',
                      fontSize: '0.72rem',
                      fontWeight: 700,
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                      whiteSpace: 'nowrap'
                    }}
                    title="Sign out of Admin Session"
                    className="admin-logout-btn"
                  >
                    Logout ➔
                  </button>
                </div>
              ) : (
                /* Customer User Badge & Logout */
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }} className="hide-on-mobile">
                  <span
                    style={{
                      height: '38px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      fontSize: '0.82rem',
                      fontWeight: 700,
                      color: 'var(--text-main)',
                      padding: '0 0.85rem',
                      background: 'var(--bg-elevated)',
                      borderRadius: 'var(--radius-full)',
                      border: '1px solid var(--border-light)'
                    }}
                  >
                    👤 {user.name}
                  </span>
                  <button
                    onClick={handleLogout}
                    className="btn-outline"
                    style={{
                      height: '38px',
                      padding: '0 0.95rem',
                      fontSize: '0.82rem',
                      fontWeight: 600,
                      borderRadius: 'var(--radius-full)'
                    }}
                  >
                    Logout
                  </button>
                </div>
              )
            ) : (
              /* Public Sign In & Register */
              <div className="hide-on-mobile nav-auth-group">
                <Link
                  to="/login"
                  className="nav-auth-signin"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="nav-auth-register"
                >
                  Register
                </Link>
              </div>
            )}

            {/* Mobile hamburger toggle */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              style={{
                display: 'none',
                alignItems: 'center',
                justifyContent: 'center',
                height: '38px',
                width: '38px',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-light)',
                background: 'transparent',
                fontSize: '1.25rem',
                cursor: 'pointer'
              }}
              className="mobile-toggle"
              aria-label="Toggle navigation menu"
            >
              ☰
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Pane */}
      {mobileMenuOpen && (
        <div
          style={{
            borderTop: '1px solid var(--border-light)',
            background: '#FFFFFF',
            padding: '1.25rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem',
            boxShadow: 'var(--shadow-md)'
          }}
          className="mobile-menu-pane"
        >
          {isAdmin ? (
            <>
              <div style={{ fontSize: '0.72rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--accent-tomato)' }}>
                🛡️ Operational Command Center
              </div>
              <Link
                to="/admin/orders"
                onClick={() => setMobileMenuOpen(false)}
                style={{
                  fontWeight: 700,
                  padding: '0.5rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  background: isActive('/admin/orders') ? 'var(--accent-tomato-light)' : 'transparent',
                  color: isActive('/admin/orders') ? 'var(--accent-tomato)' : 'var(--text-main)'
                }}
              >
                👨‍🍳 Kitchen Dispatch Queue
              </Link>
              <Link
                to="/admin/inventory"
                onClick={() => setMobileMenuOpen(false)}
                style={{
                  fontWeight: 700,
                  padding: '0.5rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  background: isActive('/admin/inventory') ? 'var(--accent-tomato-light)' : 'transparent',
                  color: isActive('/admin/inventory') ? 'var(--accent-tomato)' : 'var(--text-main)'
                }}
              >
                📦 Warehouse Inventory & Stock
              </Link>
              <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '0.75rem', fontSize: '0.72rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                Storefront Previews
              </div>
              <Link to="/" onClick={() => setMobileMenuOpen(false)} style={{ fontWeight: 600 }}>
                🏪 Customer Menu Catalogue
              </Link>
              <Link to="/builder" onClick={() => setMobileMenuOpen(false)} style={{ fontWeight: 600 }}>
                🛠️ Custom Pizza Atelier Studio
              </Link>
            </>
          ) : (
            <>
              <Link to="/" onClick={() => setMobileMenuOpen(false)} style={{ fontWeight: 700 }}>
                🍕 Menu Catalogue
              </Link>
              <Link to="/builder" onClick={() => setMobileMenuOpen(false)} style={{ fontWeight: 700 }}>
                🛠️ Custom Pizza Studio
              </Link>
              {isAuthenticated && (
                <Link to="/orders" onClick={() => setMobileMenuOpen(false)} style={{ fontWeight: 700 }}>
                  📦 Live Kitchen Orders
                </Link>
              )}
            </>
          )}

          {isAuthenticated ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', borderTop: '1px solid var(--border-light)', paddingTop: '0.85rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>
                Signed in as: {user.name} {isAdmin && '⭐ (Administrator)'}
              </div>
              <button
                onClick={() => {
                  handleLogout();
                  setMobileMenuOpen(false);
                }}
                className="btn-outline"
                style={{ width: '100%', minHeight: '40px' }}
              >
                Sign Out
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '0.75rem', borderTop: '1px solid var(--border-light)', paddingTop: '0.85rem' }}>
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="nav-auth-signin"
                style={{ flex: 1, height: '42px', minHeight: '42px', textAlign: 'center' }}
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="nav-auth-register"
                style={{ flex: 1, height: '42px', minHeight: '42px', textAlign: 'center' }}
              >
                Register
              </Link>
            </div>
          )}
        </div>
      )}

      <style>{`
        @media (max-width: 1024px) {
          .hide-on-tablet { display: none !important; }
        }
        @media (max-width: 768px) {
          .desktop-nav { display: none !important; }
          .hide-on-mobile { display: none !important; }
          .mobile-toggle { display: inline-flex !important; }
        }
        .admin-logout-btn:hover {
          background: rgba(239, 68, 68, 0.45) !important;
        }
      `}</style>
    </header>
  );
};

export default Navbar;
