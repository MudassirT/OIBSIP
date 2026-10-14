import React, { useState, useRef, useEffect } from 'react';
import { getIngredientVisual } from '../utils/ingredientVisuals';
import { kitchenAudio } from '../utils/audioSynth';

// Organic Topping Scatter Coordinates across concentric disc rings
const TOPPING_SCATTER = {
  'Black Olives': [
    { x: 42, y: 35, rot: 25 }, { x: 62, y: 38, rot: -40 }, { x: 30, y: 52, rot: 70 },
    { x: 50, y: 62, rot: -15 }, { x: 72, y: 54, rot: 50 }, { x: 38, y: 72, rot: -65 },
    { x: 64, y: 70, rot: 10 }, { x: 50, y: 45, rot: 85 }
  ],
  'Bell Peppers': [
    { x: 48, y: 28, rot: 45, color: '#DC2626' }, { x: 32, y: 44, rot: -70, color: '#16A34A' },
    { x: 68, y: 42, rot: 30, color: '#EA580C' }, { x: 40, y: 60, rot: -20, color: '#16A34A' },
    { x: 58, y: 62, rot: 85, color: '#DC2626' }, { x: 70, y: 68, rot: -55, color: '#EA580C' },
    { x: 32, y: 66, rot: 40, color: '#DC2626' }, { x: 52, y: 48, rot: -10, color: '#16A34A' }
  ],
  'Wild Mushrooms': [
    { x: 36, y: 38, rot: -30 }, { x: 58, y: 32, rot: 50 }, { x: 44, y: 50, rot: -75 },
    { x: 66, y: 48, rot: 15 }, { x: 35, y: 60, rot: 45 }, { x: 55, y: 68, rot: -35 },
    { x: 70, y: 62, rot: 80 }, { x: 48, y: 76, rot: -10 }
  ],
  'Spicy Jalapenos': [
    { x: 52, y: 36, rot: 15 }, { x: 36, y: 48, rot: -45 }, { x: 65, y: 40, rot: 60 },
    { x: 42, y: 56, rot: -80 }, { x: 60, y: 58, rot: 25 }, { x: 48, y: 72, rot: -30 },
    { x: 68, y: 72, rot: 75 }
  ],
  'Caramelized Onions': [
    { x: 44, y: 32, rot: 60 }, { x: 60, y: 44, rot: -50 }, { x: 35, y: 55, rot: 35 },
    { x: 52, y: 58, rot: -70 }, { x: 68, y: 58, rot: 15 }, { x: 42, y: 68, rot: -25 },
    { x: 60, y: 74, rot: 80 }
  ],
  'Fresh Basil Leaves': [
    { x: 50, y: 40, rot: -20 }, { x: 38, y: 46, rot: 55 }, { x: 62, y: 46, rot: -65 },
    { x: 45, y: 64, rot: 40 }, { x: 58, y: 64, rot: -35 }, { x: 52, y: 52, rot: 10 }
  ],
  'Golden Sweet Corn': [
    { x: 38, y: 34, rot: 10 }, { x: 56, y: 32, rot: 40 }, { x: 68, y: 38, rot: -20 },
    { x: 32, y: 50, rot: 60 }, { x: 46, y: 48, rot: -15 }, { x: 64, y: 52, rot: 30 },
    { x: 36, y: 66, rot: -45 }, { x: 54, y: 66, rot: 15 }, { x: 68, y: 64, rot: -30 },
    { x: 48, y: 76, rot: 50 }
  ],
  'Cherry Tomatoes': [
    { x: 46, y: 36, rot: -25 }, { x: 64, y: 40, rot: 45 }, { x: 35, y: 54, rot: -60 },
    { x: 58, y: 54, rot: 20 }, { x: 44, y: 68, rot: -40 }, { x: 66, y: 68, rot: 35 }
  ],
  'Golden Grilled Paneer': [
    { x: 40, y: 36, rot: 12 }, { x: 62, y: 34, rot: -18 }, { x: 34, y: 58, rot: 45 },
    { x: 54, y: 54, rot: -30 }, { x: 68, y: 50, rot: 22 }, { x: 46, y: 70, rot: -8 },
    { x: 62, y: 68, rot: 35 }
  ]
};

// Normalize any database veggie name into visual key
const normalizeVeggieKey = (name = '') => {
  const lower = name.toLowerCase();
  if (lower.includes('olive')) return 'Black Olives';
  if (lower.includes('pepper')) return 'Bell Peppers';
  if (lower.includes('mushroom')) return 'Wild Mushrooms';
  if (lower.includes('jalape') || lower.includes('chili')) return 'Spicy Jalapenos';
  if (lower.includes('onion')) return 'Caramelized Onions';
  if (lower.includes('basil')) return 'Fresh Basil Leaves';
  if (lower.includes('corn')) return 'Golden Sweet Corn';
  if (lower.includes('tomato')) return 'Cherry Tomatoes';
  if (lower.includes('paneer')) return 'Golden Grilled Paneer';
  return 'Bell Peppers';
};

// Render realistic SVG shape for each topping
const renderToppingShape = (rawVeggieName, item, idx) => {
  const toppingName = normalizeVeggieKey(rawVeggieName);

  if (toppingName === 'Black Olives') {
    return (
      <svg
        key={`olive_${idx}`}
        viewBox="0 0 32 32"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '26px',
          height: '26px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.55))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <ellipse cx="16" cy="16" rx="14" ry="12" fill="#1C1917" stroke="#292524" strokeWidth="2" />
        <ellipse cx="16" cy="16" rx="7" ry="6" fill="#A855F7" opacity="0.12" />
        <ellipse cx="16" cy="16" rx="6" ry="5" fill="#78350F" opacity="0.3" />
        <ellipse cx="16" cy="16" rx="4.5" ry="4" fill="#18181B" />
        <circle cx="12" cy="11" r="2" fill="#FFFFFF" opacity="0.45" />
      </svg>
    );
  }

  if (toppingName === 'Bell Peppers') {
    return (
      <svg
        key={`pepper_${idx}`}
        viewBox="0 0 40 24"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '32px',
          height: '20px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <path
          d="M 4,18 C 12,4 28,4 36,18 C 30,10 14,10 4,18 Z"
          fill={item.color || '#DC2626'}
          stroke="#450A0A"
          strokeWidth="0.8"
        />
        <path d="M 10,12 C 16,7 24,7 30,12" stroke="#FFFFFF" strokeWidth="1" opacity="0.45" fill="none" />
        <circle cx="20" cy="9" r="1.2" fill="#1C1917" opacity="0.8" />
      </svg>
    );
  }

  if (toppingName === 'Wild Mushrooms') {
    return (
      <svg
        key={`mush_${idx}`}
        viewBox="0 0 32 32"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '28px',
          height: '28px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <path d="M 6,18 C 6,8 26,8 26,18 Z" fill="#78350F" stroke="#451A03" strokeWidth="1" />
        <path d="M 13,18 L 13,26 L 19,26 L 19,18 Z" fill="#E7E5E4" stroke="#78716C" strokeWidth="0.8" />
        <ellipse cx="16" cy="18" rx="10" ry="2.5" fill="#451A03" opacity="0.65" />
        <circle cx="12" cy="13" r="1.5" fill="#FEF3C7" opacity="0.35" />
      </svg>
    );
  }

  if (toppingName === 'Spicy Jalapenos') {
    return (
      <svg
        key={`jalapeno_${idx}`}
        viewBox="0 0 30 30"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '24px',
          height: '24px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <circle cx="15" cy="15" r="12" fill="#15803D" stroke="#052E16" strokeWidth="1.5" />
        <circle cx="15" cy="15" r="7" fill="#166534" />
        <circle cx="13" cy="12" r="1.2" fill="#FEF08A" />
        <circle cx="17" cy="13" r="1.2" fill="#FEF08A" />
        <circle cx="15" cy="17" r="1.2" fill="#FEF08A" />
        <circle cx="10" cy="10" r="1.5" fill="#FFFFFF" opacity="0.35" />
      </svg>
    );
  }

  if (toppingName === 'Fresh Basil Leaves') {
    return (
      <svg
        key={`basil_${idx}`}
        viewBox="0 0 36 24"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '32px',
          height: '22px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.45))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <path d="M 2,12 C 10,2 26,2 34,12 C 26,22 10,22 2,12 Z" fill="#15803D" stroke="#052E16" strokeWidth="0.8" />
        <path d="M 2,12 Q 18,12 34,12" stroke="#86EFAC" strokeWidth="1" fill="none" opacity="0.7" />
        <circle cx="16" cy="8" r="1.5" fill="#FFFFFF" opacity="0.35" />
      </svg>
    );
  }

  if (toppingName === 'Golden Sweet Corn') {
    return (
      <svg
        key={`corn_${idx}`}
        viewBox="0 0 24 24"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '20px',
          height: '20px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(1px 2px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <ellipse cx="12" cy="12" rx="9" ry="7" fill="#FBBF24" stroke="#D97706" strokeWidth="1" />
        <ellipse cx="12" cy="12" rx="6" ry="4" fill="#FDE047" />
        <circle cx="10" cy="10" r="1.5" fill="#FFFFFF" opacity="0.6" />
      </svg>
    );
  }

  if (toppingName === 'Cherry Tomatoes') {
    return (
      <svg
        key={`tomato_${idx}`}
        viewBox="0 0 28 28"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '24px',
          height: '24px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 2px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <circle cx="14" cy="14" r="11" fill="#DC2626" stroke="#991B1B" strokeWidth="1.2" />
        <circle cx="14" cy="14" r="6" fill="#B91C1C" />
        <circle cx="12" cy="13" r="1.5" fill="#FEF08A" />
        <circle cx="16" cy="15" r="1.5" fill="#FEF08A" />
        <circle cx="10" cy="9" r="2" fill="#FFFFFF" opacity="0.5" />
      </svg>
    );
  }

  if (toppingName === 'Golden Grilled Paneer') {
    return (
      <svg
        key={`paneer_${idx}`}
        viewBox="0 0 28 28"
        style={{
          position: 'absolute',
          left: `${item.x}%`,
          top: `${item.y}%`,
          width: '24px',
          height: '24px',
          transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
          filter: 'drop-shadow(2px 3px 2px rgba(0,0,0,0.5))',
          pointerEvents: 'none',
          animation: 'toppingPopIn 0.35s ease-out'
        }}
      >
        <rect x="3" y="3" width="22" height="22" rx="3" fill="#FEF08A" stroke="#B45309" strokeWidth="1.2" />
        <line x1="6" y1="8" x2="22" y2="8" stroke="#92400E" strokeWidth="1.5" strokeLinecap="round" />
        <line x1="6" y1="14" x2="22" y2="14" stroke="#92400E" strokeWidth="1.5" strokeLinecap="round" />
        <line x1="6" y1="20" x2="22" y2="20" stroke="#92400E" strokeWidth="1.5" strokeLinecap="round" />
        <circle cx="8" cy="6" r="1" fill="#FFFFFF" opacity="0.5" />
      </svg>
    );
  }

  // Caramelized Onions
  return (
    <svg
      key={`onion_${idx}`}
      viewBox="0 0 36 20"
      style={{
        position: 'absolute',
        left: `${item.x}%`,
        top: `${item.y}%`,
        width: '28px',
        height: '18px',
        transform: `translate(-50%, -50%) rotate(${item.rot}deg)`,
        filter: 'drop-shadow(2px 2px 2px rgba(0,0,0,0.4))',
        pointerEvents: 'none',
        animation: 'toppingPopIn 0.35s ease-out'
      }}
    >
      <path d="M 2,16 C 10,2 26,2 34,16" stroke="#701A75" strokeWidth="3" fill="none" strokeLinecap="round" />
      <path d="M 6,14 C 12,6 24,6 30,14" stroke="#A21CAF" strokeWidth="1.5" fill="none" strokeLinecap="round" />
    </svg>
  );
};

// Crust-specific filter enhancements
const getCrustFilter = (crustName = '') => {
  const lower = crustName.toLowerCase();
  if (lower.includes('wheat')) return 'sepia(0.38) contrast(110%) brightness(0.86)';
  if (lower.includes('sourdough')) return 'contrast(125%) brightness(0.96)';
  if (lower.includes('burst')) return 'saturate(120%) brightness(1.04)';
  if (lower.includes('gluten') || lower.includes('cauliflower')) return 'contrast(95%) brightness(1.05)';
  if (lower.includes('pan') || lower.includes('thick')) return 'contrast(115%) brightness(0.96)';
  return 'contrast(104%) brightness(1.0)';
};

// Sauce-specific asset mapping
const getSauceImage = (sauceName = '') => {
  const lower = sauceName.toLowerCase();
  if (lower.includes('pesto')) return '/images/builder/pesto-layer.jpg';
  if (lower.includes('alfredo') || lower.includes('garlic')) return '/images/builder/alfredo-layer.jpg';
  if (lower.includes('bbq') || lower.includes('chipotle') || lower.includes('barbecue')) return '/images/builder/bbq-layer.jpg';
  if (lower.includes('peri')) return '/images/builder/periperi-layer.jpg';
  return '/images/builder/sauce-marinara.jpg';
};

// Cheese-specific filter styling
const getCheeseFilter = (cheeseName = '') => {
  const lower = cheeseName.toLowerCase();
  if (lower.includes('cheddar') || lower.includes('sharp')) return 'hue-rotate(15deg) saturate(145%) brightness(1.02)';
  if (lower.includes('gouda') || lower.includes('smoked') || lower.includes('provolone')) return 'contrast(115%) sepia(0.2) brightness(0.95)';
  if (lower.includes('vegan') || lower.includes('plant') || lower.includes('cashew')) return 'saturate(75%) brightness(1.08)';
  return 'contrast(100%) brightness(1.0)';
};

const PizzaCanvas = ({
  currentStep = 1,
  selectedBase,
  selectedSauce,
  selectedCheese,
  selectedVeggies = [],
  runningPrice = 0,
  pizzaName = 'My Custom Artisan Pizza',
  selectedSize = { name: 'Medium 12"', slices: 8, multiplier: 1 },
  onStepSelect
}) => {
  const [rotationAngle, setRotationAngle] = useState(0);
  const [sliceCount, setSliceCount] = useState(8);
  const [isZoomed, setIsZoomed] = useState(false);
  const [thermalMode, setThermalMode] = useState(false);
  const [isSizzling, setIsSizzling] = useState(false);
  const [viewMode, setViewMode] = useState('step'); // 'step' | 'solo' | 'full'
  const [tilt, setTilt] = useState({ x: 0, y: 0 });
  const [activeTab, setActiveTab] = useState('stage'); // 'stage' | 'radar'
  const [garnishes, setGarnishes] = useState({
    chiliFlakes: false,
    oregano: false,
    evooDrizzle: false
  });

  const canvasCardRef = useRef(null);

  // Automatically sync viewMode to 'step' when the user switches categories/steps
  useEffect(() => {
    setViewMode('step');
  }, [currentStep]);

  // 3D Parallax Mouse Tracking
  const handleMouseMove = (e) => {
    if (!canvasCardRef.current || isZoomed) return;
    const rect = canvasCardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left - rect.width / 2;
    const y = e.clientY - rect.top - rect.height / 2;
    setTilt({
      x: -(y / (rect.height / 2)) * 7,
      y: (x / (rect.width / 2)) * 7
    });
  };

  const handleMouseLeave = () => {
    setTilt({ x: 0, y: 0 });
  };

  // Toggle Woodfire Hearth Sizzle Audio
  const handleToggleSizzle = () => {
    const nextState = !isSizzling;
    const ok = kitchenAudio.toggleWoodfireSizzle(nextState);
    setIsSizzling(ok ? nextState : false);
  };

  // Category Information Specs
  const categorySpecs = [
    {
      step: 1,
      title: 'Artisan Crust',
      icon: '🌾',
      selectedItem: selectedBase?.name || 'Awaiting Selection',
      price: selectedBase?.price || 0
    },
    {
      step: 2,
      title: 'Signature Sauce',
      icon: '🥫',
      selectedItem: selectedSauce?.name || 'Awaiting Selection',
      price: selectedSauce?.price || 0
    },
    {
      step: 3,
      title: 'Molten Cheese',
      icon: '🧀',
      selectedItem: selectedCheese?.name || 'Awaiting Selection',
      price: selectedCheese?.price || 0
    },
    {
      step: 4,
      title: 'Farm Harvest',
      icon: '🥦',
      selectedItem: selectedVeggies.length > 0 ? `${selectedVeggies.length} Veggies Active` : 'No Veggies Yet (Optional)',
      price: selectedVeggies.reduce((a, v) => a + v.price, 0)
    }
  ];
  const activeCategory = categorySpecs[currentStep - 1] || categorySpecs[0];

  // Layer visibility logic:
  // - 'step': Progressive assembly up to currentStep
  // - 'solo': ONLY the layer being sculpted at currentStep
  // - 'full': All selected layers together
  const showCrust = viewMode !== 'solo' || currentStep === 1;
  const showSauce =
    !!selectedSauce &&
    (viewMode === 'full' ||
      (viewMode === 'step' && currentStep >= 2) ||
      (viewMode === 'solo' && currentStep === 2));

  const showCheese =
    !!selectedCheese &&
    (viewMode === 'full' ||
      (viewMode === 'step' && currentStep >= 3) ||
      (viewMode === 'solo' && currentStep === 3));

  const showToppings =
    selectedVeggies.length > 0 &&
    (viewMode === 'full' ||
      (viewMode === 'step' && currentStep >= 4) ||
      (viewMode === 'solo' && currentStep === 4));

  const isCheeseBurst = selectedBase?.name?.toLowerCase().includes('burst');

  // Live Sensory & Nutritional Estimation
  const baseCal = parseInt(getIngredientVisual(selectedBase?.name).calories) || 500;
  const sauceCal = parseInt(getIngredientVisual(selectedSauce?.name).calories) || 60;
  const cheeseCal = parseInt(getIngredientVisual(selectedCheese?.name).calories) || 220;
  const veggiesCal = selectedVeggies.reduce(
    (acc, v) => acc + (parseInt(getIngredientVisual(v.name).calories) || 25),
    0
  );
  const totalCalories = Math.round(
    (baseCal + (showSauce ? sauceCal : 0) + (showCheese ? cheeseCal : 0) + (showToppings ? veggiesCal : 0)) *
      (selectedSize?.multiplier || 1)
  );

  // Spice level meter
  const isSpicy =
    selectedSauce?.name?.toLowerCase().includes('peri') ||
    selectedVeggies.some((v) => v.name.toLowerCase().includes('jalape') || v.name.toLowerCase().includes('chili'));

  const toggleGarnish = (key) => {
    kitchenAudio.playIngredientClick();
    setGarnishes((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <div
      ref={canvasCardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      className="card"
      style={{
        background: 'linear-gradient(150deg, #101115 0%, #1A1D24 55%, #14151B 100%)',
        color: '#FFFFFF',
        borderRadius: 'var(--radius-xl)',
        padding: '1.6rem',
        border: '1px solid rgba(255, 255, 255, 0.12)',
        boxShadow: '0 25px 60px -15px rgba(0, 0, 0, 0.8), 0 0 0 1px rgba(255, 255, 255, 0.06)',
        position: 'relative',
        overflow: 'hidden',
        perspective: '1200px',
        transition: 'border-color 0.3s ease'
      }}
    >
      {/* Ambient Woodfire 450°C Glow Backdrop */}
      <div
        style={{
          position: 'absolute',
          top: '30%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: '380px',
          height: '380px',
          background: thermalMode
            ? 'radial-gradient(circle, rgba(6, 182, 212, 0.35) 0%, rgba(239, 68, 68, 0.25) 50%, transparent 75%)'
            : 'radial-gradient(circle, rgba(226, 55, 68, 0.32) 0%, rgba(245, 158, 11, 0.16) 45%, transparent 72%)',
          borderRadius: '50%',
          pointerEvents: 'none',
          transition: 'all 0.5s ease',
          zIndex: 0
        }}
      />

      {/* Top Header: Tab Selectors & Live Studio Status */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '0.85rem',
          position: 'relative',
          zIndex: 2,
          flexWrap: 'wrap',
          gap: '0.6rem'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <button
            type="button"
            onClick={() => setActiveTab('stage')}
            style={{
              background: activeTab === 'stage' ? 'rgba(226, 55, 68, 0.25)' : 'rgba(255, 255, 255, 0.06)',
              border: activeTab === 'stage' ? '1px solid #EF4444' : '1px solid rgba(255, 255, 255, 0.1)',
              color: activeTab === 'stage' ? '#FCA5A5' : '#CBD5E1',
              padding: '0.25rem 0.65rem',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.74rem',
              fontWeight: 800,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.35rem'
            }}
          >
            <span className="pulse" style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#EF4444' }} />
            450°C Live Visualizer
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('radar')}
            style={{
              background: activeTab === 'radar' ? 'rgba(16, 185, 129, 0.25)' : 'rgba(255, 255, 255, 0.06)',
              border: activeTab === 'radar' ? '1px solid #10B981' : '1px solid rgba(255, 255, 255, 0.1)',
              color: activeTab === 'radar' ? '#86EFAC' : '#CBD5E1',
              padding: '0.25rem 0.65rem',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.74rem',
              fontWeight: 800,
              cursor: 'pointer'
            }}
          >
            📊 Tasting Radar
          </button>
        </div>

        {/* Audio Ambience & Thermal Camera Toggles */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <button
            type="button"
            onClick={handleToggleSizzle}
            style={{
              background: isSizzling ? 'rgba(245, 158, 11, 0.25)' : 'rgba(255, 255, 255, 0.08)',
              border: isSizzling ? '1px solid #F59E0B' : '1px solid rgba(255, 255, 255, 0.15)',
              color: isSizzling ? '#FDE68A' : '#94A3B8',
              borderRadius: 'var(--radius-full)',
              padding: '0.22rem 0.55rem',
              fontSize: '0.72rem',
              fontWeight: 700,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.25rem'
            }}
            title="Toggle authentic 450°C woodfire oven crackle & sizzle"
          >
            <span>{isSizzling ? '🔊 Sizzle On' : '🔇 Audio Ambience'}</span>
          </button>

          <button
            type="button"
            onClick={() => setThermalMode(!thermalMode)}
            style={{
              background: thermalMode ? 'rgba(239, 68, 68, 0.3)' : 'rgba(255, 255, 255, 0.08)',
              border: thermalMode ? '1px solid #EF4444' : '1px solid rgba(255, 255, 255, 0.15)',
              color: thermalMode ? '#FCA5A5' : '#94A3B8',
              borderRadius: 'var(--radius-full)',
              padding: '0.22rem 0.55rem',
              fontSize: '0.72rem',
              fontWeight: 700,
              cursor: 'pointer'
            }}
            title="Toggle Thermal Infrared 450°C Camera View"
          >
            {thermalMode ? '🌡️ Thermal HUD' : '📷 Optical'}
          </button>
        </div>
      </div>

      {/* Prominent Active Category Status Notification Pill */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: 'rgba(226, 55, 68, 0.12)',
          border: '1.5px solid rgba(226, 55, 68, 0.4)',
          borderRadius: 'var(--radius-md)',
          padding: '0.6rem 0.95rem',
          marginBottom: '0.85rem',
          boxShadow: '0 4px 15px rgba(0, 0, 0, 0.35)',
          position: 'relative',
          zIndex: 2,
          transition: 'all 0.3s ease'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <div
            style={{
              width: '34px',
              height: '34px',
              borderRadius: '50%',
              background: 'var(--accent-tomato)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.1rem',
              boxShadow: '0 0 10px rgba(226, 55, 68, 0.6)',
              flexShrink: 0
            }}
          >
            {activeCategory.icon}
          </div>
          <div>
            <div
              style={{
                fontSize: '0.68rem',
                color: '#FCA5A5',
                fontWeight: 800,
                textTransform: 'uppercase',
                letterSpacing: '0.05em'
              }}
            >
              Active Category • Step {currentStep} of 4
            </div>
            <div style={{ fontSize: '0.92rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.01em' }}>
              {activeCategory.title}:{' '}
              <span style={{ color: '#FEF08A' }}>{activeCategory.selectedItem}</span>
            </div>
          </div>
        </div>
        {activeCategory.price > 0 && (
          <span
            style={{
              background: 'rgba(245, 158, 11, 0.25)',
              color: '#FDE68A',
              fontSize: '0.78rem',
              fontWeight: 800,
              padding: '0.2rem 0.65rem',
              borderRadius: 'var(--radius-full)',
              border: '1px solid rgba(245, 158, 11, 0.45)',
              whiteSpace: 'nowrap'
            }}
          >
            +₹{activeCategory.price}
          </span>
        )}
      </div>

      {/* Category Quick Selector Tabs */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '0.35rem',
          marginBottom: '0.85rem',
          position: 'relative',
          zIndex: 2
        }}
      >
        {categorySpecs.map((cat) => {
          const isAct = cat.step === currentStep;
          return (
            <button
              key={cat.step}
              type="button"
              onClick={() => {
                kitchenAudio.playIngredientClick();
                if (onStepSelect) onStepSelect(cat.step);
              }}
              style={{
                background: isAct ? 'var(--accent-tomato)' : 'rgba(255, 255, 255, 0.06)',
                border: isAct ? '1.5px solid #EF4444' : '1px solid rgba(255, 255, 255, 0.1)',
                color: isAct ? '#FFFFFF' : '#94A3B8',
                padding: '0.35rem 0.2rem',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.72rem',
                fontWeight: 700,
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.25rem',
                boxShadow: isAct ? '0 2px 8px rgba(226, 55, 68, 0.4)' : 'none'
              }}
              title={`Switch to ${cat.title}`}
            >
              <span>{cat.icon}</span>
              <span>{cat.title.split(' ')[1] || cat.title}</span>
            </button>
          );
        })}
      </div>

      {/* Assembly Mode Selector Switch */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          background: 'rgba(0, 0, 0, 0.35)',
          padding: '0.35rem 0.65rem',
          borderRadius: 'var(--radius-md)',
          marginBottom: '0.85rem',
          position: 'relative',
          zIndex: 2
        }}
      >
        <span style={{ fontSize: '0.72rem', color: '#CBD5E1', fontWeight: 600 }}>
          Canvas View:
        </span>
        <div style={{ display: 'flex', gap: '0.25rem' }}>
          <button
            type="button"
            onClick={() => setViewMode('step')}
            style={{
              background: viewMode === 'step' ? 'var(--accent-tomato)' : 'transparent',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: 'var(--radius-full)',
              padding: '0.2rem 0.55rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              cursor: 'pointer'
            }}
            title="Show progressive layers up to this step"
          >
            Step {currentStep} Focus
          </button>
          <button
            type="button"
            onClick={() => setViewMode('solo')}
            style={{
              background: viewMode === 'solo' ? 'var(--accent-crust)' : 'transparent',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: 'var(--radius-full)',
              padding: '0.2rem 0.55rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              cursor: 'pointer'
            }}
            title="Inspect only this category layer in isolation"
          >
            Solo Layer
          </button>
          <button
            type="button"
            onClick={() => setViewMode('full')}
            style={{
              background: viewMode === 'full' ? 'var(--accent-basil)' : 'transparent',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: 'var(--radius-full)',
              padding: '0.2rem 0.55rem',
              fontSize: '0.7rem',
              fontWeight: 700,
              cursor: 'pointer'
            }}
            title="Preview complete assembled pizza"
          >
            Full Baked
          </button>
        </div>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'stage' ? (
        <>
          {/* Steam Rising Animation */}
          <div
            style={{
              position: 'absolute',
              top: '110px',
              left: '50%',
              transform: 'translateX(-50%)',
              display: 'flex',
              gap: '34px',
              zIndex: 4,
              pointerEvents: 'none'
            }}
          >
            <span style={{ fontSize: '1.4rem', animation: 'steamRise 3.2s infinite ease-out' }}>♨️</span>
            <span style={{ fontSize: '1.4rem', animation: 'steamRise 3.8s infinite ease-out 0.9s' }}>♨️</span>
            <span style={{ fontSize: '1.4rem', animation: 'steamRise 3.4s infinite ease-out 1.7s' }}>♨️</span>
          </div>

          {/* 3D Parallax Pizza Disc Container */}
          <div
            style={{
              width: '100%',
              maxWidth: '340px',
              aspectRatio: '1 / 1',
              margin: '0.25rem auto 1.25rem',
              position: 'relative',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1,
              transformStyle: 'preserve-3d',
              transform: `rotateX(${tilt.x}deg) rotateY(${tilt.y}deg)`,
              transition: isZoomed ? 'transform 0.4s ease' : 'transform 0.1s ease-out'
            }}
          >
            <div
              style={{
                width: '100%',
                height: '100%',
                borderRadius: '50%',
                overflow: 'hidden',
                position: 'relative',
                boxShadow: thermalMode
                  ? '0 0 35px rgba(6, 182, 212, 0.6), 0 20px 45px -8px rgba(0, 0, 0, 0.85)'
                  : '0 24px 50px -10px rgba(0, 0, 0, 0.85), 0 0 0 8px rgba(30, 33, 42, 0.75)',
                border: thermalMode ? '2px solid #06B6D4' : '2px solid rgba(255, 255, 255, 0.18)',
                transition: 'transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1)',
                transform: `rotate(${rotationAngle}deg) scale(${isZoomed ? 1.18 : 1})`,
                filter: thermalMode
                  ? 'contrast(140%) saturate(190%) hue-rotate(180deg)'
                  : 'none',
                cursor: 'grab',
                backgroundColor: '#1E1F24'
              }}
              title="Move cursor over canvas to inspect with 3D tilt perspective"
            >
              {/* LAYER 1: Crust Base Image with dynamic filter for selected crust */}
              {showCrust && (
                <img
                  src="/images/builder/crust-base.jpg"
                  alt="Wood-Fired Pizza Crust Base"
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                    objectPosition: 'center',
                    display: 'block',
                    filter: getCrustFilter(selectedBase?.name),
                    transition: 'filter 0.35s ease'
                  }}
                />
              )}

              {/* Cheese Burst Stuffed Crust Outer Ring Highlight */}
              {isCheeseBurst && showCrust && (
                <div
                  style={{
                    position: 'absolute',
                    inset: '3%',
                    borderRadius: '50%',
                    border: '5px solid #F59E0B',
                    boxShadow: 'inset 0 0 14px #F59E0B, 0 0 10px #D97706',
                    opacity: 0.85,
                    pointerEvents: 'none'
                  }}
                />
              )}

              {/* LAYER 2: Sauce Spread Layer */}
              {showSauce && (
                <div
                  style={{
                    position: 'absolute',
                    inset: '8.5%',
                    borderRadius: '50%',
                    overflow: 'hidden',
                    pointerEvents: 'none',
                    transition: 'opacity 0.4s ease',
                    boxShadow: 'inset 0 0 18px rgba(0,0,0,0.5)'
                  }}
                >
                  <img
                    src={getSauceImage(selectedSauce.name)}
                    alt={selectedSauce.name}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover',
                      display: 'block',
                      transition: 'all 0.35s ease'
                    }}
                  />
                </div>
              )}

              {/* LAYER 3: Melted Cheese Layer */}
              {showCheese && (
                <div
                  style={{
                    position: 'absolute',
                    inset: '9.5%',
                    borderRadius: '50%',
                    overflow: 'hidden',
                    pointerEvents: 'none',
                    transition: 'opacity 0.4s ease',
                    boxShadow: 'inset 0 0 14px rgba(0,0,0,0.45)',
                    opacity: 0.93
                  }}
                >
                  <img
                    src="/images/builder/cheese-melted.jpg"
                    alt={selectedCheese.name}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover',
                      display: 'block',
                      filter: getCheeseFilter(selectedCheese.name),
                      transition: 'filter 0.35s ease'
                    }}
                  />
                </div>
              )}

              {/* LAYER 4: Dynamic Organic Topping Scatter Pieces (ONLY chosen veggies!) */}
              {showToppings &&
                selectedVeggies.map((veg) => {
                  const key = normalizeVeggieKey(veg.name);
                  const scatterPoints = TOPPING_SCATTER[key] || TOPPING_SCATTER['Bell Peppers'] || [];
                  return scatterPoints.map((item, idx) => renderToppingShape(veg.name, item, idx));
                })}

              {/* LAYER 5: Vignette & Charred Border Mask */}
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  background: 'radial-gradient(circle, rgba(0,0,0,0) 58%, rgba(0,0,0,0.45) 86%, rgba(0,0,0,0.7) 100%)',
                  pointerEvents: 'none'
                }}
              />

              {/* LAYER 6: Slicing Degree Guidelines */}
              {sliceCount > 0 && (
                <svg
                  viewBox="0 0 100 100"
                  style={{
                    position: 'absolute',
                    inset: 0,
                    width: '100%',
                    height: '100%',
                    pointerEvents: 'none',
                    opacity: 0.75
                  }}
                >
                  {sliceCount === 6 && (
                    <>
                      <line x1="50" y1="0" x2="50" y2="100" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                      <line x1="6.7" y1="25" x2="93.3" y2="75" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                      <line x1="6.7" y1="75" x2="93.3" y2="25" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                    </>
                  )}
                  {sliceCount === 8 && (
                    <>
                      <line x1="50" y1="0" x2="50" y2="100" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                      <line x1="0" y1="50" x2="100" y2="50" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                      <line x1="14.6" y1="14.6" x2="85.4" y2="85.4" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                      <line x1="14.6" y1="85.4" x2="85.4" y2="14.6" stroke="#FFFFFF" strokeWidth="0.8" strokeDasharray="3 2" />
                    </>
                  )}
                  <circle cx="50" cy="50" r="3" fill="#FFFFFF" opacity="0.9" />
                </svg>
              )}

              {/* LAYER 7: Live Garnish Overlays */}
              {garnishes.chiliFlakes && (
                <div
                  style={{
                    position: 'absolute',
                    inset: 0,
                    pointerEvents: 'none',
                    background:
                      'radial-gradient(circle at 45% 40%, rgba(239,68,68,0.85) 1.5px, transparent 2.5px), radial-gradient(circle at 60% 65%, rgba(220,38,38,0.9) 2px, transparent 3px), radial-gradient(circle at 35% 70%, rgba(239,68,68,0.85) 1.5px, transparent 2.5px), radial-gradient(circle at 65% 35%, rgba(220,38,38,0.9) 2px, transparent 3px)',
                    backgroundSize: '40px 40px, 50px 50px, 45px 45px, 55px 55px'
                  }}
                />
              )}

              {garnishes.oregano && (
                <div
                  style={{
                    position: 'absolute',
                    inset: 0,
                    pointerEvents: 'none',
                    background:
                      'radial-gradient(circle at 50% 50%, rgba(34,197,94,0.7) 1.2px, transparent 2.2px), radial-gradient(circle at 30% 40%, rgba(22,101,52,0.85) 1.5px, transparent 2.5px), radial-gradient(circle at 70% 60%, rgba(34,197,94,0.75) 1.2px, transparent 2.2px)',
                    backgroundSize: '30px 30px, 35px 35px, 40px 40px'
                  }}
                />
              )}

              {garnishes.evooDrizzle && (
                <div
                  style={{
                    position: 'absolute',
                    inset: 0,
                    pointerEvents: 'none',
                    boxShadow: 'inset 0 0 30px rgba(234, 179, 8, 0.5)',
                    mixBlendMode: 'color-dodge'
                  }}
                />
              )}

              {/* Thermal Infrared Overlay HUD */}
              {thermalMode && (
                <div
                  style={{
                    position: 'absolute',
                    inset: 0,
                    pointerEvents: 'none',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    padding: '1.2rem',
                    fontSize: '0.72rem',
                    fontFamily: 'monospace',
                    fontWeight: 700,
                    color: '#06B6D4',
                    textShadow: '0 0 4px #000'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>CRUST: 452°C</span>
                    <span>HEARTH: 468°C</span>
                  </div>
                  <div style={{ textAlign: 'center', color: '#F43F5E', fontSize: '0.85rem' }}>
                    CORE MELT: 345°C
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>EMISSIVITY: 0.95</span>
                    <span>FLIR-450 THERMAL</span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Interactive Controls Bar (Rotate, Slices, Macro) */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: '0.4rem',
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '0.55rem 0.85rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              marginBottom: '1rem',
              position: 'relative',
              zIndex: 2,
              flexWrap: 'wrap'
            }}
          >
            {/* 360 Rotation Controls */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              <button
                type="button"
                onClick={() => {
                  kitchenAudio.playIngredientClick();
                  setRotationAngle((prev) => (prev - 45 + 360) % 360);
                }}
                className="btn-outline"
                style={{
                  padding: '0.22rem 0.5rem',
                  fontSize: '0.74rem',
                  color: '#FFFFFF',
                  borderColor: 'rgba(255, 255, 255, 0.2)',
                  minHeight: '28px'
                }}
                title="Rotate Counter-Clockwise"
              >
                ↺ -45°
              </button>
              <button
                type="button"
                onClick={() => {
                  kitchenAudio.playIngredientClick();
                  setRotationAngle((prev) => (prev + 45) % 360);
                }}
                className="btn-outline"
                style={{
                  padding: '0.22rem 0.5rem',
                  fontSize: '0.74rem',
                  color: '#FFFFFF',
                  borderColor: 'rgba(255, 255, 255, 0.2)',
                  minHeight: '28px'
                }}
                title="Rotate Clockwise"
              >
                ↻ +45°
              </button>
              <button
                type="button"
                onClick={() => setRotationAngle(0)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: '#94A3B8',
                  fontSize: '0.72rem',
                  cursor: 'pointer',
                  padding: '0.2rem 0.35rem'
                }}
                title="Reset Angle"
              >
                Reset
              </button>
            </div>

            {/* Slices Overlay Toggles */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <span style={{ fontSize: '0.7rem', color: '#94A3B8' }}>Cuts:</span>
              {[0, 6, 8].map((cuts) => (
                <button
                  key={cuts}
                  type="button"
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setSliceCount(cuts);
                  }}
                  style={{
                    background: sliceCount === cuts ? 'var(--accent-tomato)' : 'rgba(255, 255, 255, 0.08)',
                    color: '#FFFFFF',
                    border: 'none',
                    borderRadius: 'var(--radius-xs)',
                    fontSize: '0.72rem',
                    fontWeight: 700,
                    padding: '0.22rem 0.45rem',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                >
                  {cuts === 0 ? 'Whole' : `${cuts}x`}
                </button>
              ))}
            </div>

            {/* Macro Zoom Lens */}
            <button
              type="button"
              onClick={() => {
                kitchenAudio.playIngredientClick();
                setIsZoomed(!isZoomed);
              }}
              style={{
                background: isZoomed ? '#10B981' : 'rgba(255, 255, 255, 0.08)',
                color: '#FFFFFF',
                border: 'none',
                borderRadius: 'var(--radius-xs)',
                fontSize: '0.72rem',
                fontWeight: 700,
                padding: '0.22rem 0.5rem',
                cursor: 'pointer'
              }}
              title="Toggle Macro Crust & Blister Inspection"
            >
              🔍 {isZoomed ? 'Zoom 1x' : 'Macro 1.2x'}
            </button>
          </div>

          {/* Finishing Garnishes Shaker Buttons */}
          <div style={{ marginBottom: '1.1rem', position: 'relative', zIndex: 2 }}>
            <div
              style={{
                fontSize: '0.72rem',
                color: '#94A3B8',
                fontWeight: 700,
                textTransform: 'uppercase',
                marginBottom: '0.4rem'
              }}
            >
              Finishing Table Shakers:
            </div>
            <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
              <button
                type="button"
                onClick={() => toggleGarnish('chiliFlakes')}
                style={{
                  background: garnishes.chiliFlakes ? 'rgba(239, 68, 68, 0.25)' : 'rgba(255, 255, 255, 0.06)',
                  border: garnishes.chiliFlakes ? '1px solid #EF4444' : '1px solid rgba(255, 255, 255, 0.15)',
                  color: garnishes.chiliFlakes ? '#FCA5A5' : '#CBD5E1',
                  borderRadius: 'var(--radius-full)',
                  padding: '0.25rem 0.65rem',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.3rem'
                }}
              >
                🌶️ {garnishes.chiliFlakes ? '✓ Chili Flakes' : '+ Chili Flakes'}
              </button>
              <button
                type="button"
                onClick={() => toggleGarnish('oregano')}
                style={{
                  background: garnishes.oregano ? 'rgba(16, 185, 129, 0.25)' : 'rgba(255, 255, 255, 0.06)',
                  border: garnishes.oregano ? '1px solid #10B981' : '1px solid rgba(255, 255, 255, 0.15)',
                  color: garnishes.oregano ? '#86EFAC' : '#CBD5E1',
                  borderRadius: 'var(--radius-full)',
                  padding: '0.25rem 0.65rem',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.3rem'
                }}
              >
                🌿 {garnishes.oregano ? '✓ Sicilian Oregano' : '+ Wild Oregano'}
              </button>
              <button
                type="button"
                onClick={() => toggleGarnish('evooDrizzle')}
                style={{
                  background: garnishes.evooDrizzle ? 'rgba(245, 158, 11, 0.25)' : 'rgba(255, 255, 255, 0.06)',
                  border: garnishes.evooDrizzle ? '1px solid #F59E0B' : '1px solid rgba(255, 255, 255, 0.15)',
                  color: garnishes.evooDrizzle ? '#FDE68A' : '#CBD5E1',
                  borderRadius: 'var(--radius-full)',
                  padding: '0.25rem 0.65rem',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.3rem'
                }}
              >
                🫒 {garnishes.evooDrizzle ? '✓ EVOO Drizzle' : '+ EVOO Drizzle'}
              </button>
            </div>
          </div>
        </>
      ) : (
        /* Michelin Sommelier Tasting Radar View */
        <div style={{ padding: '1rem 0 1.5rem', textAlign: 'center', position: 'relative', zIndex: 2 }}>
          <div
            style={{
              fontSize: '0.75rem',
              color: '#94A3B8',
              fontWeight: 700,
              textTransform: 'uppercase',
              marginBottom: '0.8rem'
            }}
          >
            Sensory Flavor Profile & Balance
          </div>
          <svg viewBox="0 0 240 220" style={{ width: '100%', maxWidth: '240px', margin: '0 auto', display: 'block' }}>
            <polygon points="120,20 200,60 200,160 120,200 40,160 40,60" fill="none" stroke="rgba(255,255,255,0.15)" strokeWidth="1" />
            <polygon points="120,50 170,75 170,145 120,170 70,145 70,75" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1" />
            <polygon points="120,80 140,90 140,130 120,140 100,130 100,90" fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="1" />

            <polygon
              points={`120,${25} ${showCheese ? 190 : 130},${65} 185,${155} 120,${isSpicy ? 190 : 140} ${showSauce ? 45 : 95},155 50,65`}
              fill="rgba(226, 55, 68, 0.45)"
              stroke="#EF4444"
              strokeWidth="2"
            />

            <text x="120" y="14" fill="#FBBF24" fontSize="10" textAnchor="middle" fontWeight="bold">Crispness (9.6)</text>
            <text x="205" y="62" fill="#34D399" fontSize="10" textAnchor="start" fontWeight="bold">Cheese Pull (9.8)</text>
            <text x="205" y="165" fill="#60A5FA" fontSize="10" textAnchor="start" fontWeight="bold">Umami (8.9)</text>
            <text x="120" y="215" fill="#F87171" fontSize="10" textAnchor="middle" fontWeight="bold">{isSpicy ? 'Spice (8.5)' : 'Spice (3.0)'}</text>
            <text x="35" y="165" fill="#F472B6" fontSize="10" textAnchor="end" fontWeight="bold">Acidity (7.4)</text>
            <text x="35" y="62" fill="#A78BFA" fontSize="10" textAnchor="end" fontWeight="bold">Aroma (9.2)</text>
          </svg>

          <p style={{ fontSize: '0.82rem', color: '#CBD5E1', maxWidth: '300px', margin: '0.8rem auto 0', lineHeight: 1.5 }}>
            Balanced wood-fired profile: High caramelization on outer rim with harmonious acidity and melted curd fat.
          </p>
        </div>
      )}

      {/* Bottom Live Metrics Strip */}
      <div
        style={{
          borderTop: '1px solid rgba(255, 255, 255, 0.08)',
          paddingTop: '0.9rem',
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '0.5rem',
          textAlign: 'center',
          position: 'relative',
          zIndex: 2
        }}
      >
        <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '0.45rem', borderRadius: 'var(--radius-sm)' }}>
          <div style={{ fontSize: '0.68rem', color: '#94A3B8', textTransform: 'uppercase' }}>Energy</div>
          <div style={{ fontWeight: 800, fontSize: '0.95rem', color: '#F1F5F9' }}>~{totalCalories} kcal</div>
        </div>
        <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '0.45rem', borderRadius: 'var(--radius-sm)' }}>
          <div style={{ fontSize: '0.68rem', color: '#94A3B8', textTransform: 'uppercase' }}>Bake Time</div>
          <div style={{ fontWeight: 800, fontSize: '0.95rem', color: '#F59E0B' }}>90 Sec @ 450°C</div>
        </div>
        <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '0.45rem', borderRadius: 'var(--radius-sm)' }}>
          <div style={{ fontSize: '0.68rem', color: '#94A3B8', textTransform: 'uppercase' }}>Melt Score</div>
          <div style={{ fontWeight: 800, fontSize: '0.95rem', color: showCheese ? '#10B981' : '#64748B' }}>
            {showCheese ? '10 / 10' : '0 / 10'}
          </div>
        </div>
        <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '0.45rem', borderRadius: 'var(--radius-sm)' }}>
          <div style={{ fontSize: '0.68rem', color: '#94A3B8', textTransform: 'uppercase' }}>Heat Scale</div>
          <div style={{ fontWeight: 800, fontSize: '0.95rem', color: isSpicy ? '#EF4444' : '#94A3B8' }}>
            {isSpicy ? '🌶️ Bold Heat' : '🌱 Mild Herb'}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PizzaCanvas;
