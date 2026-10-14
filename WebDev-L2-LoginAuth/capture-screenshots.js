/**
 * Automated Screenshot Capturing Script for SecureGate
 * Uses puppeteer-core with local Google Chrome to capture high-res desktop & mobile screenshots.
 */

const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const SCREENSHOTS_DIR = path.join(__dirname, 'screenshots');
const BASE_URL = 'http://localhost:3000';

async function captureAll() {
  if (!fs.existsSync(SCREENSHOTS_DIR)) {
    fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
  }

  console.log('Launching Chrome to capture screenshots...');
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();

  async function setDesktop() {
    await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 2 });
  }

  async function setTablet() {
    await page.setViewport({ width: 768, height: 1024, deviceScaleFactor: 2 });
  }

  async function setMobile() {
    await page.setViewport({ width: 375, height: 812, deviceScaleFactor: 2, isMobile: true });
  }

  // 1. REGISTER PAGE (Clean)
  console.log('1. Capturing Register Page...');
  await setDesktop();
  await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle0' });
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-register.png'), fullPage: true });

  await setMobile();
  await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle0' });
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-register.png'), fullPage: true });

  // 2. REGISTER VALIDATION ERRORS
  console.log('2. Capturing Register Validation Errors...');
  await setDesktop();
  await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle0' });
  await page.type('#reg-username', 'ab'); // too short
  await page.type('#reg-email', 'invalid-email'); // invalid email format
  await page.type('#reg-password', 'short'); // < 8 chars, no number
  await page.type('#reg-confirm-password', 'mismatch'); // mismatch
  await page.click('#reg-submit-btn');
  await new Promise(r => setTimeout(r, 600));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-validation-errors.png'), fullPage: true });

  await setMobile();
  await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle0' });
  await page.type('#reg-username', 'ab');
  await page.type('#reg-email', 'invalid-email');
  await page.type('#reg-password', 'short');
  await page.type('#reg-confirm-password', 'mismatch');
  await page.click('#reg-submit-btn');
  await new Promise(r => setTimeout(r, 600));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-validation-errors.png'), fullPage: true });

  // 3. LOGIN PAGE (Clean)
  console.log('3. Capturing Login Page (Clean)...');
  await setDesktop();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-login.png'), fullPage: true });

  await setMobile();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-login.png'), fullPage: true });

  // 4. LOGIN INVALID CREDENTIALS ERROR
  console.log('4. Capturing Login Error State...');
  await setDesktop();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.type('#login-identifier', 'unregistered_user');
  await page.type('#login-password', 'WrongPassword123');
  await page.click('#login-submit-btn');
  await new Promise(r => setTimeout(r, 800));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-login-error.png'), fullPage: true });

  await setMobile();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.type('#login-identifier', 'unregistered_user');
  await page.type('#login-password', 'WrongPassword123');
  await page.click('#login-submit-btn');
  await new Promise(r => setTimeout(r, 800));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-login-error.png'), fullPage: true });

  // Ensure user mudassir_ahmed exists for dashboard
  try {
    await fetch(`${BASE_URL}/api/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: 'mudassir_ahmed',
        email: 'mudassir@example.com',
        password: 'Password999',
        confirmPassword: 'Password999'
      })
    });
  } catch (e) {}

  // 5. LOGIN AND CAPTURE DASHBOARD
  console.log('5. Logging in and Capturing Dashboard...');
  await setDesktop();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.type('#login-identifier', 'mudassir_ahmed');
  await page.type('#login-password', 'Password999');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle0' }),
    page.click('#login-submit-btn')
  ]);
  await new Promise(r => setTimeout(r, 800));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-dashboard.png'), fullPage: true });

  await setMobile();
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-dashboard.png'), fullPage: true });

  // 6. LOGOUT AND CAPTURE REDIRECT TO LOGIN
  console.log('6. Logging out and Capturing Redirect to Login...');
  await setDesktop();
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle0' }),
    page.click('#logout-btn')
  ]);
  await new Promise(r => setTimeout(r, 600));
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'desktop-redirect-to-login.png'), fullPage: true });

  await setMobile();
  await page.screenshot({ path: path.join(SCREENSHOTS_DIR, 'mobile-redirect-to-login.png'), fullPage: true });

  await browser.close();
  console.log('All 12 screenshots successfully captured and saved to /screenshots!');
}

captureAll().catch(err => {
  console.error('Screenshot capture failed:', err);
  process.exit(1);
});
