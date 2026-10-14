// Utility helper mapping pizza keys, names, and varieties to real photography assets

export const PIZZA_IMAGE_MAP = {
  margherita: '/images/pizzas/margherita.jpg',
  farmhouse: '/images/pizzas/farmhouse.jpg',
  periperi: '/images/pizzas/periperi.jpg',
  bbq: '/images/pizzas/bbq.jpg',
  'garlic-bliss': '/images/pizzas/garlic-bliss.jpg',
  'pesto-garden': '/images/pizzas/pesto-garden.jpg'
};

/**
 * Returns the matching authentic pizza image path based on image slug or pizza name
 * @param {string} identifier - e.g. "margherita", "Farmhouse Garden Delight", etc.
 * @returns {string} Image path
 */
export const getPizzaImage = (identifier) => {
  if (!identifier) return '/images/pizzas/margherita.jpg';
  
  const key = String(identifier).trim().toLowerCase();
  
  // Exact slug match
  if (PIZZA_IMAGE_MAP[key]) {
    return PIZZA_IMAGE_MAP[key];
  }
  
  // Name-based heuristic matches
  if (key.includes('margherita') || key.includes('heritage')) return PIZZA_IMAGE_MAP.margherita;
  if (key.includes('farmhouse') || key.includes('harvest') || key.includes('garden')) return PIZZA_IMAGE_MAP.farmhouse;
  if (key.includes('peri') || key.includes('fiery') || key.includes('chili') || key.includes('spicy')) return PIZZA_IMAGE_MAP.periperi;
  if (key.includes('bbq') || key.includes('chipotle') || key.includes('smoke') || key.includes('paneer')) return PIZZA_IMAGE_MAP.bbq;
  if (key.includes('garlic') || key.includes('mushroom') || key.includes('bliss') || key.includes('truffle')) return PIZZA_IMAGE_MAP['garlic-bliss'];
  if (key.includes('pesto') || key.includes('rustic') || key.includes('ligurian')) return PIZZA_IMAGE_MAP['pesto-garden'];
  
  return '/images/pizzas/margherita.jpg';
};
