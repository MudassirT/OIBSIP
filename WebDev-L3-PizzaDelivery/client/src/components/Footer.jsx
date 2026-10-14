import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer
      style={{
        backgroundColor: 'var(--bg-dark)',
        color: '#9CA3AF',
        padding: '3rem 0 2rem',
        marginTop: 'auto',
        borderTop: '1px solid #282A30'
      }}
    >
      <div className="container">
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '2.5rem',
            marginBottom: '2.5rem'
          }}
        >
          {/* Col 1: Brand & Mission */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.75rem' }}>
              <span style={{ fontSize: '1.6rem' }}>🍕</span>
              <span
                style={{
                  fontFamily: 'var(--font-heading)',
                  fontSize: '1.35rem',
                  fontWeight: 800,
                  color: '#FFFFFF'
                }}
              >
                Pizza<span style={{ color: 'var(--accent-tomato)' }}>Forge</span>
              </span>
            </div>
            <p style={{ fontSize: '0.875rem', lineHeight: 1.6, color: '#9CA3AF' }}>
              Handcrafted artisanal pizzas prepared with slow-fermented doughs, San Marzano tomatoes, and organic herbs.
              Featuring real-time kitchen tracking & automated inventory management.
            </p>
          </div>

          {/* Col 2: Navigation */}
          <div>
            <h4 style={{ color: '#FFFFFF', marginBottom: '1rem', fontSize: '1rem' }}>Explore</h4>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.875rem' }}>
              <li><Link to="/" style={{ color: '#D1D5DB' }}>Menu Varieties</Link></li>
              <li><Link to="/builder" style={{ color: '#D1D5DB' }}>Custom Pizza Studio</Link></li>
              <li><Link to="/orders" style={{ color: '#D1D5DB' }}>Live Order Tracker</Link></li>
              <li><Link to="/admin/login" style={{ color: '#D1D5DB' }}>Admin Portal</Link></li>
            </ul>
          </div>

          {/* Col 3: Internship & Tech Details */}
          <div>
            <h4 style={{ color: '#FFFFFF', marginBottom: '1rem', fontSize: '1rem' }}>Internship Project</h4>
            <p style={{ fontSize: '0.875rem', lineHeight: 1.6, color: '#9CA3AF' }}>
              <strong>Oasis Infobyte (OIBSIP)</strong><br />
              Track: Web Development<br />
              Task: Level 3, Task 1 (Pizza Delivery Platform)<br />
              Developer: <strong>Mudassir Ahmed</strong>
            </p>
          </div>
        </div>

        <div
          style={{
            borderTop: '1px solid #282A30',
            paddingTop: '1.5rem',
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
            fontSize: '0.8rem',
            color: '#6B7280'
          }}
        >
          <div>© {new Date().getFullYear()} PizzaForge Platform. All rights reserved.</div>
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <span>Node.js</span>
            <span>•</span>
            <span>Express</span>
            <span>•</span>
            <span>React</span>
            <span>•</span>
            <span>Socket.IO</span>
            <span>•</span>
            <span>MongoDB</span>
            <span>•</span>
            <span>Razorpay</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
