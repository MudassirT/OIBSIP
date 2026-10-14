const Order = require('../models/Order');
const { calculateAndValidateOrder } = require('../utils/priceCalculator');
const { createRazorpayOrder, verifyPaymentSignature } = require('../services/razorpayService');
const { decrementOrderStockAtomic } = require('../services/inventoryService');
const { notifyAdminNewOrder } = require('../sockets/socketHandler');

/**
 * Recalculate price on the server, create Order doc and initialize Razorpay Order
 */
const checkout = async (req, res, next) => {
  try {
    const { items } = req.body;

    if (!items || !Array.isArray(items) || items.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Your cart is empty. Please add pizza items to proceed.'
      });
    }

    // STRICT: Recalculate price entirely on server & verify ingredient availability
    const { validatedItems, totalAmount } = await calculateAndValidateOrder(items);

    // Create preliminary Order record
    const order = await Order.create({
      user: req.user._id,
      items: validatedItems,
      totalAmount,
      status: 'Order Received',
      payment: {
        status: 'pending'
      }
    });

    // Create Razorpay order
    const razorpayOrder = await createRazorpayOrder({
      amountInRupees: totalAmount,
      receipt: order._id.toString(),
      notes: {
        userId: req.user._id.toString(),
        orderId: order._id.toString()
      }
    });

    order.payment.razorpayOrderId = razorpayOrder.id;
    await order.save();

    res.status(200).json({
      success: true,
      message: 'Checkout initialized successfully.',
      orderId: order._id,
      totalAmount,
      razorpayOrder,
      items: validatedItems
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Verify Razorpay payment signature, decrement stock atomically, and confirm order
 * Idempotent: double callback will not decrement stock twice
 */
const verifyPayment = async (req, res, next) => {
  try {
    const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = req.body;

    if (!orderId || !razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
      return res.status(400).json({
        success: false,
        message: 'Payment verification requires orderId, razorpayOrderId, razorpayPaymentId, and razorpaySignature.'
      });
    }

    const order = await Order.findById(orderId).populate([
      'items.base',
      'items.sauce',
      'items.cheese',
      'items.veggies'
    ]);

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found.'
      });
    }

    // Idempotency guard: If order is already paid, return early without re-decrementing
    if (order.payment.status === 'paid') {
      return res.status(200).json({
        success: true,
        message: 'Payment has already been processed for this order.',
        order
      });
    }

    // Verify HMAC SHA256 Signature
    const isValidSignature = verifyPaymentSignature({
      razorpayOrderId,
      razorpayPaymentId,
      razorpaySignature
    });

    if (!isValidSignature) {
      order.payment.status = 'failed';
      await order.save();

      return res.status(400).json({
        success: false,
        message: 'Invalid payment signature. Verification failed.'
      });
    }

    // Atomically decrement stock with automatic rollback
    const decrementResult = await decrementOrderStockAtomic(order);
    if (!decrementResult.success) {
      order.payment.status = 'failed';
      await order.save();

      return res.status(409).json({
        success: false,
        message: decrementResult.error || 'Stock could not be allocated. Order cancelled.'
      });
    }

    // Mark paid and finalized
    order.payment.status = 'paid';
    order.payment.razorpayPaymentId = razorpayPaymentId;
    order.payment.razorpaySignature = razorpaySignature;
    order.payment.razorpayOrderId = razorpayOrderId;
    order.status = 'Order Received';
    await order.save();

    // Populate user details for real-time admin broadcast
    const populatedOrder = await Order.findById(order._id)
      .populate('user', 'name email')
      .populate('items.base', 'name category price')
      .populate('items.sauce', 'name category price')
      .populate('items.cheese', 'name category price')
      .populate('items.veggies', 'name category price');

    // Real-time broadcast to Admin room
    notifyAdminNewOrder(populatedOrder);

    res.status(200).json({
      success: true,
      message: 'Payment verified successfully and order is in the kitchen queue!',
      order: populatedOrder
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get all orders belonging to the logged-in user
 */
const getMyOrders = async (req, res, next) => {
  try {
    const orders = await Order.find({ user: req.user._id })
      .sort({ createdAt: -1 })
      .populate('items.pizza', 'name image')
      .populate('items.base', 'name')
      .populate('items.sauce', 'name')
      .populate('items.cheese', 'name')
      .populate('items.veggies', 'name');

    res.status(200).json({
      success: true,
      count: orders.length,
      orders
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  checkout,
  verifyPayment,
  getMyOrders
};
