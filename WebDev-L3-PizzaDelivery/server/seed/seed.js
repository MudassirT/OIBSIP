const bcrypt = require('bcryptjs');
const mongoose = require('mongoose');
const { connectDB, disconnectDB } = require('../config/db');
const config = require('../config/env');
const User = require('../models/User');
const InventoryItem = require('../models/InventoryItem');
const Pizza = require('../models/Pizza');

const inventoryData = [
  // 5 Pizza Bases
  { name: 'Classic Thin Crust', category: 'base', stock: 100, threshold: 20, price: 60 },
  { name: 'Pan Thick Crust', category: 'base', stock: 100, threshold: 20, price: 80 },
  { name: 'Cheese Burst Crust', category: 'base', stock: 100, threshold: 20, price: 120 },
  { name: 'Whole Wheat Organic', category: 'base', stock: 100, threshold: 20, price: 75 },
  { name: 'Gluten-Free Cauliflower Base', category: 'base', stock: 100, threshold: 20, price: 110 },

  // 5 Sauces
  { name: 'Classic San Marzano Marinara', category: 'sauce', stock: 100, threshold: 20, price: 40 },
  { name: 'Fiery Peri-Peri Sauce', category: 'sauce', stock: 100, threshold: 20, price: 50 },
  { name: 'Creamy Garlic Alfredo', category: 'sauce', stock: 100, threshold: 20, price: 60 },
  { name: 'Smoky Chipotle BBQ', category: 'sauce', stock: 100, threshold: 20, price: 55 },
  { name: 'Artisan Basil Pesto', category: 'sauce', stock: 100, threshold: 20, price: 65 },

  // 4 Cheeses
  { name: 'Fior di Latte Mozzarella', category: 'cheese', stock: 100, threshold: 20, price: 70 },
  { name: 'Sharp Cheddar Blend', category: 'cheese', stock: 100, threshold: 20, price: 85 },
  { name: 'Smoked Gouda', category: 'cheese', stock: 100, threshold: 20, price: 95 },
  { name: 'Plant-Based Vegan Cheese', category: 'cheese', stock: 100, threshold: 20, price: 90 },

  // 8 Vegetables
  { name: 'Crisp Bell Peppers', category: 'veggie', stock: 100, threshold: 20, price: 30 },
  { name: 'Caramelized Red Onions', category: 'veggie', stock: 100, threshold: 20, price: 25 },
  { name: 'Spanish Black Olives', category: 'veggie', stock: 100, threshold: 20, price: 40 },
  { name: 'Pickled Jalapeños', category: 'veggie', stock: 100, threshold: 20, price: 35 },
  { name: 'Fresh Button Mushrooms', category: 'veggie', stock: 100, threshold: 20, price: 45 },
  { name: 'Golden Sweet Corn', category: 'veggie', stock: 100, threshold: 20, price: 30 },
  { name: 'Sun-Ripened Cherry Tomatoes', category: 'veggie', stock: 100, threshold: 20, price: 35 },
  { name: 'Fresh Italian Basil', category: 'veggie', stock: 100, threshold: 20, price: 25 }
];

const seed = async (options = { closeOnFinish: true }) => {
  try {
    console.log('--- Starting PizzaForge Seed Script ---');
    // Only connect if mongoose is not already connected
    if (mongoose.connection.readyState !== 1) {
      await connectDB();
    }

    // 1. Seed or Upsert Admin User
    console.log(`Checking Admin Account (${config.admin.email})...`);
    let admin = await User.findOne({ email: config.admin.email });
    const passwordHash = await bcrypt.hash(config.admin.password, 12);

    if (!admin) {
      admin = await User.create({
        name: config.admin.name,
        email: config.admin.email,
        passwordHash,
        role: 'admin',
        isVerified: true
      });
      console.log(` Created admin account: ${admin.email}`);
    } else {
      admin.role = 'admin';
      admin.isVerified = true;
      admin.passwordHash = passwordHash;
      await admin.save();
      console.log(` Updated existing admin account: ${admin.email}`);
    }

    // 2. Seed Inventory Items (Idempotent: update prices/categories, retain stock or init)
    console.log('Seeding Inventory Items...');
    const itemMap = {};
    for (const item of inventoryData) {
      let existing = await InventoryItem.findOne({ name: item.name });
      if (!existing) {
        existing = await InventoryItem.create(item);
        console.log(`  + Created inventory: [${existing.category}] ${existing.name}`);
      } else {
        existing.category = item.category;
        existing.price = item.price;
        existing.threshold = item.threshold;
        if (existing.stock <= 0) {
          existing.stock = item.stock;
          existing.lowStockNotified = false;
        }
        await existing.save();
        console.log(`  * Updated inventory: [${existing.category}] ${existing.name}`);
      }
      itemMap[item.name] = existing._id;
    }

    // 3. Predefined Pizza Varieties
    console.log('Seeding Predefined Pizzas...');
    const pizzaDefs = [
      {
        name: 'Margherita Supreme',
        description: 'San Marzano marinara, fior di latte mozzarella, cherry tomatoes and fresh Italian basil leaves on a crisp thin crust.',
        image: 'margherita',
        basePrice: 299,
        recipe: {
          base: itemMap['Classic Thin Crust'],
          sauce: itemMap['Classic San Marzano Marinara'],
          cheese: itemMap['Fior di Latte Mozzarella'],
          veggies: [itemMap['Sun-Ripened Cherry Tomatoes'], itemMap['Fresh Italian Basil']]
        }
      },
      {
        name: 'Farmhouse Garden Delight',
        description: 'Loaded with bell peppers, sweet caramelized onions, button mushrooms, and golden corn on a pan thick crust.',
        image: 'farmhouse',
        basePrice: 389,
        recipe: {
          base: itemMap['Pan Thick Crust'],
          sauce: itemMap['Classic San Marzano Marinara'],
          cheese: itemMap['Fior di Latte Mozzarella'],
          veggies: [
            itemMap['Crisp Bell Peppers'],
            itemMap['Caramelized Red Onions'],
            itemMap['Fresh Button Mushrooms'],
            itemMap['Golden Sweet Corn']
          ]
        }
      },
      {
        name: 'Fiery Peri-Peri Kick',
        description: 'Kick-ass spicy peri-peri sauce, sharp cheddar blend, pickled jalapeños, bell peppers, and crisp red onions.',
        image: 'periperi',
        basePrice: 369,
        recipe: {
          base: itemMap['Classic Thin Crust'],
          sauce: itemMap['Fiery Peri-Peri Sauce'],
          cheese: itemMap['Sharp Cheddar Blend'],
          veggies: [
            itemMap['Pickled Jalapeños'],
            itemMap['Crisp Bell Peppers'],
            itemMap['Caramelized Red Onions']
          ]
        }
      },
      {
        name: 'Smoky Chipotle BBQ Feast',
        description: 'Rich smoky BBQ sauce, smoked gouda cheese, sweet corn, crisp onions, and vibrant bell peppers.',
        image: 'bbq',
        basePrice: 419,
        recipe: {
          base: itemMap['Classic Thin Crust'],
          sauce: itemMap['Smoky Chipotle BBQ'],
          cheese: itemMap['Smoked Gouda'],
          veggies: [
            itemMap['Golden Sweet Corn'],
            itemMap['Caramelized Red Onions'],
            itemMap['Crisp Bell Peppers']
          ]
        }
      },
      {
        name: 'Creamy Garlic Mushroom Bliss',
        description: 'Cheese burst crust laden with rich garlic alfredo sauce, mozzarella, sautéed button mushrooms, and Spanish olives.',
        image: 'garlic-bliss',
        basePrice: 449,
        recipe: {
          base: itemMap['Cheese Burst Crust'],
          sauce: itemMap['Creamy Garlic Alfredo'],
          cheese: itemMap['Fior di Latte Mozzarella'],
          veggies: [
            itemMap['Fresh Button Mushrooms'],
            itemMap['Spanish Black Olives'],
            itemMap['Sun-Ripened Cherry Tomatoes']
          ]
        }
      },
      {
        name: 'Artisan Pesto Rustic',
        description: 'Stone-ground whole wheat crust, fragrant basil pesto, fior di latte mozzarella, cherry tomatoes, and black olives.',
        image: 'pesto-garden',
        basePrice: 399,
        recipe: {
          base: itemMap['Whole Wheat Organic'],
          sauce: itemMap['Artisan Basil Pesto'],
          cheese: itemMap['Fior di Latte Mozzarella'],
          veggies: [
            itemMap['Sun-Ripened Cherry Tomatoes'],
            itemMap['Spanish Black Olives'],
            itemMap['Fresh Italian Basil']
          ]
        }
      }
    ];

    for (const p of pizzaDefs) {
      const existing = await Pizza.findOne({ name: p.name });
      if (!existing) {
        await Pizza.create(p);
        console.log(`  + Created pizza: ${p.name}`);
      } else {
        existing.description = p.description;
        existing.image = p.image;
        existing.basePrice = p.basePrice;
        existing.recipe = p.recipe;
        await existing.save();
        console.log(`  * Updated pizza: ${p.name}`);
      }
    }

    console.log('--- Seed completed successfully! ---');
    console.log(`Admin account is ready: ${config.admin.email}`);
  } catch (error) {
    console.error('Seed script error:', error);
    if (options.closeOnFinish) process.exit(1);
    throw error;
  } finally {
    if (options.closeOnFinish) {
      await disconnectDB();
    }
  }
};

if (require.main === module) {
  seed({ closeOnFinish: true }).then(() => process.exit(0));
}

module.exports = seed;

