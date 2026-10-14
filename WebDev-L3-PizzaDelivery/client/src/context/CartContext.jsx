import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState(() => {
    try {
      const stored = localStorage.getItem('pizzaforge_cart');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('pizzaforge_cart', JSON.stringify(cartItems));
    } catch (e) {
      console.warn('Failed to persist cart items:', e);
    }
  }, [cartItems]);

  const addPredefinedPizza = (pizza) => {
    setCartItems((prev) => {
      const existingIndex = prev.findIndex((item) => !item.isCustom && item.pizzaId === pizza._id);
      if (existingIndex > -1) {
        const updated = [...prev];
        updated[existingIndex].quantity += 1;
        return updated;
      }
      return [
        ...prev,
        {
          id: `predef_${pizza._id}_${Date.now()}`,
          pizzaId: pizza._id,
          name: pizza.name,
          isCustom: false,
          image: pizza.image,
          unitPrice: pizza.basePrice,
          quantity: 1,
          recipe: pizza.recipe
        }
      ];
    });
  };

  const addCustomPizza = (customPizza) => {
    // customPizza: { name, base, sauce, cheese, veggies, unitPrice, breakdown }
    setCartItems((prev) => [
      ...prev,
      {
        id: `custom_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
        pizzaId: null,
        name: customPizza.name || 'Artisan Custom Pizza',
        isCustom: true,
        base: customPizza.base._id,
        sauce: customPizza.sauce._id,
        cheese: customPizza.cheese._id,
        veggies: (customPizza.veggies || []).map((v) => v._id),
        breakdown: {
          base: customPizza.base,
          sauce: customPizza.sauce,
          cheese: customPizza.cheese,
          veggies: customPizza.veggies
        },
        unitPrice: customPizza.unitPrice,
        quantity: 1
      }
    ]);
  };

  const updateQuantity = (itemId, delta) => {
    setCartItems((prev) =>
      prev
        .map((item) => {
          if (item.id === itemId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean)
    );
  };

  const removeItem = (itemId) => {
    setCartItems((prev) => prev.filter((item) => item.id !== itemId));
  };

  const clearCart = () => {
    setCartItems([]);
    localStorage.removeItem('pizzaforge_cart');
  };

  const totalItemsCount = cartItems.reduce((acc, item) => acc + item.quantity, 0);
  const cartSubtotal = cartItems.reduce((acc, item) => acc + item.unitPrice * item.quantity, 0);

  return (
    <CartContext.Provider
      value={{
        cartItems,
        addPredefinedPizza,
        addCustomPizza,
        updateQuantity,
        removeItem,
        clearCart,
        totalItemsCount,
        cartSubtotal
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
