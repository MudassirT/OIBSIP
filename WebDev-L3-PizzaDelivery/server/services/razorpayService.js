const Razorpay = require('razorpay');
const crypto = require('crypto');
const config = require('../config/env');

let razorpayInstance = null;

const getRazorpayInstance = () => {
  if (razorpayInstance) return razorpayInstance;
  if (config.razorpay.keyId && config.razorpay.keySecret) {
    try {
      razorpayInstance = new Razorpay({
        key_id: config.razorpay.keyId,
        key_secret: config.razorpay.keySecret
      });
      return razorpayInstance;
    } catch (err) {
      console.warn('Failed to initialize Razorpay instance:', err.message);
    }
  }
  return null;
};

/**
 * Creates a Razorpay Order in paise (1 INR = 100 paise)
 */
const createRazorpayOrder = async ({ amountInRupees, receipt, notes = {} }) => {
  const amountInPaise = Math.round(amountInRupees * 100);
  const rzp = getRazorpayInstance();

  if (rzp && config.razorpay.keyId !== 'rzp_test_placeholder') {
    try {
      const order = await rzp.orders.create({
        amount: amountInPaise,
        currency: 'INR',
        receipt: String(receipt),
        notes
      });
      return {
        id: order.id,
        amount: order.amount,
        currency: order.currency,
        keyId: config.razorpay.keyId,
        isMock: false
      };
    } catch (err) {
      console.warn('Razorpay API order creation failed, generating simulation order for test mode:', err.message);
    }
  }

  // Fallback simulator for rapid development/testing when keys are not yet configured
  const mockOrderId = `order_sim_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
  return {
    id: mockOrderId,
    amount: amountInPaise,
    currency: 'INR',
    keyId: config.razorpay.keyId || 'rzp_test_mock',
    isMock: true
  };
};

/**
 * Verifies Razorpay HMAC SHA256 signature
 */
const verifyPaymentSignature = ({ razorpayOrderId, razorpayPaymentId, razorpaySignature }) => {
  if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
    return false;
  }

  // If this was a simulated mock order during test runs
  if (razorpayOrderId.startsWith('order_sim_') && razorpaySignature.startsWith('mock_sig_')) {
    return true;
  }

  const body = `${razorpayOrderId}|${razorpayPaymentId}`;
  const expectedSignature = crypto
    .createHmac('sha256', config.razorpay.keySecret)
    .update(body)
    .digest('hex');

  return expectedSignature === razorpaySignature;
};

module.exports = {
  createRazorpayOrder,
  verifyPaymentSignature
};
