const Pizza = require('../models/Pizza');
const InventoryItem = require('../models/InventoryItem');

/**
 * Validates inventory availability and strictly computes price on the server.
 * Never trusts prices or recipe definitions provided by the client.
 */
const calculateAndValidateOrder = async (clientItems) => {
  if (!Array.isArray(clientItems) || clientItems.length === 0) {
    throw new Error('Order must contain at least one pizza item');
  }

  const validatedItems = [];
  let calculatedTotal = 0;

  for (let i = 0; i < clientItems.length; i++) {
    const item = clientItems[i];
    const quantity = parseInt(item.quantity || 1, 10);
    if (isNaN(quantity) || quantity < 1) {
      throw new Error(`Invalid quantity for item #${i + 1}`);
    }

    if (item.pizzaId) {
      // Predefined Pizza Variety
      const pizza = await Pizza.findById(item.pizzaId).populate([
        'recipe.base',
        'recipe.sauce',
        'recipe.cheese',
        'recipe.veggies'
      ]);

      if (!pizza) {
        throw new Error(`Pizza with ID ${item.pizzaId} does not exist`);
      }

      // Check stock availability
      const requiredIngredients = [
        pizza.recipe.base,
        pizza.recipe.sauce,
        pizza.recipe.cheese,
        ...(pizza.recipe.veggies || [])
      ];

      for (const ing of requiredIngredients) {
        if (!ing || ing.stock < quantity) {
          throw new Error(
            `"${pizza.name}" is currently out of stock (insufficient "${ing ? ing.name : 'ingredient'}")`
          );
        }
      }

      const unitPrice = pizza.basePrice;
      calculatedTotal += unitPrice * quantity;

      validatedItems.push({
        pizza: pizza._id,
        name: pizza.name,
        isCustom: false,
        base: pizza.recipe.base._id,
        sauce: pizza.recipe.sauce._id,
        cheese: pizza.recipe.cheese._id,
        veggies: (pizza.recipe.veggies || []).map((v) => v._id),
        quantity,
        unitPrice
      });
    } else {
      // Custom Pizza
      if (!item.base || !item.sauce || !item.cheese) {
        throw new Error(`Custom pizza #${i + 1} requires a base, sauce, and cheese selection`);
      }

      const [baseDoc, sauceDoc, cheeseDoc] = await Promise.all([
        InventoryItem.findById(item.base),
        InventoryItem.findById(item.sauce),
        InventoryItem.findById(item.cheese)
      ]);

      if (!baseDoc || baseDoc.category !== 'base') {
        throw new Error('Invalid pizza base selected');
      }
      if (!sauceDoc || sauceDoc.category !== 'sauce') {
        throw new Error('Invalid pizza sauce selected');
      }
      if (!cheeseDoc || cheeseDoc.category !== 'cheese') {
        throw new Error('Invalid cheese selected');
      }

      if (baseDoc.stock < quantity) throw new Error(`Base "${baseDoc.name}" is out of stock`);
      if (sauceDoc.stock < quantity) throw new Error(`Sauce "${sauceDoc.name}" is out of stock`);
      if (cheeseDoc.stock < quantity) throw new Error(`Cheese "${cheeseDoc.name}" is out of stock`);

      let veggieDocs = [];
      if (Array.isArray(item.veggies) && item.veggies.length > 0) {
        veggieDocs = await InventoryItem.find({
          _id: { $in: item.veggies },
          category: 'veggie'
        });

        if (veggieDocs.length !== item.veggies.length) {
          throw new Error('One or more selected vegetables are invalid');
        }

        for (const v of veggieDocs) {
          if (v.stock < quantity) {
            throw new Error(`Vegetable "${v.name}" is out of stock`);
          }
        }
      }

      // Calculate unit price from individual fresh ingredients
      const unitPrice =
        baseDoc.price +
        sauceDoc.price +
        cheeseDoc.price +
        veggieDocs.reduce((acc, v) => acc + v.price, 0);

      calculatedTotal += unitPrice * quantity;

      validatedItems.push({
        pizza: null,
        name: item.name || 'Custom Crafted Pizza',
        isCustom: true,
        base: baseDoc._id,
        sauce: sauceDoc._id,
        cheese: cheeseDoc._id,
        veggies: veggieDocs.map((v) => v._id),
        quantity,
        unitPrice
      });
    }
  }

  return {
    validatedItems,
    totalAmount: calculatedTotal
  };
};

module.exports = { calculateAndValidateOrder };
