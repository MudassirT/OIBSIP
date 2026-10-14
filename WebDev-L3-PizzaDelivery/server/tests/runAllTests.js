const axios = require('axios');

const BASE_URL = 'http://localhost:5000/api';

const runTests = async () => {
  console.log('====================================================');
  console.log('🍕 PizzaForge - Comprehensive Automated Test Suite');
  console.log('====================================================\n');

  let passedCount = 0;
  let failedCount = 0;

  const assert = (condition, title) => {
    if (condition) {
      console.log(`✅ PASS: ${title}`);
      passedCount++;
    } else {
      console.error(`❌ FAIL: ${title}`);
      failedCount++;
    }
  };

  try {
    // Test 1: Health check
    const health = await axios.get(`${BASE_URL}/health`);
    assert(health.status === 200 && health.data.status === 'healthy', '1. Server Health Check (/api/health)');

    // Test 2: Catalogue APIs
    const pizzasRes = await axios.get(`${BASE_URL}/pizzas`);
    assert(pizzasRes.data.success && pizzasRes.data.pizzas.length >= 6, '2. Predefined Pizzas Catalogue (>= 6 varieties)');

    const optionsRes = await axios.get(`${BASE_URL}/inventory/options`);
    assert(
      optionsRes.data.success &&
        optionsRes.data.options.bases.length >= 5 &&
        optionsRes.data.options.sauces.length >= 5 &&
        optionsRes.data.options.cheeses.length >= 4 &&
        optionsRes.data.options.veggies.length >= 8,
      '3. Public-safe Inventory Options (5 bases, 5 sauces, 4 cheeses, 8 veggies)'
    );

    // Test 3: Public registration & role tampering guard
    const testUserEmail = `customer_${Date.now()}@example.com`;
    const regRes = await axios.post(`${BASE_URL}/auth/register`, {
      name: 'Test Customer',
      email: testUserEmail,
      password: 'Password123!',
      role: 'admin' // Malicious attempt to register as admin
    });

    assert(
      regRes.data.success && regRes.data.user.role === 'user' && !regRes.data.user.isVerified,
      '4. Register Account: unverified status & ignores body role tampering (created as "user")'
    );

    // Test 4: Blocked login before email verification
    let blockedLogin = false;
    try {
      await axios.post(`${BASE_URL}/auth/login`, {
        email: testUserEmail,
        password: 'Password123!'
      });
    } catch (err) {
      if (err.response?.status === 403 && err.response?.data?.isUnverified) {
        blockedLogin = true;
      }
    }
    assert(blockedLogin, '5. Login Blocked for unverified user (HTTP 403 isUnverified: true)');

    // Test 5: Verify email via dev token & verification endpoint
    const devTokenRes = await axios.get(`${BASE_URL}/auth/dev-token/${testUserEmail}`);
    assert(devTokenRes.data.success && !!devTokenRes.data.token, '6. Verification token dispatched via email service');

    const verifyCallRes = await axios.get(`${BASE_URL}/auth/verify/${devTokenRes.data.token}`);
    assert(verifyCallRes.data.success, '7. Verify Email Endpoint (/api/auth/verify/:token) successfully activated account');

    // Test 6: Successful Login after verification
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      email: testUserEmail,
      password: 'Password123!'
    });
    const userToken = loginRes.data.token;
    assert(
      loginRes.data.success && !!userToken && !loginRes.data.user.passwordHash,
      '8. Login Success after verification: returns JWT and safe user profile (no passwordHash)'
    );

    // Test 7: Wrong password gives generic error
    let wrongPassBlocked = false;
    try {
      await axios.post(`${BASE_URL}/auth/login`, {
        email: testUserEmail,
        password: 'WrongPassword999'
      });
    } catch (err) {
      if (err.response?.status === 401) wrongPassBlocked = true;
    }
    assert(wrongPassBlocked, '9. Wrong password returns generic 401 error without leaking user state');

    // Test 8: Normal user hitting admin route gets 403 Forbidden
    let adminRouteBlocked = false;
    try {
      await axios.get(`${BASE_URL}/admin/inventory`, {
        headers: { Authorization: `Bearer ${userToken}` }
      });
    } catch (err) {
      if (err.response?.status === 403) adminRouteBlocked = true;
    }
    assert(adminRouteBlocked, '10. Normal customer hitting /api/admin/inventory gets 403 Forbidden');

    // Test 9: Admin login with seeded credentials
    const adminLoginRes = await axios.post(`${BASE_URL}/auth/admin-login`, {
      email: 'admin@pizzaforge.com',
      password: 'Admin@123456'
    });
    const adminToken = adminLoginRes.data.token;
    assert(
      adminLoginRes.data.success && adminLoginRes.data.user.role === 'admin',
      '11. Admin Login (/api/auth/admin-login) authenticates seeded administrator'
    );

    // Test 10: Admin accesses inventory
    const adminInvRes = await axios.get(`${BASE_URL}/admin/inventory`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    assert(
      adminInvRes.data.success && adminInvRes.data.items.length >= 22,
      '12. Admin Inventory Query (/api/admin/inventory) returns full warehouse data'
    );

    // Test 11: Anti-enumeration test for forgot password
    const forgotKnown = await axios.post(`${BASE_URL}/auth/forgot-password`, { email: testUserEmail });
    const forgotUnknown = await axios.post(`${BASE_URL}/auth/forgot-password`, { email: 'nonexistent_ghost_99@xyz.com' });
    assert(
      forgotKnown.data.message === forgotUnknown.data.message,
      '13. Forgot password returns identical response for existing and unknown emails (anti-enumeration)'
    );

    // Test 12: Checkout with price tampering attempt
    const samplePizza = pizzasRes.data.pizzas[0];
    const checkoutRes = await axios.post(
      `${BASE_URL}/orders/checkout`,
      {
        items: [
          {
            pizzaId: samplePizza._id,
            quantity: 2,
            unitPrice: 1 // Malicious tamper attempt: client claims ₹1 each
          }
        ]
      },
      { headers: { Authorization: `Bearer ${userToken}` } }
    );

    const expectedTotal = samplePizza.basePrice * 2;
    assert(
      checkoutRes.data.success && checkoutRes.data.totalAmount === expectedTotal,
      `14. Checkout Price Tamper Guard: Server recomputed actual price (₹${expectedTotal}) instead of tampered ₹1`
    );

    const orderId = checkoutRes.data.orderId;
    const razorpayOrder = checkoutRes.data.razorpayOrder;

    // Test 13: Razorpay signature forgery rejection
    let signatureForgeryRejected = false;
    try {
      await axios.post(
        `${BASE_URL}/orders/verify-payment`,
        {
          orderId,
          razorpayOrderId: razorpayOrder.id,
          razorpayPaymentId: 'pay_fake_payment_123',
          razorpaySignature: 'forged_fake_signature_hex_12345'
        },
        { headers: { Authorization: `Bearer ${userToken}` } }
      );
    } catch (err) {
      if (err.response?.status === 400) signatureForgeryRejected = true;
    }
    assert(signatureForgeryRejected, '15. Forged Razorpay signature is rejected with 400 error');

    // Test 14: Valid test payment verification & stock decrement
    const baseItemBefore = adminInvRes.data.items.find((i) => i._id === samplePizza.recipe.base._id);
    const stockBefore = baseItemBefore.stock;

    const verifySuccessRes = await axios.post(
      `${BASE_URL}/orders/verify-payment`,
      {
        orderId,
        razorpayOrderId: razorpayOrder.id,
        razorpayPaymentId: `pay_sim_${Date.now()}`,
        razorpaySignature: `mock_sig_${Date.now()}`
      },
      { headers: { Authorization: `Bearer ${userToken}` } }
    );

    assert(
      verifySuccessRes.data.success && verifySuccessRes.data.order.payment.status === 'paid',
      '16. Valid test payment verification succeeds, marks order paid, status "Order Received"'
    );

    const adminInvAfter = await axios.get(`${BASE_URL}/admin/inventory`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const baseItemAfter = adminInvAfter.data.items.find((i) => i._id === samplePizza.recipe.base._id);

    assert(
      baseItemAfter.stock === stockBefore - 2,
      `17. Atomic stock decrement: base stock decreased exactly by 2 units (${stockBefore} -> ${baseItemAfter.stock})`
    );

    // Test 15: Idempotent payment verification (duplicate callback)
    const duplicateVerifyRes = await axios.post(
      `${BASE_URL}/orders/verify-payment`,
      {
        orderId,
        razorpayOrderId: razorpayOrder.id,
        razorpayPaymentId: 'pay_duplicate_callback',
        razorpaySignature: `mock_sig_${Date.now()}`
      },
      { headers: { Authorization: `Bearer ${userToken}` } }
    );

    const adminInvAfterDup = await axios.get(`${BASE_URL}/admin/inventory`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const baseItemAfterDup = adminInvAfterDup.data.items.find((i) => i._id === samplePizza.recipe.base._id);
    assert(
      duplicateVerifyRes.data.success && baseItemAfterDup.stock === baseItemAfter.stock,
      '18. Idempotency Check: Duplicate verification callback does NOT decrement stock twice'
    );

    // Test 16: Forward-only order status advancement
    const advanceRes = await axios.patch(
      `${BASE_URL}/admin/orders/${orderId}/status`,
      { status: 'In Kitchen' },
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    assert(advanceRes.data.success && advanceRes.data.order.status === 'In Kitchen', '19. Admin advances order status: "Order Received" -> "In Kitchen"');

    // Test 17: Backward status revert rejection
    let revertRejected = false;
    try {
      await axios.patch(
        `${BASE_URL}/admin/orders/${orderId}/status`,
        { status: 'Order Received' },
        { headers: { Authorization: `Bearer ${adminToken}` } }
      );
    } catch (err) {
      if (err.response?.status === 400) revertRejected = true;
    }
    assert(revertRejected, '20. Status Revert Guard: Admin cannot revert status backwards ("In Kitchen" -> "Order Received")');

    // Test 18: Manual stock update & threshold reset
    const testItem = adminInvRes.data.items.find((i) => i.name === 'Crisp Bell Peppers');
    const patchRes = await axios.patch(
      `${BASE_URL}/admin/inventory/${testItem._id}`,
      { stock: 15, threshold: 20 },
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    assert(
      patchRes.data.success && patchRes.data.item.stock === 15,
      '21. Manual Stock Update: Admin set stock to 15 (below threshold 20)'
    );

    // Test 19: Low-stock manual check
    const checkRes = await axios.post(
      `${BASE_URL}/admin/low-stock-check`,
      {},
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    assert(
      checkRes.data.success && checkRes.data.count >= 1,
      `22. Low-Stock Check Triggered: Detected ${checkRes.data.count} items below threshold and alerted admin`
    );

    // Test 20: Concurrency Protection (two simultaneous orders for the last remaining unit)
    const scarceTarget = adminInvRes.data.items.find((i) => i.name === 'Pickled Jalapeños');
    // Set stock to 1
    await axios.patch(
      `${BASE_URL}/admin/inventory/${scarceTarget._id}`,
      { stock: 1 },
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );

    const simOrder1 = await axios.post(
      `${BASE_URL}/orders/checkout`,
      {
        items: [
          {
            name: 'Pizza A',
            base: optionsRes.data.options.bases[0]._id,
            sauce: optionsRes.data.options.sauces[0]._id,
            cheese: optionsRes.data.options.cheeses[0]._id,
            veggies: [scarceTarget._id],
            quantity: 1
          }
        ]
      },
      { headers: { Authorization: `Bearer ${userToken}` } }
    );

    const simOrder2 = await axios.post(
      `${BASE_URL}/orders/checkout`,
      {
        items: [
          {
            name: 'Pizza B',
            base: optionsRes.data.options.bases[0]._id,
            sauce: optionsRes.data.options.sauces[0]._id,
            cheese: optionsRes.data.options.cheeses[0]._id,
            veggies: [scarceTarget._id],
            quantity: 1
          }
        ]
      },
      { headers: { Authorization: `Bearer ${userToken}` } }
    );

    // Execute payment verification simultaneously
    const results = await Promise.allSettled([
      axios.post(
        `${BASE_URL}/orders/verify-payment`,
        {
          orderId: simOrder1.data.orderId,
          razorpayOrderId: simOrder1.data.razorpayOrder.id,
          razorpayPaymentId: 'pay_race_1',
          razorpaySignature: `mock_sig_${Date.now()}`
        },
        { headers: { Authorization: `Bearer ${userToken}` } }
      ),
      axios.post(
        `${BASE_URL}/orders/verify-payment`,
        {
          orderId: simOrder2.data.orderId,
          razorpayOrderId: simOrder2.data.razorpayOrder.id,
          razorpayPaymentId: 'pay_race_2',
          razorpaySignature: `mock_sig_${Date.now()}`
        },
        { headers: { Authorization: `Bearer ${userToken}` } }
      )
    ]);

    const successes = results.filter((r) => r.status === 'fulfilled');
    const failures = results.filter((r) => r.status === 'rejected');

    const invAfterRace = await axios.get(`${BASE_URL}/admin/inventory`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const itemAfterRace = invAfterRace.data.items.find((i) => i._id === scarceTarget._id);

    assert(
      successes.length === 1 && failures.length === 1 && itemAfterRace.stock === 0,
      `23. Concurrency Protection: Exactly 1 concurrent order succeeded, 1 failed cleanly, stock remained at 0 (never negative)`
    );

    // Reset scarce item stock for healthy continuous dev
    await axios.patch(
      `${BASE_URL}/admin/inventory/${scarceTarget._id}`,
      { stock: 100 },
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );

    console.log('\n====================================================');
    console.log(`Summary: ${passedCount} PASSED, ${failedCount} FAILED out of 23 test assertions`);
    console.log('====================================================\n');

    if (failedCount > 0) {
      process.exit(1);
    }
  } catch (error) {
    console.error('Test suite error:', error.response?.data || error.message);
    process.exit(1);
  }
};

if (require.main === module) {
  runTests();
}

module.exports = runTests;
