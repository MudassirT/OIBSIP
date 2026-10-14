import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../services/api';
import { loadRazorpayScript } from '../services/razorpay';
import { getPizzaImage } from '../utils/pizzaImages';

const OrderSummary = () => {
  const { cartItems, updateQuantity, removeItem, clearCart, cartSubtotal } = useCart();
  const { user, isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [isProcessing, setIsProcessing] = useState(false);
  const [createdOrder, setCreatedOrder] = useState(null);
  const [couponCode, setCouponCode] = useState('');
  const [discountPercent, setDiscountPercent] = useState(0);
  const [couponMessage, setCouponMessage] = useState('');
  const [deliveryAddress, setDeliveryAddress] = useState('742 Evergreen Terrace, Springfield, OR 97477');

  if (cartItems.length === 0) {
    return (
      <div className="main-content">
        <div className="container" style={{ padding: '4rem 1rem', textAlign: 'center' }}>
          <div
            className="card"
            style={{
              maxWidth: '540px',
              margin: '0 auto',
              padding: '3.5rem 2rem',
              border: '1px solid var(--border-light)',
              boxShadow: 'var(--shadow-lg)'
            }}
          >
            <div
              style={{
                width: '80px',
                height: '80px',
                borderRadius: '50%',
                background: 'var(--accent-tomato-light)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2.5rem',
                margin: '0 auto 1.5rem'
              }}
            >
              🛒
            </div>
            <h2 style={{ fontSize: '1.85rem', marginBottom: '0.5rem', letterSpacing: '-0.02em' }}>
              Your Feast Basket is Empty
            </h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '2rem', lineHeight: 1.6 }}>
              You haven't added any pizzas to your cart yet. Explore our chef-curated wood-fired specialties or sculpt
              your custom recipe in the live studio!
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <Link to="/" className="btn-outline" style={{ padding: '0.75rem 1.75rem', fontWeight: 600 }}>
                Explore Menu
              </Link>
              <Link to="/builder" className="btn-primary" style={{ padding: '0.75rem 2rem', fontWeight: 700 }}>
                Craft Custom Pizza →
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Handle promo coupon
  const handleApplyCoupon = (e) => {
    e.preventDefault();
    if (!couponCode.trim()) return;

    if (couponCode.trim().toUpperCase() === 'FORGE20') {
      setDiscountPercent(20);
      setCouponMessage('🎉 Coupon "FORGE20" applied! 20% discount unlocked.');
      showToast('20% discount coupon applied!', 'success');
    } else {
      setDiscountPercent(0);
      setCouponMessage('Invalid or expired coupon code.');
      showToast('Invalid coupon code. Try "FORGE20"', 'warning');
    }
  };

  const discountAmount = Math.round((cartSubtotal * discountPercent) / 100);
  const payableAmount = Math.max(0, cartSubtotal - discountAmount);

  // Handle Checkout & Razorpay
  const handleProceedToPayment = async () => {
    if (!isAuthenticated) {
      showToast('Please log in to finalize your pizza order', 'warning');
      navigate('/login', { state: { from: { pathname: '/summary' } } });
      return;
    }

    setIsProcessing(true);

    try {
      // 1. Ensure Razorpay checkout script is loaded
      const scriptLoaded = await loadRazorpayScript();
      if (!scriptLoaded) {
        console.warn('Could not inject Razorpay script from CDN. Proceeding to simulated test checkout mode.');
      }

      // 2. Prepare payload for server-side recomputation & Razorpay Order creation
      const itemsPayload = cartItems.map((item) => {
        if (!item.isCustom) {
          return {
            pizzaId: item.pizzaId,
            quantity: item.quantity
          };
        } else {
          return {
            name: item.name,
            base: item.base,
            sauce: item.sauce,
            cheese: item.cheese,
            veggies: item.veggies,
            quantity: item.quantity
          };
        }
      });

      // 3. Initiate checkout on backend (strictly recomputes prices on server)
      const checkoutRes = await api.post('/orders/checkout', { items: itemsPayload });
      if (!checkoutRes.data.success) {
        throw new Error(checkoutRes.data.message || 'Checkout failed');
      }

      const { orderId, totalAmount, razorpayOrder } = checkoutRes.data;
      setCreatedOrder({ orderId, totalAmount });

      // 4. Open Razorpay Popup (or simulated test payment dialog if test keys or offline)
      if (window.Razorpay && !razorpayOrder.isMock) {
        const options = {
          key: razorpayOrder.keyId || import.meta.env.VITE_RAZORPAY_KEY_ID,
          amount: razorpayOrder.amount,
          currency: 'INR',
          name: 'PizzaForge Kitchen',
          description: `Artisanal Pizza Order #${orderId.substring(0, 8)}`,
          order_id: razorpayOrder.id,
          prefill: {
            name: user.name,
            email: user.email
          },
          theme: {
            color: '#E23744'
          },
          handler: async function (response) {
            try {
              showToast('Verifying payment HMAC signature with kitchen server...', 'info');
              const verifyRes = await api.post('/orders/verify-payment', {
                orderId,
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature
              });

              if (verifyRes.data.success) {
                clearCart();
                showToast('🎉 Order placed successfully! Dispatched to kitchen.', 'success');
                navigate('/orders');
              }
            } catch (err) {
              const msg = err.response?.data?.message || 'Payment verification failed.';
              showToast(msg, 'error');
            }
          },
          modal: {
            ondismiss: function () {
              setIsProcessing(false);
              showToast('Payment window closed. Your order remains pending in kitchen queue.', 'warning');
            }
          }
        };

        const rzp = new window.Razorpay(options);
        rzp.on('payment.failed', function (response) {
          setIsProcessing(false);
          showToast(`Payment failed: ${response.error?.description || 'Transaction declined'}`, 'error');
        });
        rzp.open();
      } else {
        // Test Simulation Mode (gracefully supported for developer sandbox)
        const mockPaymentId = `pay_sim_${Date.now()}_test`;
        const mockSignature = `mock_sig_${Date.now()}`;

        showToast('Initiating Razorpay Sandbox Payment Verification...', 'info');
        const verifyRes = await api.post('/orders/verify-payment', {
          orderId,
          razorpayOrderId: razorpayOrder.id,
          razorpayPaymentId: mockPaymentId,
          razorpaySignature: mockSignature
        });

        if (verifyRes.data.success) {
          clearCart();
          showToast('🎉 Test Payment Verified! Order dispatched to live kitchen.', 'success');
          navigate('/orders');
        }
      }
    } catch (err) {
      console.error('Payment checkout error:', err);
      const msg = err.response?.data?.message || err.message || 'Payment processing failed.';
      showToast(msg, 'error');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="main-content">
      <div className="container" style={{ maxWidth: '1080px' }}>
        {/* Header */}
        <div style={{ marginBottom: '2.5rem' }}>
          <span className="badge badge-tomato" style={{ marginBottom: '0.5rem' }}>
            Secure Checkout
          </span>
          <h1 style={{ fontSize: '2.4rem', letterSpacing: '-0.02em', marginBottom: '0.35rem' }}>
            Review Your Order & Feast
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
            Verify your itemized ingredients, delivery coordinates, and complete payment via Razorpay.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '2.5rem' }} className="summary-layout">
          {/* Left Column: Delivery Address & Cart Items */}
          <div>
            {/* Delivery Destination Card */}
            <div
              className="card"
              style={{
                marginBottom: '1.5rem',
                border: '1px solid var(--border-light)',
                padding: '1.5rem'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '1.35rem' }}>📍</span>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 800 }}>Delivery Destination</h3>
                <span
                  style={{
                    marginLeft: 'auto',
                    fontSize: '0.75rem',
                    color: 'var(--accent-basil)',
                    fontWeight: 700,
                    background: 'var(--accent-basil-light)',
                    padding: '0.2rem 0.6rem',
                    borderRadius: 'var(--radius-full)'
                  }}
                >
                  ⚡ 25-30 Mins Express
                </span>
              </div>
              <input
                type="text"
                value={deliveryAddress}
                onChange={(e) => setDeliveryAddress(e.target.value)}
                className="form-input"
                style={{ fontSize: '0.9rem', color: 'var(--text-main)', fontWeight: 500 }}
              />
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
                Contact Recipient: <strong>{user?.name || 'Customer'}</strong> ({user?.email || 'Logged in account'})
              </div>
            </div>

            {/* Itemized Cart List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {cartItems.map((item) => (
                <div
                  key={item.id}
                  className="card"
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.85rem',
                    padding: '1.5rem',
                    border: '1px solid var(--border-light)',
                    boxShadow: 'var(--shadow-sm)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                        <div
                          style={{
                            width: '56px',
                            height: '56px',
                            borderRadius: 'var(--radius-md)',
                            overflow: 'hidden',
                            flexShrink: 0,
                            border: '1.5px solid var(--border-light)',
                            background: '#1E1F24',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
                          }}
                        >
                          <img
                            src={getPizzaImage(item.image || item.name)}
                            alt={item.name}
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            onError={(e) => {
                              e.target.style.display = 'none';
                            }}
                          />
                        </div>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>{item.name}</h3>
                            {item.isCustom && (
                              <span
                                className="badge badge-warning"
                                style={{ fontSize: '0.68rem', padding: '0.2rem 0.5rem' }}
                              >
                                Custom Build
                              </span>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Ingredient Breakdown Chips */}
                      {item.isCustom && item.breakdown && (
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: '0.65rem' }}>
                          <span
                            style={{
                              fontSize: '0.75rem',
                              background: '#F1F5F9',
                              padding: '0.2rem 0.55rem',
                              borderRadius: 'var(--radius-sm)',
                              fontWeight: 600,
                              color: '#334155'
                            }}
                          >
                            🌾 {item.breakdown.base?.name}
                          </span>
                          <span
                            style={{
                              fontSize: '0.75rem',
                              background: '#FEE2E2',
                              padding: '0.2rem 0.55rem',
                              borderRadius: 'var(--radius-sm)',
                              fontWeight: 600,
                              color: '#991B1B'
                            }}
                          >
                            🥫 {item.breakdown.sauce?.name}
                          </span>
                          <span
                            style={{
                              fontSize: '0.75rem',
                              background: '#FEF3C7',
                              padding: '0.2rem 0.55rem',
                              borderRadius: 'var(--radius-sm)',
                              fontWeight: 600,
                              color: '#92400E'
                            }}
                          >
                            🧀 {item.breakdown.cheese?.name}
                          </span>
                          {item.breakdown.veggies?.map((v) => (
                            <span
                              key={v._id}
                              style={{
                                fontSize: '0.75rem',
                                background: '#ECFDF5',
                                padding: '0.2rem 0.55rem',
                                borderRadius: 'var(--radius-sm)',
                                fontWeight: 600,
                                color: '#065F46'
                              }}
                            >
                              🥦 {v.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>

                    <button
                      onClick={() => removeItem(item.id)}
                      style={{
                        color: 'var(--status-danger)',
                        fontSize: '0.85rem',
                        fontWeight: 600,
                        padding: '0.25rem 0.65rem',
                        minHeight: '34px',
                        borderRadius: 'var(--radius-sm)',
                        background: '#FEE2E2'
                      }}
                      title="Remove item"
                    >
                      ✕ Remove
                    </button>
                  </div>

                  {/* Quantity & Pricing Row */}
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      borderTop: '1px solid var(--border-subtle)',
                      paddingTop: '0.85rem',
                      marginTop: '0.25rem'
                    }}
                  >
                    {/* Quantity controls */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                      <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                        Quantity:
                      </span>
                      <button
                        onClick={() => updateQuantity(item.id, -1)}
                        className="btn-outline"
                        style={{ minHeight: '34px', width: '34px', padding: 0, fontWeight: 700 }}
                      >
                        -
                      </button>
                      <span style={{ fontWeight: 800, minWidth: '26px', textAlign: 'center', fontSize: '1rem' }}>
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => updateQuantity(item.id, 1)}
                        className="btn-outline"
                        style={{ minHeight: '34px', width: '34px', padding: 0, fontWeight: 700 }}
                      >
                        +
                      </button>
                    </div>

                    {/* Subtotal */}
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>₹{item.unitPrice} each</div>
                      <div
                        style={{
                          fontSize: '1.35rem',
                          fontWeight: 900,
                          color: 'var(--text-main)',
                          fontFamily: 'var(--font-heading)'
                        }}
                      >
                        ₹{item.unitPrice * item.quantity}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Link
                to="/"
                style={{
                  color: 'var(--accent-tomato)',
                  fontWeight: 700,
                  fontSize: '0.92rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.35rem'
                }}
              >
                <span>←</span> Add more delicious pizzas
              </Link>
              <button
                onClick={clearCart}
                style={{
                  color: 'var(--text-muted)',
                  fontSize: '0.85rem',
                  minHeight: '34px',
                  fontWeight: 600
                }}
              >
                Clear entire cart
              </button>
            </div>
          </div>

          {/* Right Column: Checkout Breakdown Card */}
          <div>
            <div
              className="card"
              style={{
                background: '#FFFFFF',
                position: 'sticky',
                top: '90px',
                padding: '1.75rem',
                border: '1px solid var(--border-light)',
                boxShadow: 'var(--shadow-card)'
              }}
            >
              <h3
                style={{
                  fontSize: '1.35rem',
                  marginBottom: '1.25rem',
                  borderBottom: '1px solid var(--border-light)',
                  paddingBottom: '0.85rem',
                  fontWeight: 800
                }}
              >
                Payment Breakdown
              </h3>

              {/* Coupon Code Input */}
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ fontSize: '0.78rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                  Promo Coupon Code
                </label>
                <form
                  onSubmit={handleApplyCoupon}
                  style={{ display: 'flex', gap: '0.5rem', marginTop: '0.35rem' }}
                >
                  <input
                    type="text"
                    placeholder="Try FORGE20"
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value)}
                    className="form-input"
                    style={{ minHeight: '40px', fontSize: '0.88rem', textTransform: 'uppercase' }}
                  />
                  <button
                    type="submit"
                    className="btn-dark"
                    style={{ minHeight: '40px', padding: '0 1rem', fontSize: '0.85rem', whiteSpace: 'nowrap' }}
                  >
                    Apply
                  </button>
                </form>
                {couponMessage && (
                  <div
                    style={{
                      fontSize: '0.75rem',
                      marginTop: '0.35rem',
                      color: discountPercent > 0 ? 'var(--accent-basil)' : '#DC2626',
                      fontWeight: 600
                    }}
                  >
                    {couponMessage}
                  </div>
                )}
              </div>

              {/* Invoice Lines */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.95rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Item Subtotal</span>
                  <span style={{ fontWeight: 600 }}>₹{cartSubtotal}</span>
                </div>

                {discountAmount > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--accent-basil)' }}>
                    <span>Promo Discount (20%)</span>
                    <span style={{ fontWeight: 700 }}>-₹{discountAmount}</span>
                  </div>
                )}

                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Thermal Packaging</span>
                  <span style={{ color: 'var(--accent-basil)', fontWeight: 600 }}>FREE</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Artisanal Courier</span>
                  <span style={{ color: 'var(--accent-basil)', fontWeight: 600 }}>FREE</span>
                </div>

                <div
                  style={{
                    borderTop: '2px solid var(--border-light)',
                    marginTop: '0.5rem',
                    paddingTop: '0.85rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'baseline'
                  }}
                >
                  <span style={{ fontWeight: 800, fontSize: '1.15rem' }}>Payable Amount</span>
                  <span
                    style={{
                      fontSize: '1.95rem',
                      fontWeight: 900,
                      color: 'var(--accent-tomato)',
                      fontFamily: 'var(--font-heading)'
                    }}
                  >
                    ₹{payableAmount}
                  </span>
                </div>
              </div>

              {/* Secure Notice */}
              <div
                style={{
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.85rem',
                  marginTop: '1.25rem',
                  fontSize: '0.78rem',
                  color: 'var(--text-muted)',
                  lineHeight: 1.5,
                  border: '1px solid var(--border-light)'
                }}
              >
                🔒 <strong>Server Verified Guarantee</strong>: All ingredient prices are strictly re-verified on the server
                before dispatch.
              </div>

              {/* Checkout Button */}
              <button
                onClick={handleProceedToPayment}
                disabled={isProcessing}
                className="btn-primary"
                style={{
                  width: '100%',
                  marginTop: '1.25rem',
                  minHeight: '52px',
                  fontSize: '1.05rem',
                  fontWeight: 800,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.6rem',
                  boxShadow: 'var(--shadow-glow-tomato)'
                }}
              >
                <span>💳</span>
                <span>{isProcessing ? 'Verifying Gateway...' : `Pay ₹${payableAmount} via Razorpay`}</span>
              </button>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  marginTop: '1rem',
                  fontSize: '0.75rem',
                  color: 'var(--text-light)'
                }}
              >
                <span>🛡️ Razorpay Test Sandbox</span>
                <span>•</span>
                <span>HMAC SHA256 Verified</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        @media (max-width: 860px) {
          .summary-layout {
            grid-template-columns: 1fr !important;
          }
        }
      `}</style>
    </div>
  );
};

export default OrderSummary;
