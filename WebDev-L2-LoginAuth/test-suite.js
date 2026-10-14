/**
 * Comprehensive Automated Verification Suite for SecureGate
 * Tests all requirements, status codes, security controls, and edge cases.
 */

const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:3000';

async function runTests() {
  console.log('====================================================');
  console.log('  STARTING SECUREGATE AUTOMATED VERIFICATION SUITE  ');
  console.log('====================================================\n');

  let passedCount = 0;
  let totalCount = 0;

  function assert(condition, testName) {
    totalCount++;
    if (condition) {
      console.log(`[PASS] ${testName}`);
      passedCount++;
    } else {
      console.error(`[FAIL] ${testName}`);
    }
  }

  // Clear data/users.json before running test suite to start with a fresh slate
  const usersPath = path.join(__dirname, 'data', 'users.json');
  fs.writeFileSync(usersPath, '[]', 'utf8');

  // Test 1: Register with valid details
  console.log('\n--- Section 1: Registration Validation & Hashing ---');
  let regRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'mudassir_admin',
      email: 'mudassir@example.com',
      password: 'SecurePassword123',
      confirmPassword: 'SecurePassword123'
    })
  });
  let regData = await regRes.json();
  assert(regRes.status === 201, 'Test 1.1: Valid registration returns 201 Created');
  assert(regData.success === true, 'Test 1.2: Returns success flag');

  // Verify users.json storage and hashing
  const usersData = JSON.parse(fs.readFileSync(usersPath, 'utf8'));
  assert(usersData.length === 1, 'Test 1.3: User saved in data/users.json');
  const storedUser = usersData[0];
  assert(storedUser.username === 'mudassir_admin', 'Test 1.4: Username stored correctly');
  assert(storedUser.email === 'mudassir@example.com', 'Test 1.5: Email stored correctly');
  assert(storedUser.passwordHash && storedUser.passwordHash.startsWith('$2'), 'Test 1.6: Password hashed with bcrypt ($2a/b/y)');
  assert(!JSON.stringify(usersData).includes('SecurePassword123'), 'Test 1.7: No plain-text password stored anywhere');

  // Test 2: Password validation (client & server enforced)
  let shortPassRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'test_user2',
      email: 'user2@example.com',
      password: 'Pass1', // < 8 chars
      confirmPassword: 'Pass1'
    })
  });
  assert(shortPassRes.status === 400, 'Test 2.1: Password < 8 characters rejected with 400');

  let noNumPassRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'test_user3',
      email: 'user3@example.com',
      password: 'PasswordWithoutNumber',
      confirmPassword: 'PasswordWithoutNumber'
    })
  });
  assert(noNumPassRes.status === 400, 'Test 2.2: Password with no numbers rejected with 400');

  let passMismatchRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'test_user4',
      email: 'user4@example.com',
      password: 'Password123',
      confirmPassword: 'DifferentPassword123'
    })
  });
  assert(passMismatchRes.status === 400, 'Test 2.3: Password mismatch rejected with 400');

  // Test 3: Duplicate checks
  console.log('\n--- Section 2: Duplicate Checks & Input Sanitization ---');
  let dupUserRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'MUDASSIR_ADMIN', // same in different case
      email: 'different@example.com',
      password: 'ValidPassword1',
      confirmPassword: 'ValidPassword1'
    })
  });
  let dupUserData = await dupUserRes.json();
  assert(dupUserRes.status === 409, 'Test 3.1: Duplicate username (case-insensitive) rejected with 409');
  assert(dupUserData.error.includes('already exists'), 'Test 3.2: Duplicate error message displayed');

  let dupEmailRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'new_unique_user',
      email: 'MUDASSIR@EXAMPLE.COM', // same email in uppercase
      password: 'ValidPassword1',
      confirmPassword: 'ValidPassword1'
    })
  });
  assert(dupEmailRes.status === 409, 'Test 3.3: Duplicate email (case-insensitive) rejected with 409');

  // Test 4: Malformed input / XSS input
  let xssUserRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: '<script>alert(1)</script>',
      email: 'valid@example.com',
      password: 'ValidPassword1',
      confirmPassword: 'ValidPassword1'
    })
  });
  assert(xssUserRes.status === 400, 'Test 4.1: Script tag username rejected by regex validation (400)');

  let badEmailRes = await fetch(`${BASE_URL}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: 'clean_user',
      email: 'notanemailaddress',
      password: 'ValidPassword1',
      confirmPassword: 'ValidPassword1'
    })
  });
  assert(badEmailRes.status === 400, 'Test 4.2: Invalid email rejected with 400');

  // Test 5: Protected Dashboard & API access when unauthenticated
  console.log('\n--- Section 3: Protected Routes & Session Handling ---');
  let unauthMeRes = await fetch(`${BASE_URL}/api/me`);
  assert(unauthMeRes.status === 401, 'Test 5.1: GET /api/me without session returns 401');

  let unauthDashRes = await fetch(`${BASE_URL}/dashboard`, { redirect: 'manual' });
  assert(unauthDashRes.status === 302, 'Test 5.2: GET /dashboard without session redirects (302)');
  assert(unauthDashRes.headers.get('location') === '/login', 'Test 5.3: Redirect targets /login');

  let rootUnauthRes = await fetch(`${BASE_URL}/`, { redirect: 'manual' });
  assert(rootUnauthRes.status === 302 && rootUnauthRes.headers.get('location') === '/login', 'Test 5.4: GET / redirects unauthenticated visitor to /login');

  // Test 6: Login with Incorrect Credentials & Timing Leak Protection
  console.log('\n--- Section 4: Login Verification & Identical Error Handling ---');
  let wrongPassRes = await fetch(`${BASE_URL}/api/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      identifier: 'mudassir_admin',
      password: 'WrongPassword999'
    })
  });
  let wrongPassData = await wrongPassRes.json();
  assert(wrongPassRes.status === 401, 'Test 6.1: Wrong password returns 401');

  let nonExistentRes = await fetch(`${BASE_URL}/api/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      identifier: 'non_existent_user_99999',
      password: 'WrongPassword999'
    })
  });
  let nonExistentData = await nonExistentRes.json();
  assert(nonExistentRes.status === 401, 'Test 6.2: Non-existent user returns 401');
  assert(wrongPassData.error === nonExistentData.error, 'Test 6.3: Non-existent user and wrong password return identical error message');
  assert(wrongPassData.error === 'Invalid credentials. Please try again.', 'Test 6.4: Generic error matches required text');

  // Test 7: Successful Login with Username & Email
  console.log('\n--- Section 5: Successful Login & Authenticated Session ---');
  // Login using email identifier
  let loginRes = await fetch(`${BASE_URL}/api/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      identifier: 'mudassir@example.com',
      password: 'SecurePassword123'
    })
  });
  assert(loginRes.status === 200, 'Test 7.1: Login with email returns 200 OK');
  const setCookie = loginRes.headers.get('set-cookie');
  assert(setCookie && setCookie.includes('securegate.sid'), 'Test 7.2: HTTP-only session cookie returned in headers');
  assert(setCookie.includes('HttpOnly'), 'Test 7.3: Cookie has HttpOnly flag');
  assert(setCookie.includes('SameSite=Lax'), 'Test 7.4: Cookie has SameSite=Lax flag');

  const cookieHeader = setCookie.split(';')[0];

  // Test 8: Authenticated GET /api/me
  let authMeRes = await fetch(`${BASE_URL}/api/me`, {
    headers: { Cookie: cookieHeader }
  });
  let authMeData = await authMeRes.json();
  assert(authMeRes.status === 200, 'Test 8.1: GET /api/me with session returns 200');
  assert(authMeData.user.username === 'mudassir_admin', 'Test 8.2: Returns correct username');
  assert(authMeData.user.email === 'mudassir@example.com', 'Test 8.3: Returns correct email');
  assert(authMeData.user.passwordHash === undefined, 'Test 8.4: Password hash is NEVER returned in response');

  // Test 9: Authenticated GET /dashboard
  let authDashRes = await fetch(`${BASE_URL}/dashboard`, {
    headers: { Cookie: cookieHeader }
  });
  let authDashHtml = await authDashRes.text();
  assert(authDashRes.status === 200, 'Test 9.1: Authenticated user loads /dashboard (200)');
  assert(authDashRes.headers.get('cache-control').includes('no-store'), 'Test 9.2: Dashboard response has Cache-Control: no-store');
  assert(authDashHtml.includes('SecureGate'), 'Test 9.3: Dashboard HTML contains SecureGate app');

  // Test 10: Redirect logged-in user away from /login and /register
  let authLoginVisit = await fetch(`${BASE_URL}/login`, {
    headers: { Cookie: cookieHeader },
    redirect: 'manual'
  });
  assert(authLoginVisit.status === 302 && authLoginVisit.headers.get('location') === '/dashboard', 'Test 10.1: Logged-in user visiting /login redirected to /dashboard');

  let authRegisterVisit = await fetch(`${BASE_URL}/register`, {
    headers: { Cookie: cookieHeader },
    redirect: 'manual'
  });
  assert(authRegisterVisit.status === 302 && authRegisterVisit.headers.get('location') === '/dashboard', 'Test 10.2: Logged-in user visiting /register redirected to /dashboard');

  // Test 11: Logout
  console.log('\n--- Section 6: Logout & Cache Invalidation ---');
  let logoutRes = await fetch(`${BASE_URL}/api/logout`, {
    method: 'POST',
    headers: { Cookie: cookieHeader }
  });
  assert(logoutRes.status === 200, 'Test 11.1: POST /api/logout returns 200 OK');

  let postLogoutMe = await fetch(`${BASE_URL}/api/me`, {
    headers: { Cookie: cookieHeader }
  });
  assert(postLogoutMe.status === 401, 'Test 11.2: After logout, /api/me returns 401');

  let postLogoutDash = await fetch(`${BASE_URL}/dashboard`, {
    headers: { Cookie: cookieHeader },
    redirect: 'manual'
  });
  assert(postLogoutDash.status === 302 && postLogoutDash.headers.get('location') === '/login', 'Test 11.3: After logout, /dashboard redirects to /login');

  // Test 12: Rate Limiting (5 failed attempts per min)
  console.log('\n--- Section 7: In-Memory Rate Limiting ---');
  let rateLimited = false;
  for (let i = 1; i <= 6; i++) {
    let failRes = await fetch(`${BASE_URL}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier: 'test_bruteforce', password: 'bad_password' })
    });
    if (failRes.status === 429) {
      rateLimited = true;
      let failData = await failRes.json();
      assert(failData.error.includes('Too many failed attempts'), `Test 12.1: Rate limit 429 received on attempt ${i}`);
      break;
    }
  }
  assert(rateLimited, 'Test 12.2: Rate limiter successfully triggered after rapid failed attempts');

  console.log('\n====================================================');
  console.log(`  VERIFICATION COMPLETE: ${passedCount}/${totalCount} TESTS PASSED`);
  console.log('====================================================\n');
}

runTests().catch(console.error);
