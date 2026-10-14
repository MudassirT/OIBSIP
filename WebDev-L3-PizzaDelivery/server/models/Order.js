const mongoose = require('mongoose');

const orderItemSchema = new mongoose.Schema(
  {
    pizza: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Pizza',
      default: null
    },
    name: {
      type: String,
      required: true
    },
    isCustom: {
      type: Boolean,
      default: false
    },
    base: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InventoryItem',
      required: true
    },
    sauce: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InventoryItem',
      required: true
    },
    cheese: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InventoryItem',
      required: true
    },
    veggies: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InventoryItem'
      }
    ],
    quantity: {
      type: Number,
      required: true,
      min: [1, 'Quantity must be at least 1'],
      default: 1
    },
    unitPrice: {
      type: Number,
      required: true,
      min: 0
    }
  },
  { _id: true }
);

const orderSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true
    },
    items: {
      type: [orderItemSchema],
      validate: [
        function (val) {
          return val && val.length > 0;
        },
        'Order must contain at least one item'
      ]
    },
    totalAmount: {
      type: Number,
      required: true,
      min: 0
    },
    status: {
      type: String,
      enum: ['Order Received', 'In Kitchen', 'Sent to Delivery', 'Delivered'],
      default: 'Order Received'
    },
    payment: {
      razorpayOrderId: {
        type: String,
        default: null
      },
      razorpayPaymentId: {
        type: String,
        default: null
      },
      razorpaySignature: {
        type: String,
        default: null
      },
      status: {
        type: String,
        enum: ['pending', 'paid', 'failed'],
        default: 'pending'
      }
    }
  },
  {
    timestamps: true
  }
);

orderSchema.index({ user: 1, createdAt: -1 });
orderSchema.index({ status: 1 });
orderSchema.index({ 'payment.razorpayOrderId': 1 });

module.exports = mongoose.model('Order', orderSchema);
