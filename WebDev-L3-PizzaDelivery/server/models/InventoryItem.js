const mongoose = require('mongoose');

const inventoryItemSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Item name is required'],
      trim: true,
      unique: true
    },
    category: {
      type: String,
      enum: {
        values: ['base', 'sauce', 'cheese', 'veggie'],
        message: '{VALUE} is not a valid inventory category'
      },
      required: [true, 'Category is required']
    },
    stock: {
      type: Number,
      required: [true, 'Stock quantity is required'],
      min: [0, 'Stock cannot be negative'],
      default: 0
    },
    threshold: {
      type: Number,
      required: [true, 'Low stock threshold is required'],
      min: [0, 'Threshold cannot be negative'],
      default: 20
    },
    price: {
      type: Number,
      required: [true, 'Unit price is required'],
      min: [0, 'Price cannot be negative']
    },
    lowStockNotified: {
      type: Boolean,
      default: false
    }
  },
  {
    timestamps: true
  }
);

inventoryItemSchema.index({ category: 1, stock: 1 });

module.exports = mongoose.model('InventoryItem', inventoryItemSchema);
