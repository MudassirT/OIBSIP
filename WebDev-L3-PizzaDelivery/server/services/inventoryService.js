const InventoryItem = require('../models/InventoryItem');
const { sendLowStockAlertEmail } = require('./emailService');
const config = require('../config/env');

/**
 * Extracts aggregate ingredient requirements for an order
 */
const getOrderIngredientMap = (order) => {
  const map = new Map(); // id -> { id, qty, name }

  for (const item of order.items) {
    const qty = item.quantity;
    const ingredients = [item.base, item.sauce, item.cheese, ...(item.veggies || [])];

    for (const ing of ingredients) {
      if (!ing) continue;
      const idStr = (ing._id || ing).toString();
      const current = map.get(idStr) || 0;
      map.set(idStr, current + qty);
    }
  }

  return map;
};

/**
 * Atomically decrements stock for all ingredients in an order.
 * If any ingredient is insufficient, rolls back previous decrements.
 */
const decrementOrderStockAtomic = async (order) => {
  const ingredientMap = getOrderIngredientMap(order);
  const decrementedHistory = []; // { id, qty }

  try {
    for (const [ingredientId, requiredQty] of ingredientMap.entries()) {
      const updated = await InventoryItem.findOneAndUpdate(
        { _id: ingredientId, stock: { $gte: requiredQty } },
        { $inc: { stock: -requiredQty } },
        { new: true }
      );

      if (!updated) {
        // Find item name for detailed error message
        const failedItem = await InventoryItem.findById(ingredientId);
        const itemName = failedItem ? failedItem.name : 'Unknown ingredient';
        console.warn(`Atomic stock decrement failed: insufficient stock for "${itemName}". Rolling back...`);

        // Roll back previously decremented items
        for (const history of decrementedHistory) {
          await InventoryItem.findByIdAndUpdate(history.id, {
            $inc: { stock: history.qty }
          });
        }

        return {
          success: false,
          error: `Insufficient stock for ingredient "${itemName}". Order could not be fulfilled.`
        };
      }

      decrementedHistory.push({ id: ingredientId, qty: requiredQty, item: updated });
    }

    // Check if any decremented item fell below threshold
    const lowStockNewlyTriggered = [];
    for (const h of decrementedHistory) {
      if (h.item.stock < h.item.threshold && !h.item.lowStockNotified) {
        lowStockNewlyTriggered.push(h.item);
        // Mark lowStockNotified to avoid duplicate spam
        await InventoryItem.findByIdAndUpdate(h.item._id, { lowStockNotified: true });
      }
    }

    if (lowStockNewlyTriggered.length > 0) {
      console.log(`Order completed and dropped ${lowStockNewlyTriggered.length} items below safety threshold. Triggering alert...`);
      sendLowStockAlertEmail(config.admin.email, lowStockNewlyTriggered).catch((err) =>
        console.error('Immediate low stock email error:', err.message)
      );
    }

    return { success: true };
  } catch (error) {
    console.error('Error during atomic stock decrement:', error);
    // Roll back on unhandled error
    for (const history of decrementedHistory) {
      await InventoryItem.findByIdAndUpdate(history.id, {
        $inc: { stock: history.qty }
      }).catch((e) => console.error('Rollback failure:', e.message));
    }
    return { success: false, error: error.message };
  }
};

/**
 * Checks for low stock items and dispatches an alert email to the admin.
 * If force=true, ignores the lowStockNotified flag (e.g. for manual admin trigger).
 */
const checkAndNotifyLowStock = async (force = false) => {
  try {
    const query = {
      $expr: { $lt: ['$stock', '$threshold'] }
    };

    if (!force) {
      query.lowStockNotified = false;
    }

    const lowStockItems = await InventoryItem.find(query);

    if (lowStockItems.length === 0) {
      console.log('Low-stock check: All items are healthy above threshold.');
      return { count: 0, items: [], emailSent: false };
    }

    console.log(`Low-stock check: Found ${lowStockItems.length} item(s) below threshold. Dispatching email to ${config.admin.email}`);
    await sendLowStockAlertEmail(config.admin.email, lowStockItems);

    // Mark items as notified
    const ids = lowStockItems.map((i) => i._id);
    await InventoryItem.updateMany({ _id: { $in: ids } }, { $set: { lowStockNotified: true } });

    return {
      count: lowStockItems.length,
      items: lowStockItems,
      emailSent: true
    };
  } catch (error) {
    console.error('Error in checkAndNotifyLowStock:', error);
    throw error;
  }
};

/**
 * Updates an inventory item's stock or threshold.
 * Resets lowStockNotified to false if stock is restored above threshold.
 */
const updateItemStock = async (itemId, { stock, addStock, threshold }) => {
  const item = await InventoryItem.findById(itemId);
  if (!item) {
    throw new Error('Inventory item not found');
  }

  if (stock !== undefined) {
    const parsedStock = parseInt(stock, 10);
    if (isNaN(parsedStock) || parsedStock < 0) {
      throw new Error('Stock must be a non-negative integer');
    }
    item.stock = parsedStock;
  }

  if (addStock !== undefined) {
    const parsedAdd = parseInt(addStock, 10);
    if (isNaN(parsedAdd) || parsedAdd < 0) {
      throw new Error('Add stock quantity must be a non-negative integer');
    }
    item.stock += parsedAdd;
  }

  if (threshold !== undefined) {
    const parsedThresh = parseInt(threshold, 10);
    if (isNaN(parsedThresh) || parsedThresh < 0) {
      throw new Error('Threshold must be a non-negative integer');
    }
    item.threshold = parsedThresh;
  }

  // If restored above or equal to threshold, reset notification flag
  if (item.stock >= item.threshold) {
    item.lowStockNotified = false;
  }

  await item.save();
  return item;
};

module.exports = {
  decrementOrderStockAtomic,
  checkAndNotifyLowStock,
  updateItemStock
};
