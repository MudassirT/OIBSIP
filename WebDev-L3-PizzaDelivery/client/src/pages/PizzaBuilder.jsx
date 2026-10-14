import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import PizzaCanvas from '../components/PizzaCanvas';
import { getIngredientVisual } from '../utils/ingredientVisuals';
import { kitchenAudio } from '../utils/audioSynth';

const STEPS = [
  { step: 1, title: 'Artisan Crust', icon: '🌾', category: 'bases', desc: 'Hand-stretched dough fermented up to 36 hours' },
  { step: 2, title: 'Signature Sauce', icon: '🥫', category: 'sauces', desc: 'Slow-simmered volcanic tomato, pesto, or white garlic' },
  { step: 3, title: 'Molten Cheese', icon: '🧀', category: 'cheeses', desc: 'Artisanal curds with golden blister melt' },
  { step: 4, title: 'Farm Harvest', icon: '🥦', category: 'veggies', desc: 'Pick unlimited crisp vegetables & paneer' }
];

const SIZES = [
  { id: '10', name: 'Personal 10"', slices: 4, label: 'Single 10"', desc: '1 Person • 4 Slices', priceOffset: 0, multiplier: 0.85 },
  { id: '12', name: 'Artisan 12"', slices: 6, label: 'Medium 12"', desc: '2-3 Persons • 6 Slices', priceOffset: 50, multiplier: 1.0 },
  { id: '14', name: 'Grande 14"', slices: 8, label: 'Large 14"', desc: '3-4 Persons • 8 Slices', priceOffset: 110, multiplier: 1.25 }
];

const NAME_SUGGESTIONS = [
  "Mudassir's Wood-Fired Masterpiece",
  'Rustic Napoli Truffle Pie',
  'Artisan Hearth Firehouse',
  'Tuscan Garden Harvest',
  'Chef Specialty Pepper Crunch'
];

const PizzaBuilder = () => {
  const { addCustomPizza } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [currentStep, setCurrentStep] = useState(1);
  const [selectedSize, setSelectedSize] = useState(SIZES[1]); // Default 12"
  const [options, setOptions] = useState({
    bases: [],
    sauces: [],
    cheeses: [],
    veggies: []
  });
  const [loading, setLoading] = useState(true);

  // Selection states
  const [selectedBase, setSelectedBase] = useState(null);
  const [selectedSauce, setSelectedSauce] = useState(null);
  const [selectedCheese, setSelectedCheese] = useState(null);
  const [selectedVeggies, setSelectedVeggies] = useState([]);
  const [pizzaName, setPizzaName] = useState("Mudassir's Artisan Firehouse");

  useEffect(() => {
    const fetchOptions = async () => {
      try {
        setLoading(true);
        const res = await api.get('/inventory/options');
        if (res.data.success) {
          const opts = res.data.options;
          setOptions(opts);
          // Preselect first available default base, sauce, cheese for immediate visual satisfaction
          if (opts.bases?.length && !selectedBase) {
            const firstAvailBase = opts.bases.find((b) => b.isAvailable) || opts.bases[0];
            setSelectedBase(firstAvailBase);
          }
          if (opts.sauces?.length && !selectedSauce) {
            const firstAvailSauce = opts.sauces.find((s) => s.isAvailable) || opts.sauces[0];
            setSelectedSauce(firstAvailSauce);
          }
          if (opts.cheeses?.length && !selectedCheese) {
            const firstAvailCheese = opts.cheeses.find((c) => c.isAvailable) || opts.cheeses[0];
            setSelectedCheese(firstAvailCheese);
          }
        }
      } catch (err) {
        console.error('Error fetching inventory builder options:', err);
        showToast('Failed to load ingredients for pizza builder', 'error');
      } finally {
        setLoading(false);
      }
    };

    fetchOptions();
  }, [showToast]);

  // Global Keyboard Shortcuts (Press 1, 2, 3, 4 to quickly switch atelier categories)
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;
      if (['1', '2', '3', '4'].includes(e.key)) {
        const stepNum = parseInt(e.key, 10);
        kitchenAudio.playIngredientClick();
        setCurrentStep(stepNum);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Live running price with size offset
  const ingredientsSubtotal =
    (selectedBase?.price || 0) +
    (selectedSauce?.price || 0) +
    (selectedCheese?.price || 0) +
    selectedVeggies.reduce((acc, v) => acc + v.price, 0);

  const runningPrice = ingredientsSubtotal + selectedSize.priceOffset;

  // Validation per step
  const canProceed = () => {
    if (currentStep === 1) return !!selectedBase;
    if (currentStep === 2) return !!selectedSauce;
    if (currentStep === 3) return !!selectedCheese;
    if (currentStep === 4) return true; // Veggies are optional
    return false;
  };

  const handleNext = () => {
    if (!canProceed()) {
      showToast('Please make a selection to proceed to the next step', 'warning');
      return;
    }
    kitchenAudio.playIngredientClick();
    if (currentStep < 4) {
      setCurrentStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    kitchenAudio.playIngredientClick();
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
    }
  };

  const toggleVeggie = (veggie) => {
    if (!veggie.isAvailable) {
      showToast(`"${veggie.name}" is currently out of stock in kitchen`, 'warning');
      return;
    }
    kitchenAudio.playToppingPop();
    setSelectedVeggies((prev) => {
      const exists = prev.some((v) => v._id === veggie._id);
      if (exists) {
        return prev.filter((v) => v._id !== veggie._id);
      } else {
        return [...prev, veggie];
      }
    });
  };

  const handleFinishAndOrder = () => {
    if (!selectedBase || !selectedSauce || !selectedCheese) {
      showToast('Please complete all required pizza steps (Crust, Sauce, Cheese)', 'warning');
      return;
    }

    kitchenAudio.playIngredientClick();
    addCustomPizza({
      name: `${pizzaName.trim() || 'Custom Crafted Pizza'} (${selectedSize.name})`,
      base: selectedBase,
      sauce: selectedSauce,
      cheese: selectedCheese,
      veggies: selectedVeggies,
      unitPrice: runningPrice
    });

    showToast('🎉 Custom pizza successfully forged and added to cart!', 'success');
    navigate('/summary');
  };

  // Dynamic Sommelier Tasting Commentary Generator
  const getChefTastingNote = () => {
    const crust = selectedBase?.name || 'Hand-Stretched Crust';
    const sauce = selectedSauce?.name || 'San Marzano Marinara';
    const cheese = selectedCheese?.name || 'Fior di Latte Mozzarella';

    let note = `Our Master Pizzaiolo notes: The ${crust} produces an airy, blistered rim baked at 450°C. Coupled with ${sauce}, the palate enjoys vibrant volcanic sweetness and acidity, complemented by the molten pull of ${cheese}.`;
    if (selectedVeggies.length > 0) {
      const vegNames = selectedVeggies.map((v) => v.name).join(', ');
      note += ` The fresh additions of ${vegNames} bring crisp farm sweetness, caramelization, and aromatic essential oils.`;
    }
    return note;
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '6rem 1rem', textAlign: 'center' }}>
        <div style={{ fontSize: '3.5rem', animation: 'spin 1.5s infinite linear' }}>🥣</div>
        <h2 style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>Firing Up PizzaForge Live Kitchen</h2>
        <p style={{ color: 'var(--text-muted)' }}>
          Retrieving organic ingredient batches & wood-fired kitchen inventory...
        </p>
      </div>
    );
  }

  const activeStepObj = STEPS.find((s) => s.step === currentStep);
  const currentOptions = options[activeStepObj.category] || [];

  return (
    <div className="main-content" style={{ paddingBottom: '7rem' }}>
      <div className="container">
        {/* Figma Header Banner */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'var(--accent-tomato-light)',
              color: 'var(--accent-tomato)',
              padding: '0.4rem 1.1rem',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.82rem',
              fontWeight: 800,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              marginBottom: '0.85rem',
              border: '1px solid rgba(226, 55, 68, 0.25)',
              boxShadow: '0 2px 8px rgba(226, 55, 68, 0.08)'
            }}
          >
            <span>🔥 LIVE 450°C WOOD-FIRED PIZZAIOLO ATELIER</span>
          </div>
          <h1 style={{ fontSize: 'clamp(2.2rem, 4vw, 3rem)', marginBottom: '0.5rem', letterSpacing: '-0.02em' }}>
            Sculpt Your <span className="gradient-text">Artisan Pizza</span>
          </h1>
          <p style={{ color: 'var(--text-muted)', maxWidth: '660px', margin: '0 auto', fontSize: '1.02rem', lineHeight: 1.6 }}>
            Customize your handcrafted pizza layer by layer. Every dough ball, simmered sauce, and farm harvest topping is
            synchronized in real time with our 450°C wood-fired hearth.
          </p>

          {/* Pizza Diameter & Size Selector Bar */}
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              background: '#FFFFFF',
              border: '1.5px solid var(--border-light)',
              borderRadius: 'var(--radius-full)',
              padding: '0.35rem',
              gap: '0.4rem',
              marginTop: '1.5rem',
              boxShadow: 'var(--shadow-sm)'
            }}
          >
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', padding: '0 0.75rem' }}>
              PIZZA SIZE:
            </span>
            {SIZES.map((sz) => {
              const isSelected = selectedSize.id === sz.id;
              return (
                <button
                  key={sz.id}
                  type="button"
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setSelectedSize(sz);
                  }}
                  style={{
                    border: 'none',
                    borderRadius: 'var(--radius-full)',
                    padding: '0.4rem 1rem',
                    fontSize: '0.82rem',
                    fontWeight: 700,
                    cursor: 'pointer',
                    background: isSelected ? 'var(--accent-tomato)' : 'transparent',
                    color: isSelected ? '#FFFFFF' : 'var(--text-main)',
                    boxShadow: isSelected ? '0 2px 8px rgba(226, 55, 68, 0.35)' : 'none',
                    transition: 'all 0.2s ease',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.35rem'
                  }}
                >
                  <span>{sz.label}</span>
                  {sz.priceOffset > 0 && (
                    <span
                      style={{
                        fontSize: '0.7rem',
                        opacity: isSelected ? 0.9 : 0.6,
                        fontWeight: 600
                      }}
                    >
                      (+₹{sz.priceOffset})
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Stepper Navigation Bar */}
        <div
          style={{
            background: '#FFFFFF',
            border: '1px solid var(--border-light)',
            borderRadius: 'var(--radius-xl)',
            padding: '1.25rem 1.75rem',
            marginBottom: '2.5rem',
            boxShadow: 'var(--shadow-card)'
          }}
        >
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(4, 1fr)',
              gap: '1rem',
              alignItems: 'center'
            }}
            className="builder-step-nav"
          >
            {STEPS.map((s) => {
              const isDone = s.step < currentStep;
              const isCurrent = s.step === currentStep;

              return (
                <div
                  key={s.step}
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setCurrentStep(s.step);
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.85rem',
                    cursor: 'pointer',
                    opacity: s.step > currentStep ? 0.6 : 1,
                    transition: 'all 0.25s ease',
                    padding: '0.5rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    background: isCurrent ? 'var(--accent-tomato-light)' : 'transparent',
                    border: isCurrent ? '1px solid rgba(226, 55, 68, 0.2)' : '1px solid transparent'
                  }}
                >
                  <div
                    style={{
                      width: '44px',
                      height: '44px',
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 800,
                      fontSize: '1.15rem',
                      backgroundColor: isDone
                        ? 'var(--accent-basil)'
                        : isCurrent
                        ? 'var(--accent-tomato)'
                        : '#E2E8F0',
                      color: isDone || isCurrent ? '#FFFFFF' : '#64748B',
                      boxShadow: isCurrent ? '0 0 0 4px rgba(226, 55, 68, 0.22)' : 'none',
                      transition: 'all 0.25s ease',
                      flexShrink: 0
                    }}
                  >
                    {isDone ? '✓' : s.icon}
                  </div>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.72rem', color: 'var(--text-light)', fontWeight: 700, textTransform: 'uppercase' }}>
                      <span>Step {s.step}</span>
                      <kbd
                        style={{
                          background: isCurrent ? 'var(--accent-tomato)' : '#E2E8F0',
                          color: isCurrent ? '#FFFFFF' : '#64748B',
                          padding: '0.05rem 0.35rem',
                          borderRadius: '4px',
                          fontSize: '0.62rem',
                          fontFamily: 'monospace',
                          fontWeight: 800
                        }}
                      >
                        {s.step}
                      </kbd>
                    </div>
                    <div
                      style={{
                        fontSize: '0.95rem',
                        fontWeight: isCurrent ? 800 : 600,
                        color: isCurrent ? 'var(--accent-tomato)' : 'var(--text-main)',
                        letterSpacing: '-0.01em'
                      }}
                    >
                      {s.title}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 2-Column Interactive Studio Layout */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'minmax(350px, 430px) 1fr',
            gap: '2.5rem',
            alignItems: 'start'
          }}
          className="studio-grid"
        >
          {/* Column 1: Sticky Pizza Visualizer Canvas & Craft Recipe Card */}
          <div style={{ position: 'sticky', top: '90px' }} className="studio-sticky-col">
            <PizzaCanvas
              currentStep={currentStep}
              selectedBase={selectedBase}
              selectedSauce={selectedSauce}
              selectedCheese={selectedCheese}
              selectedVeggies={selectedVeggies}
              runningPrice={runningPrice}
              pizzaName={pizzaName}
              selectedSize={selectedSize}
              onStepSelect={setCurrentStep}
            />

            {/* Custom Pizza Name & Sommelier Tasting Card */}
            <div
              className="card"
              style={{
                marginTop: '1.5rem',
                padding: '1.5rem',
                border: '1px solid var(--border-light)',
                boxShadow: 'var(--shadow-card)'
              }}
            >
              <div style={{ marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label style={{ fontSize: '0.78rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                    Name Your Signature Pie
                  </label>
                  <span style={{ fontSize: '0.72rem', color: 'var(--accent-tomato)', fontWeight: 700 }}>
                    ✏️ Editable
                  </span>
                </div>
                <input
                  type="text"
                  value={pizzaName}
                  onChange={(e) => setPizzaName(e.target.value)}
                  placeholder="e.g. Mudassir's Firehouse Classic"
                  className="form-input"
                  style={{
                    fontWeight: 800,
                    fontSize: '1.05rem',
                    marginTop: '0.4rem',
                    minHeight: '44px',
                    borderColor: 'var(--border-light)',
                    color: 'var(--text-main)'
                  }}
                />

                {/* Quick Name Ideas Strip */}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginTop: '0.5rem' }}>
                  {NAME_SUGGESTIONS.slice(0, 3).map((idea, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => {
                        kitchenAudio.playIngredientClick();
                        setPizzaName(idea);
                      }}
                      style={{
                        background: 'var(--bg-main)',
                        border: '1px solid var(--border-light)',
                        borderRadius: 'var(--radius-full)',
                        padding: '0.15rem 0.55rem',
                        fontSize: '0.72rem',
                        color: 'var(--text-muted)',
                        cursor: 'pointer'
                      }}
                    >
                      💡 {idea}
                    </button>
                  ))}
                </div>
              </div>

              {/* Live Sommelier Commentary Box */}
              <div
                style={{
                  background: 'linear-gradient(135deg, #FAF8F5 0%, #F5EFEB 100%)',
                  padding: '0.85rem 1rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid #EBE5DC',
                  marginBottom: '1rem'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.35rem' }}>
                  <span style={{ fontSize: '0.95rem' }}>👨‍🍳</span>
                  <span style={{ fontSize: '0.74rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--accent-crust)' }}>
                    Pizzaiolo Tasting Notes
                  </span>
                </div>
                <p style={{ fontSize: '0.82rem', color: '#475569', lineHeight: 1.5, margin: 0, fontStyle: 'italic' }}>
                  "{getChefTastingNote()}"
                </p>
              </div>

              {/* Recipe Spec Breakdown with Interactive Active Category Rows */}
              <div
                style={{
                  fontSize: '0.84rem',
                  color: 'var(--text-muted)',
                  borderTop: '1px solid var(--border-light)',
                  paddingTop: '0.85rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.45rem'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.3rem 0.5rem' }}>
                  <span>📏 Size:</span>
                  <strong style={{ color: 'var(--text-main)' }}>
                    {selectedSize.name} ({selectedSize.slices} Slices)
                  </strong>
                </div>

                {/* Category 1: Crust */}
                <div
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setCurrentStep(1);
                  }}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.55rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    background: currentStep === 1 ? 'rgba(226, 55, 68, 0.08)' : 'transparent',
                    border: currentStep === 1 ? '1.5px solid var(--accent-tomato)' : '1.5px solid transparent',
                    boxShadow: currentStep === 1 ? '0 2px 8px rgba(226, 55, 68, 0.12)' : 'none',
                    transition: 'all 0.2s ease'
                  }}
                  title="Click to select or change Artisan Crust"
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <span style={{ fontSize: '1.05rem' }}>🌾</span>
                    <span style={{ fontWeight: currentStep === 1 ? 800 : 600, color: currentStep === 1 ? 'var(--accent-tomato)' : 'inherit' }}>
                      Crust Dough:
                    </span>
                    {currentStep === 1 && (
                      <span
                        style={{
                          fontSize: '0.65rem',
                          background: 'var(--accent-tomato)',
                          color: '#FFFFFF',
                          padding: '0.12rem 0.45rem',
                          borderRadius: 'var(--radius-full)',
                          fontWeight: 800
                        }}
                      >
                        Active
                      </span>
                    )}
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <strong style={{ color: currentStep === 1 ? 'var(--accent-tomato)' : 'var(--text-main)' }}>
                      {selectedBase?.name || 'Pick Crust'}
                    </strong>
                    {selectedBase?.price > 0 && (
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                        (+₹{selectedBase.price})
                      </span>
                    )}
                  </div>
                </div>

                {/* Category 2: Sauce */}
                <div
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setCurrentStep(2);
                  }}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.55rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    background: currentStep === 2 ? 'rgba(226, 55, 68, 0.08)' : 'transparent',
                    border: currentStep === 2 ? '1.5px solid var(--accent-tomato)' : '1.5px solid transparent',
                    boxShadow: currentStep === 2 ? '0 2px 8px rgba(226, 55, 68, 0.12)' : 'none',
                    transition: 'all 0.2s ease'
                  }}
                  title="Click to select or change Signature Sauce"
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <span style={{ fontSize: '1.05rem' }}>🥫</span>
                    <span style={{ fontWeight: currentStep === 2 ? 800 : 600, color: currentStep === 2 ? 'var(--accent-tomato)' : 'inherit' }}>
                      Sauce Base:
                    </span>
                    {currentStep === 2 && (
                      <span
                        style={{
                          fontSize: '0.65rem',
                          background: 'var(--accent-tomato)',
                          color: '#FFFFFF',
                          padding: '0.12rem 0.45rem',
                          borderRadius: 'var(--radius-full)',
                          fontWeight: 800
                        }}
                      >
                        Active
                      </span>
                    )}
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <strong style={{ color: currentStep === 2 ? 'var(--accent-tomato)' : 'var(--text-main)' }}>
                      {selectedSauce?.name || 'Pick Sauce'}
                    </strong>
                    {selectedSauce?.price > 0 && (
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                        (+₹{selectedSauce.price})
                      </span>
                    )}
                  </div>
                </div>

                {/* Category 3: Cheese */}
                <div
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setCurrentStep(3);
                  }}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.55rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    background: currentStep === 3 ? 'rgba(226, 55, 68, 0.08)' : 'transparent',
                    border: currentStep === 3 ? '1.5px solid var(--accent-tomato)' : '1.5px solid transparent',
                    boxShadow: currentStep === 3 ? '0 2px 8px rgba(226, 55, 68, 0.12)' : 'none',
                    transition: 'all 0.2s ease'
                  }}
                  title="Click to select or change Molten Cheese"
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <span style={{ fontSize: '1.05rem' }}>🧀</span>
                    <span style={{ fontWeight: currentStep === 3 ? 800 : 600, color: currentStep === 3 ? 'var(--accent-tomato)' : 'inherit' }}>
                      Cheese Melt:
                    </span>
                    {currentStep === 3 && (
                      <span
                        style={{
                          fontSize: '0.65rem',
                          background: 'var(--accent-tomato)',
                          color: '#FFFFFF',
                          padding: '0.12rem 0.45rem',
                          borderRadius: 'var(--radius-full)',
                          fontWeight: 800
                        }}
                      >
                        Active
                      </span>
                    )}
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <strong style={{ color: currentStep === 3 ? 'var(--accent-tomato)' : 'var(--text-main)' }}>
                      {selectedCheese?.name || 'Pick Cheese'}
                    </strong>
                    {selectedCheese?.price > 0 && (
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                        (+₹{selectedCheese.price})
                      </span>
                    )}
                  </div>
                </div>

                {/* Category 4: Harvest Veggies */}
                <div
                  onClick={() => {
                    kitchenAudio.playIngredientClick();
                    setCurrentStep(4);
                  }}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.55rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    background: currentStep === 4 ? 'rgba(226, 55, 68, 0.08)' : 'transparent',
                    border: currentStep === 4 ? '1.5px solid var(--accent-tomato)' : '1.5px solid transparent',
                    boxShadow: currentStep === 4 ? '0 2px 8px rgba(226, 55, 68, 0.12)' : 'none',
                    transition: 'all 0.2s ease'
                  }}
                  title="Click to select or change Farm Harvest Toppings"
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <span style={{ fontSize: '1.05rem' }}>🥦</span>
                    <span style={{ fontWeight: currentStep === 4 ? 800 : 600, color: currentStep === 4 ? 'var(--accent-tomato)' : 'inherit' }}>
                      Harvest Toppings:
                    </span>
                    {currentStep === 4 && (
                      <span
                        style={{
                          fontSize: '0.65rem',
                          background: 'var(--accent-tomato)',
                          color: '#FFFFFF',
                          padding: '0.12rem 0.45rem',
                          borderRadius: 'var(--radius-full)',
                          fontWeight: 800
                        }}
                      >
                        Active
                      </span>
                    )}
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <strong style={{ color: currentStep === 4 ? 'var(--accent-basil)' : 'var(--text-main)' }}>
                      {selectedVeggies.length > 0 ? `${selectedVeggies.length} Selected` : 'None'}
                    </strong>
                    {selectedVeggies.length > 0 && (
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                        (+₹{selectedVeggies.reduce((a, v) => a + v.price, 0)})
                      </span>
                    )}
                  </div>
                </div>

                {selectedVeggies.length > 0 && (
                  <div
                    style={{
                      fontSize: '0.78rem',
                      color: '#64748B',
                      lineHeight: 1.4,
                      marginTop: '0.2rem',
                      padding: '0 0.75rem'
                    }}
                  >
                    {selectedVeggies.map((v) => v.name).join(', ')}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Column 2: Interactive Ingredient Options & Step Controls */}
          <div>
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: 'var(--radius-xl)',
                border: '1px solid var(--border-light)',
                padding: '2rem',
                boxShadow: 'var(--shadow-card)',
                marginBottom: '1.75rem'
              }}
            >
              {/* Step Header */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '1.75rem',
                  borderBottom: '1px solid var(--border-subtle)',
                  paddingBottom: '1.25rem'
                }}
              >
                <div>
                  <span
                    style={{
                      fontSize: '0.78rem',
                      fontWeight: 800,
                      color: 'var(--accent-tomato)',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em'
                    }}
                  >
                    Step {currentStep} of 4 • {activeStepObj.title}
                  </span>
                  <h2 style={{ fontSize: '1.8rem', marginTop: '0.25rem', letterSpacing: '-0.02em' }}>
                    {activeStepObj.title}
                  </h2>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.96rem', marginTop: '0.2rem' }}>
                    {activeStepObj.desc}
                  </p>
                </div>

                <div
                  style={{
                    background: 'var(--bg-main)',
                    padding: '0.45rem 0.95rem',
                    borderRadius: 'var(--radius-full)',
                    fontSize: '0.82rem',
                    fontWeight: 700,
                    color: 'var(--text-main)',
                    border: '1px solid var(--border-light)',
                    whiteSpace: 'nowrap'
                  }}
                >
                  {currentStep === 4 ? '🥦 Multi-Select' : '🔘 Pick 1 Option'}
                </div>
              </div>

              {/* Ultra-Realistic Ingredient Cards Grid */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
                  gap: '1.35rem',
                  marginBottom: '2.5rem'
                }}
              >
                {currentOptions.map((opt) => {
                  let isSelected = false;
                  if (currentStep === 1) isSelected = selectedBase?._id === opt._id;
                  else if (currentStep === 2) isSelected = selectedSauce?._id === opt._id;
                  else if (currentStep === 3) isSelected = selectedCheese?._id === opt._id;
                  else if (currentStep === 4) isSelected = selectedVeggies.some((v) => v._id === opt._id);

                  const isOutOfStock = !opt.isAvailable;
                  const visual = getIngredientVisual(opt.name);

                  return (
                    <div
                      key={opt._id}
                      onClick={() => {
                        if (isOutOfStock) {
                          showToast(`"${opt.name}" is currently out of stock`, 'warning');
                          return;
                        }
                        kitchenAudio.playIngredientClick();
                        if (currentStep === 1) setSelectedBase(opt);
                        else if (currentStep === 2) setSelectedSauce(opt);
                        else if (currentStep === 3) setSelectedCheese(opt);
                        else if (currentStep === 4) toggleVeggie(opt);
                      }}
                      style={{
                        cursor: isOutOfStock ? 'not-allowed' : 'pointer',
                        opacity: isOutOfStock ? 0.45 : 1,
                        border: isSelected
                          ? '2px solid var(--accent-tomato)'
                          : '1.5px solid var(--border-light)',
                        backgroundColor: isSelected ? 'var(--accent-tomato-light)' : '#FFFFFF',
                        borderRadius: 'var(--radius-lg)',
                        overflow: 'hidden',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between',
                        position: 'relative',
                        transition: 'all var(--transition-normal)',
                        boxShadow: isSelected
                          ? '0 10px 24px -4px rgba(226, 55, 68, 0.22)'
                          : 'var(--shadow-card)'
                      }}
                      className="card-interactive"
                    >
                      {/* Photographic Preview Banner */}
                      <div
                        style={{
                          width: '100%',
                          height: '115px',
                          position: 'relative',
                          overflow: 'hidden',
                          backgroundColor: '#1E1F24'
                        }}
                      >
                        <img
                          src={visual.thumbnail}
                          alt={opt.name}
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                            transition: 'transform 0.4s ease'
                          }}
                          className="pizza-photo-hover"
                        />
                        <div
                          style={{
                            position: 'absolute',
                            inset: 0,
                            background: 'linear-gradient(180deg, rgba(0,0,0,0.15) 0%, rgba(0,0,0,0.65) 100%)',
                            pointerEvents: 'none'
                          }}
                        />

                        {/* Top Origin Tag */}
                        <div
                          style={{
                            position: 'absolute',
                            top: '0.6rem',
                            left: '0.65rem',
                            background: 'rgba(0, 0, 0, 0.65)',
                            backdropFilter: 'blur(6px)',
                            color: '#FFFFFF',
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            padding: '0.2rem 0.55rem',
                            borderRadius: 'var(--radius-full)',
                            border: '1px solid rgba(255, 255, 255, 0.2)'
                          }}
                        >
                          📍 {visual.origin}
                        </div>

                        {/* Selection Checkmark on Banner */}
                        <div
                          style={{
                            position: 'absolute',
                            top: '0.6rem',
                            right: '0.65rem',
                            width: '26px',
                            height: '26px',
                            borderRadius: '50%',
                            backgroundColor: isSelected ? 'var(--accent-tomato)' : 'rgba(0, 0, 0, 0.6)',
                            border: isSelected ? 'none' : '2px solid rgba(255, 255, 255, 0.6)',
                            color: '#FFFFFF',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '0.8rem',
                            fontWeight: 900,
                            boxShadow: isSelected ? '0 2px 8px rgba(226, 55, 68, 0.5)' : 'none'
                          }}
                        >
                          {isSelected ? '✓' : ''}
                        </div>

                        {/* Bottom Tag */}
                        <div
                          style={{
                            position: 'absolute',
                            bottom: '0.5rem',
                            left: '0.65rem',
                            fontSize: '0.72rem',
                            color: '#FEF08A',
                            fontWeight: 700,
                            textShadow: '0 1px 2px rgba(0,0,0,0.8)'
                          }}
                        >
                          ★ {visual.tag}
                        </div>
                      </div>

                      {/* Content Area */}
                      <div style={{ padding: '1.15rem', display: 'flex', flexDirection: 'column', flex: 1 }}>
                        <div
                          style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'flex-start',
                            gap: '0.5rem',
                            marginBottom: '0.35rem'
                          }}
                        >
                          <h4
                            style={{
                              fontSize: '1.08rem',
                              fontWeight: 800,
                              color: isSelected ? 'var(--accent-tomato)' : 'var(--text-main)',
                              lineHeight: 1.25,
                              letterSpacing: '-0.01em'
                            }}
                          >
                            {opt.name}
                          </h4>
                          <span
                            style={{
                              fontSize: '1.05rem',
                              fontWeight: 900,
                              color: isSelected ? 'var(--accent-tomato)' : 'var(--text-main)',
                              whiteSpace: 'nowrap',
                              fontFamily: 'var(--font-heading)'
                            }}
                          >
                            +₹{opt.price}
                          </span>
                        </div>

                        {/* Sensory Tasting Note */}
                        <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', lineHeight: 1.5, marginBottom: '0.85rem', flex: 1 }}>
                          {visual.sensory}
                        </p>

                        {/* Bottom Metadata & Kitchen Stock */}
                        <div
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            borderTop: '1px solid rgba(0, 0, 0, 0.06)',
                            paddingTop: '0.65rem',
                            fontSize: '0.75rem'
                          }}
                        >
                          <span style={{ color: '#64748B', fontWeight: 600 }}>
                            🔥 {visual.calories}
                          </span>

                          {isOutOfStock ? (
                            <span
                              style={{
                                fontWeight: 700,
                                color: '#DC2626',
                                background: '#FEE2E2',
                                padding: '0.15rem 0.45rem',
                                borderRadius: 'var(--radius-sm)'
                              }}
                            >
                              Out of Stock
                            </span>
                          ) : (
                            <span
                              style={{
                                color: 'var(--accent-basil)',
                                fontWeight: 700,
                                display: 'flex',
                                alignItems: 'center',
                                gap: '0.25rem'
                              }}
                            >
                              <span>●</span> Fresh Stock
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Step Navigation Controls */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  borderTop: '1px solid var(--border-light)',
                  paddingTop: '1.5rem',
                  gap: '1rem',
                  flexWrap: 'wrap'
                }}
              >
                <button
                  type="button"
                  onClick={handleBack}
                  disabled={currentStep === 1}
                  className="btn-outline"
                  style={{
                    padding: '0.75rem 1.6rem',
                    opacity: currentStep === 1 ? 0.35 : 1,
                    fontWeight: 700
                  }}
                >
                  ← Back
                </button>

                <div style={{ display: 'flex', gap: '0.85rem' }}>
                  {currentStep < 4 ? (
                    <button
                      type="button"
                      onClick={handleNext}
                      disabled={!canProceed()}
                      className="btn-primary"
                      style={{
                        padding: '0.75rem 2rem',
                        fontSize: '0.96rem',
                        fontWeight: 800,
                        boxShadow: 'var(--shadow-glow-tomato)'
                      }}
                    >
                      Continue to {STEPS[currentStep].title} →
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={handleFinishAndOrder}
                      disabled={!canProceed()}
                      className="btn-primary"
                      style={{
                        padding: '0.85rem 2.4rem',
                        fontSize: '1.05rem',
                        fontWeight: 900,
                        boxShadow: 'var(--shadow-glow-tomato)'
                      }}
                    >
                      🔥 Forge & Add to Order (₹{runningPrice})
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Quick Kitchen Guarantees Strip */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, 1fr)',
                gap: '1.25rem'
              }}
              className="builder-guarantees"
            >
              <div
                className="card"
                style={{
                  padding: '1.25rem',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  borderRadius: 'var(--radius-lg)'
                }}
              >
                <span style={{ fontSize: '1.6rem', marginBottom: '0.35rem' }}>🪵</span>
                <strong style={{ fontSize: '0.9rem', color: 'var(--text-main)' }}>450°C Oak Wood Oven</strong>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Neapolitan 90-second bake</span>
              </div>
              <div
                className="card"
                style={{
                  padding: '1.25rem',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  borderRadius: 'var(--radius-lg)'
                }}
              >
                <span style={{ fontSize: '1.6rem', marginBottom: '0.35rem' }}>🛡️</span>
                <strong style={{ fontSize: '0.9rem', color: 'var(--text-main)' }}>Server Recalculated</strong>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Zero client price tampering</span>
              </div>
              <div
                className="card"
                style={{
                  padding: '1.25rem',
                  textAlign: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  borderRadius: 'var(--radius-lg)'
                }}
              >
                <span style={{ fontSize: '1.6rem', marginBottom: '0.35rem' }}>⚡</span>
                <strong style={{ fontSize: '0.9rem', color: 'var(--text-main)' }}>Live Socket Stream</strong>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Real-time kitchen order tracking</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Floating Michelin Atelier Dock (Sticky Bottom Navigation Bar) */}
      <div
        style={{
          position: 'fixed',
          bottom: '1.25rem',
          left: '50%',
          transform: 'translateX(-50%)',
          width: 'calc(100% - 2.5rem)',
          maxWidth: '1020px',
          background: 'rgba(18, 19, 23, 0.92)',
          backdropFilter: 'blur(16px)',
          border: '1px solid rgba(255, 255, 255, 0.16)',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.08)',
          borderRadius: 'var(--radius-full)',
          padding: '0.65rem 1.25rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          zIndex: 100,
          color: '#FFFFFF'
        }}
        className="atelier-floating-dock"
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #E23744 0%, #FF5722 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.2rem',
              boxShadow: '0 4px 12px rgba(226, 55, 68, 0.4)',
              flexShrink: 0
            }}
          >
            🍕
          </div>
          <div>
            <div style={{ fontWeight: 800, fontSize: '0.96rem', letterSpacing: '-0.01em', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>{pizzaName}</span>
              <span
                style={{
                  background: 'rgba(255, 255, 255, 0.12)',
                  fontSize: '0.72rem',
                  padding: '0.15rem 0.5rem',
                  borderRadius: 'var(--radius-full)',
                  color: '#FED7AA'
                }}
              >
                {selectedSize.name}
              </span>
            </div>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', gap: '0.5rem' }}>
              <span>Step {currentStep}/4</span>
              <span>•</span>
              <span style={{ color: '#10B981' }}>{selectedVeggies.length} Veggies Picked</span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.7rem', color: '#94A3B8', textTransform: 'uppercase', fontWeight: 600 }}>Total Price</div>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 900, fontSize: '1.35rem', color: '#FBBF24' }}>
              ₹{runningPrice}
            </div>
          </div>

          {currentStep < 4 ? (
            <button
              type="button"
              onClick={handleNext}
              disabled={!canProceed()}
              className="btn-primary"
              style={{
                padding: '0.7rem 1.6rem',
                fontSize: '0.92rem',
                borderRadius: 'var(--radius-full)'
              }}
            >
              Next Step →
            </button>
          ) : (
            <button
              type="button"
              onClick={handleFinishAndOrder}
              disabled={!canProceed()}
              className="btn-primary"
              style={{
                padding: '0.75rem 1.85rem',
                fontSize: '0.96rem',
                borderRadius: 'var(--radius-full)',
                boxShadow: 'var(--shadow-glow-tomato)'
              }}
            >
              🔥 Add to Order
            </button>
          )}
        </div>
      </div>

      <style>{`
        @media (max-width: 900px) {
          .studio-grid {
            grid-template-columns: 1fr !important;
          }
          .studio-sticky-col {
            position: relative !important;
            top: 0 !important;
          }
          .builder-step-nav {
            grid-template-columns: repeat(2, 1fr) !important;
          }
          .builder-guarantees {
            grid-template-columns: 1fr !important;
          }
          .atelier-floating-dock {
            padding: 0.6rem 1rem !important;
          }
        }
      `}</style>
    </div>
  );
};

export default PizzaBuilder;
