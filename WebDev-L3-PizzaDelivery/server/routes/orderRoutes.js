const express = require('express');
const { checkout, verifyPayment, getMyOrders } = require('../controllers/orderController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.post('/checkout', protect, checkout);
router.post('/verify-payment', protect, verifyPayment);
router.get('/mine', protect, getMyOrders);

module.exports = router;
