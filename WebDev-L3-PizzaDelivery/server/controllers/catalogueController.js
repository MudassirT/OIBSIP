const Pizza = require('../models/Pizza');
const InventoryItem = require('../models/InventoryItem');

/**
 * Get all predefined pizza varieties with calculated stock availability
 */
const getAllPizzas = async (req, res, next) => {
  try {
    const pizzas = await Pizza.find().populate([
      { path: 'recipe.base', select: 'name category price stock' },
      { path: 'recipe.sauce', select: 'name category price stock' },
      { path: 'recipe.cheese', select: 'name category price stock' },
      { path: 'recipe.veggies', select: 'name category price stock' }
    ]);

    const result = pizzas.map((p) => {
      const required = [p.recipe.base, p.recipe.sauce, p.recipe.cheese, ...(p.recipe.veggies || [])];
      // A pizza is in stock if every ingredient has stock >= 1
      const isAvailable = required.every((item) => item && item.stock > 0);

      return {
        _id: p._id,
        name: p.name,
        description: p.description,
        image: p.image,
        basePrice: p.basePrice,
        isAvailable,
        recipe: {
          base: p.recipe.base
            ? { _id: p.recipe.base._id, name: p.recipe.base.name, price: p.recipe.base.price }
            : null,
          sauce: p.recipe.sauce
            ? { _id: p.recipe.sauce._id, name: p.recipe.sauce.name, price: p.recipe.sauce.price }
            : null,
          cheese: p.recipe.cheese
            ? { _id: p.recipe.cheese._id, name: p.recipe.cheese.name, price: p.recipe.cheese.price }
            : null,
          veggies: (p.recipe.veggies || []).map((v) => ({
            _id: v._id,
            name: v.name,
            price: v.price
          }))
        }
      };
    });

    res.status(200).json({
      success: true,
      count: result.length,
      pizzas: result
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get public-safe inventory options for the custom pizza builder
 * Categorized and exposes availability without exposing exact warehouse quantities
 */
const getInventoryOptions = async (req, res, next) => {
  try {
    const items = await InventoryItem.find().sort({ price: 1 });

    const grouped = {
      bases: [],
      sauces: [],
      cheeses: [],
      veggies: []
    };

    items.forEach((item) => {
      const safeItem = {
        _id: item._id,
        name: item.name,
        category: item.category,
        price: item.price,
        isAvailable: item.stock > 0
      };

      if (item.category === 'base') grouped.bases.push(safeItem);
      else if (item.category === 'sauce') grouped.sauces.push(safeItem);
      else if (item.category === 'cheese') grouped.cheeses.push(safeItem);
      else if (item.category === 'veggie') grouped.veggies.push(safeItem);
    });

    res.status(200).json({
      success: true,
      options: grouped
    });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getAllPizzas,
  getInventoryOptions
};
