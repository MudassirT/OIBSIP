import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';

// Visual metadata and real image mapping based on pizza variety
const PIZZA_METADATA = {
  margherita: {
    image: '/images/pizzas/margherita.jpg',
    badge: 'Classic Heritage',
    rating: '4.9',
    prepTime: '12 min',
    dietary: '🌱 100% Veg'
  },
  farmhouse: {
    image: '/images/pizzas/farmhouse.jpg',
    badge: 'Farm Fresh Harvest',
    rating: '4.8',
    prepTime: '15 min',
    dietary: '🌱 100% Veg'
  },
  periperi: {
    image: '/images/pizzas/periperi.jpg',
    badge: 'Fiery Artisan',
    rating: '4.9',
    prepTime: '14 min',
    dietary: '🌶️ Hot & Spicy'
  },
  bbq: {
    image: '/images/pizzas/bbq.jpg',
    badge: 'Wood-Smoked BBQ',
    rating: '4.7',
    prepTime: '16 min',
    dietary: '🔥 Smoky Glaze'
  },
  'garlic-bliss': {
    image: '/images/pizzas/garlic-bliss.jpg',
    badge: 'Truffle & Confit',
    rating: '4.9',
    prepTime: '15 min',
    dietary: '🧀 Cheese Burst'
  },
  'pesto-garden': {
    image: '/images/pizzas/pesto-garden.jpg',
    badge: 'Ligurian Pesto',
    rating: '4.8',
    prepTime: '13 min',
    dietary: '🌿 Farm Pesto'
  }
};

const PizzaCard = ({ pizza }) => {
  const { addPredefinedPizza } = useCart();
  const { showToast } = useToast();
  const [justAdded, setJustAdded] = useState(false);
  const [imgError, setImgError] = useState(false);

  const meta = PIZZA_METADATA[pizza.image] || {
    image: `/images/pizzas/${pizza.image || 'margherita'}.jpg`,
    badge: 'Chef Specialty',
    rating: '4.8',
    prepTime: '14 min',
    dietary: 'Artisanal'
  };

  const handleAddToCart = () => {
    if (!pizza.isAvailable) {
      showToast(`Sorry, ${pizza.name} is currently out of stock`, 'warning');
      return;
    }
    addPredefinedPizza(pizza);
    showToast(`Added "${pizza.name}" to your order!`, 'success');
    setJustAdded(true);
    setTimeout(() => setJustAdded(false), 1200);
  };

  return (
    <div
      className="card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '0',
        overflow: 'hidden',
        border: '1px solid var(--border-light)',
        boxShadow: 'var(--shadow-card)',
        transition: 'all var(--transition-normal)',
        position: 'relative',
        borderRadius: 'var(--radius-xl)'
      }}
    >
      {/* Real Pizza Photography Banner */}
      <div
        style={{
          width: '100%',
          height: '210px',
          position: 'relative',
          overflow: 'hidden',
          backgroundColor: '#1E1F24'
        }}
      >
        {!imgError ? (
          <img
            src={meta.image}
            alt={pizza.name}
            onError={() => setImgError(true)}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center',
              transition: 'transform 0.4s ease'
            }}
            loading="lazy"
            className="pizza-photo-hover"
          />
        ) : (
          <div
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '4.5rem',
              background: 'linear-gradient(135deg, #1A1C23 0%, #2A2E39 100%)'
            }}
          >
            🍕
          </div>
        )}

        {/* Gradient Shadow Vignette Overlay */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(180deg, rgba(0,0,0,0.45) 0%, rgba(0,0,0,0) 40%, rgba(0,0,0,0.65) 100%)',
            pointerEvents: 'none'
          }}
        />

        {/* Top Badges Row */}
        <div
          style={{
            position: 'absolute',
            top: '0.85rem',
            left: '0.85rem',
            right: '0.85rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            zIndex: 2
          }}
        >
          <span
            style={{
              fontSize: '0.72rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.04em',
              background: 'rgba(0, 0, 0, 0.65)',
              color: '#FFFFFF',
              padding: '0.25rem 0.65rem',
              borderRadius: 'var(--radius-full)',
              backdropFilter: 'blur(8px)',
              border: '1px solid rgba(255, 255, 255, 0.2)'
            }}
          >
            {meta.badge}
          </span>

          <div style={{ display: 'flex', gap: '0.4rem' }}>
            <span
              style={{
                fontSize: '0.72rem',
                fontWeight: 700,
                background: 'rgba(0, 0, 0, 0.65)',
                color: '#FBBF24',
                padding: '0.25rem 0.55rem',
                borderRadius: 'var(--radius-full)',
                backdropFilter: 'blur(8px)',
                display: 'flex',
                alignItems: 'center',
                gap: '0.2rem',
                border: '1px solid rgba(255, 255, 255, 0.2)'
              }}
            >
              ★ {meta.rating}
            </span>
            {pizza.isAvailable ? (
              <span
                style={{
                  fontSize: '0.72rem',
                  fontWeight: 700,
                  background: 'rgba(16, 185, 129, 0.9)',
                  color: '#FFFFFF',
                  padding: '0.25rem 0.55rem',
                  borderRadius: 'var(--radius-full)',
                  boxShadow: '0 2px 6px rgba(16, 185, 129, 0.4)'
                }}
              >
                ● In Stock
              </span>
            ) : (
              <span
                style={{
                  fontSize: '0.72rem',
                  fontWeight: 700,
                  background: 'rgba(239, 68, 68, 0.9)',
                  color: '#FFFFFF',
                  padding: '0.25rem 0.55rem',
                  borderRadius: 'var(--radius-full)'
                }}
              >
                Out of Stock
              </span>
            )}
          </div>
        </div>

        {/* Bottom Metadata in Image Vignette */}
        <div
          style={{
            position: 'absolute',
            bottom: '0.75rem',
            left: '0.85rem',
            right: '0.85rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            zIndex: 2,
            fontSize: '0.75rem',
            color: '#F1F5F9',
            fontWeight: 600
          }}
        >
          <span style={{ textShadow: '0 1px 3px rgba(0,0,0,0.8)' }}>
            ⏱️ {meta.prepTime} Bake
          </span>
          <span
            style={{
              background: 'rgba(255, 255, 255, 0.2)',
              backdropFilter: 'blur(6px)',
              padding: '0.15rem 0.5rem',
              borderRadius: 'var(--radius-full)',
              color: '#FFFFFF',
              fontSize: '0.7rem'
            }}
          >
            {meta.dietary}
          </span>
        </div>
      </div>

      {/* Content Area */}
      <div style={{ padding: '1.4rem', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <h3
          style={{
            marginBottom: '0.35rem',
            fontSize: '1.25rem',
            fontWeight: 800,
            letterSpacing: '-0.02em',
            color: 'var(--text-main)'
          }}
        >
          {pizza.name}
        </h3>
        <p style={{ fontSize: '0.86rem', color: 'var(--text-muted)', marginBottom: '1.1rem', flex: 1, lineHeight: 1.55 }}>
          {pizza.description}
        </p>

        {/* Ingredient Breakdown Chips */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '1.25rem' }}>
          {pizza.recipe?.base && (
            <span
              style={{
                fontSize: '0.72rem',
                background: 'var(--bg-elevated)',
                padding: '0.25rem 0.55rem',
                borderRadius: 'var(--radius-sm)',
                color: '#475569',
                fontWeight: 600
              }}
            >
              🌾 {pizza.recipe.base.name}
            </span>
          )}
          {pizza.recipe?.cheese && (
            <span
              style={{
                fontSize: '0.72rem',
                background: '#FEF3C7',
                padding: '0.25rem 0.55rem',
                borderRadius: 'var(--radius-sm)',
                color: '#92400E',
                fontWeight: 600
              }}
            >
              🧀 {pizza.recipe.cheese.name}
            </span>
          )}
          {pizza.recipe?.sauce && (
            <span
              style={{
                fontSize: '0.72rem',
                background: '#FEE2E2',
                padding: '0.25rem 0.55rem',
                borderRadius: 'var(--radius-sm)',
                color: '#991B1B',
                fontWeight: 600
              }}
            >
              🥫 {pizza.recipe.sauce.name}
            </span>
          )}
        </div>

        {/* Price & Action Row */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderTop: '1px solid var(--border-light)',
            paddingTop: '0.95rem',
            marginTop: 'auto'
          }}
        >
          <div>
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>
              Price
            </span>
            <span
              style={{
                fontSize: '1.5rem',
                fontWeight: 900,
                color: 'var(--text-main)',
                fontFamily: 'var(--font-heading)'
              }}
            >
              ₹{pizza.basePrice}
            </span>
          </div>

          <button
            onClick={handleAddToCart}
            disabled={!pizza.isAvailable}
            className={pizza.isAvailable ? (justAdded ? 'btn-secondary' : 'btn-primary') : 'btn-outline'}
            style={{
              padding: '0.65rem 1.45rem',
              fontSize: '0.92rem',
              fontWeight: 700
            }}
          >
            {justAdded ? '✓ Added!' : pizza.isAvailable ? '+ Add to Order' : 'Sold Out'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PizzaCard;
