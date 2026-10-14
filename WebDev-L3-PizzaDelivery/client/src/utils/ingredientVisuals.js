// Ultra-realistic visual metadata, culinary flavor profiles, and sensory notes for PizzaForge Studio

export const INGREDIENT_VISUALS = {
  // --- BASES (DOUGH CRUSTS) ---
  'Thin Crust': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Classic Roman',
    origin: 'Rome, Italy',
    texture: 'Crisp & Airy',
    flour: 'Tipo 00 + Semolina',
    calories: '520 kcal',
    sensory: 'Charred blistered rim, paper-thin center with delicate snap.'
  },
  'Classic Thin Crust': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Classic Roman',
    origin: 'Rome, Italy',
    texture: 'Crisp & Airy',
    flour: 'Tipo 00 + Semolina',
    calories: '520 kcal',
    sensory: 'Charred blistered rim, paper-thin center with delicate snap.'
  },
  'Pan Thick Crust': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Deep Golden Pan',
    origin: 'Chicago Hearth',
    texture: 'Fluffy & Golden',
    flour: 'High-Protein Hard Wheat',
    calories: '610 kcal',
    sensory: 'Crisp olive oil fried base with a fluffy, airy cloud crumb.'
  },
  'Cheese Burst': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Double Stuffed',
    origin: 'House Specialty',
    texture: 'Molten Core',
    flour: 'Rich Butter Dough',
    calories: '780 kcal',
    sensory: 'Crust rim injected with flowing molten cheddar and mozzarella.'
  },
  'Cheese Burst Crust': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Double Stuffed',
    origin: 'House Specialty',
    texture: 'Molten Core',
    flour: 'Rich Butter Dough',
    calories: '780 kcal',
    sensory: 'Crust rim injected with flowing molten cheddar and mozzarella.'
  },
  'Whole Wheat': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Stoneground',
    origin: 'Punjab Organic Farms',
    texture: 'Nutty & Dense',
    flour: '100% Whole Wheat',
    calories: '490 kcal',
    sensory: 'Earthy stoneground grain aroma with hearty rustic bite.'
  },
  'Whole Wheat Organic': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Stoneground',
    origin: 'Punjab Organic Farms',
    texture: 'Nutty & Dense',
    flour: '100% Whole Wheat',
    calories: '490 kcal',
    sensory: 'Earthy stoneground grain aroma with hearty rustic bite.'
  },
  'Sourdough': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: '36-Hr Fermented',
    origin: 'San Francisco Culture',
    texture: 'Chewy & Blistered',
    flour: 'Wild Starter Ferment',
    calories: '510 kcal',
    sensory: 'Tangy fermented sourdough profile with bold charred leopard spots.'
  },
  'Gluten Free': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Grain-Free Certified',
    origin: 'Ancient Grain Mill',
    texture: 'Golden & Crisp',
    flour: 'Almond & Tapioca Flour',
    calories: '460 kcal',
    sensory: 'Light crispy ancient grain crust, certified 100% celiac safe.'
  },
  'Gluten-Free Cauliflower Base': {
    thumbnail: '/images/builder/crust-base.jpg',
    tag: 'Cauliflower Hearth',
    origin: 'Ancient Grain Mill',
    texture: 'Golden & Crisp',
    flour: 'Riced Cauliflower & Almond Flour',
    calories: '440 kcal',
    sensory: 'Low-carb roasted cauliflower base with seasoned oregano snap.'
  },

  // --- SAUCES ---
  'San Marzano Tomato Sauce': {
    thumbnail: '/images/builder/sauce-marinara.jpg',
    tag: 'D.O.P. Certified',
    origin: 'Campania, Italy',
    acidity: 'Low Acid, Sweet',
    herbs: 'Wild Sicilian Oregano',
    calories: '60 kcal',
    sensory: 'Sun-ripened volcanic plum tomatoes, cold-pressed olive oil, sea salt.'
  },
  'Classic San Marzano Marinara': {
    thumbnail: '/images/builder/sauce-marinara.jpg',
    tag: 'D.O.P. Certified',
    origin: 'Campania, Italy',
    acidity: 'Low Acid, Sweet',
    herbs: 'Wild Sicilian Oregano',
    calories: '60 kcal',
    sensory: 'Sun-ripened volcanic plum tomatoes, cold-pressed olive oil, sea salt.'
  },
  'Creamy Garlic Alfredo': {
    thumbnail: '/images/builder/alfredo-layer.jpg',
    tag: 'White Velvet',
    origin: 'Emilia-Romagna, Italy',
    acidity: 'Rich Cream',
    herbs: 'Roasted Garlic Confit',
    calories: '140 kcal',
    sensory: 'Slow-simmered heavy cream, 24-month Parmigiano, sweet roasted garlic.'
  },
  'Spicy Peri Peri Sauce': {
    thumbnail: '/images/builder/periperi-layer.jpg',
    tag: 'African Fire',
    origin: 'Mozambique Chillies',
    acidity: 'Tangy Citrus Heat',
    herbs: 'Bird’s Eye Chilli & Lime',
    calories: '75 kcal',
    sensory: 'Vibrant crushed bird’s eye peppers with citrus zest and smoky paprika.'
  },
  'Fiery Peri-Peri Sauce': {
    thumbnail: '/images/builder/periperi-layer.jpg',
    tag: 'African Fire',
    origin: 'Mozambique Chillies',
    acidity: 'Tangy Citrus Heat',
    herbs: 'Bird’s Eye Chilli & Lime',
    calories: '75 kcal',
    sensory: 'Vibrant crushed bird’s eye peppers with citrus zest and smoky paprika.'
  },
  'Sweet Barbecue Sauce': {
    thumbnail: '/images/builder/bbq-layer.jpg',
    tag: 'Wood-Smoked',
    origin: 'Oak Barrel Aged',
    acidity: 'Sweet & Smoky',
    herbs: 'Smoked Molasses & Cider',
    calories: '95 kcal',
    sensory: 'Dark mahogany glaze, caramelized brown sugar, charred hardwood smoke.'
  },
  'Smoky Chipotle BBQ': {
    thumbnail: '/images/builder/bbq-layer.jpg',
    tag: 'Wood-Smoked',
    origin: 'Oak Barrel Aged',
    acidity: 'Sweet & Smoky',
    herbs: 'Smoked Molasses & Chipotle',
    calories: '95 kcal',
    sensory: 'Dark mahogany glaze, charred hardwood smoke, sweet chipotle kick.'
  },
  'Basil Pesto Sauce': {
    thumbnail: '/images/builder/pesto-layer.jpg',
    tag: 'Ligurian Emerald',
    origin: 'Genoa, Italy',
    acidity: 'Aromatic Herbal',
    herbs: 'Fresh Genovese Basil',
    calories: '160 kcal',
    sensory: 'Stone-crushed fresh basil leaves, pine nuts, pecorino, extra virgin oil.'
  },
  'Artisan Basil Pesto': {
    thumbnail: '/images/builder/pesto-layer.jpg',
    tag: 'Ligurian Emerald',
    origin: 'Genoa, Italy',
    acidity: 'Aromatic Herbal',
    herbs: 'Fresh Genovese Basil',
    calories: '160 kcal',
    sensory: 'Stone-crushed fresh basil leaves, pine nuts, pecorino, extra virgin oil.'
  },

  // --- CHEESES ---
  'Fior di Latte Mozzarella': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Fresh Pulled Curd',
    origin: 'Agerola, Italy',
    melt: 'Stringy & Creamy',
    aging: 'Fresh Daily',
    calories: '220 kcal',
    sensory: 'Pure whole cow’s milk curd, delicate milky flavor with golden crusting.'
  },
  'Sharp Cheddar Blend': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Sharp & Creamy',
    origin: 'Wisconsin, USA',
    melt: 'Gooey Melt',
    aging: 'Aged 12 Months',
    calories: '260 kcal',
    sensory: 'Sharp cheddar punch blended with smooth, creamy buttery Monterey Jack.'
  },
  'Cheddar & Monterey Jack Blend': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Sharp & Creamy',
    origin: 'Wisconsin, USA',
    melt: 'Gooey Melt',
    aging: 'Aged 12 Months',
    calories: '260 kcal',
    sensory: 'Sharp cheddar punch blended with smooth, creamy buttery Monterey Jack.'
  },
  'Smoked Gouda': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Beechwood Smoked',
    origin: 'Gouda, Netherlands',
    melt: 'Silky Stretch',
    aging: 'Smoked 45 Days',
    calories: '240 kcal',
    sensory: 'Subtle beechwood smoke with rich buttery melt and savory depth.'
  },
  'Smoked Provolone': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: 'Beechwood Smoked',
    origin: 'Lombardy, Italy',
    melt: 'Silky Stretch',
    aging: 'Smoked 30 Days',
    calories: '230 kcal',
    sensory: 'Subtle beechwood smoke with pleasant piquant finish and rich pull.'
  },
  'Plant-Based Vegan Cheese': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: '100% Plant-Based',
    origin: 'Artisan Cultured',
    melt: 'Velvety Soft',
    aging: 'Naturally Fermented',
    calories: '180 kcal',
    sensory: 'Cultured organic raw cashew puree, nutritional yeast, clean finish.'
  },
  'Vegan Cashew Cheese': {
    thumbnail: '/images/builder/cheese-melted.jpg',
    tag: '100% Plant-Based',
    origin: 'Artisan Cultured',
    melt: 'Velvety Soft',
    aging: 'Naturally Fermented',
    calories: '180 kcal',
    sensory: 'Cultured organic raw cashew puree, nutritional yeast, clean finish.'
  },

  // --- HARVEST VEGGIES ---
  'Black Olives': {
    thumbnail: '/images/pizzas/farmhouse.jpg',
    tag: 'Kalamata In Brine',
    origin: 'Peloponnese, Greece',
    prep: 'Sliced Coins',
    calories: '35 kcal',
    sensory: 'Plump brine-cured Kalamata olives with salty, fruity umami note.'
  },
  'Spanish Black Olives': {
    thumbnail: '/images/pizzas/farmhouse.jpg',
    tag: 'Kalamata In Brine',
    origin: 'Peloponnese, Greece',
    prep: 'Sliced Coins',
    calories: '35 kcal',
    sensory: 'Plump brine-cured Kalamata olives with salty, fruity umami note.'
  },
  'Bell Peppers': {
    thumbnail: '/images/pizzas/farmhouse.jpg',
    tag: 'Flame-Roasted',
    origin: 'Greenhouse Harvest',
    prep: 'Crisp Ribbons',
    calories: '20 kcal',
    sensory: 'Sweet tricolor red, yellow, and green peppers with fresh crunchy bite.'
  },
  'Crisp Bell Peppers': {
    thumbnail: '/images/pizzas/farmhouse.jpg',
    tag: 'Flame-Roasted',
    origin: 'Greenhouse Harvest',
    prep: 'Crisp Ribbons',
    calories: '20 kcal',
    sensory: 'Sweet tricolor red, yellow, and green peppers with fresh crunchy bite.'
  },
  'Wild Mushrooms': {
    thumbnail: '/images/builder/alfredo-layer.jpg',
    tag: 'Woodland Blend',
    origin: 'Himachal Pine Forests',
    prep: 'Pan-Seared in Thyme',
    calories: '25 kcal',
    sensory: 'Earthy cremini and button mushrooms sautéed with fresh garden herbs.'
  },
  'Fresh Button Mushrooms': {
    thumbnail: '/images/builder/alfredo-layer.jpg',
    tag: 'Woodland Blend',
    origin: 'Himachal Pine Forests',
    prep: 'Pan-Seared in Thyme',
    calories: '25 kcal',
    sensory: 'Earthy cremini and button mushrooms sautéed with fresh garden herbs.'
  },
  'Spicy Jalapenos': {
    thumbnail: '/images/builder/periperi-layer.jpg',
    tag: 'Escabeche Pickled',
    origin: 'Oaxaca, Mexico',
    prep: 'Sliced Rounds',
    calories: '15 kcal',
    sensory: 'Pickled jalapeño rings delivering immediate crisp capsicum heat.'
  },
  'Pickled Jalapeños': {
    thumbnail: '/images/builder/periperi-layer.jpg',
    tag: 'Escabeche Pickled',
    origin: 'Oaxaca, Mexico',
    prep: 'Sliced Rounds',
    calories: '15 kcal',
    sensory: 'Pickled jalapeño rings delivering immediate crisp capsicum heat.'
  },
  'Caramelized Onions': {
    thumbnail: '/images/builder/bbq-layer.jpg',
    tag: 'Slow-Braised',
    origin: 'Balsamic Glaze',
    prep: '45-Min Reduction',
    calories: '40 kcal',
    sensory: 'Slow-simmered red onions melted down into sweet, jammy mahogany ribbons.'
  },
  'Caramelized Red Onions': {
    thumbnail: '/images/builder/bbq-layer.jpg',
    tag: 'Slow-Braised',
    origin: 'Balsamic Glaze',
    prep: '45-Min Reduction',
    calories: '40 kcal',
    sensory: 'Slow-simmered red onions melted down into sweet, jammy mahogany ribbons.'
  },
  'Fresh Basil Leaves': {
    thumbnail: '/images/builder/pesto-layer.jpg',
    tag: 'Sweet Genovese',
    origin: 'Hydroponic Garden',
    prep: 'Whole Hand-Torn',
    calories: '5 kcal',
    sensory: 'Aromatic sweet basil torn fresh post-bake with intense herbal perfume.'
  },
  'Fresh Italian Basil': {
    thumbnail: '/images/builder/pesto-layer.jpg',
    tag: 'Sweet Genovese',
    origin: 'Hydroponic Garden',
    prep: 'Whole Hand-Torn',
    calories: '5 kcal',
    sensory: 'Aromatic sweet basil torn fresh post-bake with intense herbal perfume.'
  },
  'Golden Sweet Corn': {
    thumbnail: '/images/builder/harvest-delight.jpg',
    tag: 'Sun-Drenched',
    origin: 'Organic Corn Belt',
    prep: 'Steamed Sweet Kernels',
    calories: '30 kcal',
    sensory: 'Plump golden sweet corn bursts with juicy natural farm sweetness.'
  },
  'Sun-Ripened Cherry Tomatoes': {
    thumbnail: '/images/builder/harvest-delight.jpg',
    tag: 'Vine-Ripened',
    origin: 'Salerno, Italy',
    prep: 'Halved Jewels',
    calories: '20 kcal',
    sensory: 'Juicy cherry tomato halves releasing sweet burst and vibrant fresh acidity.'
  },
  'Sun-dried Tomatoes': {
    thumbnail: '/images/builder/harvest-delight.jpg',
    tag: 'Tuscan Dried',
    origin: 'Tuscany, Italy',
    prep: 'Julienned Strips',
    calories: '45 kcal',
    sensory: 'Intense concentrated sweet-tart tomato essence cured in olive oil.'
  },
  'Golden Grilled Paneer': {
    thumbnail: '/images/builder/harvest-delight.jpg',
    tag: 'Tandoor Charred',
    origin: 'Farm Milk Curd',
    prep: 'Spiced Cubes',
    calories: '90 kcal',
    sensory: 'Tender cottage cheese cubes marinated in mild yogurt tikka spices.'
  }
};

const DEFAULT_VISUAL = {
  thumbnail: '/images/builder/harvest-delight.jpg',
  tag: 'Artisan Kitchen',
  origin: 'Master Hearth',
  texture: 'Fresh',
  calories: '45 kcal',
  sensory: 'Handpicked artisan ingredient prepared fresh in our kitchen.'
};

export const getIngredientVisual = (name = '') => {
  if (!name) return DEFAULT_VISUAL;
  if (INGREDIENT_VISUALS[name]) return INGREDIENT_VISUALS[name];

  const lower = name.toLowerCase();

  // Search through all keys for substring match
  for (const [key, val] of Object.entries(INGREDIENT_VISUALS)) {
    if (lower.includes(key.toLowerCase()) || key.toLowerCase().includes(lower)) {
      return val;
    }
  }

  // Keywords
  if (lower.includes('wheat')) return INGREDIENT_VISUALS['Whole Wheat'];
  if (lower.includes('burst')) return INGREDIENT_VISUALS['Cheese Burst'];
  if (lower.includes('thin')) return INGREDIENT_VISUALS['Thin Crust'];
  if (lower.includes('pan') || lower.includes('thick')) return INGREDIENT_VISUALS['Pan Thick Crust'];
  if (lower.includes('gluten') || lower.includes('cauliflower')) return INGREDIENT_VISUALS['Gluten Free'];

  if (lower.includes('marinara') || lower.includes('marzano')) return INGREDIENT_VISUALS['San Marzano Tomato Sauce'];
  if (lower.includes('peri')) return INGREDIENT_VISUALS['Spicy Peri Peri Sauce'];
  if (lower.includes('alfredo') || lower.includes('garlic')) return INGREDIENT_VISUALS['Creamy Garlic Alfredo'];
  if (lower.includes('bbq') || lower.includes('chipotle') || lower.includes('barbecue')) return INGREDIENT_VISUALS['Sweet Barbecue Sauce'];
  if (lower.includes('pesto')) return INGREDIENT_VISUALS['Basil Pesto Sauce'];

  if (lower.includes('mozzarella') || lower.includes('fior')) return INGREDIENT_VISUALS['Fior di Latte Mozzarella'];
  if (lower.includes('cheddar')) return INGREDIENT_VISUALS['Sharp Cheddar Blend'];
  if (lower.includes('gouda') || lower.includes('smoked') || lower.includes('provolone')) return INGREDIENT_VISUALS['Smoked Gouda'];
  if (lower.includes('vegan') || lower.includes('plant') || lower.includes('cashew')) return INGREDIENT_VISUALS['Plant-Based Vegan Cheese'];

  if (lower.includes('olive')) return INGREDIENT_VISUALS['Black Olives'];
  if (lower.includes('pepper')) return INGREDIENT_VISUALS['Bell Peppers'];
  if (lower.includes('mushroom')) return INGREDIENT_VISUALS['Wild Mushrooms'];
  if (lower.includes('jalape') || lower.includes('chili')) return INGREDIENT_VISUALS['Spicy Jalapenos'];
  if (lower.includes('onion')) return INGREDIENT_VISUALS['Caramelized Onions'];
  if (lower.includes('basil')) return INGREDIENT_VISUALS['Fresh Basil Leaves'];
  if (lower.includes('corn')) return INGREDIENT_VISUALS['Golden Sweet Corn'];
  if (lower.includes('tomato')) return INGREDIENT_VISUALS['Sun-Ripened Cherry Tomatoes'];
  if (lower.includes('paneer')) return INGREDIENT_VISUALS['Golden Grilled Paneer'];

  return DEFAULT_VISUAL;
};
