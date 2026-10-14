const InventoryItem = require('../models/InventoryItem');
const Order = require('../models/Order');
const { updateItemStock, checkAndNotifyLowStock } = require('../services/inventoryService');
const { notifyOrderStatusUpdate } = require('../sockets/socketHandler');

const ORDER_STATUS_SEQUENCE = ['Order Received', 'In Kitchen', 'Sent to Delivery', 'Delivered'];

/**
 * Get all inventory items with low-stock flag and summaries
 */
const getAdminInventory = async (req, res, next) => {
  try {
    const items = await InventoryItem.find().sort({ category: 1, name: 1 });

    const lowStockCount = items.filter((item) => item.stock < item.threshold).length;

    res.status(200).json({
      success: true,
      count: items.length,
      lowStockCount,
      items
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update stock count or threshold for a specific inventory item
 */
const updateAdminInventoryItem = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { stock, addStock, threshold } = req.body;

    const updatedItem = await updateItemStock(id, { stock, addStock, threshold });

    res.status(200).json({
      success: true,
      message: `Inventory item "${updatedItem.name}" updated successfully.`,
      item: updatedItem
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get all customer orders with filtering
 */
const getAdminOrders = async (req, res, next) => {
  try {
    const { status } = req.query;
    const filter = {};

    if (status && ORDER_STATUS_SEQUENCE.includes(status)) {
      filter.status = status;
    }

    const orders = await Order.find(filter)
      .sort({ createdAt: -1 })
      .populate('user', 'name email')
      .populate('items.pizza', 'name image')
      .populate('items.base', 'name category price')
      .populate('items.sauce', 'name category price')
      .populate('items.cheese', 'name category price')
      .populate('items.veggies', 'name category price');

    res.status(200).json({
      success: true,
      count: orders.length,
      orders
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update order status with strict forward-only sequence validation
 * Emits real-time socket events to user and admin rooms
 */
const updateOrderStatus = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { status: newStatus } = req.body;

    if (!newStatus || !ORDER_STATUS_SEQUENCE.includes(newStatus)) {
      return res.status(400).json({
        success: false,
        message: `Invalid status. Allowed values: ${ORDER_STATUS_SEQUENCE.join(', ')}`
      });
    }

    const order = await Order.findById(id).populate('user', 'name email');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Order not found.'
      });
    }

    const currentIndex = ORDER_STATUS_SEQUENCE.indexOf(order.status);
    const newIndex = ORDER_STATUS_SEQUENCE.indexOf(newStatus);

    // Enforcement: Only forward transitions allowed (no backwards movement)
    if (newIndex <= currentIndex) {
      return res.status(400).json({
        success: false,
        message: `Cannot revert order status from "${order.status}" to "${newStatus}". Only forward progression is permitted.`
      });
    }

    order.status = newStatus;
    order.updatedAt = new Date();
    await order.save();

    // Broadcast real-time status update to client socket room and admin room
    notifyOrderStatusUpdate(order.user._id, order);

    res.status(200).json({
      success: true,
      message: `Order status advanced to "${newStatus}".`,
      order
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Trigger immediate manual low-stock check and email notification
 */
const triggerManualLowStockCheck = async (req, res, next) => {
  try {
    const result = await checkAndNotifyLowStock(true); // force=true

    res.status(200).json({
      success: true,
      message:
        result.count > 0
          ? `Dispatched low stock alert email for ${result.count} item(s).`
          : 'All inventory items are currently healthy above their safety thresholds.',
      count: result.count,
      items: result.items,
      emailSent: result.emailSent
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getAdminInventory,
  updateAdminInventoryItem,
  getAdminOrders,
  updateOrderStatus,
  triggerManualLowStockCheck
};
