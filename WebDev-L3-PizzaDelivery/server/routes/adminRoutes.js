const express = require('express');
const {
  getAdminInventory,
  updateAdminInventoryItem,
  getAdminOrders,
  updateOrderStatus,
  triggerManualLowStockCheck
} = require('../controllers/adminController');
const { protect } = require('../middleware/authMiddleware');
const { adminOnly } = require('../middleware/adminMiddleware');

const router = express.Router();

// All routes here strictly require valid JWT and role === 'admin'
router.use(protect);
router.use(adminOnly);

router.get('/inventory', getAdminInventory);
router.patch('/inventory/:id', updateAdminInventoryItem);
router.get('/orders', getAdminOrders);
router.patch('/orders/:id/status', updateOrderStatus);
router.post('/low-stock-check', triggerManualLowStockCheck);

module.exports = router;
