const mongoose = require('mongoose');

const pizzaSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Pizza name is required'],
      trim: true,
      unique: true
    },
    description: {
      type: String,
      required: [true, 'Pizza description is required'],
      trim: true
    },
    image: {
      type: String,
      required: [true, 'Image or visual style identifier is required'],
      default: 'gradient-margherita'
    },
    basePrice: {
      type: Number,
      required: [true, 'Base price is required'],
      min: [0, 'Base price cannot be negative']
    },
    recipe: {
      base: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InventoryItem',
        required: [true, 'Recipe base is required']
      },
      sauce: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InventoryItem',
        required: [true, 'Recipe sauce is required']
      },
      cheese: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InventoryItem',
        required: [true, 'Recipe cheese is required']
      },
      veggies: [
        {
          type: mongoose.Schema.Types.ObjectId,
          ref: 'InventoryItem'
        }
      ]
    }
  },
  {
    timestamps: true
  }
);

module.exports = mongoose.model('Pizza', pizzaSchema);
